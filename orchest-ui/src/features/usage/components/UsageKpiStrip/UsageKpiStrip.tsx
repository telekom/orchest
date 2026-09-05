import clsx from 'clsx';
import React from 'react';
import styles from './UsageKpiStrip.module.css';

export interface UsageKpiStripProps {
  total: number;
  label?: string;
  isLoading?: boolean;
  onSelectTotal?: () => void;
}

export const UsageKpiStrip: React.FC<UsageKpiStripProps> = ({
  total,
  label = 'Total executions',
  isLoading,
  onSelectTotal,
}) => {
  return (
    <div className={styles.strip} role="list" aria-label="Usage summary">
      <button
        type="button"
        role="listitem"
        className={clsx(styles.card, styles.total)}
        onClick={onSelectTotal}
        disabled={isLoading}
        aria-label={`${label}: ${total}`}
      >
        <span className={styles.label}>{label}</span>
        <span className={styles.value}>
          {isLoading ? '—' : total.toLocaleString()}
        </span>
      </button>
    </div>
  );
};
