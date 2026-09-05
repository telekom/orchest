import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/design-system/components/ui/tooltip/tooltip';
import React, { useMemo } from 'react';
import {
  buildEntityRows,
  buildTopEntityBars,
  type UsageKind,
} from '../../utils/usageEntityMappers';
import styles from './UsageCharts.module.css';

export interface UsageChartsProps {
  kind: UsageKind;
  entities: { id: string; total: number }[] | undefined;
  isLoading?: boolean;
  onEntityClick?: (id: string) => void;
}

export const UsageCharts: React.FC<UsageChartsProps> = ({
  kind,
  entities,
  isLoading,
  onEntityClick,
}) => {
  const bars = useMemo(
    () => buildTopEntityBars(buildEntityRows(entities), 10),
    [entities],
  );

  const max = useMemo(
    () => bars.reduce((m, b) => Math.max(m, b.total), 0),
    [bars],
  );

  const grandTotal = useMemo(
    () => bars.reduce((sum, b) => sum + b.total, 0),
    [bars],
  );

  const noun = kind === 'bpmn' ? 'process' : 'decision';
  const title = kind === 'bpmn' ? 'Top process signals' : 'Top decision signals';

  if (isLoading) {
    return (
      <div className={styles.panel} aria-busy="true">
        <div className={styles.panelHeader}>
          <h2 className={styles.title}>{title}</h2>
        </div>
        <div className={styles.spectrum} aria-hidden="true">
          {Array.from({ length: 8 }).map((_, i) => (
            <div
              key={i}
              className={styles.skeletonCol}
              style={{ ['--h' as string]: `${30 + ((i * 17) % 55)}%` }}
            />
          ))}
        </div>
      </div>
    );
  }

  if (!bars.some((b) => b.total > 0)) {
    return (
      <div className={styles.panel}>
        <div className={styles.panelHeader}>
          <h2 className={styles.title}>{title}</h2>
        </div>
        <p className={styles.empty}>No signal volume for this period.</p>
      </div>
    );
  }

  const leader = bars[0];

  return (
    <div className={styles.panel}>
      <div className={styles.panelHeader}>
        <div>
          <p className={styles.kicker}>Spectrum</p>
          <h2 className={styles.title}>{title}</h2>
          <p className={styles.subtitle}>
            Tallest column is peak volume among the top {bars.length}.
          </p>
        </div>
        {leader && (
          <div className={styles.leaderChip}>
            <span className={styles.leaderLabel}>#1 share</span>
            <span className={styles.leaderValue}>
              {grandTotal > 0
                ? `${((leader.total / grandTotal) * 100).toFixed(0)}%`
                : '—'}
            </span>
          </div>
        )}
      </div>

      <TooltipProvider delayDuration={120}>
        <div
          className={styles.spectrum}
          role="list"
          aria-label={`${noun} volume spectrum`}
        >
          {bars.map((bar, index) => {
            const heightPct =
              max > 0 ? Math.max(8, (bar.total / max) * 100) : 0;
            const share =
              grandTotal > 0 ? (bar.total / grandTotal) * 100 : 0;
            const isLeader = index === 0;

            return (
              <Tooltip key={bar.id}>
                <TooltipTrigger asChild>
                  <button
                    type="button"
                    role="listitem"
                    className={`${styles.col} ${isLeader ? styles.colLeader : ''}`}
                    style={{
                      ['--h' as string]: `${heightPct}%`,
                      ['--delay' as string]: `${Math.min(index, 9) * 40}ms`,
                    }}
                    onClick={() => onEntityClick?.(bar.id)}
                    aria-label={`${bar.id}: ${bar.total.toLocaleString()} executions`}
                  >
                    <span className={styles.value}>
                      {bar.total.toLocaleString()}
                    </span>
                    <span className={styles.pillar}>
                      <span className={styles.pillarFill} />
                      <span className={styles.pillarSheen} />
                    </span>
                    <span className={styles.colMeta}>
                      <span className={styles.rank}>
                        {String(index + 1).padStart(2, '0')}
                      </span>
                      <span className={styles.colName} title={bar.id}>
                        {bar.label}
                      </span>
                      <span className={styles.colShare}>
                        {share < 0.1 ? '<0.1' : share.toFixed(1)}%
                      </span>
                    </span>
                  </button>
                </TooltipTrigger>
                <TooltipContent>
                  <strong>{bar.id}</strong>
                  <br />
                  {bar.total.toLocaleString()} executions
                  <br />
                  {share.toFixed(2)}% of top {bars.length}
                </TooltipContent>
              </Tooltip>
            );
          })}
        </div>
      </TooltipProvider>
    </div>
  );
};
