/**
 * Loading state messages
 * Messages displayed during async operations
 */
export const LOADING_MESSAGES = {
  GENERIC: 'Loading...',
  PROCESS_DETAILS: 'Loading process details...',
  PROCESS_LIST: 'Loading process list...',
  APPROVALS: 'Loading approvals...',
  DIAGRAM: 'Loading diagram...',
  DECISION_DETAILS: 'Loading decision details...',
  DECISION_LIST: 'Loading decision list...',
  ORCHESTRATION_SETTINGS: 'Loading orchestration settings...',
  STATS: 'Loading statistics...',
} as const;

export type LoadingMessageType = typeof LOADING_MESSAGES[keyof typeof LOADING_MESSAGES];
