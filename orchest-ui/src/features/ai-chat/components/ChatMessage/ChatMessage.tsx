import type { ChatType } from '@/api/domains/chat';
import { toast } from '@/design-system/components/ui/sonner';
import clsx from 'clsx';
import { formatDistanceToNow } from 'date-fns';
import { AlertTriangle, Bot, Copy, FileSearch, RotateCcw, User, Wrench } from 'lucide-react';
import React, { useCallback } from 'react';
import type { ChatMessage as ChatMessageType } from '../../types/chat.types';
import { MarkdownRenderer } from '../MarkdownRenderer/MarkdownRenderer';
import styles from './ChatMessage.module.css';

const CHAT_TYPE_LABELS: Record<ChatType, { label: string; icon: React.ReactNode }> = {
  DETAILED_ANALYSIS: { label: 'Analysis', icon: <FileSearch size={11} /> },
  INCIDENT_ANALYSIS: { label: 'Incident', icon: <AlertTriangle size={11} /> },
  SUGGEST_FIX: { label: 'Suggest Fix', icon: <Wrench size={11} /> },
};

interface ChatMessageProps {
  message: ChatMessageType;
  isStreaming?: boolean;
  onRetry?: () => void;
}

export const ChatMessage: React.FC<ChatMessageProps> = React.memo(({ message, isStreaming = false, onRetry }) => {
  const isUser = message.role === 'user';

  const handleCopy = useCallback(() => {
    navigator.clipboard.writeText(message.content);
    toast.success('Copied to clipboard');
  }, [message.content]);

  const timeAgo = formatDistanceToNow(new Date(message.timestamp), { addSuffix: true });

  return (
    <div className={clsx(styles.messageRow, isUser && styles.messageRowUser)}>
      <div className={clsx(styles.avatar, isUser ? styles.avatarUser : styles.avatarAssistant, isStreaming && styles.avatarStreaming)}>
        {isUser ? <User size={14} /> : <Bot size={14} />}
      </div>
      <div className={clsx(styles.bubble, isUser ? styles.bubbleUser : styles.bubbleAssistant, isStreaming && styles.bubbleStreaming)}>
        {isUser ? (
          <>
            {message.chatType && CHAT_TYPE_LABELS[message.chatType] && (
              <span className={styles.chatTypeBadge}>
                {CHAT_TYPE_LABELS[message.chatType].icon}
                {CHAT_TYPE_LABELS[message.chatType].label}
              </span>
            )}
            <p className={styles.userText}>{message.content}</p>
          </>
        ) : (
          <div className={clsx(styles.streamContent, isStreaming && styles.streamActive)}>
            <MarkdownRenderer content={message.content} />
            {isStreaming && <span className={styles.cursor} />}
          </div>
        )}
        {!isStreaming && (
          <div className={styles.meta}>
            <span className={styles.timestamp}>{timeAgo}</span>
            <div className={styles.actions}>
              <button className={styles.actionBtn} onClick={handleCopy} title="Copy">
                <Copy size={12} />
              </button>
              {!isUser && onRetry && (
                <button className={styles.actionBtn} onClick={onRetry} title="Retry">
                  <RotateCcw size={12} />
                </button>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
});

ChatMessage.displayName = 'ChatMessage';
