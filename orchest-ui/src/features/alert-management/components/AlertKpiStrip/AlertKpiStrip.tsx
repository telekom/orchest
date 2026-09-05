import type { OpsAlertState } from '@/features/alert-management/hooks/useAlertsData';
import SegmentedProgressBar from '@/features/process-management/components/SegmentedProgressBar/SegmentedProgressBar';
import clsx from 'clsx';
import { BellOff, CheckCircle2, Radio, ThumbsUp, VolumeX } from 'lucide-react';
import React, { useMemo } from 'react';
import styles from './AlertKpiStrip.module.css';

export interface AlertKpiStripProps {
  counts: Record<OpsAlertState, number>;
  selectedState: OpsAlertState;
  isLoading?: boolean;
  onStateClick: (state: OpsAlertState) => void;
}

const STATE_META: Record<
  OpsAlertState,
  { label: string; hint: string; color: string; Icon: typeof Radio }
> = {
  FIRING: {
    label: 'Firing',
    hint: 'Active signal',
    color: 'var(--color-red-500)',
    Icon: Radio,
  },
  SILENCED: {
    label: 'Silenced',
    hint: 'Held quiet',
    color: 'var(--color-gray-500)',
    Icon: VolumeX,
  },
  DISABLED: {
    label: 'Disabled',
    hint: 'Turned off',
    color: 'var(--color-orange-500)',
    Icon: BellOff,
  },
  ACKNOWLEDGED: {
    label: 'Acknowledged',
    hint: 'Seen by operator',
    color: 'var(--color-blue-500)',
    Icon: ThumbsUp,
  },
  RESOLVED: {
    label: 'Resolved',
    hint: 'Issue closed',
    color: 'var(--color-green-500)',
    Icon: CheckCircle2,
  },
};

const ALL_STATES: OpsAlertState[] = ['FIRING', 'SILENCED', 'DISABLED', 'ACKNOWLEDGED', 'RESOLVED'];

export const AlertKpiStrip: React.FC<AlertKpiStripProps> = ({
  counts,
  selectedState,
  isLoading,
  onStateClick,
}) => {
  const total = ALL_STATES.reduce((sum, key) => sum + counts[key], 0);
  const firingShare = total > 0 ? (counts.FIRING / total) * 100 : 0;

  const segments = useMemo(
    () =>
      ALL_STATES.map((key) => ({
        key,
        label: STATE_META[key].label,
        count: isLoading ? 0 : counts[key],
        color: STATE_META[key].color,
        onClick: () => onStateClick(key),
      })),
    [counts, isLoading, onStateClick],
  );

  return (
    <section className={styles.hero} aria-label="Alert operations summary">
      <div className={styles.heroTop}>
        <div className={styles.totalBlock}>
          <p className={styles.totalKicker}>Live ops load</p>
          <p className={styles.totalValue}>
            {isLoading ? '—' : total.toLocaleString()}
          </p>
          <p className={styles.totalHint}>
            All alert states in scope
            {!isLoading && total > 0 && (
              <>
                {' · '}
                <span className={styles.totalAccent}>
                  {firingShare < 0.1 && firingShare > 0
                    ? '<0.1'
                    : firingShare.toFixed(firingShare < 10 ? 1 : 0)}
                  % firing
                </span>
              </>
            )}
          </p>
        </div>

        <div className={styles.tiles} role="list">
          {ALL_STATES.map((state) => {
            const meta = STATE_META[state];
            const isActive = selectedState === state;
            const Icon = meta.Icon;

            return (
              <button
                key={state}
                type="button"
                role="listitem"
                className={clsx(styles.tile, styles[state.toLowerCase()], {
                  [styles.active]: isActive,
                })}
                disabled={isLoading}
                aria-label={`${state}: ${counts[state].toLocaleString()}`}
                aria-pressed={isActive}
                onClick={() => onStateClick(state)}
              >
                <span className={styles.tileHead}>
                  <Icon className={styles.tileIcon} aria-hidden="true" size={16} />
                  <span className={styles.tileLabel}>{meta.label}</span>
                </span>
                <span className={styles.tileValue}>
                  {isLoading ? '—' : counts[state].toLocaleString()}
                </span>
                <span className={styles.tileHint}>{meta.hint}</span>
              </button>
            );
          })}
        </div>
      </div>

      <div className={styles.ratio}>
        <div className={styles.ratioHeader}>
          <span>Signal mix</span>
          <span className={styles.ratioMeta}>Click a segment to focus the list</span>
        </div>
        <SegmentedProgressBar
          segments={segments}
          total={isLoading ? 0 : total}
          size="large"
          showLegend
          showInlineCounts
          emptyLabel={isLoading ? 'Loading signal mix…' : 'No alerts in scope'}
        />
      </div>
    </section>
  );
};
