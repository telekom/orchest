import { DEFAULT_FILTERS } from '@/features/task-management/constants/filterOptions';
import type { ActiveTaskFilter } from '@/features/task-management/types';
import {
  DEFAULT_TASKS_URL_STATE,
  tasksUrlSchema,
  type TasksUrlState,
} from '@/shared/url-state/configs/tasksUrlState';
import { useUrlFilters } from '@/shared/url-state/hooks/useUrlFilters';
import type { UrlHistoryMode } from '@/shared/url-state/types';
import { useCallback, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';

function filterIdToActiveFilter(filterId: string): ActiveTaskFilter {
  switch (filterId) {
    case DEFAULT_FILTERS.ASSIGNED_TO_ME.id:
      return {
        id: DEFAULT_FILTERS.ASSIGNED_TO_ME.id,
        name: DEFAULT_FILTERS.ASSIGNED_TO_ME.label,
        state: DEFAULT_FILTERS.ASSIGNED_TO_ME.state,
        assignedToCurrentUser: true,
      };
    case DEFAULT_FILTERS.UNASSIGNED.id:
      return {
        id: DEFAULT_FILTERS.UNASSIGNED.id,
        name: DEFAULT_FILTERS.UNASSIGNED.label,
        state: DEFAULT_FILTERS.UNASSIGNED.state,
      };
    case DEFAULT_FILTERS.COMPLETED.id:
      return {
        id: DEFAULT_FILTERS.COMPLETED.id,
        name: DEFAULT_FILTERS.COMPLETED.label,
        state: DEFAULT_FILTERS.COMPLETED.state,
      };
    default:
      return {
        id: DEFAULT_FILTERS.ALL_OPEN.id,
        name: DEFAULT_FILTERS.ALL_OPEN.label,
      };
  }
}

export function useTasksPageUrlState() {
  const [searchParams] = useSearchParams();

  const urlState = useUrlFilters<TasksUrlState>({
    schema: tasksUrlSchema,
    defaults: DEFAULT_TASKS_URL_STATE,
    pushHistoryKeys: ['filter', 'sort'],
    onInvalidParams: (keys) => {
      if (import.meta.env.DEV) {
        console.warn('[TasksPage] Ignored invalid URL params:', keys);
      }
    },
  });

  const activeFilter = useMemo(
    () => filterIdToActiveFilter(urlState.filters.filter),
    [urlState.filters.filter],
  );

  const sortOption = urlState.filters.sort;

  const setActiveFilter = useCallback(
    (filter: ActiveTaskFilter) => {
      const filterId = filter.id ?? DEFAULT_FILTERS.ALL_OPEN.id;
      urlState.setFilters({ filter: filterId }, 'push');
    },
    [urlState.setFilters],
  );

  const setSortOption = useCallback(
    (sort: string) => {
      urlState.setFilters({ sort }, 'push');
    },
    [urlState.setFilters],
  );

  const clearFilters = useCallback(() => {
    urlState.clearFilters('push');
  }, [urlState.clearFilters]);

  const hasActiveFilters = urlState.filters.filter !== DEFAULT_TASKS_URL_STATE.filter;

  const taskQueryString = useMemo(() => searchParams.toString(), [searchParams]);

  const buildTaskPath = useCallback(
    (taskId?: string) => {
      const basePath = taskId ? `/tasks/${taskId}` : '/tasks';
      return taskQueryString ? `${basePath}?${taskQueryString}` : basePath;
    },
    [taskQueryString],
  );

  return {
    activeFilter,
    sortOption,
    setActiveFilter,
    setSortOption,
    clearFilters,
    hasActiveFilters,
    buildTaskPath,
    setFilters: urlState.setFilters as (patch: Partial<TasksUrlState>, mode?: UrlHistoryMode) => void,
  };
}

export type { TasksUrlState };
