import type { ReportingRangeSlice } from '@/features/process-management/utils/dashboardReportingRange';
import {
  isValidDashboardHm,
  isValidDashboardYmd,
  normalizeDashboardTimeZoneId,
} from '@/features/process-management/utils/dashboardReportingRange';

const SESSION_KEY = 'orchest.usage.reportingRange';

export function loadUsageReportingRangeFromSession(): ReportingRangeSlice {
  const empty: ReportingRangeSlice = {
    fromDate: null,
    fromTime: null,
    toDate: null,
    toTime: null,
    timeZoneId: 'UTC',
  };

  try {
    const raw = sessionStorage.getItem(SESSION_KEY);
    if (!raw) return empty;

    const o = JSON.parse(raw) as Record<string, unknown>;
    const storedTz =
      typeof o.timeZoneId === 'string' && o.timeZoneId.length > 0
        ? o.timeZoneId
        : 'UTC';
    const timeZoneId = normalizeDashboardTimeZoneId(storedTz);

    const fd =
      typeof o.fromDate === 'string' && isValidDashboardYmd(o.fromDate)
        ? o.fromDate
        : null;
    const td =
      typeof o.toDate === 'string' && isValidDashboardYmd(o.toDate)
        ? o.toDate
        : null;
    const ft =
      typeof o.fromTime === 'string' && isValidDashboardHm(o.fromTime)
        ? o.fromTime.slice(0, 5)
        : null;
    const tt =
      typeof o.toTime === 'string' && isValidDashboardHm(o.toTime)
        ? o.toTime.slice(0, 5)
        : null;

    return { fromDate: fd, fromTime: ft, toDate: td, toTime: tt, timeZoneId };
  } catch {
    return empty;
  }
}

export function saveUsageReportingRangeToSession(
  slice: ReportingRangeSlice,
): void {
  try {
    const normalized: ReportingRangeSlice = {
      ...slice,
      timeZoneId: normalizeDashboardTimeZoneId(slice.timeZoneId),
      fromTime: slice.fromTime ? slice.fromTime.slice(0, 5) : null,
      toTime: slice.toTime ? slice.toTime.slice(0, 5) : null,
    };
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(normalized));
  } catch {
    /* quota / private mode */
  }
}

export function dateToYmd(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

export function dateToHm(d: Date): string {
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

export function parseYmdToLocalDate(ymd: string, hm = '00:00'): Date | null {
  if (!isValidDashboardYmd(ymd) || !isValidDashboardHm(hm)) return null;
  const [y, mo, d] = ymd.split('-').map(Number);
  const [h, mi] = hm.split(':').map(Number);
  return new Date(y, mo - 1, d, h, mi, 0, 0);
}
