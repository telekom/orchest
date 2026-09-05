import { DateRangePicker } from "@/design-system/components/ui/date-range-picker";
import { Combobox } from "@/design-system/components/ui/combobox";
import { AlertsWorkspace } from "@/features/alert-management/components/AlertsWorkspace/AlertsWorkspace";
import AppLayout from "@/shared/layouts/AppLayout";
import { StaggerList } from "@/shared/components";
import { ProcessStatus } from "@/shared/constants/status";
import { buildDecisionListPath, buildProcessListPath } from "@/shared/url-state";
import type { DecisionStateKey } from "@/features/decision-management/hooks/useDecisionDashboardData";
import clsx from "clsx";
import { RefreshCw } from "lucide-react";
import React, { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import BreakdownPanel, {
  BreakdownEntry,
  BreakdownStateMeta,
  SortDef,
} from "../components/BreakdownPanel/BreakdownPanel";
import {
  buildOrchestStatsApiQuery,
  type ReportingRangeSlice,
} from "../utils/dashboardReportingRange";
import SegmentedProgressBar, {
  BarSegment,
} from "../components/SegmentedProgressBar/SegmentedProgressBar";
import type { DashboardStateKey } from "../hooks/useDashboardData";
import { useOrchestDashboardData } from "../hooks/useOrchestDashboardData";
import styles from "./Dashboard.module.css";

type DashboardView = "overview" | "alerts";

/* ---------------- PROCESS CONFIG ---------------- */

const PROCESS_STATE_META: Record<
  DashboardStateKey,
  { label: string; color: string; status: ProcessStatus }
> = {
  running: {
    label: "Running",
    color: "var(--color-green-500)",
    status: ProcessStatus.RUNNING,
  },
  completed: {
    label: "Completed",
    color: "var(--color-blue-500)",
    status: ProcessStatus.COMPLETED,
  },
  failed: {
    label: "Failed",
    color: "var(--color-red-500)",
    status: ProcessStatus.FAILED,
  },
  incident: {
    label: "Incident",
    color: "var(--color-orange-500)",
    status: ProcessStatus.INCIDENT,
  },
  hold: {
    label: "Hold",
    color: "var(--color-yellow-500)",
    status: ProcessStatus.HOLD,
  },
  cancelled: {
    label: "Cancelled",
    color: "var(--color-gray-400)",
    status: ProcessStatus.CANCELLED,
  },
};

const PROCESS_STATE_ORDER: DashboardStateKey[] = [
  "running",
  "completed",
  "hold",
  "incident",
  "failed",
  "cancelled",
];

const PROCESS_STATES: BreakdownStateMeta<DashboardStateKey>[] =
  PROCESS_STATE_ORDER.map((k) => ({
    key: k,
    label: PROCESS_STATE_META[k].label,
    color: PROCESS_STATE_META[k].color,
  }));

const PROCESS_SORTS: SortDef<DashboardStateKey>[] = [
  {
    key: "total",
    label: "Volume",
    compare: (a, b) => b.total - a.total,
  },
  {
    key: "running",
    label: "Active",
    compare: (a, b) => b.counts.running - a.counts.running,
  },
  {
    key: "failed",
    label: "Failures",
    compare: (a, b) =>
      b.counts.failed + b.counts.incident - (a.counts.failed + a.counts.incident),
  },
  {
    key: "name",
    label: "A–Z",
    compare: (a, b) => a.id.localeCompare(b.id),
  },
];

/* ---------------- DECISION CONFIG ---------------- */

const DECISION_STATE_META: Record<
  DecisionStateKey,
  { label: string; color: string; apiState: string }
> = {
  evaluated: {
    label: "Evaluated",
    color: "var(--color-blue-500)",
    apiState: "EVALUATED",
  },
  failed: {
    label: "Failed",
    color: "var(--color-red-500)",
    apiState: "FAILED",
  },
  unknown: {
    label: "Unknown",
    color: "var(--color-gray-400)",
    apiState: "UNKNOWN",
  },
};

const DECISION_STATE_ORDER: DecisionStateKey[] = [
  "evaluated",
  "failed",
  "unknown",
];

const DECISION_STATES: BreakdownStateMeta<DecisionStateKey>[] =
  DECISION_STATE_ORDER.map((k) => ({
    key: k,
    label: DECISION_STATE_META[k].label,
    color: DECISION_STATE_META[k].color,
  }));

const DECISION_SORTS: SortDef<DecisionStateKey>[] = [
  {
    key: "total",
    label: "Volume",
    compare: (a, b) => b.total - a.total,
  },
  {
    key: "evaluated",
    label: "Evaluated",
    compare: (a, b) => b.counts.evaluated - a.counts.evaluated,
  },
  {
    key: "failed",
    label: "Failures",
    compare: (a, b) => b.counts.failed - a.counts.failed,
  },
  {
    key: "name",
    label: "A–Z",
    compare: (a, b) => a.id.localeCompare(b.id),
  },
];

/* ---------------- HELPERS ---------------- */

const REFRESH_INTERVAL_MS = 5000;

const LiveIndicator: React.FC<{ paused?: boolean }> = ({ paused = false }) => {
  return (
    <span className={styles.livePill} aria-live="polite">
      <span className={styles.liveDot} />
      <span className={styles.liveLabel}>{paused ? 'PAUSED' : 'LIVE'}</span>
    </span>
  );
};

/* ---------------- PAGE ---------------- */

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const view: DashboardView =
    searchParams.get("view") === "alerts" ? "alerts" : "overview";
  const [alertsMounted, setAlertsMounted] = useState(view === "alerts");

  useEffect(() => {
    if (view === "alerts") setAlertsMounted(true);
  }, [view]);

  const setView = (next: DashboardView) => {
    setSearchParams(
      (prev) => {
        const params = new URLSearchParams(prev);
        if (next === "alerts") params.set("view", "alerts");
        else params.delete("view");
        return params;
      },
      { replace: true },
    );
  };

  const [processSortKey, setProcessSortKey] = useState<string>("total");
  const [decisionSortKey, setDecisionSortKey] = useState<string>("total");
  const [manuallyRefreshing, setManuallyRefreshing] = useState(false);
  const [processExpanded, setProcessExpanded] = useState<Set<string>>(new Set());
  const [decisionExpanded, setDecisionExpanded] = useState<Set<string>>(new Set());

  const toggleSet = (
    setter: React.Dispatch<React.SetStateAction<Set<string>>>,
    id: string
  ) => {
    setter((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const [reportingRange, setReportingRange] = useState<ReportingRangeSlice>({
    fromDate: null,
    fromTime: null,
    toDate: null,
    toTime: null,
    timeZoneId: "UTC",
  });

  const [draftFrom, setDraftFrom] = useState<Date | null>(null);
  const [draftTo, setDraftTo] = useState<Date | null>(null);
  const [draftTz, setDraftTz] = useState<string>("UTC");

  const handleApplyDateFilter = (
    appliedFrom?: Date | null,
    appliedTo?: Date | null,
  ) => {
    const nextFrom = appliedFrom !== undefined ? appliedFrom : draftFrom;
    const nextTo = appliedTo !== undefined ? appliedTo : draftTo;
    if (appliedFrom !== undefined) setDraftFrom(appliedFrom);
    if (appliedTo !== undefined) setDraftTo(appliedTo);

    const fromDate = nextFrom
      ? `${nextFrom.getFullYear()}-${String(nextFrom.getMonth() + 1).padStart(2, '0')}-${String(nextFrom.getDate()).padStart(2, '0')}`
      : null;
    const fromTime = nextFrom
      ? `${String(nextFrom.getHours()).padStart(2, '0')}:${String(nextFrom.getMinutes()).padStart(2, '0')}`
      : null;
    const toDate = nextTo
      ? `${nextTo.getFullYear()}-${String(nextTo.getMonth() + 1).padStart(2, '0')}-${String(nextTo.getDate()).padStart(2, '0')}`
      : null;
    const toTime = nextTo
      ? `${String(nextTo.getHours()).padStart(2, '0')}:${String(nextTo.getMinutes()).padStart(2, '0')}`
      : null;
    setReportingRange({ fromDate, fromTime, toDate, toTime, timeZoneId: draftTz });
  };

  const handleClearDateFilter = () => {
    setDraftFrom(null);
    setDraftTo(null);
    setReportingRange({ fromDate: null, fromTime: null, toDate: null, toTime: null, timeZoneId: "UTC" });
  };

  const statsQuerySlice = useMemo(
    () => buildOrchestStatsApiQuery(reportingRange),
    [reportingRange]
  );

  const isRangeFiltered = statsQuerySlice != null;

  const {
    data: orchestDash,
    isLoading: dashLoading,
    refetch: refetchOrchestDash,
  } = useOrchestDashboardData({
    statsQuery: statsQuerySlice,
    refetchInterval: isRangeFiltered ? false : REFRESH_INTERVAL_MS,
    refetchIntervalInBackground: !isRangeFiltered,
    refetchOnWindowFocus: !isRangeFiltered,
    refetchOnReconnect: !isRangeFiltered,
    refetchOnMount: isRangeFiltered ? false : undefined,
    staleTime: isRangeFiltered ? Infinity : 0,
  });

  const cumulativeCounts = useMemo<Record<DashboardStateKey, number>>(
    () => ({
      running: orchestDash?.process?.counts?.running ?? 0,
      completed: orchestDash?.process?.counts?.completed ?? 0,
      failed: orchestDash?.process?.counts?.failed ?? 0,
      incident: orchestDash?.process?.counts?.incident ?? 0,
      hold: orchestDash?.process?.counts?.hold ?? 0,
      cancelled: orchestDash?.process?.counts?.cancelled ?? 0,
    }),
    [orchestDash?.process?.counts]
  );

  const processInstanceTotal = orchestDash?.process?.total ?? 0;
  const decisionInstanceTotal = orchestDash?.decision?.total ?? 0;

  /* ---------------- NAVIGATION ---------------- */

  const goToProcessFiltered = (
    status?: ProcessStatus,
    processDefinitionId?: string,
    version?: number
  ) => {
    if (!status && !processDefinitionId && version === undefined) {
      navigate("/processes");
      return;
    }

    navigate(
      buildProcessListPath({
        status: status ?? null,
        process: processDefinitionId ?? null,
        version: version !== undefined ? String(version) : null,
        searchText: "",
        from: null,
        to: null,
      }),
    );
  };

  const goToDecisionFiltered = (
    state?: string,
    decisionId?: string,
    version?: number
  ) => {
    if (!state && !decisionId && version === undefined) {
      navigate("/decisions");
      return;
    }

    navigate(
      buildDecisionListPath({
        status: state ?? null,
        decisionId: decisionId ?? null,
        version: version !== undefined ? String(version) : null,
        searchText: "",
        from: null,
        to: null,
      }),
    );
  };

  /* ---------------- PROCESS FILTERS ---------------- */

  const [processFilter, setProcessFilter] = useState<string>("__all__");
  const [versionFilter, setVersionFilter] = useState<string>("__all__");

  const allProcessRows: BreakdownEntry<DashboardStateKey>[] = useMemo(
    () =>
      (orchestDash?.process?.byProcess ?? []).map((row) => ({
        id: row.processDefinitionId,
        total: row.total,
        counts: row.counts,
        versions: row.versions,
      })),
    [orchestDash?.process?.byProcess]
  );

  const processOptions = useMemo(
    () => [
      { value: "__all__", label: "All Processes" },
      ...allProcessRows.map((r) => ({ value: r.id, label: r.id })),
    ],
    [allProcessRows]
  );

  const versionOptions = useMemo(() => {
    const selected = processFilter === "__all__" ? allProcessRows : allProcessRows.filter((r) => r.id === processFilter);
    const versionSet = new Set<number>();
    selected.forEach((r) => r.versions.forEach((v) => versionSet.add(v.version)));
    const sorted = [...versionSet].sort((a, b) => b - a);
    return [
      { value: "__all__", label: "All Versions" },
      ...sorted.map((v) => ({ value: String(v), label: `v${v}` })),
    ];
  }, [allProcessRows, processFilter]);

  // Reset version filter when process changes
  useEffect(() => {
    setVersionFilter("__all__");
  }, [processFilter]);

  const processRows: BreakdownEntry<DashboardStateKey>[] = useMemo(() => {
    let rows = allProcessRows;
    if (processFilter !== "__all__") {
      rows = rows.filter((r) => r.id === processFilter);
    }
    if (versionFilter !== "__all__") {
      const v = Number(versionFilter);
      rows = rows.map((r) => {
        const bucket = r.versions.find((vb) => vb.version === v);
        if (!bucket) return { ...r, total: 0, counts: Object.fromEntries(Object.keys(r.counts).map(k => [k, 0])) as Record<DashboardStateKey, number>, versions: [] };
        return { ...r, total: bucket.total, counts: bucket.counts, versions: [bucket] };
      }).filter((r) => r.total > 0);
    }
    return rows;
  }, [allProcessRows, processFilter, versionFilter]);

  const cumulativeSegments: BarSegment[] = PROCESS_STATE_ORDER.map((key) => {
    const meta = PROCESS_STATE_META[key];
    return {
      key,
      label: meta.label,
      count: cumulativeCounts[key],
      color: meta.color,
      onClick: () => goToProcessFiltered(meta.status),
    };
  });

  /* ---------------- DECISION DATA ---------------- */

  const decisionRows: BreakdownEntry<DecisionStateKey>[] = useMemo(
    () =>
      (orchestDash?.decision?.byDecision ?? []).map((row) => ({
        id: row.decisionId,
        total: row.total,
        counts: row.counts,
        versions: row.versions,
      })),
    [orchestDash?.decision?.byDecision]
  );

  /* ---------------- LOADING / REFRESH ---------------- */

  const hasEverLoaded = useRef(false);
  useEffect(() => {
    if (!dashLoading && orchestDash !== undefined) {
      hasEverLoaded.current = true;
    }
  }, [dashLoading, orchestDash]);

  const isInitialLoading =
    !hasEverLoaded.current && dashLoading;

  const handleRefresh = async () => {
    setManuallyRefreshing(true);
    try {
      await refetchOrchestDash();
    } finally {
      setManuallyRefreshing(false);
    }
  };

  const processTruncatedNotice = null;
  const decisionTruncatedNotice = null;

  return (
    <AppLayout>
      <div className={styles.page}>
        <div className={styles.viewToggleBar}>
          <div
            className={styles.viewToggle}
            role="tablist"
            aria-label="Dashboard views"
          >
            <button
              type="button"
              role="tab"
              aria-selected={view === "overview"}
              className={clsx(
                styles.viewToggleBtn,
                view === "overview" && styles.viewToggleBtnActive,
              )}
              onClick={() => setView("overview")}
            >
              Dashboard
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={view === "alerts"}
              className={clsx(
                styles.viewToggleBtn,
                view === "alerts" && styles.viewToggleBtnActive,
              )}
              onClick={() => setView("alerts")}
            >
              Alerts
            </button>
          </div>
        </div>

        <div className={styles.viewStage}>
          <div
            className={clsx(
              styles.viewPane,
              view === "overview" && styles.viewPaneActive,
            )}
            role="tabpanel"
            aria-hidden={view !== "overview"}
          >
            <header className={styles.header}>
              <div className={styles.headerLeft}>
                <span className={styles.eyebrow}>
                  <span className={styles.eyebrowDot} />
                  OrchesT · Live overview
                </span>
                <h1 className={styles.title}>Process Dashboard</h1>
                <p className={styles.subtitle}>
                  Cumulative state distribution across processes and decisions
                  tracked by the orchestrator.
                </p>
              </div>
              <div className={styles.headerRight}>
                <div className={styles.dateFilters}>
                  <DateRangePicker
                    from={draftFrom}
                    to={draftTo}
                    onChange={(f, t) => { setDraftFrom(f); setDraftTo(t); }}
                    onTimezoneChange={(tz) => setDraftTz(tz === 'local' ? 'UTC' : tz)}
                    onApply={handleApplyDateFilter}
                    onClear={handleClearDateFilter}
                    showClear={isRangeFiltered}
                    disabled={manuallyRefreshing}
                  />
                  {isRangeFiltered && (
                    <button
                      type="button"
                      className={styles.clearFilterButton}
                      onClick={handleClearDateFilter}
                    >
                      Clear
                    </button>
                  )}
                </div>
                <div className={styles.toolbarRow}>
                  <LiveIndicator paused={isRangeFiltered} />
                  <button
                    type="button"
                    onClick={handleRefresh}
                    className={clsx(
                      styles.refreshButton,
                      manuallyRefreshing && styles.spinning
                    )}
                    aria-label="Refresh dashboard"
                    disabled={manuallyRefreshing}
                  >
                    <RefreshCw className={styles.refreshIcon} />
                    {manuallyRefreshing ? "Refreshing" : "Refresh"}
                  </button>
                </div>
              </div>
            </header>

            {/* HERO TOTAL */}
            <section className={styles.hero}>
              <div className={styles.heroLeft}>
                <span className={styles.heroLabel}>Total Process Instances</span>
                <span className={styles.heroNumber}>
                  {isInitialLoading ? "—" : processInstanceTotal.toLocaleString()}
                </span>
                <div className={styles.heroSummary}>
                  <span className={styles.heroSummaryItem}>
                    <strong>{cumulativeCounts.running.toLocaleString()}</strong>
                    running
                  </span>
                  <span className={styles.heroSummaryItem}>
                    <strong>
                      {(
                        cumulativeCounts.failed + cumulativeCounts.incident
                      ).toLocaleString()}
                    </strong>
                    needing attention
                  </span>
                  <span className={styles.heroSummaryItem}>
                    <strong>
                      {decisionInstanceTotal.toLocaleString()}
                    </strong>
                    decision instances
                  </span>
                </div>
              </div>
              <div className={styles.heroRight}>
                <div className={styles.heroBarLabel}>
                  <span className={styles.heroBarTitle}>State Distribution</span>
                  <span className={styles.heroBarMeta}>
                    {isInitialLoading
                      ? "Loading…"
                      : `${processInstanceTotal.toLocaleString()} total`}
                  </span>
                </div>
                {isInitialLoading ? (
                  <div className={clsx(styles.skeleton, styles.skelBar)} />
                ) : (
                  <SegmentedProgressBar
                    size="large"
                    total={processInstanceTotal}
                    segments={cumulativeSegments}
                    showInlineCounts
                    showLegend
                  />
                )}
              </div>
            </section>

            {/* PARALLEL BREAKDOWN PANELS */}
            <StaggerList className={styles.panels}>
              <BreakdownPanel<DashboardStateKey>
                title="By Process"
                rows={processRows}
                states={PROCESS_STATES}
                primaryStateKey="running"
                sortOptions={PROCESS_SORTS}
                sortKey={processSortKey}
                onSortChange={setProcessSortKey}
                expanded={processExpanded}
                onToggleExpanded={(id) => toggleSet(setProcessExpanded, id)}
                onOpenRow={(row) => goToProcessFiltered(undefined, row.id)}
                onOpenVersion={(row, v) =>
                  goToProcessFiltered(undefined, row.id, v)
                }
                onSegmentClick={(row, key) =>
                  goToProcessFiltered(PROCESS_STATE_META[key].status, row.id)
                }
                onVersionSegmentClick={(row, v, key) =>
                  goToProcessFiltered(PROCESS_STATE_META[key].status, row.id, v)
                }
                isLoading={isInitialLoading}
                emptyTitle="No process instances yet"
                emptyHint="Once instances start running they’ll appear here."
                truncatedNotice={processTruncatedNotice}
                itemSingular="version"
                itemPlural="versions"
                filterSlot={
                  <div className={styles.panelFilters}>
                    <Combobox
                      value={processFilter}
                      onSelect={(v) => setProcessFilter(v || "__all__")}
                      items={processOptions}
                      placeholder="All Processes"
                      searchPlaceholder="Search process…"
                      emptyMessage="No processes found"
                    />
                    {processFilter !== "__all__" && (
                      <Combobox
                        value={versionFilter}
                        onSelect={(v) => setVersionFilter(v || "__all__")}
                        items={versionOptions}
                        placeholder="All Versions"
                        searchPlaceholder="Search version…"
                        emptyMessage="No versions found"
                      />
                    )}
                  </div>
                }
              />

              <BreakdownPanel<DecisionStateKey>
                title="By Decision"
                rows={decisionRows}
                states={DECISION_STATES}
                primaryStateKey="evaluated"
                sortOptions={DECISION_SORTS}
                sortKey={decisionSortKey}
                onSortChange={setDecisionSortKey}
                expanded={decisionExpanded}
                onToggleExpanded={(id) => toggleSet(setDecisionExpanded, id)}
                onOpenRow={(row) => goToDecisionFiltered(undefined, row.id)}
                onOpenVersion={(row, v) =>
                  goToDecisionFiltered(undefined, row.id, v)
                }
                onSegmentClick={(row, key) =>
                  goToDecisionFiltered(DECISION_STATE_META[key].apiState, row.id)
                }
                onVersionSegmentClick={(row, v, key) =>
                  goToDecisionFiltered(
                    DECISION_STATE_META[key].apiState,
                    row.id,
                    v
                  )
                }
                isLoading={isInitialLoading}
                emptyTitle="No decision instances yet"
                emptyHint="Once decisions are evaluated they’ll appear here."
                truncatedNotice={decisionTruncatedNotice}
                itemSingular="version"
                itemPlural="versions"
              />
            </StaggerList>
          </div>

          {alertsMounted && (
            <div
              className={clsx(
                styles.viewPane,
                view === "alerts" && styles.viewPaneActive,
              )}
              role="tabpanel"
              aria-hidden={view !== "alerts"}
            >
              <AlertsWorkspace />
            </div>
          )}
        </div>
      </div>
    </AppLayout>
  );
};
export default React.memo(Dashboard);
