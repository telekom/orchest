/**
 * Dialog and confirmation messages
 * Messages displayed in modal dialogs and confirmation prompts
 */
export const DIALOG_MESSAGES = {
  CONFIRM_DELETE: 'Are you sure you want to delete this item?',
  CONFIRM_CANCEL: 'Are you sure you want to cancel? Changes will be lost.',
  UNSAVED_CHANGES: 'You have unsaved changes. Do you want to save before leaving?',
  DELETE_VARIABLE: 'Are you sure you want to delete this variable?',
  CANCEL_INSTANCE: 'Are you sure you want to cancel this instance?',
  MODIFY_INSTANCE: 'Confirm instance modification',
} as const;

export type DialogMessageType = typeof DIALOG_MESSAGES[keyof typeof DIALOG_MESSAGES];
