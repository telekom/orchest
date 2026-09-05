/**
 * User task state enum
 * Represents all possible states of a user task in the workflow
 */
export enum TaskState {
  /** Task has been created but not yet claimed */
  CREATED = 'CREATED',

  /** Task has been assigned/claimed by a user */
  ASSIGNED = 'ASSIGNED',

  /** Task has been completed */
  COMPLETED = 'COMPLETED',

  /** Task has been cancelled */
  CANCELLED = 'CANCELLED',
}

/**
 * Check if a task state is terminal (final, no further transitions)
 *
 * @param state - Task state to check
 * @returns True if state is terminal (COMPLETED, CANCELLED)
 */
export const isTerminalTaskState = (state: TaskState | string): boolean => {
  const terminalStates = [TaskState.COMPLETED, TaskState.CANCELLED];

  if (typeof state === 'string') {
    return terminalStates.includes(state as TaskState) ||
           terminalStates.some(s => s === state.toUpperCase());
  }

  return terminalStates.includes(state);
};

/**
 * Check if a task is claimable (available for claiming)
 *
 * @param state - Task state to check
 * @returns True if task can be claimed
 */
export const isClaimableTaskState = (state: TaskState | string): boolean => {
  const claimableStates = [TaskState.CREATED];

  if (typeof state === 'string') {
    return claimableStates.includes(state as TaskState) ||
           claimableStates.some(s => s === state.toUpperCase());
  }

  return claimableStates.includes(state);
};

/**
 * Check if a task is assigned (claimed by a user)
 *
 * @param state - Task state to check
 * @returns True if task is assigned
 */
export const isAssignedTaskState = (state: TaskState | string): boolean => {
  const assignedStates = [TaskState.ASSIGNED];

  if (typeof state === 'string') {
    return assignedStates.includes(state as TaskState) ||
           assignedStates.some(s => s === state.toUpperCase());
  }

  return assignedStates.includes(state);
};
