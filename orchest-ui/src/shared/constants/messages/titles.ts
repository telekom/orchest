/**
 * Page and section title constants
 * Titles displayed in page headers and section headings
 */
export const TITLE_TEXT = {
  PROCESS_LIST: 'Process List',
  PROCESS_DETAILS: 'Process Details',
  DECISION_LIST: 'Decision List',
  DECISION_DETAILS: 'Decision Details',
  MODELER: 'Workflow Modeler',
  BPMN_GENERATOR: 'BPMN Generator',
  ORCHESTRATION_SETTINGS: 'Orchestration Settings',
  VIEW_VARIABLE: 'View Variable',
  EDIT_VARIABLE: 'Edit Variable',
  DELETE_VARIABLE: 'Delete Variable',
  ACTIVITY_ACTIONS: 'Activity Actions',
} as const;

export type TitleTextType = typeof TITLE_TEXT[keyof typeof TITLE_TEXT];
