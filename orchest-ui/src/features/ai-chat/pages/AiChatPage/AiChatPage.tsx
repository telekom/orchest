import AppLayout from '@/shared/layouts/AppLayout';
import clsx from 'clsx';
import { Bot, PanelLeftClose, PanelLeftOpen, Sparkles, Trash2 } from 'lucide-react';
import React, { useCallback, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ChatInput } from '../../components/ChatInput/ChatInput';
import { ChatMessage } from '../../components/ChatMessage/ChatMessage';
import { ChatSidebar } from '../../components/ChatSidebar/ChatSidebar';
import { EmptyState } from '../../components/EmptyState/EmptyState';
import { useChatApi } from '../../hooks/useChatApi';
import { useChatStore } from '../../hooks/useChatStore';
import styles from './AiChatPage.module.css';

interface LocationState {
  chatMessage?: string;
  chatType?: string;
}

const AiChatPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const activeConversationId = useChatStore((s) => s.activeConversationId);
  const conversations = useChatStore((s) => s.conversations);
  const statusMap = useChatStore((s) => s.statusMap);
  const createConversation = useChatStore((s) => s.createConversation);
  const deleteConversation = useChatStore((s) => s.deleteConversation);
  const { sendMessage, abort } = useChatApi();
  const scrollAreaRef = useRef<HTMLDivElement>(null);
  const [sidebarOpen, setSidebarOpen] = React.useState(true);
  const autoSentRef = useRef(false);

  const activeConversation = conversations.find((c) => c.id === activeConversationId) ?? null;
  const messages = activeConversation?.messages ?? [];
  const isStreaming = activeConversationId ? statusMap[activeConversationId] === 'streaming' : false;

  // Auto-scroll: on new messages and continuously during streaming
  const scrollToBottom = useCallback(() => {
    const el = scrollAreaRef.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages.length, scrollToBottom]);

  useEffect(() => {
    if (!isStreaming) return;
    const interval = setInterval(scrollToBottom, 100);
    return () => clearInterval(interval);
  }, [isStreaming, scrollToBottom]);

  // Abort streaming on unmount to prevent orphaned connections
  useEffect(() => {
    return () => { abort(); };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Auto-send from navigation state (e.g. from Analyse Incident button)
  useEffect(() => {
    if (autoSentRef.current) return;
    const state = location.state as LocationState | null;
    if (!state?.chatMessage) return;
    autoSentRef.current = true;
    const convId = createConversation();
    sendMessage(convId, state.chatMessage, state.chatType as import('@/api/domains/chat').ChatType);
    // Clear location state so page refresh won't re-send
    navigate(location.pathname, { replace: true });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSend = useCallback((content: string, chatType?: string) => {
    let convId = activeConversationId;
    if (!convId) {
      convId = createConversation();
    }
    sendMessage(convId, content, chatType as import('@/api/domains/chat').ChatType);
  }, [activeConversationId, createConversation, sendMessage]);

  const handlePromptClick = useCallback((prompt: string) => {
    const convId = createConversation();
    sendMessage(convId, prompt);
  }, [createConversation, sendMessage]);

  const handleRetry = useCallback((messageIndex: number) => {
    if (!activeConversationId || !messages[messageIndex - 1]) return;
    const userMsg = messages[messageIndex - 1];
    if (userMsg.role === 'user') {
      sendMessage(activeConversationId, userMsg.content, userMsg.chatType);
    }
  }, [activeConversationId, messages, sendMessage]);

  return (
    <AppLayout>
      <div className={styles.page}>
        <div className={clsx(styles.sidebarArea, !sidebarOpen && styles.sidebarHidden)}>
          <ChatSidebar />
        </div>

        <div className={styles.main}>
          <div className={styles.topBar}>
            <div className={styles.topBarLeft}>
              <button
                className={styles.toggleBtn}
                onClick={() => setSidebarOpen(!sidebarOpen)}
                title={sidebarOpen ? "Hide sidebar" : "Show sidebar"}
              >
                {sidebarOpen ? <PanelLeftClose size={16} /> : <PanelLeftOpen size={16} />}
              </button>
              {activeConversation && (
                <span className={styles.chatTitle}>{activeConversation.title}</span>
              )}
            </div>
            <div className={styles.topBarRight}>
              {activeConversation && (
                <>
                  <span className={styles.modelBadge}>
                    <Sparkles size={12} /> OrchesT AI
                  </span>
                  <span className={styles.betaBadge}>Beta</span>
                  <button
                    className={styles.deleteBtn}
                    onClick={() => deleteConversation(activeConversation.id)}
                    title="Delete conversation"
                  >
                    <Trash2 size={14} />
                  </button>
                </>
              )}
            </div>
          </div>

          <div className={styles.conversationArea} ref={scrollAreaRef}>
            {messages.length === 0 && !isStreaming ? (
              <EmptyState onPromptClick={handlePromptClick} />
            ) : (
              <div className={styles.messages}>
                {messages.map((msg, idx) => {
                  const isLastAssistant = isStreaming && idx === messages.length - 1 && msg.role === 'assistant';
                  if (isLastAssistant && !msg.content) return null;
                  return (
                    <ChatMessage
                      key={msg.id}
                      message={msg}
                      isStreaming={isLastAssistant}
                      onRetry={msg.role === 'assistant' && !isStreaming ? () => handleRetry(idx) : undefined}
                    />
                  );
                })}
                {isStreaming && messages[messages.length - 1]?.content === '' && (
                  <div className={styles.thinkingRow}>
                    <div className={styles.thinkingAvatar}>
                      <Bot size={14} />
                    </div>
                    <div className={styles.thinkingContent}>
                      <div className={styles.thinkingDots}>
                        <span /><span /><span />
                      </div>
                      <div className={styles.shimmerLines}>
                        <span /><span /><span />
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>

          <ChatInput
            onSend={handleSend}
            onStop={abort}
            isStreaming={isStreaming}
          />
        </div>
      </div>
    </AppLayout>
  );
};

export default AiChatPage;
