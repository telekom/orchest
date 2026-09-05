import type { OrchestStatsDTO, OrchestStatItem } from "@/api/types/orchest-api";
import type {
  DecisionBreakdownRow,
  DecisionDashboardAggregates,
  DecisionStateKey,
  DecisionVersionBreakdown,
} from "@/features/decision-management/hooks/useDecisionDashboardData";
import { ProcessStatus } from "@/shared/constants/status";
import { ProcessInstanceState } from "@/shared/enums";
import type {
  DashboardAggregates,
  DashboardStateKey,
  ProcessBreakdownRow,
  VersionBreakdown,
} from "../hooks/useDashboardData";

const EMPTY_PROCESS: Record<DashboardStateKey, number> = {
  running: 0,
  completed: 0,
  failed: 0,
  incident: 0,
  hold: 0,
  cancelled: 0,
};

const EMPTY_DECISION: Record<DecisionStateKey, number> = {
  evaluated: 0,
  failed: 0,
  unknown: 0,
};

const classifyProcessState = (
  state: string | undefined
): DashboardStateKey | null => {
  if (!state) return null;
  const upper = state.toUpperCase();
  switch (upper) {
    case ProcessStatus.ACTIVE:
    case ProcessStatus.RUNNING:
    case ProcessStatus.STARTED:
    case ProcessInstanceState.TRIGGERED:
    case ProcessInstanceState.PENDING:
      return "running";
    case ProcessStatus.COMPLETED:
      return "completed";
    case ProcessStatus.FAILED:
      return "failed";
    case ProcessStatus.INCIDENT:
      return "incident";
    case ProcessStatus.HOLD:
      return "hold";
    case ProcessStatus.CANCELLED:
    case ProcessStatus.TERMINATED:
      return "cancelled";
    default:
      return null;
  }
};

const classifyDecisionState = (state: string | undefined): DecisionStateKey => {
  if (!state) return "unknown";
  const upper = state.toUpperCase();
  switch (upper) {
    case "EVALUATED":
    case "EXECUTED":
      return "evaluated";
    case "FAILED":
      return "failed";
    default:
      return "unknown";
  }
};

const sumProcessCounts = (c: Record<DashboardStateKey, number>): number =>
  (Object.values(c) as number[]).reduce((a, b) => a + b, 0);

const statsListToProcessCounts = (
  list: OrchestStatItem[] | undefined | null
): Record<DashboardStateKey, number> => {
  const out = { ...EMPTY_PROCESS };
  for (const item of list ?? []) {
    const key = classifyProcessState(item.state);
    if (key) out[key] += item.count ?? 0;
  }
  return out;
};

const statsListToDecisionCounts = (
  list: OrchestStatItem[] | undefined | null
): Record<DecisionStateKey, number> => {
  const out = { ...EMPTY_DECISION };
  for (const item of list ?? []) {
    const key = classifyDecisionState(item.state);
    out[key] += item.count ?? 0;
  }
  return out;
};

const mergeProcessCounts = (
  a: Record<DashboardStateKey, number>,
  b: Record<DashboardStateKey, number>
): Record<DashboardStateKey, number> => {
  const out = { ...EMPTY_PROCESS };
  (Object.keys(out) as DashboardStateKey[]).forEach((k) => {
    out[k] = a[k] + b[k];
  });
  return out;
};

const mergeDecisionCounts = (
  a: Record<DecisionStateKey, number>,
  b: Record<DecisionStateKey, number>
): Record<DecisionStateKey, number> => {
  const out = { ...EMPTY_DECISION };
  (Object.keys(out) as DecisionStateKey[]).forEach((k) => {
    out[k] = a[k] + b[k];
  });
  return out;
};

const parseVersionKey = (key: string): number => {
  const n = Number.parseInt(key, 10);
  return Number.isFinite(n) ? n : 0;
};

function mapProcessGrouped(
  processStats: NonNullable<OrchestStatsDTO["processStats"]>
): ProcessBreakdownRow[] {
  const rows: ProcessBreakdownRow[] = [];

  for (const [processDefinitionId, versionMapRaw] of Object.entries(
    processStats
  )) {
    if (!versionMapRaw || typeof versionMapRaw !== "object") continue;

    const versions: VersionBreakdown[] = [];
    let aggregatedCounts = { ...EMPTY_PROCESS };

    for (const [versionKey, statList] of Object.entries(versionMapRaw)) {
      const version = parseVersionKey(versionKey);
      const counts = statsListToProcessCounts(
        Array.isArray(statList) ? statList : []
      );
      const total = sumProcessCounts(counts);
      aggregatedCounts = mergeProcessCounts(aggregatedCounts, counts);
      versions.push({ version, total, counts });
    }

    versions.sort((a, b) => b.version - a.version);

    const id = processDefinitionId || "Unknown Process";
    rows.push({
      processDefinitionId: id,
      total: sumProcessCounts(aggregatedCounts),
      counts: aggregatedCounts,
      versions,
    });
  }

  return rows.sort((a, b) => b.total - a.total);
}

function mapDecisionGrouped(
  decisionStats: NonNullable<OrchestStatsDTO["decisionStats"]>
): DecisionBreakdownRow[] {
  const rows: DecisionBreakdownRow[] = [];

  for (const [decisionId, versionMapRaw] of Object.entries(decisionStats)) {
    if (!versionMapRaw || typeof versionMapRaw !== "object") continue;

    const versions: DecisionVersionBreakdown[] = [];
    let aggregatedCounts = { ...EMPTY_DECISION };

    for (const [versionKey, statList] of Object.entries(versionMapRaw)) {
      const version = parseVersionKey(versionKey);
      const counts = statsListToDecisionCounts(
        Array.isArray(statList) ? statList : []
      );
      const total =
        counts.evaluated + counts.failed + counts.unknown;
      aggregatedCounts = mergeDecisionCounts(aggregatedCounts, counts);
      versions.push({ version: version || 0, total, counts });
    }

    versions.sort((a, b) => b.version - a.version);

    const id = decisionId || "Unknown Decision";
    rows.push({
      decisionId: id,
      total:
        aggregatedCounts.evaluated +
        aggregatedCounts.failed +
        aggregatedCounts.unknown,
      counts: aggregatedCounts,
      versions,
    });
  }

  return rows.sort((a, b) => b.total - a.total);
}

export interface MappedOrchestDashboard {
  readonly process: DashboardAggregates;
  readonly decision: DecisionDashboardAggregates;
  /** Parsed server timestamp when present (ISO-8601). */
  readonly lastUpdatedAtIso: string | null;
}

/**
 * Maps {@link OrchestStatsDTO} (GET `/orchest/stats`) into the aggregate shapes
 * consumed by {@link Dashboard}.
 */
export function mapOrchestStatsToDashboard(dto: OrchestStatsDTO): MappedOrchestDashboard {
  const byProcess = mapProcessGrouped(dto.processStats ?? {});
  const byDecision = mapDecisionGrouped(dto.decisionStats ?? {});

  let processCounts = statsListToProcessCounts(dto.totalProcessStats);
  if (sumProcessCounts(processCounts) === 0 && byProcess.length > 0) {
    processCounts = sumCountsFromProcessBreakdownRows(byProcess);
  }
  const processTotal = sumProcessCounts(processCounts);

  let decisionCounts = statsListToDecisionCounts(dto.totalDecisionStats);
  let decisionTotal =
    decisionCounts.evaluated +
    decisionCounts.failed +
    decisionCounts.unknown;
  if (decisionTotal === 0 && byDecision.length > 0) {
    decisionCounts = sumDecisionCountsRows(byDecision);
    decisionTotal =
      decisionCounts.evaluated +
      decisionCounts.failed +
      decisionCounts.unknown;
  }

  const process: DashboardAggregates = {
    total: processTotal,
    counts: processCounts,
    byProcess,
    scannedCount: processTotal,
    scannedPages: 1,
    truncated: false,
  };

  const decision: DecisionDashboardAggregates = {
    total: decisionTotal,
    counts: decisionCounts,
    byDecision,
    scannedCount: decisionTotal,
    scannedPages: 1,
    truncated: false,
  };

  return {
    process,
    decision,
    lastUpdatedAtIso: dto.lastUpdatedAt ?? null,
  };
}

function sumCountsFromProcessBreakdownRows(
  rows: ProcessBreakdownRow[]
): Record<DashboardStateKey, number> {
  const out = { ...EMPTY_PROCESS };
  for (const row of rows) {
    (Object.keys(out) as DashboardStateKey[]).forEach((k) => {
      out[k] += row.counts[k];
    });
  }
  return out;
}

function sumDecisionCountsRows(
  rows: DecisionBreakdownRow[]
): Record<DecisionStateKey, number> {
  const out = { ...EMPTY_DECISION };
  for (const row of rows) {
    (Object.keys(out) as DecisionStateKey[]).forEach((k) => {
      out[k] += row.counts[k];
    });
  }
  return out;
}
