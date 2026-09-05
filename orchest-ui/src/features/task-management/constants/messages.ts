export const TASK_MESSAGES = {
  SUCCESS: {
    TASK_CLAIMED: 'Task claimed successfully',
    TASK_UNCLAIMED: 'Task released successfully',
    TASK_COMPLETED: 'Task completed successfully',
    TASK_ASSIGNED: 'Task assigned successfully',
    FILTER_SAVED: 'Filter saved successfully',
    FILTER_DELETED: 'Filter deleted successfully',
    VARIABLES_SAVED: 'Variables saved successfully',
    FORM_SUBMITTED: 'Form submitted successfully',
  },

  ERROR: {
    TASK_NOT_FOUND: 'Task not found',
    TASK_CLAIM_FAILED: 'Failed to claim task. Please try again.',
    TASK_UNCLAIM_FAILED: 'Failed to release task. Please try again.',
    TASK_COMPLETE_FAILED: 'Failed to complete task. Please try again.',
    TASK_ASSIGN_FAILED: 'Failed to assign task. Please try again.',
    LOADING_TASKS: 'Failed to load tasks. Please refresh the page.',
    LOADING_TASK_DETAILS: 'Failed to load task details',
    LOADING_FORM: 'Failed to load task form',
    INVALID_FORM_DATA: 'Please check your form data and try again',
    UNAUTHORIZED_CLAIM: 'You are not authorized to claim this task',
    UNAUTHORIZED_COMPLETE: 'You are not authorized to complete this task',
    UNAUTHORIZED_ASSIGN: 'You do not have permission to assign tasks',
    TASK_ALREADY_CLAIMED: 'This task has already been claimed',
    TASK_ALREADY_COMPLETED: 'This task has already been completed',
    SAVING_FILTER: 'Failed to save filter',
    DELETING_FILTER: 'Failed to delete filter',
    SAVING_VARIABLES: 'Failed to save variables',
  },

  INFO: {
    NO_TASKS_AVAILABLE: 'No tasks available',
    NO_TASKS_MATCH_FILTER: 'No tasks match the current filter',
    SELECT_TASK: 'Select a task from the queue to view details',
    TASK_ALREADY_ASSIGNED: 'This task is already assigned to another user',
    NOTIFICATIONS_ENABLED: 'Task notifications enabled',
    NOTIFICATIONS_DISABLED: 'Task notifications disabled',
    NOTIFICATIONS_PERMISSION_DENIED: 'Browser notification permission denied',
  },

  ASSIGN: {
    ASSIGN_HINT: 'The task will be reassigned to the specified user. This action requires admin privileges.',
  },

  NOTIFICATIONS: {
    NEW_TASK_ASSIGNED: 'New task assigned to you',
    TASK_DUE_SOON: 'Task due soon',
    TASK_OVERDUE: 'Task is overdue',
  },

  LOADING: {
    CLAIMING_TASK: 'Claiming task...',
    UNCLAIMING_TASK: 'Releasing task...',
    COMPLETING_TASK: 'Completing task...',
    ASSIGNING_TASK: 'Assigning task...',
    LOADING_TASKS: 'Loading tasks...',
    LOADING_TASK_DETAILS: 'Loading task details...',
    LOADING_FORM: 'Loading form...',
    SAVING_VARIABLES: 'Saving variables...',
  },

  EMPTY_STATE: {
    NO_TASKS_TITLE: 'No tasks available',
    NO_TASKS_DESCRIPTION: 'There are no tasks assigned to you or your groups',
    NO_TASKS_FILTERED_TITLE: 'No tasks found',
    NO_TASKS_FILTERED_DESCRIPTION: 'Try adjusting your filters to see more tasks',
    NO_TASK_SELECTED_TITLE: 'No task selected',
    NO_TASK_SELECTED_DESCRIPTION: 'Select a task from the queue to view its details',
    NO_SAVED_FILTERS_TITLE: 'No saved filters',
    NO_SAVED_FILTERS_DESCRIPTION: 'Create custom filters to quickly find relevant tasks',
  },
} as const;

export const TASK_FORM_LABELS = {
  TASK_NAME: 'Task Name',
  ASSIGNEE: 'Assignee',
  DUE_DATE: 'Due Date',
  FOLLOW_UP_DATE: 'Follow-up Date',
  PRIORITY: 'Priority',
  CANDIDATE_USERS: 'Candidate Users',
  CANDIDATE_GROUPS: 'Candidate Groups',
  VARIABLES: 'Variables',
  FORM: 'Form',
  PROCESS: 'Process',
  CREATED_AT: 'Created',
  CLAIMED_AT: 'Claimed',
  COMPLETED_AT: 'Completed',
} as const;

export const TASK_ACTION_LABELS = {
  CLAIM: 'Assign to me',
  UNCLAIM: 'Release',
  COMPLETE: 'Complete Task',
  REASSIGN: 'Reassign',
  ADD_VARIABLE: 'Add Variable',
  SAVE_FILTER: 'Save Filter',
  APPLY_FILTER: 'Apply',
  CLEAR_FILTER: 'Clear Filter',
  CREATE_FILTER: 'Create Filter',
  EDIT_FILTER: 'Edit Filter',
  DELETE_FILTER: 'Delete Filter',
} as const;
