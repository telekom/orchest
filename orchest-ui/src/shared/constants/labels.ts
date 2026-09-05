export const COMMON_LABELS = {
  ACTIONS: 'Actions',
  BACK: 'Back',
  RETRY: 'Retry',
  STATUS: 'Status',
  VERSION: 'Version',
  PROCESS: 'Process',
  ENABLED: 'ENABLED',
  DISABLED: 'DISABLED',
} as const;

export const PROCESS_LABELS = {
  PROCESS_DETAILS: 'Process Details',
  PROCESS_NAME: 'Process Name',
  PROCESS_VARIABLES: 'Process Variables',
  PROCESS_FILTERS: 'Process Filters',
  TASK_HISTORY: 'Task History',
  WINDOW_DURATION: 'Window Duration',
  ALLOWED_SIZE: 'Allowed Size',
} as const;

export const FILTER_LABELS = {
  ALL_PROCESSES: 'All Processes',
  ALL_VERSIONS: 'All Versions',
  ALL_STATUS: 'All Status',
  SELECT_STATUS: 'Select status',
  SELECT_PROCESS: 'Select process',
  SELECT_VERSION: 'Select version',
  SELECT_MODEL: 'Select Model',
} as const;

export const STATUS_LABELS = {
  RUNNING: 'Running',
  COMPLETED: 'Completed',
  FAILED: 'Failed',
  HOLD: 'Hold',
  INCIDENT: 'Incident',
  CANCELLED: 'Cancelled',
} as const;

export const PLACEHOLDER_LABELS = {
  SEARCH_PROCESSES: 'Search processes...',
} as const;

export const ACTION_LABELS = {
  EDIT_ORCHESTRATION_SETTING: 'Edit Orchestration Setting',
} as const;
