/**
 * Accessibility (ARIA) labels
 * Screen reader labels for improved accessibility
 */
export const ARIA_LABELS = {
  BACK_TO_PROCESS_LIST: 'Back to process list',
  BACK_TO_DECISION_LIST: 'Back to decision list',
  EXPORT_TO_CSV: 'Export process instances to CSV',
  REFRESH_LIST: 'Refresh process list',
  RETRY_LOADING: 'Retry loading',
  CLOSE_DIALOG: 'Close dialog',
  OPEN_MENU: 'Open menu',
  CLOSE_MENU: 'Close menu',
  SELECT_MODELER_TYPE: 'Select modeler type',
  RESET_ZOOM: 'Reset zoom',
  EDIT_VARIABLE: 'Edit variable',
  DELETE_VARIABLE: 'Delete variable',
  VIEW_VARIABLE: 'View variable details',
  COPY_TO_CLIPBOARD: 'Copy value to clipboard',
  REFRESH_VARIABLES: 'Refresh variables',
  CANCEL_INSTANCE: 'Cancel instance',
  RETRY_INSTANCE: 'Retry instance',
} as const;

export type AriaLabelType = typeof ARIA_LABELS[keyof typeof ARIA_LABELS];
