/**
 * UI label constants
 * Button labels, field labels, and tab labels for the user interface
 */
export const BUTTON_LABELS = {
  SAVE: 'Save',
  CANCEL: 'Cancel',
  DELETE: 'Delete',
  EDIT: 'Edit',
  CREATE: 'Create',
  UPDATE: 'Update',
  BACK: 'Back',
  NEXT: 'Next',
  CONFIRM: 'Confirm',
  CLOSE: 'Close',
  RETRY: 'Retry',
  REFRESH: 'Refresh',
  EXPORT: 'Export',
  IMPORT: 'Import',
  UPLOAD: 'Upload',
  DOWNLOAD: 'Download',
  SEARCH: 'Search',
  FILTER: 'Filter',
  CLEAR: 'Clear Filters',
  APPLY: 'Apply',
  RESET: 'Reset',
  VIEW: 'View',
  DEPLOY: 'Deploy',
  NEW: 'New',
  UNDO: 'Undo',
  REDO: 'Redo',
  ZOOM_IN: 'Zoom In',
  ZOOM_OUT: 'Zoom Out',
  FIT_TO_SCREEN: 'Fit to Screen',
  CENTER: 'Center diagram',
  GENERATE_CODE: 'Generate Code',
  COPY: 'Copy',
  PASTE: 'Paste',
} as const;

export const FIELD_LABELS = {
  NAME: 'Name',
  TYPE: 'Type',
  VALUE: 'Value',
  STATUS: 'Status',
  VERSION: 'Version',
  CREATED_AT: 'Created At',
  UPDATED_AT: 'Updated At',
  DESCRIPTION: 'Description',
} as const;

export const TAB_LABELS = {
  PROCESS_VARIABLES: 'Process Variables',
  TASK_HISTORY: 'Task History',
  OVERVIEW: 'Overview',
  DIAGRAM: 'Diagram',
  DETAILS: 'Details',
  SETTINGS: 'Settings',
} as const;

export type ButtonLabelType = typeof BUTTON_LABELS[keyof typeof BUTTON_LABELS];
export type FieldLabelType = typeof FIELD_LABELS[keyof typeof FIELD_LABELS];
export type TabLabelType = typeof TAB_LABELS[keyof typeof TAB_LABELS];
