import React, { useCallback, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useUserTasks, useKeyboardShortcuts, useTasksPageUrlState } from '../../hooks';
import { TaskQueue } from '../../components/TaskQueue/TaskQueue';
import { TaskDetailsPanel } from '../../components/TaskDetailsPanel/TaskDetailsPanel';
import { FilterSidebar } from '@/shared/components';
import AppLayout from '@/shared/layouts/AppLayout';
import { useFilterSidebarCollapsed, useUIStore } from '@/shared/stores/uiStore';
import { useAuth } from '@/shared/auth';
import { toApiSortFormat } from '../../constants/filterOptions';
import { TASK_MESSAGES } from '../../constants/messages';
import commonStyles from '@/shared/styles/common.module.css';
import styles from './TasksPage.module.css';

const TasksPage: React.FC = () => {
  const { taskId } = useParams<{ taskId?: string }>();
  const navigate = useNavigate();
  const sidebarCollapsed = useFilterSidebarCollapsed();
  const toggleSidebar = useUIStore((state) => state.toggleFilterSidebar);
  const { user } = useAuth();

  const {
    activeFilter,
    sortOption,
    setActiveFilter,
    setSortOption,
    clearFilters,
    hasActiveFilters,
    buildTaskPath,
  } = useTasksPageUrlState();

  const { data: tasks = [], isLoading, refetch } = useUserTasks({
    state: activeFilter.state as string | undefined,
    sort: toApiSortFormat(sortOption),
  });

  useEffect(() => {
    if (tasks.length > 0 && !taskId) {
      navigate(buildTaskPath(tasks[0].taskId), { replace: true });
    }
  }, [tasks, taskId, navigate, buildTaskPath]);

  const handleFilterChange = useCallback((filter: Parameters<typeof setActiveFilter>[0]) => {
    setActiveFilter(filter);
  }, [setActiveFilter]);

  const handleClearFilters = useCallback(() => {
    clearFilters();
  }, [clearFilters]);

  const handleSortChange = useCallback((newSort: string) => {
    setSortOption(newSort);
  }, [setSortOption]);

  const handleTaskSelect = useCallback((selectedTaskId: string) => {
    navigate(buildTaskPath(selectedTaskId));
  }, [navigate, buildTaskPath]);

  const handleCompleteTask = useCallback(() => {
    refetch();

    if (tasks.length > 1) {
      const currentIndex = tasks.findIndex((t) => t.taskId === taskId);
      const nextTask = tasks[currentIndex + 1] || tasks[currentIndex - 1];
      if (nextTask && nextTask.taskId !== taskId) {
        navigate(buildTaskPath(nextTask.taskId));
        return;
      }
    }

    navigate(buildTaskPath());
  }, [tasks, taskId, navigate, refetch, buildTaskPath]);

  const handleNextTask = useCallback(() => {
    if (tasks.length === 0) return;
    const currentIndex = tasks.findIndex((t) => t.taskId === taskId);
    if (currentIndex < tasks.length - 1) {
      navigate(buildTaskPath(tasks[currentIndex + 1].taskId));
    }
  }, [tasks, taskId, navigate, buildTaskPath]);

  const handlePrevTask = useCallback(() => {
    if (tasks.length === 0) return;
    const currentIndex = tasks.findIndex((t) => t.taskId === taskId);
    if (currentIndex > 0) {
      navigate(buildTaskPath(tasks[currentIndex - 1].taskId));
    }
  }, [tasks, taskId, navigate, buildTaskPath]);

  useKeyboardShortcuts({
    onNextTask: handleNextTask,
    onPrevTask: handlePrevTask,
    enabled: true,
  });

  return (
    <AppLayout
      sidebar={
        <FilterSidebar
          title="Task Queue"
          isCollapsed={sidebarCollapsed}
          onToggleCollapse={toggleSidebar}
          hasActiveFilters={hasActiveFilters}
          onClearFilters={handleClearFilters}
          headerIcon="checkmark-type-standard"
          actionSlot={
            <span className={styles.taskCount}>
              {tasks.length} {tasks.length === 1 ? 'task' : 'tasks'}
            </span>
          }
        >
          <TaskQueue
            tasks={tasks}
            selectedTaskId={taskId}
            onTaskSelect={handleTaskSelect}
            activeFilter={activeFilter}
            onFilterChange={handleFilterChange}
            sortBy={sortOption}
            onSortChange={handleSortChange}
            isLoading={isLoading}
          />
        </FilterSidebar>
      }
      sidebarCollapsed={sidebarCollapsed}
    >
      <div className={commonStyles.pageContainerMediumGap}>
        {taskId ? (
          <TaskDetailsPanel
            taskId={taskId}
            currentUserEmail={user?.email}
            onComplete={handleCompleteTask}
          />
        ) : (
          <div className={styles.emptyState}>
            <p className={styles.emptyTitle}>
              {TASK_MESSAGES.EMPTY_STATE.NO_TASK_SELECTED_TITLE}
            </p>
            <p className={styles.emptyDescription}>
              {TASK_MESSAGES.EMPTY_STATE.NO_TASK_SELECTED_DESCRIPTION}
            </p>
          </div>
        )}
      </div>
    </AppLayout>
  );
};

export default TasksPage;
