import { UserTaskDTO } from '@/api/types/orchest-api';
import { Select } from '@/design-system/components/ui/select';
import React, { useMemo } from 'react';
import { DEFAULT_FILTERS, SORT_OPTIONS } from '../../constants/filterOptions';
import { TASK_MESSAGES } from '../../constants/messages';
import { ActiveTaskFilter } from '../../types';
import { TaskTile } from '../TaskTile/TaskTile';
import styles from './TaskQueue.module.css';

export interface TaskQueueProps {
  tasks: UserTaskDTO[];
  selectedTaskId?: string;
  onTaskSelect: (taskId: string) => void;
  activeFilter: ActiveTaskFilter;
  onFilterChange: (filter: ActiveTaskFilter) => void;
  sortBy: string;
  onSortChange: (sortValue: string) => void;
  isLoading?: boolean;
}

const FILTER_OPTIONS = Object.values(DEFAULT_FILTERS).map(filter => ({
  value: filter.id,
  label: filter.label,
  filter: {
    id: filter.id,
    name: filter.label,
    state: filter.state,
    ...(filter.assignedToCurrentUser && { assignedToCurrentUser: true }),
  },
}));

export const TaskQueue: React.FC<TaskQueueProps> = React.memo(({
  tasks,
  selectedTaskId,
  onTaskSelect,
  activeFilter,
  onFilterChange,
  sortBy,
  onSortChange,
  isLoading = false,
}) => {
  const odsFilterOptions = useMemo(() => FILTER_OPTIONS, []);
  const odsSortOptions = useMemo(() => SORT_OPTIONS, []);

  const handleFilterChange = (value: string) => {
    
    const selectedFilter = FILTER_OPTIONS.find(opt => opt.value === value);
    if (selectedFilter) {
      onFilterChange(selectedFilter.filter);
    }
  };

  const handleSortChange = (value: string) => {
    
    if (value) onSortChange(value);
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.filterGroup}>
          <div className={styles.filterInputWrapper}>
            <div className={styles.dropdownWrapper}>
              <Select
                label="Filter by"
                value={activeFilter.id || DEFAULT_FILTERS.ALL_OPEN.id}
                items={odsFilterOptions}
                onValueChange={handleFilterChange}
                disabled={isLoading}
                size="sm"
                className={styles.dropdown}
              />
            </div>
          </div>
          <div className={styles.filterInputWrapper}>
            <div className={styles.dropdownWrapper}>
              <Select
                label="Sort by"
                value={sortBy}
                items={odsSortOptions}
                onValueChange={handleSortChange}
                disabled={isLoading}
                size="sm"
                className={styles.dropdown}
              />
            </div>
          </div>
        </div>
      </div>

      <div
        className={styles.queue}
        role="listbox"
        aria-label="Available tasks"
        aria-busy={isLoading}
      >
        {isLoading ? (
          <div className={styles.loadingContainer} aria-label="Loading tasks">
            {[1, 2, 3].map((i) => (
              <div key={i} className={styles.skeleton} role="progressbar" aria-label="Loading" />
            ))}
          </div>
        ) : tasks.length === 0 ? (
          <div className={styles.emptyState} role="status" aria-live="polite">
            <p className={styles.emptyTitle}>
              {TASK_MESSAGES.EMPTY_STATE.NO_TASKS_TITLE}
            </p>
            <p className={styles.emptyDescription}>
              {TASK_MESSAGES.EMPTY_STATE.NO_TASKS_DESCRIPTION}
            </p>
          </div>
        ) : (
          tasks.map((task) => (
            <TaskTile
              key={task.taskId}
              task={task}
              isSelected={selectedTaskId === task.taskId}
              onClick={() => onTaskSelect(task.taskId)}
            />
          ))
        )}
      </div>
    </div>
  );
});

TaskQueue.displayName = 'TaskQueue';

export default TaskQueue;
