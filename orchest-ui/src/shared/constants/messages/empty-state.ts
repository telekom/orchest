/**
 * Empty state messages
 * Messages displayed when no data is available
 */
export const EMPTY_STATE_MESSAGES = {
  NO_PROCESSES: 'No process instances found',
  NO_DECISIONS: 'No decision instances found',
  NO_APPROVALS: 'No pending approvals',
  NO_RESULTS: 'No results found',
  NO_DIAGRAM: 'No diagram available',
  NO_DATA: 'No data available',
  NO_VARIABLES: 'No variables found',
  NO_HISTORY: 'No history available',
} as const;

export type EmptyStateMessageType = typeof EMPTY_STATE_MESSAGES[keyof typeof EMPTY_STATE_MESSAGES];
