/**
 * Storage keys for task management feature
 * All keys are prefixed with 'orchest-ui-tasks-' to avoid conflicts
 */
export const TASK_STORAGE_KEYS = {
  /** Saved custom filters (array of filter objects) */
  SAVED_FILTERS: 'orchest-ui-tasks-saved-filters',

  /** Currently active filter ID */
  ACTIVE_FILTER: 'orchest-ui-tasks-active-filter',

  /** Current sort option */
  SORT_OPTION: 'orchest-ui-tasks-sort-option',

  /** Browser notification permission state */
  NOTIFICATIONS_ENABLED: 'orchest-ui-tasks-notifications-enabled',

  /** Last selected task ID (for restoring selection) */
  LAST_SELECTED_TASK: 'orchest-ui-tasks-last-selected',

  /** Queue panel width (for resizable split view) */
  QUEUE_PANEL_WIDTH: 'orchest-ui-tasks-queue-panel-width',
} as const;

export type TaskStorageKeyType = typeof TASK_STORAGE_KEYS[keyof typeof TASK_STORAGE_KEYS];
