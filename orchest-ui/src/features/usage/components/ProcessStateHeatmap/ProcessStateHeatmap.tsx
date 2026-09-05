import { Button } from '@/design-system/components/ui/button';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/design-system/components/ui/tooltip/tooltip';
import React, { useMemo, useState } from 'react';
import {
  buildEntityRows,
  maxEntityTotal,
  type UsageEntityRow,
  type UsageKind,
} from '../../utils/usageEntityMappers';
import styles from './ProcessStateHeatmap.module.css';

const DEFAULT_VISIBLE = 25;

export interface ProcessStateHeatmapProps {
  kind: UsageKind;
  entities: { id: string; total: number }[] | undefined;
  isLoading?: boolean;
  onRowClick?: (id: string) => void;
}

function shareOf(total: number, grandTotal: number): number {
  if (grandTotal <= 0) return 0;
  return (total / grandTotal) * 100;
}

export const ProcessStateHeatmap: React.FC<ProcessStateHeatmapProps> = ({
  kind,
  entities,
  isLoading,
  onRowClick,
}) => {
  const [showAll, setShowAll] = useState(false);
  const noun = kind === 'bpmn' ? 'process' : 'decision';
  const title = kind === 'bpmn' ? 'Traffic by process' : 'Traffic by decision';

  const allRows = useMemo(() => buildEntityRows(entities), [entities]);
  const rows: UsageEntityRow[] = showAll
    ? allRows
    : allRows.slice(0, DEFAULT_VISIBLE);
  const max = maxEntityTotal(allRows);
  const grandTotal = useMemo(
    () => allRows.reduce((sum, r) => sum + r.total, 0),
    [allRows],
  );
  const hiddenCount = Math.max(0, allRows.length - DEFAULT_VISIBLE);

  if (isLoading) {
    return (
      <div className={styles.panel} aria-busy="true">
        <div className={styles.panelHeader}>
          <h2 className={styles.title}>{title}</h2>
        </div>
        <div className={styles.skeletonTrack}>
          {Array.from({ length: 6 }).map((_, i) => (
            <div
              key={i}
              className={styles.skeletonRow}
              style={{ ['--w' as string]: `${88 - i * 10}%` }}
            />
          ))}
        </div>
      </div>
    );
  }

  if (!allRows.length) {
    return (
      <div className={styles.panel}>
        <div className={styles.panelHeader}>
          <h2 className={styles.title}>{title}</h2>
        </div>
        <p className={styles.empty}>
          No {noun} traffic in this period. Apply a wider date range to see
          volume.
        </p>
      </div>
    );
  }

  return (
    <div className={styles.panel}>
      <div className={styles.panelHeader}>
        <div>
          <p className={styles.kicker}>Live volume</p>
          <h2 className={styles.title}>{title}</h2>
          <p className={styles.subtitle}>
            Ranked by executions — bar length is share of peak volume.
          </p>
        </div>
        <div className={styles.peakBadge} aria-label={`Peak ${max.toLocaleString()}`}>
          <span className={styles.peakLabel}>Peak</span>
          <span className={styles.peakValue}>{max.toLocaleString()}</span>
        </div>
      </div>

      <TooltipProvider delayDuration={180}>
        <ol className={styles.list} aria-label={`${noun} traffic ranking`}>
          {rows.map((row, index) => {
            const widthPct = max > 0 ? Math.max(4, (row.total / max) * 100) : 0;
            const share = shareOf(row.total, grandTotal);
            const rank = index + 1;
            const isLeader = rank === 1;

            return (
              <li
                key={row.id}
                className={styles.item}
                style={{
                  ['--delay' as string]: `${Math.min(index, 12) * 35}ms`,
                  ['--bar-w' as string]: `${widthPct}%`,
                }}
              >
                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      className={`${styles.row} ${isLeader ? styles.leader : ''}`}
                      onClick={() => onRowClick?.(row.id)}
                      aria-label={`${row.id}: ${row.total.toLocaleString()} executions, ${share.toFixed(1)} percent of total`}
                    >
                      <span className={styles.rank} aria-hidden="true">
                        {String(rank).padStart(2, '0')}
                      </span>
                      <span className={styles.track}>
                        <span className={styles.bar}>
                          <span className={styles.barGlow} />
                        </span>
                        <span className={styles.meta}>
                          <span className={styles.name} title={row.id}>
                            {row.id}
                          </span>
                          <span className={styles.metrics}>
                            <span className={styles.count}>
                              {row.total.toLocaleString()}
                            </span>
                            <span className={styles.share}>
                              {share < 0.1 ? '<0.1' : share.toFixed(1)}%
                            </span>
                          </span>
                        </span>
                      </span>
                    </button>
                  </TooltipTrigger>
                  <TooltipContent side="top" className={styles.tip}>
                    <strong>{row.id}</strong>
                    <br />
                    {row.total.toLocaleString()} executions
                    <br />
                    {share.toFixed(2)}% of all {noun} traffic
                  </TooltipContent>
                </Tooltip>
              </li>
            );
          })}
        </ol>
      </TooltipProvider>

      {hiddenCount > 0 && (
        <div className={styles.footer}>
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={() => setShowAll((v) => !v)}
          >
            {showAll
              ? 'Show top 25'
              : `Show all ${allRows.length} ${noun}s`}
          </Button>
        </div>
      )}
    </div>
  );
};
