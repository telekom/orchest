/**
 * Placeholder text constants
 * Placeholder text for input fields and select dropdowns
 */
export const PLACEHOLDER_TEXT = {
  SEARCH: 'Search...',
  SEARCH_VARIABLES: 'Search variables...',
  SEARCH_PROCESSES: 'Search processes...',
  SELECT_MODEL: 'Select Model',
  SELECT_TYPE: 'Select Type',
  SELECT_BOOLEAN: 'Select boolean value',
  ENTER_VALUE: 'Enter value',
  VARIABLE_NAME: 'Variable name',
  MESSAGE_ASSISTANT: 'Message BPMN Assistant...',
  VERSION: 'Version',
} as const;

export type PlaceholderTextType = typeof PLACEHOLDER_TEXT[keyof typeof PLACEHOLDER_TEXT];
