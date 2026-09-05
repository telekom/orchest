import { DEFAULT_FILTERS, DEFAULT_SORT, SORT_OPTIONS } from '@/features/task-management/constants/filterOptions';
import { buildSearchParams } from '../buildSearchParams';
import { parseEnum, serializeEnum } from '../serializers/primitives';
import type { UrlStateSchema } from '../types';

export const TASK_FILTER_IDS = [
  DEFAULT_FILTERS.ALL_OPEN.id,
  DEFAULT_FILTERS.ASSIGNED_TO_ME.id,
  DEFAULT_FILTERS.UNASSIGNED.id,
  DEFAULT_FILTERS.COMPLETED.id,
] as const;

export const TASK_SORT_VALUES = SORT_OPTIONS.map((option) => option.value);

export interface TasksUrlState {
  filter: string;
  sort: string;
}

export const DEFAULT_TASKS_URL_STATE: TasksUrlState = {
  filter: DEFAULT_FILTERS.ALL_OPEN.id,
  sort: DEFAULT_SORT,
};

export const tasksUrlSchema: UrlStateSchema<TasksUrlState> = {
  filter: {
    parse: (value) => parseEnum(value, TASK_FILTER_IDS, DEFAULT_TASKS_URL_STATE.filter) ?? DEFAULT_TASKS_URL_STATE.filter,
    serialize: (value) => serializeEnum(value, TASK_FILTER_IDS),
    isEmpty: (value) => value === DEFAULT_TASKS_URL_STATE.filter,
  },
  sort: {
    parse: (value) => parseEnum(value, TASK_SORT_VALUES, DEFAULT_TASKS_URL_STATE.sort) ?? DEFAULT_TASKS_URL_STATE.sort,
    serialize: (value) => serializeEnum(value, TASK_SORT_VALUES),
    isEmpty: (value) => value === DEFAULT_TASKS_URL_STATE.sort,
  },
};

export function buildTasksSearchParams(patch: Partial<TasksUrlState> = {}): URLSearchParams {
  return buildSearchParams(
    { ...DEFAULT_TASKS_URL_STATE, ...patch },
    tasksUrlSchema,
    DEFAULT_TASKS_URL_STATE,
  );
}

export function buildTasksPath(
  patch: Partial<TasksUrlState> = {},
  taskId?: string,
): string {
  const params = buildTasksSearchParams(patch);
  const query = params.toString();
  const basePath = taskId ? `/tasks/${taskId}` : '/tasks';
  return query ? `${basePath}?${query}` : basePath;
}
