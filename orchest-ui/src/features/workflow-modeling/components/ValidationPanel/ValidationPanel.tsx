import type { ValidationIssue } from '../../hooks/useValidation';
import { AlertCircle, AlertTriangle, X } from 'lucide-react';
import React from 'react';
import styles from './ValidationPanel.module.css';

interface ValidationPanelProps {
  issues: ValidationIssue[];
  onDismiss: () => void;
  onDeployAnyway?: () => void;
  hasErrors: boolean;
}

export const ValidationPanel: React.FC<ValidationPanelProps> = ({
  issues,
  onDismiss,
  onDeployAnyway,
  hasErrors,
}) => {
  const errors = issues.filter((i) => i.severity === 'error');
  const warnings = issues.filter((i) => i.severity === 'warning');

  return (
    <div className={`${styles.panel} ${hasErrors ? styles.panelError : styles.panelWarning}`}>
      <div className={styles.header}>
        <div className={styles.headerLeft}>
          {hasErrors ? (
            <AlertCircle size={14} className={styles.headerIconError} />
          ) : (
            <AlertTriangle size={14} className={styles.headerIconWarning} />
          )}
          <span className={styles.headerTitle}>
            {errors.length > 0 && (
              <span className={styles.countError}>
                {errors.length} error{errors.length !== 1 ? 's' : ''}
              </span>
            )}
            {errors.length > 0 && warnings.length > 0 && ', '}
            {warnings.length > 0 && (
              <span className={styles.countWarning}>
                {warnings.length} warning{warnings.length !== 1 ? 's' : ''}
              </span>
            )}
          </span>
        </div>
        <div className={styles.headerRight}>
          {!hasErrors && onDeployAnyway && (
            <button type="button" className={styles.deployAnywayBtn} onClick={onDeployAnyway}>
              Deploy Anyway
            </button>
          )}
          <button type="button" className={styles.dismissBtn} onClick={onDismiss} aria-label="Dismiss">
            <X size={14} />
          </button>
        </div>
      </div>
      <div className={styles.issueList}>
        {issues.map((issue, idx) => (
          <div key={`${issue.id}-${issue.rule}-${idx}`} className={styles.issueRow}>
            <span className={`${styles.severityBadge} ${issue.severity === 'error' ? styles.badgeError : styles.badgeWarning}`}>
              {issue.severity}
            </span>
            <span className={styles.elementId}>
              {issue.elementName || issue.id}
            </span>
            <span className={styles.message}>{issue.message}</span>
            <span className={styles.rule}>{issue.rule}</span>
          </div>
        ))}
      </div>
    </div>
  );
};