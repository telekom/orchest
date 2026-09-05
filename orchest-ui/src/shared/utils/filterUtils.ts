/**
 * Filter utility functions
 * Helper functions for working with filter values
 */

export const FILTER_CONFIG = {
  RESET_VALUE: 'all',
} as const;

/**
 * Process filter "all" option value (matches label so combobox shows "All Processes", not "all").
 */
export const PROCESS_FILTER_ALL_VALUE = 'All Processes' as const;

/**
 * Convert filter value to null if it's the reset value
 *
 * @param value - Filter value from dropdown
 * @returns null if reset value, otherwise the original value
 *
 * @example
 * ```typescript
 * convertFilterValue('all') // returns null
 * convertFilterValue(PROCESS_FILTER_ALL_VALUE) // returns null
 * convertFilterValue('specific-id') // returns 'specific-id'
 * ```
 */
export const convertFilterValue = (value: string): string | null => {
  if (value === FILTER_CONFIG.RESET_VALUE || value === PROCESS_FILTER_ALL_VALUE) {
    return null;
  }
  return value;
};
