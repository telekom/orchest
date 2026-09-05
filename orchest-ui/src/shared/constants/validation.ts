/**
 * Validation regex patterns and limits
 *
 * Used for form validation and input sanitization
 *
 * @example
 * ```typescript
 * const isValid = VALIDATION_REGEX.EMAIL.test(userInput);
 * ```
 */
export const VALIDATION_REGEX = {
  /** Email validation pattern */
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,

  /** Process ID validation: alphanumeric, underscore, and hyphen only */
  PROCESS_ID: /^[a-zA-Z0-9_-]+$/,
} as const;

/**
 * Validation limits for various inputs
 *
 * Centralized constraints to ensure consistency across forms
 */
export const VALIDATION_LIMITS = {
  // Pagination limits
  /** Minimum page size for pagination */
  MIN_PAGE_SIZE: 1,

  /** Maximum page size for pagination */
  MAX_PAGE_SIZE: 100,

  // Retry limits
  /** Minimum retry count for failed operations */
  MIN_RETRY_COUNT: 1,

  /** Maximum retry count for failed operations */
  MAX_RETRY_COUNT: 10,

  // Text input limits (from ui.config.ts)
  /** Minimum password length */
  MIN_PASSWORD_LENGTH: 8,

  /** Maximum general text length */
  MAX_TEXT_LENGTH: 500,

  /** Maximum name field length */
  MAX_NAME_LENGTH: 100,

  /** Minimum name field length */
  MIN_NAME_LENGTH: 2,

  /** Maximum message/description length */
  MAX_MESSAGE_LENGTH: 5000,

  /** Minimum version string length */
  MIN_VERSION_LENGTH: 1,

  /** Maximum version string length */
  MAX_VERSION_LENGTH: 50,
} as const;

export type ValidationRegexType = typeof VALIDATION_REGEX[keyof typeof VALIDATION_REGEX];
export type ValidationLimitsType = typeof VALIDATION_LIMITS;
