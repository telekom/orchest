import clsx from "clsx";
import React from "react";
import styles from "./SegmentedProgressBar.module.css";

export interface BarSegment {
  key: string;
  label: string;
  count: number;
  color: string;
  onClick?: () => void;
}

export interface SegmentedProgressBarProps {
  segments: BarSegment[];
  total: number;
  size?: "default" | "large";
  showLegend?: boolean;
  showInlineCounts?: boolean;
  emptyLabel?: string;
}

const formatPct = (n: number, total: number) => {
  if (!total) return "0%";
  const v = (n / total) * 100;
  if (v < 1 && v > 0) return "<1%";
  return `${v.toFixed(v < 10 ? 1 : 0)}%`;
};

export const SegmentedProgressBar: React.FC<SegmentedProgressBarProps> = ({
  segments,
  total,
  size = "default",
  showLegend = false,
  showInlineCounts = false,
  emptyLabel = "No instances yet",
}) => {
  const visible = segments.filter((s) => s.count > 0);
  const safeTotal = total > 0 ? total : 0;

  return (
    <div>
      <div
        className={clsx(styles.bar, size === "large" && styles.barLarge)}
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={total || 1}
        aria-valuenow={visible.reduce((acc, s) => acc + s.count, 0)}
      >
        {visible.length === 0 || safeTotal === 0 ? (
          <div className={styles.empty}>{emptyLabel}</div>
        ) : (
          visible.map((seg) => {
            const widthPct = (seg.count / safeTotal) * 100;
            return (
              <button
                key={seg.key}
                type="button"
                onClick={seg.onClick}
                className={styles.segment}
                style={{
                  width: `${widthPct}%`,
                  ["--seg-color" as string]: seg.color,
                }}
                title={`${seg.label}: ${seg.count.toLocaleString()} (${formatPct(
                  seg.count,
                  safeTotal
                )})`}
                aria-label={`${seg.label} ${seg.count}`}
              >
                {showInlineCounts && widthPct >= 10 && (
                  <span className={styles.segmentLabel}>
                    {seg.count.toLocaleString()}
                  </span>
                )}
              </button>
            );
          })
        )}
      </div>

      {showLegend && (
        <div className={styles.legend}>
          {segments.map((seg) => (
            <button
              key={seg.key}
              type="button"
              className={styles.legendItem}
              onClick={seg.onClick}
              style={{ ["--seg-color" as string]: seg.color }}
              disabled={!seg.onClick}
            >
              <span className={styles.legendDot} />
              <span>{seg.label}</span>
              <span className={styles.legendCount}>
                {seg.count.toLocaleString()}
              </span>
              <span className={styles.legendShare}>
                {formatPct(seg.count, safeTotal)}
              </span>
            </button>
          ))}
        </div>
      )}
    </div>
  );
};

export default SegmentedProgressBar;
