/**
 * Toast notification messages
 * Success, error, info, and warning messages displayed via toast notifications
 */
export const TOAST_MESSAGES = {
  // Success messages
  SUCCESS: {
    SAVE: 'Changes saved successfully',
    DELETE: 'Item deleted successfully',
    CREATE: 'Item created successfully',
    UPDATE: 'Item updated successfully',
    UPLOAD: 'File uploaded successfully',
    COPY: 'Copied to clipboard',
    EXPORT: 'Data exported successfully',
    BPMN_UPLOADED: 'BPMN successfully uploaded',
    FORM_SAVED: 'Form saved successfully',
    VARIABLES_REFRESHED: 'Variables refreshed',
    CODE_GENERATION_STARTED: 'Code generation started in background. Hold tight!',
    CODE_GENERATION_COMPLETED: 'Code generation completed successfully!',
  },

  // Error messages
  ERROR: {
    GENERIC: 'An unexpected error occurred. Please try again.',
    NETWORK: 'Network error. Please check your connection.',
    UNAUTHORIZED: 'You are not authorized to perform this action.',
    FORBIDDEN: 'Access denied. You do not have permission.',
    PERMISSION_ERROR: 'Permission check failed. Please try again.',
    ROLE_REQUIRED: 'This action requires specific user role(s).',
    NOT_FOUND: 'The requested resource was not found.',
    SERVER_ERROR: 'Server error. Please try again later.',
    VALIDATION: 'Please check your input and try again.',
    FILE_TOO_LARGE: 'File size exceeds the maximum allowed limit.',
    INVALID_FILE_TYPE: 'Invalid file type. Please upload a valid file.',
    PROCESS_NOT_FOUND: 'Process instance not found.',
    DECISION_NOT_FOUND: 'Decision instance not found.',
    DEPLOYMENT_FAILED: 'Deployment failed. Please check your BPMN/DMN file.',
    LOADING_ORCHESTRATION: 'Error loading orchestration settings',
    LOADING_BPMN_DIAGRAM: 'Failed to load BPMN diagram',
    LOADING_BPMN_FILE: 'There was a problem while loading the BPMN file',
    LOADING_FORM_SCHEMA: 'Failed to load form schema',
    LOADING_VERSION: 'Failed to load version',
    LOADING_PROCESS_DETAILS: 'Failed to load process details',
    SAVING_FORM: 'Failed to save form',
    GENERATING_CODE: 'Failed to generate code. Please try again.',
    CODE_GENERATION_FAILED: 'Code generation failed. Please try again.',
    CODE_GENERATION_TIMEOUT: 'Code generation timed out. Please try again.',
    EXPORT_BPMN_XML: 'Failed to export BPMN XML.',
    NO_SEQUENCE_DATA: 'No sequence execution data available',
    NO_RETRYABLE_ACTIVITIES: 'No retryable activities found',
    MODELER_NOT_INITIALIZED: 'BPMN modeler is not initialized.',
    MODEL_SELECTION_REQUIRED: 'You need to select a model first.',
    MESSAGE_TOO_LONG: 'Message is too long.',
  },

  // Info messages
  INFO: {
    DIAGRAM_NOT_AVAILABLE: 'BPMN diagram not available for this process',
    NO_INSTANCES_FOUND: 'No instances found for this process',
  },

  // Warning messages
  WARNING: {
    REDIRECTING_TO_PROCESS_LIST: 'Redirecting to process list...',
  },
} as const;

export type ToastMessageType =
  | typeof TOAST_MESSAGES[keyof typeof TOAST_MESSAGES][keyof typeof TOAST_MESSAGES[keyof typeof TOAST_MESSAGES]]
  | string;
