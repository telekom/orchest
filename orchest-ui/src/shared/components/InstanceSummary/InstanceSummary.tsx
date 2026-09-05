import { Check, Copy } from "lucide-react";
import React, { ReactNode } from "react";
import styles from './InstanceSummary.module.css';

export interface SummaryField {
  label: string;
  value: string | ReactNode;
  icon?: ReactNode;
  copyValue?: string;
  copyId?: string;
  onClick?: () => void;
  tooltip?: string;
}

interface InstanceSummaryProps {
  fields: SummaryField[];
  copiedItems: Record<string, boolean>;
  onCopyToClipboard: (text: string, itemId: string) => void;
  leftSlot?: ReactNode;
  rightSlot?: ReactNode;
}

const InstanceSummary: React.FC<InstanceSummaryProps> = ({
  fields,
  copiedItems,
  onCopyToClipboard,
  leftSlot,
  rightSlot,
}) => {
  return (
    <div className={styles.container}>
      <div className={styles.innerWrapper}>
        {leftSlot && <div className={styles.leftSlot}>{leftSlot}</div>}
        <div className={styles.fieldsContainer}>
        {fields.map((field, index) => (
          <div key={index} className={styles.fieldItem}>
            <div className={styles.fieldLabelRow}>
              {field.icon && <span className={styles.fieldIcon}>{field.icon}</span>}
              <span className={styles.fieldLabel}>{field.label}</span>
              {field.copyValue && field.copyId && (
                <button
                  type="button"
                  className={styles.copyButton}
                  onClick={() => onCopyToClipboard(field.copyValue!, field.copyId!)}
                  title={`Copy ${field.label} to clipboard`}
                >
                  {copiedItems[field.copyId] ? (
                    <Check />
                  ) : (
                    <Copy />
                  )}
                </button>
              )}
            </div>
            <div
              className={field.onClick ? `${styles.fieldValue} ${styles.clickableValue}` : styles.fieldValue}
              onClick={field.onClick}
              title={field.tooltip}
              role={field.onClick ? 'button' : undefined}
              tabIndex={field.onClick ? 0 : undefined}
              onKeyDown={field.onClick ? (e) => { if (e.key === 'Enter' || e.key === ' ') field.onClick!(); } : undefined}
            >
              {field.value}
            </div>
          </div>
        ))}
        </div>
        {rightSlot && <div className={styles.rightSlot}>{rightSlot}</div>}
      </div>
    </div>
  );
};

export default InstanceSummary;
