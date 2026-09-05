import { Activity, AlertTriangle, Bot, Search, Zap } from 'lucide-react';
import React from 'react';
import { SUGGESTED_PROMPTS } from '../../constants/chat.constants';
import styles from './EmptyState.module.css';

interface EmptyStateProps {
  onPromptClick: (prompt: string) => void;
}

const PROMPT_ICONS = [Search, AlertTriangle, Activity, Zap];

export const EmptyState: React.FC<EmptyStateProps> = ({ onPromptClick }) => {
  return (
    <div className={styles.container}>
      <div className={styles.hero}>
        <div className={styles.iconRing}>
          <div className={styles.icon}>
            <Bot size={28} />
          </div>
        </div>
        <h1 className={styles.title}>OrchesT AI</h1>
        <p className={styles.subtitle}>
          Your intelligent assistant for process orchestration. Ask about failures,
          performance, optimization, or anything about your workflows.
        </p>
      </div>

      <div className={styles.prompts}>
        <span className={styles.promptsLabel}>Try asking</span>
        <div className={styles.promptsGrid}>
          {SUGGESTED_PROMPTS.map((prompt, idx) => {
            const Icon = PROMPT_ICONS[idx % PROMPT_ICONS.length];
            return (
              <button
                key={prompt}
                type="button"
                className={styles.promptCard}
                onClick={() => onPromptClick(prompt)}
              >
                <Icon size={14} className={styles.promptIcon} />
                <span className={styles.promptText}>{prompt}</span>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};
