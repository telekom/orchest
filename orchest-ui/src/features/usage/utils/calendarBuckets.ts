import type { OrchestStatsQuerySlice } from '@/api/domains';
import {
  buildOrchestStatsApiQuery,
  type ReportingRangeSlice,
} from '@/features/process-management/utils/dashboardReportingRange';

export type CalendarBucketGranularity = 'day' | 'week';

export interface CalendarBucket {
  id: string;
  /** Inclusive start date `yyyy-MM-dd` in reporting wall calendar */
  fromDate: string;
  /** Inclusive end date `yyyy-MM-dd` */
  toDate: string;
  label: string;
  granularity: CalendarBucketGranularity;
  statsQuery: OrchestStatsQuerySlice;
}

const DAY_MS = 24 * 60 * 60 * 1000;
const DAY_MODE_MAX_DAYS = 42;
const WEEK_MODE_MAX_BUCKETS = 26;

function parseYmdUtc(ymd: string): Date {
  const [y, m, d] = ymd.split('-').map(Number);
  return new Date(Date.UTC(y, m - 1, d));
}

function formatYmdUtc(d: Date): string {
  return d.toISOString().slice(0, 10);
}

function addDaysUtc(ymd: string, days: number): string {
  const d = parseYmdUtc(ymd);
  d.setUTCDate(d.getUTCDate() + days);
  return formatYmdUtc(d);
}

function daysBetweenInclusive(fromYmd: string, toYmd: string): number {
  const a = parseYmdUtc(fromYmd).getTime();
  const b = parseYmdUtc(toYmd).getTime();
  return Math.floor((b - a) / DAY_MS) + 1;
}

function formatDayLabel(ymd: string): string {
  const d = parseYmdUtc(ymd);
  return d.toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    timeZone: 'UTC',
  });
}

function formatWeekLabel(fromYmd: string, toYmd: string): string {
  return `${formatDayLabel(fromYmd)} – ${formatDayLabel(toYmd)}`;
}

/**
 * Split an applied reporting range into day or week buckets for calendar heatmaps.
 * Returns [] when the range is unbounded / incomplete.
 */
export function buildCalendarBuckets(
  slice: ReportingRangeSlice,
): CalendarBucket[] {
  const { fromDate, toDate, timeZoneId } = slice;
  if (!fromDate || !toDate) return [];

  let start = fromDate;
  let end = toDate;
  if (start > end) {
    const tmp = start;
    start = end;
    end = tmp;
  }

  const span = daysBetweenInclusive(start, end);
  if (span <= 0) return [];

  const granularity: CalendarBucketGranularity =
    span <= DAY_MODE_MAX_DAYS ? 'day' : 'week';
  const buckets: CalendarBucket[] = [];

  if (granularity === 'day') {
    let cursor = start;
    while (cursor <= end) {
      const query = buildOrchestStatsApiQuery({
        fromDate: cursor,
        fromTime: '00:00',
        toDate: cursor,
        toTime: '23:59',
        timeZoneId,
      });
      if (query) {
        buckets.push({
          id: `day-${cursor}`,
          fromDate: cursor,
          toDate: cursor,
          label: formatDayLabel(cursor),
          granularity: 'day',
          statsQuery: query,
        });
      }
      cursor = addDaysUtc(cursor, 1);
    }
  } else {
    let cursor = start;
    let safety = 0;
    while (cursor <= end && safety < WEEK_MODE_MAX_BUCKETS) {
      const weekEndCandidate = addDaysUtc(cursor, 6);
      const weekEnd = weekEndCandidate < end ? weekEndCandidate : end;
      const query = buildOrchestStatsApiQuery({
        fromDate: cursor,
        fromTime: '00:00',
        toDate: weekEnd,
        toTime: '23:59',
        timeZoneId,
      });
      if (query) {
        buckets.push({
          id: `week-${cursor}-${weekEnd}`,
          fromDate: cursor,
          toDate: weekEnd,
          label: formatWeekLabel(cursor, weekEnd),
          granularity: 'week',
          statsQuery: query,
        });
      }
      cursor = addDaysUtc(weekEnd, 1);
      safety += 1;
    }
  }

  return buckets;
}
