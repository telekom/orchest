/**
 * Time Formatting Utilities
 * Shared time formatting functions for consistent display
 */

/**
 * Formats milliseconds into a human-readable time string
 * @param ms - Time in milliseconds
 * @returns Formatted time string (e.g., "2m 30s" or "45s")
 */
export function formatTimeRemaining(ms: number): string {
  const seconds = Math.floor(ms / 1000);
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;

  if (minutes > 0) {
    return `${minutes}m ${remainingSeconds}s`;
  }
  return `${remainingSeconds}s`;
}

/**
 * Formats milliseconds into minutes and seconds separately
 * @param ms - Time in milliseconds
 * @returns Object with minutes and seconds
 */
export function formatTimeComponents(ms: number): { minutes: number; seconds: number } {
  const seconds = Math.floor(ms / 1000);
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;

  return { minutes, seconds: remainingSeconds };
}
