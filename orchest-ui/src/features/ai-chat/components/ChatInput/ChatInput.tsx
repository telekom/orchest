import type { ChatType } from '@/api/domains/chat';
import { ArrowUp, FileSearch, AlertTriangle, Wrench, Square } from 'lucide-react';
import React, { useCallback, useRef, useState } from 'react';
import styles from './ChatInput.module.css';

const CHAT_TYPES: { value: ChatType; label: string; icon: React.ReactNode }[] = [
  { value: 'DETAILED_ANALYSIS', label: 'Analysis', icon: <FileSearch size={13} /> },
  { value: 'INCIDENT_ANALYSIS', label: 'Incident', icon: <AlertTriangle size={13} /> },
  { value: 'SUGGEST_FIX', label: 'Suggest Fix', icon: <Wrench size={13} /> },
];

interface ChatInputProps {
  onSend: (message: string, chatType?: ChatType) => void;
  onStop?: () => void;
  isStreaming?: boolean;
  placeholder?: string;
}

export const ChatInput: React.FC<ChatInputProps> = ({
  onSend,
  onStop,
  isStreaming = false,
  placeholder = "Ask anything...",
}) => {
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [chatType, setChatType] = useState<ChatType>('DETAILED_ANALYSIS');

  const handleSubmit = useCallback(() => {
    const value = textareaRef.current?.value.trim();
    if (!value || isStreaming) return;
    onSend(value, chatType);
    if (textareaRef.current) {
      textareaRef.current.value = '';
      textareaRef.current.style.height = 'auto';
    }
  }, [onSend, isStreaming, chatType]);

  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  }, [handleSubmit]);

  const handleInput = useCallback(() => {
    const el = textareaRef.current;
    if (!el) return;
    el.style.height = 'auto';
    el.style.height = `${Math.min(el.scrollHeight, 150)}px`;
  }, []);

  return (
    <div className={styles.container}>
      <div className={styles.typeSelector}>
        {CHAT_TYPES.map((t) => (
          <label key={t.value} className={styles.typeOption}>
            <input
              type="radio"
              name="chatType"
              value={t.value}
              checked={chatType === t.value}
              onChange={() => setChatType(t.value)}
              className={styles.typeRadio}
            />
            <span className={styles.typeLabel} data-active={chatType === t.value || undefined}>
              {t.icon}
              {t.label}
            </span>
          </label>
        ))}
      </div>
      <div className={styles.inputWrapper}>
        <textarea
          ref={textareaRef}
          className={styles.textarea}
          placeholder={placeholder}
          onKeyDown={handleKeyDown}
          onInput={handleInput}
          disabled={isStreaming}
          rows={1}
        />
        {isStreaming ? (
          <button
            type="button"
            className={styles.stopButton}
            onClick={onStop}
            title="Stop generating"
          >
            <Square size={14} />
          </button>
        ) : (
          <button
            type="button"
            className={styles.sendButton}
            onClick={handleSubmit}
            title="Send message"
          >
            <ArrowUp size={16} />
          </button>
        )}
      </div>
      <span className={styles.hint}>Enter to send, Shift+Enter for new line</span>
    </div>
  );
};
