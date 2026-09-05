import { UserTaskDTO } from '@/api/types/orchest-api';
import { TaskState } from '../constants/taskState';
import { TaskSortField, SortDirection } from '../constants/filterOptions';

/**
 * Custom filter rule definition
 */
export interface TaskFilterRule {
  field: 'assignee' | 'state' | 'processDefinitionId' | 'dueDate' | 'followUpDate' | 'priority' | 'candidateGroup';
  operator: 'equals' | 'contains' | 'before' | 'after' | 'greaterThan' | 'lessThan';
  value: string | number | Date;
}

/**
 * Saved custom filter
 */
export interface SavedTaskFilter {
  id: string;
  name: string;
  rules: TaskFilterRule[];
  createdAt: string;
}

/**
 * Active filter state
 */
export interface ActiveTaskFilter {
  id?: string; // undefined for default filters
  name: string;
  state?: TaskState;
  assignedToCurrentUser?: boolean;
  rules?: TaskFilterRule[];
}

/**
 * Task sort configuration
 */
export interface TaskSort {
  field: TaskSortField;
  direction: SortDirection;
}

/**
 * Task queue preferences (stored in localStorage)
 */
export interface TaskQueuePreferences {
  sortOption: string;
  activeFilterId?: string;
}

/**
 * Task list state
 */
export interface TaskListState {
  tasks: UserTaskDTO[];
  selectedTaskId?: string;
  filter: ActiveTaskFilter;
  sort: TaskSort;
  isLoading: boolean;
  error?: string;
}

/**
 * Task form data (for completing tasks)
 */
export interface TaskFormData {
  variables: Record<string, unknown>;
}

/**
 * Task notification payload
 */
export interface TaskNotification {
  taskId: string;
  taskName: string;
  processName?: string;
  assignedAt: string;
}

/**
 * Variable editor row
 */
export interface VariableRow {
  name: string;
  value: unknown;
  type: 'string' | 'number' | 'boolean' | 'object' | 'array';
  isNew?: boolean;
  isEditing?: boolean;
}
