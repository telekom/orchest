import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/design-system/components/ui/tooltip/tooltip';
import { LayoutGrid, TrendingUp } from 'lucide-react';
import clsx from 'clsx';
import React, { useMemo, useState } from 'react';
import type { CalendarSeriesPoint } from '../../hooks/useUsageCalendarSeries';
import { heatColorForCount } from '../../utils/usageMappers';
import styles from './UsageCalendarHeatmap.module.css';

export type CalendarViewMode = 'cards' | 'graph';

export interface UsageCalendarHeatmapProps {
  points: CalendarSeriesPoint[];
  maxTotal: number;
  isLoading: boolean;
  enabled: boolean;
  granularityLabel?: string;
  loadedCount?: number;
  totalBuckets?: number;
  onBucketClick?: (fromDate: string, toDate: string) => void;
}

function buildAreaPath(
  values: number[],
  max: number,
  width: number,
  height: number,
  padY: number,
): { line: string; area: string } {
  if (values.length === 0) return { line: '', area: '' };
  const usableH = height - padY * 2;
  const step = values.length === 1 ? 0 : width / (values.length - 1);

  const coords = values.map((v, i) => {
    const x = values.length === 1 ? width / 2 : i * step;
    const ratio = max > 0 ? v / max : 0;
    const y = height - padY - ratio * usableH;
    return { x, y };
  });

  const line = coords
    .map((c, i) => `${i === 0 ? 'M' : 'L'} ${c.x.toFixed(1)} ${c.y.toFixed(1)}`)
    .join(' ');

  const area = [
    `M 0 ${height}`,
    `L ${coords[0].x.toFixed(1)} ${coords[0].y.toFixed(1)}`,
    ...coords.slice(1).map((c) => `L ${c.x.toFixed(1)} ${c.y.toFixed(1)}`),
    `L ${width} ${height}`,
    'Z',
  ].join(' ');

  return { line, area };
}

export const UsageCalendarHeatmap: React.FC<UsageCalendarHeatmapProps> = ({
  points,
  maxTotal,
  isLoading,
  enabled,
  granularityLabel,
  loadedCount,
  totalBuckets,
  onBucketClick,
}) => {
  const [view, setView] = useState<CalendarViewMode>('cards');

  const chart = useMemo(() => {
    const width = 640;
    const height = 200;
    const padY = 16;
    const values = points.map((p) => (p.isLoading ? 0 : p.total));
    const { line, area } = buildAreaPath(values, maxTotal, width, height, padY);
    const step = points.length <= 1 ? 0 : width / (points.length - 1);

    const dots = points.map((point, i) => {
      const ratio = maxTotal > 0 && !point.isLoading ? point.total / maxTotal : 0;
      const x = points.length === 1 ? width / 2 : i * step;
      const y = height - padY - ratio * (height - padY * 2);
      return { point, x, y };
    });

    return { width, height, line, area, dots };
  }, [points, maxTotal]);

  if (!enabled) {
    return (
      <div className={styles.panel}>
        <div className={styles.panelHeader}>
          <div>
            <p className={styles.kicker}>Timeline</p>
            <h2 className={styles.title}>Execution calendar</h2>
          </div>
        </div>
        <p className={styles.empty}>
          Apply a date range to see daily or weekly execution intensity across
          the selected period.
        </p>
      </div>
    );
  }

  const granularity = points[0]?.bucket.granularity ?? 'day';

  return (
    <div className={styles.panel}>
      <div className={styles.panelHeader}>
        <div>
          <p className={styles.kicker}>Timeline</p>
          <h2 className={styles.title}>Execution calendar</h2>
          <p className={styles.subtitle}>
            {granularityLabel ??
              (granularity === 'day'
                ? 'Daily execution totals for the selected range'
                : 'Weekly execution totals for the selected range')}
            {typeof loadedCount === 'number' &&
              typeof totalBuckets === 'number' &&
              isLoading && (
                <span className={styles.progress}>
                  {' '}
                  · Loading {loadedCount}/{totalBuckets}
                </span>
              )}
          </p>
        </div>

        <div className={styles.headerRight}>
          <div
            className={styles.segment}
            role="tablist"
            aria-label="Calendar view"
          >
            <button
              type="button"
              role="tab"
              aria-selected={view === 'cards'}
              className={clsx(
                styles.segmentBtn,
                view === 'cards' && styles.segmentBtnActive,
              )}
              onClick={() => setView('cards')}
            >
              <LayoutGrid size={14} aria-hidden />
              Cards
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={view === 'graph'}
              className={clsx(
                styles.segmentBtn,
                view === 'graph' && styles.segmentBtnActive,
              )}
              onClick={() => setView('graph')}
            >
              <TrendingUp size={14} aria-hidden />
              Graph
            </button>
          </div>
          {view === 'cards' && (
            <div className={styles.legend} aria-hidden="true">
              <span className={styles.legendLabel}>Low</span>
              <span className={styles.legendBar} />
              <span className={styles.legendLabel}>High</span>
            </div>
          )}
        </div>
      </div>

      {view === 'cards' ? (
        <TooltipProvider delayDuration={150}>
          <div
            className={clsx(
              styles.grid,
              granularity === 'week' ? styles.weekGrid : styles.dayGrid,
            )}
            role="list"
            aria-label="Execution calendar cards"
          >
            {points.map((point) => {
              const heat = heatColorForCount(point.total, maxTotal);
              const intensity =
                maxTotal > 0 && !point.isLoading
                  ? Math.min(1, point.total / maxTotal)
                  : 0;

              return (
                <Tooltip key={point.bucket.id}>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      role="listitem"
                      className={clsx(
                        styles.cell,
                        point.isLoading && styles.cellLoading,
                      )}
                      style={{
                        ['--heat' as string]: point.isLoading
                          ? 'var(--color-gray-300)'
                          : heat,
                        ['--tint' as string]: point.isLoading
                          ? '0'
                          : String(0.06 + intensity * 0.1),
                      }}
                      aria-label={`${point.bucket.label}: ${point.total} executions`}
                      disabled={point.isLoading}
                      onClick={() =>
                        onBucketClick?.(
                          point.bucket.fromDate,
                          point.bucket.toDate,
                        )
                      }
                    >
                      <span className={styles.heatRail} aria-hidden="true" />
                      <span className={styles.cellLabel}>
                        {point.bucket.label}
                      </span>
                      <span className={styles.cellValue}>
                        {point.isLoading
                          ? '…'
                          : point.total.toLocaleString()}
                      </span>
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>
                    {point.bucket.label}
                    <br />
                    {point.isLoading
                      ? 'Loading…'
                      : `${point.total.toLocaleString()} executions`}
                  </TooltipContent>
                </Tooltip>
              );
            })}
          </div>
        </TooltipProvider>
      ) : (
        <TooltipProvider delayDuration={100}>
          <div className={styles.graphWrap}>
            <svg
              className={styles.graphSvg}
              viewBox={`0 0 ${chart.width} ${chart.height}`}
              role="img"
              aria-label="Execution time graph"
              preserveAspectRatio="none"
            >
              <defs>
                <linearGradient id="usageAreaFill" x1="0" y1="0" x2="0" y2="1">
                  <stop
                    offset="0%"
                    stopColor="var(--color-magenta)"
                    stopOpacity="0.35"
                  />
                  <stop
                    offset="100%"
                    stopColor="var(--color-magenta)"
                    stopOpacity="0.02"
                  />
                </linearGradient>
              </defs>
              <path d={chart.area} fill="url(#usageAreaFill)" />
              <path
                d={chart.line}
                fill="none"
                stroke="var(--color-magenta)"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                vectorEffect="non-scaling-stroke"
              />
            </svg>

            <div className={styles.graphDots} aria-hidden={false}>
              {chart.dots.map(({ point, x, y }) => (
                <Tooltip key={point.bucket.id}>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      className={clsx(
                        styles.dotBtn,
                        point.isLoading && styles.cellLoading,
                      )}
                      style={{
                        left: `${(x / chart.width) * 100}%`,
                        top: `${(y / chart.height) * 100}%`,
                      }}
                      disabled={point.isLoading}
                      aria-label={`${point.bucket.label}: ${point.total} executions`}
                      onClick={() =>
                        onBucketClick?.(
                          point.bucket.fromDate,
                          point.bucket.toDate,
                        )
                      }
                    >
                      <span className={styles.dot} />
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>
                    {point.bucket.label}
                    <br />
                    {point.isLoading
                      ? 'Loading…'
                      : `${point.total.toLocaleString()} executions`}
                  </TooltipContent>
                </Tooltip>
              ))}
            </div>

            <div className={styles.graphAxis}>
              <span>{points[0]?.bucket.label}</span>
              <span>{points[points.length - 1]?.bucket.label}</span>
            </div>
          </div>
        </TooltipProvider>
      )}
    </div>
  );
};
