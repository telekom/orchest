import { toISODateTime } from '@/api/core/utils/dateUtils';

const YMD = /^\d{4}-\d{2}-\d{2}$/;
const HM = /^([01]\d|2[0-3]):[0-5]\d$/;

export const DASHBOARD_REPORTING_TIMEZONES = [
  { label: 'UTC', value: 'UTC' as const },
  { label: 'CET', value: 'Europe/Berlin' as const },
  { label: 'IST', value: 'Asia/Kolkata' as const },
] as const;

export type DashboardReportingTimeZone =
  (typeof DASHBOARD_REPORTING_TIMEZONES)[number]['value'];

export type ReportingRangeSlice = {
  /** Date in selected zone, `yyyy-MM-dd` */
  fromDate: string | null;
  /** Local time in selected zone, `HH:mm` */
  fromTime: string | null;
  toDate: string | null;
  toTime: string | null;
  timeZoneId: string;
};

const SESSION_KEY = 'orchest.dashboard.reportingRange';

export function minYmd(a: string, b: string): string {
  return a <= b ? a : b;
}

export function isValidDashboardYmd(s: string | null | undefined): boolean {
  return !!(s && YMD.test(s));
}

export function isValidDashboardHm(s: string | null | undefined): boolean {
  return !!(s && HM.test(s));
}

export function normalizeDashboardTimeZoneId(stored: string): DashboardReportingTimeZone {
  if (
    stored === 'UTC' ||
    stored === 'Europe/Berlin' ||
    stored === 'Asia/Kolkata'
  ) {
    return stored;
  }
  return 'UTC';
}

/** Resolved IANA id for stats bounds (dashboard only exposes UTC / CET / IST). */
export function resolveReportingTimeZone(storedId: string): string {
  return normalizeDashboardTimeZoneId(storedId || 'UTC');
}

export function plainDateTodayInZone(timeZone: string): string {
  try {
    const fmt = new Intl.DateTimeFormat('en-CA', {
      timeZone,
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
    return fmt.format(new Date());
  } catch {
    return new Date().toISOString().slice(0, 10);
  }
}

export function wallInZoneToUtcIso(
  dateStr: string,
  timeHm: string,
  zone: string
): string | null {
  try {
    if (!YMD.test(dateStr) || !HM.test(timeHm)) return null;
    const [y, mo, d] = dateStr.split('-').map(Number);
    const [h, mi] = timeHm.split(':').map(Number);

    if (zone === 'UTC') {
      return new Date(Date.UTC(y, mo - 1, d, h, mi, 0, 0)).toISOString();
    }

    const utcGuessMs = Date.UTC(y, mo - 1, d, h, mi, 0, 0);
    const fmt = new Intl.DateTimeFormat('en-US', {
      timeZone: zone,
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
    });
    const parts = fmt.formatToParts(new Date(utcGuessMs));
    const get = (t: string) =>
      Number(parts.find((p) => p.type === t)?.value ?? '0');
    let hz = get('hour');
    if (hz === 24) hz = 0;
    const zoneSeenAsUtcMs = Date.UTC(
      get('year'),
      get('month') - 1,
      get('day'),
      hz,
      get('minute'),
      get('second'),
      0,
    );
    const offsetMs = zoneSeenAsUtcMs - utcGuessMs;
    const actualUtcMs = utcGuessMs - offsetMs;
    return new Date(actualUtcMs).toISOString();
  } catch (err) {
    // eslint-disable-next-line no-console
    console.warn('[wallInZoneToUtcIso] failed', { dateStr, timeHm, zone, err });
    return null;
  }
}

/** API payload fragment for `GET /orchest/stats` — null means “no date filter”. */
export function buildOrchestStatsApiQuery(slice: ReportingRangeSlice): {
  from: string;
  to: string;
  timeZone: string;
} | null {
  const { fromDate, fromTime, toDate, toTime, timeZoneId } = slice;
  const timeZone = resolveReportingTimeZone(timeZoneId);

  const hasAny =
    fromDate != null ||
    toDate != null ||
    fromTime != null ||
    toTime != null;
  if (!hasAny) return null;

  if (
    !isValidDashboardYmd(fromDate) ||
    !isValidDashboardYmd(toDate) ||
    !isValidDashboardHm(fromTime) ||
    !isValidDashboardHm(toTime)
  ) {
    return null;
  }

  const fromOffset = toISODateTime(`${fromDate}T${fromTime}`, timeZone);
  const toOffset = toISODateTime(`${toDate}T${toTime}`, timeZone);

  if (Date.parse(fromOffset) > Date.parse(toOffset)) {
    return { from: toOffset, to: fromOffset, timeZone };
  }

  return { from: fromOffset, to: toOffset, timeZone };
}

export function loadReportingRangeFromSession(): ReportingRangeSlice {
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

    if ('fromDate' in o || 'toDate' in o) {
      const fd =
        typeof o.fromDate === 'string' && isValidDashboardYmd(o.fromDate) ? o.fromDate : null;
      const td =
        typeof o.toDate === 'string' && isValidDashboardYmd(o.toDate) ? o.toDate : null;
      const ft =
        typeof o.fromTime === 'string' && isValidDashboardHm(o.fromTime)
          ? o.fromTime.slice(0, 5)
          : null;
      const tt =
        typeof o.toTime === 'string' && isValidDashboardHm(o.toTime)
          ? o.toTime.slice(0, 5)
          : null;
      return { fromDate: fd, fromTime: ft, toDate: td, toTime: tt, timeZoneId };
    }

    const fy = typeof o.fromYmd === 'string' && isValidDashboardYmd(o.fromYmd) ? o.fromYmd : null;
    const ty = typeof o.toYmd === 'string' && isValidDashboardYmd(o.toYmd) ? o.toYmd : null;
    if (fy) {
      const toD = ty ?? fy;
      return {
        fromDate: fy,
        fromTime: '00:00',
        toDate: toD,
        toTime: '23:59',
        timeZoneId,
      };
    }

    return { ...empty, timeZoneId };
  } catch {
    return empty;
  }
}

export function saveReportingRangeToSession(slice: ReportingRangeSlice): void {
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
