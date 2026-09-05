import { UserTaskDTO } from '@/api/types/orchest-api';
import { PriorityBadge } from '@/shared/components/PriorityBadge/PriorityBadge';
import clsx from 'clsx';
import { formatDistanceToNow } from 'date-fns';
import { AlertCircle, User } from 'lucide-react';
import React from 'react';
import styles from './TaskTile.module.css';

export interface TaskTileProps {
  task: UserTaskDTO;
  isSelected?: boolean;
  onClick?: () => void;
}

export const TaskTile: React.FC<TaskTileProps> = React.memo(({
  task,
  isSelected = false,
  onClick,
}) => {
  const dueDate = task.dueDate ? new Date(task.dueDate) : null;

  const ariaLabel = [
    `Task: ${task.taskName}`,
    task.assignee ? `Assigned to: ${task.assignee}` : 'Unassigned',
    dueDate && `Due ${formatDistanceToNow(dueDate)}`,
    isSelected && 'Currently selected',
  ].filter(Boolean).join('. ');

  return (
    <button
      className={clsx(
        styles.tile,
        isSelected && styles.selected
      )}
      onClick={onClick}
      aria-pressed={isSelected}
      aria-label={ariaLabel}
      role="option"
      aria-selected={isSelected}
      tabIndex={isSelected ? 0 : -1}
    >
      <div className={styles.header}>
        <h3 className={styles.taskName}>{task.taskName}</h3>
        {task.priority !== undefined && task.priority !== null && (
          <PriorityBadge priority={task.priority as number} />
        )}
      </div>

      <div className={styles.meta}>
          <div className={styles.metaItem}>
            <User className={styles.metaIcon} aria-hidden="true" />
            {task.assignee || task.claimedBy ? (
              <span className={styles.metaValue}>{task.assignee || task.claimedBy}</span>
            ) : (
              <span className={styles.unassignedBadge}>Unassigned</span>
            )}
          </div>
        {dueDate && (
          <div className={clsx(styles.metaItem, styles.dueMeta)}>
            <AlertCircle className={styles.metaIcon} aria-hidden="true" />
            <span className={styles.dueValue}>
              Due {formatDistanceToNow(dueDate)}
            </span>
          </div>
        )}
      </div>
    </button>
  );
});

TaskTile.displayName = 'TaskTile';

export default TaskTile;
