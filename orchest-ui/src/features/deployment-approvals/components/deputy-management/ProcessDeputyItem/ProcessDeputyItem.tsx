import { ProcessDefinitionDTO } from '@/api/types/orchest-api';
import {
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/design-system/components/ui/accordion';
import { FileCode, Users } from 'lucide-react';
import React from 'react';
import { DeputyFormInput } from '../DeputyFormInput/DeputyFormInput';
import { DeputyListItem } from '../DeputyListItem/DeputyListItem';
import styles from './ProcessDeputyItem.module.css';

interface ProcessDeputyItemProps {
  process: ProcessDefinitionDTO;
  inputValue: string;
  onInputChange: (value: string) => void;
  onAddDeputy: () => void;
  onRemoveDeputy: (deputyEmail: string) => void;
  isAddingDeputy: boolean;
  isRemovingDeputy: boolean;
}

export const ProcessDeputyItem: React.FC<ProcessDeputyItemProps> = ({
  process,
  inputValue,
  onInputChange,
  onAddDeputy,
  onRemoveDeputy,
  isAddingDeputy,
  isRemovingDeputy,
}) => {
  const approverCount = process.approvers.length;
  const approverText = `${approverCount} ${approverCount === 1 ? 'approver' : 'approvers'}`;

  return (
    <AccordionItem value={process.id} className={styles.accordionItem}>
      <AccordionTrigger className={styles.accordionTrigger}>
        <div className={styles.triggerContent}>
          <FileCode className={styles.processIcon} />
          <div className={styles.processInfo}>
            <div className={styles.processIdHeader}>
              <span className={styles.processId}>{process.processId}</span>
            </div>
            <div className={styles.processMetadata}>
              <span className={styles.metadataId}>ID: {process.id}</span>
              <span className={styles.metadataSeparator}>•</span>
              <span className={styles.approverCount}>
                <Users className={styles.approverCountIcon} />
                {approverText}
              </span>
            </div>
          </div>
        </div>
      </AccordionTrigger>

      <AccordionContent className={styles.accordionContent}>
        <div className={styles.formSection}>
          <DeputyFormInput
            value={inputValue}
            onChange={onInputChange}
            onSubmit={onAddDeputy}
            isSubmitting={isAddingDeputy}
          />
        </div>

        <div className={styles.approverList}>
          {process.approvers.length === 0 ? (
            <div className={styles.emptyState}>No approvers assigned yet</div>
          ) : (
            process.approvers.map((approver) => (
              <DeputyListItem
                key={approver}
                deputyEmail={approver}
                onRemove={() => onRemoveDeputy(approver)}
                isRemoving={isRemovingDeputy}
              />
            ))
          )}
        </div>
      </AccordionContent>
    </AccordionItem>
  );
};
