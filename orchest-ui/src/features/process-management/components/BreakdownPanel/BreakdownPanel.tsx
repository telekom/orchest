import clsx from "clsx";
import {
  Activity,
  ArrowUpDown,
  ChevronRight,
  ExternalLink,
  Inbox,
} from "lucide-react";
import React, { useMemo } from "react";
import SegmentedProgressBar, {
  BarSegment,
} from "../SegmentedProgressBar/SegmentedProgressBar";
import styles from "../../pages/Dashboard.module.css";

export interface BreakdownStateMeta<S extends string> {
  key: S;
  label: string;
  color: string;
}

export interface VersionBucket<S extends string> {
  version: number;
  total: number;
  counts: Record<S, number>;
}

export interface BreakdownEntry<S extends string> {
  id: string;
  total: number;
  counts: Record<S, number>;
  versions: VersionBucket<S>[];
}

export interface SortDef<S extends string> {
  key: string;
  label: string;
  compare: (a: BreakdownEntry<S>, b: BreakdownEntry<S>) => number;
}

export interface BreakdownPanelProps<S extends string> {
  title: string;
  rows: BreakdownEntry<S>[];
  states: BreakdownStateMeta<S>[];
  primaryStateKey: S;
  sortOptions: SortDef<S>[];
  sortKey: string;
  onSortChange: (key: string) => void;
  expanded: Set<string>;
  onToggleExpanded: (id: string) => void;
  onOpenRow: (entry: BreakdownEntry<S>) => void;
  onOpenVersion: (entry: BreakdownEntry<S>, version: number) => void;
  onSegmentClick: (entry: BreakdownEntry<S>, state: S) => void;
  onVersionSegmentClick: (
    entry: BreakdownEntry<S>,
    version: number,
    state: S
  ) => void;
  isLoading: boolean;
  emptyTitle: string;
  emptyHint: string;
  truncatedNotice?: string | null;
  itemSingular?: string;
  itemPlural?: string;
  versionLabelPrefix?: string;
  /** Optional slot rendered between header and rows for filters. */
  filterSlot?: React.ReactNode;
}

export function BreakdownPanel<S extends string>({
  title,
  rows,
  states,
  primaryStateKey,
  sortOptions,
  sortKey,
  onSortChange,
  expanded,
  onToggleExpanded,
  onOpenRow,
  onOpenVersion,
  onSegmentClick,
  onVersionSegmentClick,
  isLoading,
  emptyTitle,
  emptyHint,
  truncatedNotice,
  itemSingular = "version",
  itemPlural = "versions",
  versionLabelPrefix = "v",
  filterSlot,
}: BreakdownPanelProps<S>) {
  const activeSort = sortOptions.find((s) => s.key === sortKey) ?? sortOptions[0];

  const sortedRows = useMemo(() => {
    return [...rows].sort(activeSort.compare);
  }, [rows, activeSort]);

  return (
    <section className={styles.section}>
      <div className={styles.sectionHeader}>
        <div className={styles.sectionTitle}>
          <h2>{title}</h2>
          <span className={styles.sectionCount}>{sortedRows.length}</span>
        </div>
        <div
          className={styles.sortControl}
          role="group"
          aria-label={`Sort ${title}`}
        >
          {sortOptions.map((opt) => (
            <button
              key={opt.key}
              type="button"
              onClick={() => onSortChange(opt.key)}
              className={clsx(
                styles.sortChip,
                sortKey === opt.key && styles.sortChipActive
              )}
              aria-pressed={sortKey === opt.key}
            >
              {opt.key === sortKey && (
                <ArrowUpDown
                  style={{
                    width: 12,
                    height: 12,
                    marginRight: 4,
                    verticalAlign: "-2px",
                  }}
                />
              )}
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      {filterSlot}

      {truncatedNotice && (
        <div style={{ padding: "0 24px" }}>
          <div className={styles.notice}>{truncatedNotice}</div>
        </div>
      )}

      {isLoading ? (
        <div
          style={{
            padding: "var(--spacing-3) var(--spacing-6) var(--spacing-6)",
          }}
        >
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className={clsx(styles.skeleton, styles.skelRow)} />
          ))}
        </div>
      ) : sortedRows.length === 0 ? (
        <div className={styles.empty}>
          <span className={styles.emptyIcon}>
            <Inbox size={20} />
          </span>
          <strong>{emptyTitle}</strong>
          <span style={{ fontSize: "var(--font-size-body-sm)" }}>
            {emptyHint}
          </span>
        </div>
      ) : (
        <div className={styles.rows}>
          {sortedRows.map((row, idx) => {
            const isOpen = expanded.has(row.id);
            const segments: BarSegment[] = states.map((s) => ({
              key: s.key,
              label: s.label,
              count: row.counts[s.key],
              color: s.color,
              onClick: () => onSegmentClick(row, s.key),
            }));
            const visibleStates = states.filter((s) => row.counts[s.key] > 0);

            return (
              <div
                key={row.id}
                className={clsx(
                  styles.rowGroup,
                  isOpen && styles.rowGroupExpanded
                )}
                style={{ ["--row-delay" as string]: `${idx * 40}ms` }}
              >
                <div
                  className={styles.row}
                  onClick={() => onToggleExpanded(row.id)}
                  role="button"
                  tabIndex={0}
                  aria-expanded={isOpen}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" || e.key === " ") {
                      e.preventDefault();
                      onToggleExpanded(row.id);
                    }
                  }}
                >
                  <button
                    type="button"
                    className={styles.expandToggle}
                    onClick={(e) => {
                      e.stopPropagation();
                      onToggleExpanded(row.id);
                    }}
                    aria-label={isOpen ? "Collapse" : "Expand"}
                    aria-expanded={isOpen}
                    disabled={row.versions.length === 0}
                  >
                    <ChevronRight
                      className={clsx(
                        styles.chevron,
                        isOpen && styles.chevronOpen
                      )}
                    />
                  </button>
                  <div className={styles.rowName}>
                    <span className={styles.rowProcessName} title={row.id}>
                      {row.id}
                    </span>
                    <span className={styles.rowMeta}>
                      <Activity size={11} style={{ verticalAlign: "-1px" }} />{" "}
                      {row.counts[primaryStateKey]} ·{" "}
                      {row.versions.length}{" "}
                      {row.versions.length === 1 ? itemSingular : itemPlural}
                    </span>
                  </div>
                  <div className={styles.rowTotal}>
                    <span className={styles.rowTotalNumber}>
                      {row.total.toLocaleString()}
                    </span>
                    <span className={styles.rowTotalLabel}>inst</span>
                  </div>
                  <div
                    className={styles.rowBarWrap}
                    onClick={(e) => e.stopPropagation()}
                  >
                    <SegmentedProgressBar
                      total={row.total}
                      segments={segments}
                    />
                    <div className={styles.rowChips}>
                      {visibleStates.map((s) => (
                        <span
                          key={s.key}
                          className={styles.chip}
                          style={{ ["--seg-color" as string]: s.color }}
                        >
                          <span className={styles.chipDot} />
                          {s.label} {row.counts[s.key]}
                        </span>
                      ))}
                    </div>
                  </div>
                  <button
                    type="button"
                    className={styles.openButton}
                    onClick={(e) => {
                      e.stopPropagation();
                      onOpenRow(row);
                    }}
                    aria-label={`Open ${row.id}`}
                    title="Open"
                  >
                    <ExternalLink size={14} />
                  </button>
                </div>

                <div
                  className={clsx(
                    styles.subRows,
                    !isOpen && styles.subRowsCollapsed
                  )}
                  aria-hidden={!isOpen}
                >
                  <div className={styles.subRowsInner}>
                    {row.versions.map((v) => {
                      const vSegments: BarSegment[] = states.map((s) => ({
                        key: s.key,
                        label: s.label,
                        count: v.counts[s.key],
                        color: s.color,
                        onClick: () =>
                          onVersionSegmentClick(row, v.version, s.key),
                      }));
                      const vVisibleStates = states.filter((s) => v.counts[s.key] > 0);
                      return (
                        <div
                          key={v.version}
                          className={styles.subRow}
                          onClick={() => onOpenVersion(row, v.version)}
                          role="button"
                          tabIndex={0}
                          onKeyDown={(e) => {
                            if (e.key === "Enter" || e.key === " ") {
                              e.preventDefault();
                              onOpenVersion(row, v.version);
                            }
                          }}
                        >
                          <div className={styles.subRowRail} />
                          <div className={styles.versionTag}>
                            <span className={styles.versionBadge}>
                              {versionLabelPrefix}
                              {v.version}
                            </span>
                            <span className={styles.versionMeta}>
                              {v.counts[primaryStateKey]}
                            </span>
                          </div>
                          <div className={styles.subRowTotal}>
                            <span className={styles.subRowTotalNumber}>
                              {v.total.toLocaleString()}
                            </span>
                            <span className={styles.subRowTotalLabel}>
                              inst
                            </span>
                          </div>
                          <div className={styles.rowBarWrap} onClick={(e) => e.stopPropagation()}>
                            <SegmentedProgressBar
                              total={v.total}
                              segments={vSegments}
                            />
                            <div className={styles.rowChips}>
                              {vVisibleStates.map((s) => (
                                <span
                                  key={s.key}
                                  className={styles.chip}
                                  style={{ ["--seg-color" as string]: s.color }}
                                >
                                  <span className={styles.chipDot} />
                                  {s.label} {v.counts[s.key]}
                                </span>
                              ))}
                            </div>
                          </div>
                          <button
                            type="button"
                            className={styles.openButton}
                            onClick={(e) => {
                              e.stopPropagation();
                              onOpenVersion(row, v.version);
                            }}
                            aria-label={`Open ${row.id} ${versionLabelPrefix}${v.version}`}
                            title="Open"
                          >
                            <ExternalLink size={13} />
                          </button>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
}

export default BreakdownPanel;
