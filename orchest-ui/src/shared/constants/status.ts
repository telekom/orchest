/**
 * Filter value constants
 * Special filter values used in dropdowns and filters
 */
export enum FilterValue {
  ALL = 'all',
  TOTAL = 'total'
}

/**
 * Process instance status enum
 * Represents all possible states of a process instance
 */
export enum ProcessStatus {
  ACTIVE = 'ACTIVE',
  RUNNING = 'RUNNING', 
  STARTED = 'STARTED',
  
  COMPLETED = 'COMPLETED',
  
  FAILED = 'FAILED',
  INCIDENT = 'INCIDENT',
  
  HOLD = 'HOLD',
  CANCELLED = 'CANCELLED',
  TERMINATED = 'TERMINATED'
}

/**
 * Decision instance status enum
 * Represents all possible states of a decision instance
 */
export enum DecisionStatus {
  EXECUTED = 'EXECUTED',
  FAILED = 'FAILED'
}

/**
 * Configuration status enum
 * Used for enabled/disabled states in settings
 */
export enum ConfigStatus {
  ENABLED = 'enabled',
  DISABLED = 'disabled'
}

/**
 * Check if a process status is terminal (final, no further transitions)
 *
 * @param status - Process status to check
 * @returns True if status is terminal (COMPLETED, FAILED, CANCELLED, TERMINATED)
 */
export const isTerminalProcessStatus = (status: ProcessStatus | string): boolean => {
  const terminalStatuses = [
    ProcessStatus.COMPLETED,
    ProcessStatus.FAILED,
    ProcessStatus.CANCELLED,
    ProcessStatus.TERMINATED
  ];
  
  if (typeof status === 'string') {
    return terminalStatuses.includes(status as ProcessStatus) ||
           terminalStatuses.some(s => s === status.toUpperCase());
  }
  
  return terminalStatuses.includes(status);
};

export const isErrorProcessStatus = (status: ProcessStatus | string): boolean => {
  const errorStatuses = [
    ProcessStatus.FAILED,
    ProcessStatus.INCIDENT
  ];
  
  if (typeof status === 'string') {
    return errorStatuses.includes(status as ProcessStatus) ||
           errorStatuses.some(s => s === status.toUpperCase());
  }
  
  return errorStatuses.includes(status);
};

export const isActiveProcessStatus = (status: ProcessStatus | string): boolean => {
  const activeStatuses = [
    ProcessStatus.ACTIVE,
    ProcessStatus.RUNNING,
    ProcessStatus.STARTED
  ];
  
  if (typeof status === 'string') {
    return activeStatuses.includes(status as ProcessStatus) ||
           activeStatuses.some(s => s === status.toUpperCase());
  }
  
  return activeStatuses.includes(status);
};