/**
 * Known timezone offsets in minutes from UTC.
 * Positive = ahead of UTC, Negative = behind UTC.
 */
const TIMEZONE_OFFSET_MINUTES: Record<string, number> = {
  'UTC': 0,
  'Europe/Berlin': 60,   // CET (+01:00), CEST would be +02:00
  'Europe/Paris': 60,
  'Asia/Kolkata': 330,   // IST (+05:30)
  'Asia/Calcutta': 330,
};

/**
 * Get offset in minutes for a timezone. Uses Intl API for accuracy (DST-aware),
 * falls back to static map.
 */
function getOffsetMinutes(timezone: string, refDate: Date): number {
  if (timezone === 'UTC') return 0;

  if (timezone === 'local' || !timezone) {
    return -refDate.getTimezoneOffset();
  }

  try {
    const utcStr = refDate.toLocaleString('en-US', { timeZone: 'UTC' });
    const tzStr = refDate.toLocaleString('en-US', { timeZone: timezone });
    const utcDate = new Date(utcStr);
    const tzDate = new Date(tzStr);
    return Math.round((tzDate.getTime() - utcDate.getTime()) / 60000);
  } catch {
    return TIMEZONE_OFFSET_MINUTES[timezone] ?? 0;
  }
}

/**
 * Converts a local date-time string (as seen by the user in their selected timezone)
 * to a UTC ISO string for the API.
 *
 * Example: toISODateTime("2024-01-15T14:30", "Asia/Kolkata")
 *   → User means 14:30 IST which is 09:00 UTC
 *   → Returns "2024-01-15T09:00:00.000Z"
 *
 * Example: toISODateTime("2024-01-15T14:30", "UTC")
 *   → User means 14:30 UTC
 *   → Returns "2024-01-15T14:30:00.000Z"
 */
export const toISODateTime = (dateString: string, timezone?: string): string => {
  if (dateString.endsWith('Z') || /[+-]\d{2}:\d{2}$/.test(dateString)) {
    return dateString;
  }

  let year: number, month: number, day: number, hours = 0, minutes = 0;

  if (dateString.includes('T')) {
    const [datePart, timePart] = dateString.split('T');
    const [y, mo, d] = datePart.split('-').map(Number);
    const [h, mi] = timePart.split(':').map(Number);
    year = y; month = mo; day = d; hours = h || 0; minutes = mi || 0;
  } else {
    const [y, mo, d] = dateString.split('-').map(Number);
    year = y; month = mo; day = d;
  }

  const refDate = new Date(Date.UTC(year, month - 1, day, hours, minutes, 0, 0));
  const offsetMin = getOffsetMinutes(timezone || 'local', refDate);

  const utcMs = Date.UTC(year, month - 1, day, hours, minutes, 0, 0) - (offsetMin * 60000);
  const utcDate = new Date(utcMs);

  return utcDate.toISOString();
};
