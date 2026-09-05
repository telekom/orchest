import { UserTaskDTO } from '@/api/types/orchest-api';
import { SpinnerLoader } from '@/shared/components/Loader/Loader';
import { StatusBadge } from '@/shared/components';
import { useUserTasksForProcessInstance } from '@/features/task-management/hooks';
import { EmptyState } from '@/shared/components/EmptyState/EmptyState';
import { PriorityBadge } from '@/shared/components/PriorityBadge/PriorityBadge';
import { formatDistanceToNow } from 'date-fns';
import { CheckSquare } from 'lucide-react';
import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import styles from './ProcessUserTasks.module.css';

export interface ProcessUserTasksProps {
  processInstanceId: string;
  /** userTaskId values from sequence execution metaData */
  userTaskIds?: string[];
  /** When false, skips fetching */
  enabled?: boolean;
}

const formatRelative = (value?: string) => {
  if (!value) return null;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return null;
  return formatDistanceToNow(date, { addSuffix: true });
};

const MetaItem: React.FC<{ label: string; value?: React.ReactNode }> = ({
  label,
  value,
}) => {
  if (value === null || value === undefined || value === '') return null;
  return (
    <div className={styles.metaItem}>
      <span className={styles.metaLabel}>{label}</span>
      <span className={styles.metaValue}>{value}</span>
    </div>
  );
};

const buildTaskMeta = (task: UserTaskDTO) => {
  const created = formatRelative(task.createdAt);
  const claimed = formatRelative(task.claimedAt);
  const completed = formatRelative(task.completedAt);
  const due = formatRelative(task.dueDate);
  const followUp = formatRelative(task.followUpDate);

  return [
    { label: 'Task ID', value: task.taskId },
    { label: 'Activity ID', value: task.activityId },
    { label: 'Process', value: task.processDefinitionId },
    { label: 'Process Instance', value: task.processInstanceId },
    { label: 'Assignee', value: task.assignee },
    { label: 'Claimed by', value: !task.assignee ? task.claimedBy : undefined },
    {
      label: 'Candidate Users',
      value: task.candidateUsers?.length ? task.candidateUsers.join(', ') : undefined,
    },
    {
      label: 'Candidate Groups',
      value: task.candidateGroups?.length ? task.candidateGroups.join(', ') : undefined,
    },
    { label: 'Form Key', value: task.formKey },
    { label: 'Created', value: created },
    { label: 'Claimed', value: claimed },
    { label: 'Due', value: due },
    { label: 'Follow-up', value: followUp },
    { label: 'Completed', value: completed },
  ] as const;
};

/**
 * ProcessUserTasks - Displays user tasks for a process instance as cards.
 * Prefers loading by metaData.userTaskId from sequence executions.
 */
export const ProcessUserTasks: React.FC<ProcessUserTasksProps> = ({
  processInstanceId,
  userTaskIds = [],
  enabled = true,
}) => {
  const { data: tasks = [], isLoading } = useUserTasksForProcessInstance(
    processInstanceId,
    userTaskIds,
    { enabled: enabled && !!processInstanceId }
  );

  const sortedTasks = useMemo(
    () =>
      [...tasks].sort((a, b) => {
        const aTime = a.createdAt ? new Date(a.createdAt).getTime() : 0;
        const bTime = b.createdAt ? new Date(b.createdAt).getTime() : 0;
        return bTime - aTime;
      }),
    [tasks]
  );

  if (!enabled) {
    return null;
  }

  if (isLoading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>
          <SpinnerLoader size="lg" text="Loading tasks..." />
        </div>
      </div>
    );
  }

  if (sortedTasks.length === 0) {
    return (
      <div className={styles.container}>
        <EmptyState
          icon={<CheckSquare />}
          title="No User Tasks"
          description="No user tasks were found for this process instance."
        />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h3 className={styles.title}>
          User Tasks ({sortedTasks.length})
        </h3>
      </div>

      <div className={styles.taskList}>
        {sortedTasks.map((task) => {
          const meta = buildTaskMeta(task);
          const isUnassigned = !task.assignee && !task.claimedBy;

          return (
            <Link
              key={task.taskId}
              to={`/tasks/${task.taskId}`}
              className={styles.taskCard}
            >
              <div className={styles.taskHeader}>
                <h4 className={styles.taskName}>{task.taskName || task.activityId}</h4>
                <div className={styles.headerBadges}>
                  {task.state && <StatusBadge status={task.state} />}
                  {task.priority !== undefined && task.priority !== null && (
                    <PriorityBadge priority={task.priority as number} />
                  )}
                </div>
              </div>

              <div className={styles.taskMeta}>
                {isUnassigned && (
                  <div className={styles.metaItem}>
                    <span className={styles.unassignedBadge}>Unassigned</span>
                  </div>
                )}
                {meta.map((item) => (
                  <MetaItem key={item.label} label={item.label} value={item.value} />
                ))}
              </div>

              <div className={styles.taskFooter}>
                <span className={styles.viewLink}>Open task →</span>
              </div>
            </Link>
          );
        })}
      </div>
    </div>
  );
};

export default ProcessUserTasks;
