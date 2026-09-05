/**
 * Hook for handling local date parsing and formatting without timezone issues
 */

/**
 * Parse a date string as a local Date object.
 * Accepts "YYYY-MM-DD" or "YYYY-MM-DDTHH:mm"
 */
export const parseLocalDate = (dateStr: string): Date => {
  if (dateStr.includes('T')) {
    return new Date(dateStr);
  }
  const [year, month, day] = dateStr.split('-').map(Number);
  return new Date(year, month - 1, day);
};

/**
 * Format a Date object as YYYY-MM-DDTHH:mm using local timezone.
 * Includes time if hours or minutes are non-zero, otherwise returns YYYY-MM-DD.
 */
export const formatLocalDate = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = date.getHours();
  const minutes = date.getMinutes();

  if (hours === 0 && minutes === 0) {
    return `${year}-${month}-${day}`;
  }

  const hh = String(hours).padStart(2, '0');
  const mm = String(minutes).padStart(2, '0');
  return `${year}-${month}-${day}T${hh}:${mm}`;
};

/**
 * Hook that provides local date utilities
 */
export const useLocalDate = () => {
  return {
    parseLocalDate,
    formatLocalDate,
  };
};
