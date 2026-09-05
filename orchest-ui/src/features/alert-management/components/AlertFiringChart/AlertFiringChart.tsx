import type { AlertStats } from '@/api/domains/alerts';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/design-system/components/ui/tooltip/tooltip';
import React, { useMemo } from 'react';
import styles from './AlertFiringChart.module.css';

export interface AlertFiringChartProps {
  stats: AlertStats[] | undefined;
  isLoading?: boolean;
  onProcessClick: (processDefinitionId: string) => void;
}

export const AlertFiringChart: React.FC<AlertFiringChartProps> = ({
  stats,
  isLoading,
  onProcessClick,
}) => {
  const bars = useMemo(
    () =>
      [...(stats ?? [])]
        .sort((a, b) => b.totalCount - a.totalCount)
        .slice(0, 10),
    [stats],
  );

  const max = useMemo(
    () => bars.reduce((highest, bar) => Math.max(highest, bar.totalCount), 0),
    [bars],
  );

  const grandTotal = useMemo(
    () => bars.reduce((sum, bar) => sum + bar.totalCount, 0),
    [bars],
  );

  if (isLoading) {
    return (
      <section className={styles.panel} aria-busy="true">
        <div className={styles.panelHeader}>
          <h2 className={styles.title}>Firing alerts by process</h2>
        </div>
        <div className={styles.spectrum} aria-hidden="true">
          {Array.from({ length: 8 }, (_, index) => (
            <div
              key={index}
              className={styles.skeletonRow}
              style={{ ['--w' as string]: `${35 + ((index * 17) % 55)}%` }}
            />
          ))}
        </div>
      </section>
    );
  }

  if (max === 0) {
    return (
      <section className={styles.panel}>
        <div className={styles.panelHeader}>
          <h2 className={styles.title}>Firing alerts by process</h2>
        </div>
        <p className={styles.empty}>No firing alerts.</p>
      </section>
    );
  }

  const leader = bars[0];

  return (
    <section className={styles.panel}>
      <div className={styles.panelHeader}>
        <div>
          <p className={styles.kicker}>Spectrum</p>
          <h2 className={styles.title}>Firing alerts by process</h2>
          <p className={styles.subtitle}>
            Longest bar is peak firing volume among the top {bars.length}.
          </p>
        </div>
        {leader && (
          <div className={styles.leaderChip}>
            <span className={styles.leaderLabel}>#1 share</span>
            <span className={styles.leaderValue}>
              {grandTotal > 0
                ? `${((leader.totalCount / grandTotal) * 100).toFixed(0)}%`
                : '—'}
            </span>
          </div>
        )}
      </div>

      <TooltipProvider delayDuration={120}>
        <div
          className={styles.spectrum}
          role="list"
          aria-label="Firing alert counts by process"
        >
          {bars.map((bar, index) => {
            const label = `${bar.processDefinitionId} v${bar.version}`;
            const widthPct =
              max > 0 ? Math.max(8, (bar.totalCount / max) * 100) : 0;
            const share =
              grandTotal > 0 ? (bar.totalCount / grandTotal) * 100 : 0;
            const isLeader = index === 0;

            return (
              <Tooltip key={`${bar.processDefinitionId}-${bar.version}`}>
                <TooltipTrigger asChild>
                  <button
                    type="button"
                    role="listitem"
                    className={`${styles.row} ${isLeader ? styles.rowLeader : ''}`}
                    style={{
                      ['--w' as string]: `${widthPct}%`,
                      ['--delay' as string]: `${Math.min(index, 9) * 40}ms`,
                    }}
                    aria-label={`${label}: ${bar.totalCount.toLocaleString()} firing alerts`}
                    onClick={() => onProcessClick(bar.processDefinitionId)}
                  >
                    <span className={styles.rowMeta}>
                      <span className={styles.rank}>
                        {String(index + 1).padStart(2, '0')}
                      </span>
                      <span className={styles.rowName} title={label}>
                        {label}
                      </span>
                    </span>
                    <span className={styles.track}>
                      <span className={styles.bar}>
                        <span className={styles.barFill} />
                        <span className={styles.barSheen} />
                      </span>
                      <span className={styles.rowStats}>
                        <span className={styles.value}>
                          {bar.totalCount.toLocaleString()}
                        </span>
                        <span className={styles.rowShare}>
                          {share < 0.1 ? '<0.1' : share.toFixed(1)}%
                        </span>
                      </span>
                    </span>
                  </button>
                </TooltipTrigger>
                <TooltipContent>
                  <strong>{label}</strong>
                  <br />
                  {bar.totalCount.toLocaleString()} firing occurrences
                  <br />
                  {share.toFixed(2)}% of top {bars.length}
                </TooltipContent>
              </Tooltip>
            );
          })}
        </div>
      </TooltipProvider>
    </section>
  );
};
