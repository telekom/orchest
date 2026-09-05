/**
 * Chat reducer action type constants
 * Used by BpmnGeneratorChat component for state management
 */
export const CHAT_ACTIONS = {
  SET_LOADING: 'SET_LOADING',
  ADD_MESSAGE: 'ADD_MESSAGE',
  APPEND_TO_LAST_MESSAGE: 'APPEND_TO_LAST_MESSAGE',
  SET_MESSAGES: 'SET_MESSAGES',
  SET_INPUT: 'SET_INPUT',
  SET_MODEL: 'SET_MODEL',
  SET_ERROR: 'SET_ERROR',
  RESET: 'RESET',
} as const;

export type ChatActionType = typeof CHAT_ACTIONS[keyof typeof CHAT_ACTIONS];
