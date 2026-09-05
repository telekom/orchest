import { ActionIconButton } from '@/shared/components/ActionIconButton/ActionIconButton';
import { Loader2, User } from 'lucide-react';
import React from 'react';
import styles from './DeputyListItem.module.css';

interface DeputyListItemProps {
  deputyEmail: string;
  onRemove: () => void;
  isRemoving: boolean;
}

export const DeputyListItem: React.FC<DeputyListItemProps> = ({
  deputyEmail,
  onRemove,
  isRemoving,
}) => {
  return (
    <div className={styles.container}>
      <div className={styles.contentWrapper}>
        <div className={styles.iconContainer}>
          <User className={styles.icon} />
        </div>
        <span className={styles.email}>
          {deputyEmail}
        </span>
      </div>
      {isRemoving ? (
        <div className={styles.loaderContainer}>
          <Loader2 className={styles.loader} />
        </div>
      ) : (
        <ActionIconButton
          icon="delete"
          onClick={() => onRemove()}
          title={`Remove approver ${deputyEmail}`}
          variant="danger"
          className={styles.deleteButton}
        />
      )}
    </div>
  );
};
