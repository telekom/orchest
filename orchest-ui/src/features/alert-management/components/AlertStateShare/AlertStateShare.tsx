import type { OpsAlertState } from '@/features/alert-management/hooks/useAlertsData';
import React, { useMemo } from 'react';
import styles from './AlertStateShare.module.css';

export interface AlertStateShareProps {
  counts: Record<OpsAlertState, number>;
  selectedState: OpsAlertState;
  isLoading?: boolean;
  onStateClick: (state: OpsAlertState) => void;
}

const STATE_RING_META: { key: OpsAlertState; label: string; color: string; cssClass: string }[] = [
  { key: 'FIRING', label: 'Firing', color: 'var(--color-red-500)', cssClass: 'firing' },
  { key: 'SILENCED', label: 'Silenced', color: 'var(--color-gray-500)', cssClass: 'silenced' },
  { key: 'DISABLED', label: 'Disabled', color: 'var(--color-orange-500)', cssClass: 'disabled' },
  { key: 'ACKNOWLEDGED', label: 'Acknowledged', color: 'var(--color-blue-500)', cssClass: 'acknowledged' },
  { key: 'RESOLVED', label: 'Resolved', color: 'var(--color-green-500)', cssClass: 'resolved' },
];

function pct(part: number, total: number) {
  if (total <= 0) return 0;
  return (part / total) * 100;
}

function fmtPct(value: number) {
  if (value === 0) return '0';
  if (value < 0.1) return '<0.1';
  return value.toFixed(0);
}

export const AlertStateShare: React.FC<AlertStateShareProps> = ({
  counts,
  selectedState,
  isLoading,
  onStateClick,
}) => {
  const total = STATE_RING_META.reduce((sum, s) => sum + counts[s.key], 0);

  const ringStyle = useMemo(() => {
    if (total === 0) return {} as React.CSSProperties;
    let cursor = 0;
    const stops: string[] = [];
    for (const s of STATE_RING_META) {
      const share = (counts[s.key] / total) * 360;
      if (share > 0) {
        stops.push(`${s.color} ${cursor}deg ${cursor + share}deg`);
      }
      cursor += share;
    }
    return {
      background: `conic-gradient(from -90deg, ${stops.join(', ')})`,
    } as React.CSSProperties;
  }, [counts, total]);

  if (isLoading) {
    return (
      <section className={styles.panel} aria-busy="true">
        <div className={styles.header}>
          <p className={styles.kicker}>Balance</p>
          <h2 className={styles.title}>Alert state share</h2>
        </div>
        <div className={styles.skeletonRing} aria-hidden="true" />
      </section>
    );
  }

  return (
    <section className={styles.panel}>
      <div className={styles.header}>
        <div>
          <p className={styles.kicker}>Balance</p>
          <h2 className={styles.title}>Alert state share</h2>
          <p className={styles.subtitle}>
            Distribution of alerts across all operational states.
          </p>
        </div>
      </div>

      <div className={styles.body}>
        <div className={styles.ringWrap} style={ringStyle} aria-hidden={total === 0}>
          <div className={styles.ringCore}>
            <span className={styles.ringTotal}>
              {total === 0 ? '—' : total.toLocaleString()}
            </span>
            <span className={styles.ringLabel}>in scope</span>
          </div>
        </div>

        <div className={styles.legend} role="list">
          {STATE_RING_META.map((s) => {
            const share = pct(counts[s.key], total);
            return (
              <button
                key={s.key}
                type="button"
                role="listitem"
                className={`${styles.legendItem} ${styles[s.cssClass]} ${
                  selectedState === s.key ? styles.selected : ''
                }`}
                aria-pressed={selectedState === s.key}
                onClick={() => onStateClick(s.key)}
              >
                <span className={styles.swatch} />
                <span className={styles.legendCopy}>
                  <span className={styles.legendName}>{s.label}</span>
                  <span className={styles.legendCount}>
                    {counts[s.key].toLocaleString()} · {fmtPct(share)}%
                  </span>
                </span>
              </button>
            );
          })}
        </div>
      </div>
    </section>
  );
};
