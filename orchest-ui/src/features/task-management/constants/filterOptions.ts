import { TaskState } from './taskState';

export const DEFAULT_FILTERS = {
  ALL_OPEN: {
    id: 'all-open',
    label: 'All open tasks',
    state: undefined,
  },
  ASSIGNED_TO_ME: {
    id: 'assigned-to-me',
    label: 'Assigned to me',
    state: TaskState.ASSIGNED,
    assignedToCurrentUser: true,
  },
  UNASSIGNED: {
    id: 'unassigned',
    label: 'Unassigned',
    state: TaskState.CREATED,
  },
  COMPLETED: {
    id: 'completed',
    label: 'Completed tasks',
    state: TaskState.COMPLETED,
  },
} as const;

export enum TaskSortField {
  CREATED_AT = 'createdAt',
  DUE_DATE = 'dueDate',
}

export enum SortDirection {
  ASC = 'asc',
  DESC = 'desc',
}

export const SORT_OPTIONS = [
  {
    value: `${TaskSortField.CREATED_AT}:${SortDirection.DESC}`,
    label: 'Created (Newest first)',
    field: TaskSortField.CREATED_AT,
    direction: SortDirection.DESC,
  },
  {
    value: `${TaskSortField.CREATED_AT}:${SortDirection.ASC}`,
    label: 'Created (Oldest first)',
    field: TaskSortField.CREATED_AT,
    direction: SortDirection.ASC,
  },
  {
    value: `${TaskSortField.DUE_DATE}:${SortDirection.ASC}`,
    label: 'Due date (Earliest first)',
    field: TaskSortField.DUE_DATE,
    direction: SortDirection.ASC,
  },
  {
    value: `${TaskSortField.DUE_DATE}:${SortDirection.DESC}`,
    label: 'Due date (Latest first)',
    field: TaskSortField.DUE_DATE,
    direction: SortDirection.DESC,
  },
] as const;

export const DEFAULT_SORT = SORT_OPTIONS[0].value;

export const toApiSortFormat = (sortValue: string): string => {
  const [field, direction] = sortValue.split(':');
  const prefix = direction === SortDirection.ASC ? '+' : '-';
  return `${prefix}${field}`;
};
