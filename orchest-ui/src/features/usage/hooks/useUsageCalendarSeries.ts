import { statsService } from '@/api/domains';
import { queryKeys } from '@/shared/constants/queryKeys';
import { useQueries } from '@tanstack/react-query';
import { useMemo } from 'react';
import type { CalendarBucket } from '../utils/calendarBuckets';
import { mapOrchestStatsToDashboard } from '@/features/process-management/utils/orchestStatsDashboardMapper';

export interface CalendarSeriesPoint {
  bucket: CalendarBucket;
  total: number;
  isLoading: boolean;
  isError: boolean;
}

const CONCURRENCY_HINT = 6;

/**
 * Fetches per-bucket totals for the calendar heatmap.
 * Relies on TanStack Query caching; callers should pass a stable bucket list.
 */
export function useUsageCalendarSeries(
  buckets: CalendarBucket[],
  kind: 'bpmn' | 'dmn' = 'bpmn',
) {
  const queries = useQueries({
    queries: buckets.map((bucket) => ({
      // Cache raw OrchestStatsDTO — same key/shape as Process/Decision/Dashboard.
      queryKey: queryKeys.orchest.stats(bucket.statsQuery),
      queryFn: async () => statsService.getOrchestStats(bucket.statsQuery),
      staleTime: Infinity,
      retry: 1,
      enabled: buckets.length > 0 && buckets.length <= 60,
    })),
  });

  const points: CalendarSeriesPoint[] = useMemo(
    () =>
      buckets.map((bucket, i) => {
        const q = queries[i];
        const mapped = q?.data
          ? mapOrchestStatsToDashboard(q.data)
          : undefined;
        const total =
          kind === 'dmn'
            ? (mapped?.decision.total ?? 0)
            : (mapped?.process.total ?? 0);
        return {
          bucket,
          total,
          isLoading: q?.isLoading ?? false,
          isError: q?.isError ?? false,
        };
      }),
    [buckets, queries, kind],
  );

  const maxTotal = useMemo(
    () => points.reduce((m, p) => Math.max(m, p.total), 0),
    [points],
  );

  const isLoading = points.some((p) => p.isLoading);
  const loadedCount = points.filter((p) => !p.isLoading).length;

  return {
    points,
    maxTotal,
    isLoading,
    loadedCount,
    totalBuckets: buckets.length,
    /** Exposed for diagnostics / future throttling UI */
    concurrencyHint: CONCURRENCY_HINT,
  };
}
