/**
 * Date format constants for consistent date formatting across the application
 *
 * Used with date formatting libraries like date-fns
 *
 * @example
 * ```typescript
 * import { format } from 'date-fns';
 * format(new Date(), DATE_FORMATS.DISPLAY); // "Jan 15, 2024 14:30"
 * ```
 */
export const DATE_FORMATS = {
  /** Display format: MMM dd, yyyy HH:mm (e.g., "Jan 15, 2024 14:30") */
  DISPLAY: 'MMM dd, yyyy HH:mm',

  /** Short format: MMM dd (e.g., "Jan 15") */
  SHORT: 'MMM dd',

  /** Long format with seconds: MMMM dd, yyyy HH:mm:ss (e.g., "January 15, 2024 14:30:45") */
  LONG: 'MMMM dd, yyyy HH:mm:ss',

  /** ISO 8601 format with timezone */
  ISO: "yyyy-MM-dd'T'HH:mm:ss.SSSxxx",

  /** Date only: yyyy-MM-dd (e.g., "2024-01-15") */
  DATE_ONLY: 'yyyy-MM-dd',

  /** Time only: HH:mm:ss (e.g., "14:30:45") */
  TIME_ONLY: 'HH:mm:ss',
} as const;

export type DateFormatType = typeof DATE_FORMATS[keyof typeof DATE_FORMATS];
