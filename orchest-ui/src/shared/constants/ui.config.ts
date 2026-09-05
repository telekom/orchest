/**
 * UI Configuration Constants
 * All UI-related configuration values and magic numbers
 */

export const LAYOUT = {
  SIDEBAR_WIDTH: '16rem',
  SIDEBAR_COLLAPSED_WIDTH: '3rem',
  MIN_DIAGRAM_HEIGHT: '300px',
  DEFAULT_DIAGRAM_HEIGHT: 'min(50vh, 600px)',
  MAX_DIAGRAM_HEIGHT: '800px',
  RESIZABLE_MIN_HEIGHT: '500px',
  TABLE_MAX_HEIGHT: '500px',
  TABLE_MAX_HEIGHT_MEDIUM: '40vh',
} as const;

export const SPACING = {
  NONE: 0,
  XS: '0.25rem',  // 4px
  SM: '0.5rem',   // 8px
  MD: '0.75rem',  // 12px
  LG: '1rem',     // 16px
  XL: '1.5rem',   // 24px
  XXL: '2rem',    // 32px
} as const;

export const SIZING = {
  ICON_SM: '0.875rem', // 14px
  ICON_MD: '1rem',     // 16px
  ICON_LG: '1.25rem',  // 20px
  ICON_XL: '1.5rem',   // 24px
  BUTTON_HEIGHT: '1.875rem', // 30px
  BUTTON_HEIGHT_SM: '1.5rem', // 24px
  TAB_HEIGHT: '1.875rem', // 30px
} as const;

export const Z_INDEX = {
  DROPDOWN: 1000,
  STICKY: 1020,
  FIXED: 1030,
  MODAL_BACKDROP: 1040,
  MODAL: 1050,
  POPOVER: 1060,
  TOOLTIP: 1070,
} as const;

export const BREAKPOINTS = {
  SM: '640px',
  MD: '768px',
  LG: '1024px',
  XL: '1280px',
  '2XL': '1536px',
} as const;

export const TIMING = {
  // Polling intervals
  POLLING_INTERVAL: 1000,
  POLLING_TIMEOUT: 60000,
  PROCESS_DETAILS_POLL_INTERVAL: 5000,
  APPROVALS_POLL_INTERVAL: 5000,

  // Debounce delays
  DEBOUNCE_DELAY: 300,
  ROLE_CHANGE_DEBOUNCE: 100,
  SEARCH_DEBOUNCE: 500,

  // Toast durations (milliseconds)
  TOAST_ERROR_DURATION: 1000,
  TOAST_WARNING_DURATION: 1000,
  TOAST_DEFAULT_DURATION: 1000,
  TOAST_REMOVE_DELAY: 1000,
  COPY_FEEDBACK_DURATION: 2000,
  NOTIFICATION_TEST_DURATION: 1000,

  // Redirect delays
  NAVIGATION_REDIRECT_DELAY: 2000,
  AUTH_REDIRECT_DELAY: 3000,
  QUICK_REDIRECT_DELAY: 1500,
  INSTANCE_MODIFICATION_DELAY: 500,

  // Auth init: max wait for MSAL initialize + silent token refresh
  AUTH_INIT_TIMEOUT: 15000,
  // MSAL hidden-iframe silent renew timeout
  AUTH_IFRAME_HASH_TIMEOUT: 10000,

  // Component delays
  DIAGRAM_INIT_DELAY: 0,
  DIAGRAM_ZOOM_RESET_DELAY: 150,
  TEXT_OVERFLOW_CHECK_DELAY: 10,

  // Animation durations
  ANIMATION_FAST: 150,
  ANIMATION_DEFAULT: 300,
  ANIMATION_SLOW: 500,
} as const;

export const PAGINATION = {
  ITEMS_PER_PAGE: 25,
  MAX_ITEMS_PER_PAGE: 100,
  PAGE_SIZE_OPTIONS: [10, 25, 50, 100],
  DEFAULT_PAGE: 1,
} as const;

/**
 * @deprecated Use VALIDATION_LIMITS from './validation' instead
 * Kept for backwards compatibility
 */
export { VALIDATION_LIMITS } from './validation';

export const FILE_LIMITS = {
  MAX_SIZE: 10 * 1024 * 1024, // 10MB
  ALLOWED_TYPES: ['.bpmn', '.dmn', '.xml', '.form'] as const,
  CHUNK_SIZE: 1024 * 1024, // 1MB chunks for large files
} as const;

export const DIAGRAM_CONFIG = {
  DEFAULT_ZOOM: 1,
  MIN_ZOOM: 0.1,
  MAX_ZOOM: 4,
  ZOOM_STEP: 0.1,
  FIT_VIEWPORT_PADDING: 20,
} as const;

export const RETRY_CONFIG = {
  MAX_RETRIES: 3,
  RETRY_DELAY: 1000,
  EXPONENTIAL_BACKOFF: true,
} as const;

export const CHART_CONFIG = {
  DEFAULT_COLORS: [
    '#3b82f6', '#ef4444', '#10b981', '#f59e0b',
    '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'
  ] as const,
  ANIMATION_DURATION: 300,
  MAX_CHART_POINTS: 100,
} as const;

export const TABLE_CONFIG = {
  MIN_COLUMN_WIDTH: 100,
  DEFAULT_COLUMN_WIDTH: 150,
  MAX_COLUMN_WIDTH: 500,
  HEADER_HEIGHT: 40,
  ROW_HEIGHT: 48,
} as const;

// Process/Decision instance states
export const INSTANCE_STATES = {
  RETRYABLE: ['hold', 'incident'] as const,
  TERMINAL: ['completed', 'terminated', 'cancelled'] as const,
} as const;

// Feature flags
export const FEATURE_FLAGS = {
  ENABLE_DARK_MODE: true,
  ENABLE_ADVANCED_FILTERS: true,
  ENABLE_EXPORT_FEATURES: true,
  ENABLE_REAL_TIME_UPDATES: false,
  ENABLE_ANALYTICS: true,
  ENABLE_CODE_GENERATION: true,
} as const;

// Type exports
export type SpacingValue = typeof SPACING[keyof typeof SPACING];
export type SizingValue = typeof SIZING[keyof typeof SIZING];
export type ZIndexValue = typeof Z_INDEX[keyof typeof Z_INDEX];
export type TimingValue = typeof TIMING[keyof typeof TIMING];
