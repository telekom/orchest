import type { OrchestStatItem, OrchestStatsDTO } from "@/api/types/orchest-api";
import type { OrchestStatsQuerySlice } from "@/api/domains";
import { toISODateTime } from "@/api/core/utils/dateUtils";
import { ProcessStatus } from "@/shared/constants/status";
import { ProcessInstanceState } from "@/shared/enums";

export interface ProcessListStats {
  total: number;
  completed: number;
  running: number;
  hold: number;
  cancelled: number;
  failed: number;
  incident: number;
}

export interface DecisionListStats {
  total: number;
  evaluated: number;
  failed: number;
  unknown: number;
}

export interface ListStatsDateFilters {
  from?: string | null;
  to?: string | null;
  timezone?: string | null;
}

export interface ProcessListStatsFilters extends ListStatsDateFilters {
  process?: string | null;
  version?: string | null;
}

export interface DecisionListStatsFilters extends ListStatsDateFilters {
  decisionId?: string | null;
  version?: string | null;
}

export const DEFAULT_PROCESS_LIST_STATS: ProcessListStats = {
  total: 0,
  completed: 0,
  running: 0,
  hold: 0,
  cancelled: 0,
  failed: 0,
  incident: 0,
};

export const DEFAULT_DECISION_LIST_STATS: DecisionListStats = {
  total: 0,
  evaluated: 0,
  failed: 0,
  unknown: 0,
};

type ProcessStateKey = Exclude<keyof ProcessListStats, "total">;
type DecisionStateKey = Exclude<keyof DecisionListStats, "total">;

const classifyProcessState = (state: string | undefined): ProcessStateKey | null => {
  if (!state) return null;
  switch (state.toUpperCase()) {
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
  switch (state.toUpperCase()) {
    case "EVALUATED":
    case "EXECUTED":
      return "evaluated";
    case "FAILED":
      return "failed";
    default:
      return "unknown";
  }
};

const sumStatLists = (lists: OrchestStatItem[][]): OrchestStatItem[] => {
  const byState = new Map<string, number>();
  for (const list of lists) {
    for (const item of list) {
      const key = item.state;
      byState.set(key, (byState.get(key) ?? 0) + (item.count ?? 0));
    }
  }
  return Array.from(byState.entries()).map(([state, count]) => ({ state, count }));
};

const collectVersionLists = (
  versionMap: Record<string, OrchestStatItem[]> | undefined | null,
  version: string | null | undefined,
): OrchestStatItem[] => {
  if (!versionMap) return [];

  const normalizedVersion =
    version && version !== "all" && version.trim() !== "" ? version.trim() : null;

  if (normalizedVersion) {
    const exact = versionMap[normalizedVersion];
    if (Array.isArray(exact)) return exact;

    // Tolerate numeric vs string key mismatch ("1" vs "01")
    const asNum = Number.parseInt(normalizedVersion, 10);
    if (Number.isFinite(asNum)) {
      for (const [key, list] of Object.entries(versionMap)) {
        if (Number.parseInt(key, 10) === asNum && Array.isArray(list)) {
          return list;
        }
      }
    }
    return [];
  }

  return sumStatLists(
    Object.values(versionMap).filter((list): list is OrchestStatItem[] => Array.isArray(list)),
  );
};

export const listStatsToProcessStats = (
  list: OrchestStatItem[] | undefined | null,
): ProcessListStats => {
  const out: ProcessListStats = { ...DEFAULT_PROCESS_LIST_STATS };
  for (const item of list ?? []) {
    const key = classifyProcessState(item.state);
    if (!key) continue;
    out[key] += item.count ?? 0;
  }
  out.total =
    out.running +
    out.completed +
    out.failed +
    out.incident +
    out.hold +
    out.cancelled;
  return out;
};

export const listStatsToDecisionStats = (
  list: OrchestStatItem[] | undefined | null,
): DecisionListStats => {
  const out: DecisionListStats = { ...DEFAULT_DECISION_LIST_STATS };
  for (const item of list ?? []) {
    const key = classifyDecisionState(item.state);
    out[key] += item.count ?? 0;
  }
  out.total = out.evaluated + out.failed + out.unknown;
  return out;
};

/**
 * Project process list count-card stats from `GET /orchest/stats`.
 * Applies process/version filters via the nested `processStats` map.
 * Status / searchText are intentionally ignored — cards show the status breakdown.
 */
export function projectProcessListStats(
  dto: OrchestStatsDTO | undefined | null,
  filters: ProcessListStatsFilters = {},
): ProcessListStats {
  if (!dto) return { ...DEFAULT_PROCESS_LIST_STATS };

  const processId = filters.process?.trim() || null;
  if (processId) {
    const versionMap = dto.processStats?.[processId];
    return listStatsToProcessStats(collectVersionLists(versionMap, filters.version));
  }

  const totals = listStatsToProcessStats(dto.totalProcessStats);
  if (totals.total > 0) return totals;

  // Fallback: sum nested processStats when totals are empty
  const nested = dto.processStats ?? {};
  const allLists = Object.values(nested).flatMap((versionMap) =>
    collectVersionLists(versionMap, null),
  );
  return listStatsToProcessStats(allLists);
}

/**
 * Project decision list count-card stats from `GET /orchest/stats`.
 */
export function projectDecisionListStats(
  dto: OrchestStatsDTO | undefined | null,
  filters: DecisionListStatsFilters = {},
): DecisionListStats {
  if (!dto) return { ...DEFAULT_DECISION_LIST_STATS };

  const decisionId = filters.decisionId?.trim() || null;
  if (decisionId) {
    const versionMap = dto.decisionStats?.[decisionId];
    return listStatsToDecisionStats(collectVersionLists(versionMap, filters.version));
  }

  const totals = listStatsToDecisionStats(dto.totalDecisionStats);
  if (totals.total > 0) return totals;

  const nested = dto.decisionStats ?? {};
  const allLists = Object.values(nested).flatMap((versionMap) =>
    collectVersionLists(versionMap, null),
  );
  return listStatsToDecisionStats(allLists);
}

/**
 * Build optional `GET /orchest/stats` date query from list date filters.
 * Returns null when neither bound is set (unbounded aggregates).
 */
export function buildListStatsQuerySlice(
  filters: ListStatsDateFilters,
): OrchestStatsQuerySlice | null {
  const from = filters.from?.trim() || null;
  const to = filters.to?.trim() || null;
  if (!from && !to) return null;

  const timezone = filters.timezone?.trim() || "local";
  const slice: OrchestStatsQuerySlice = {
    timeZone: timezone === "local" ? Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC" : timezone,
  };
  if (from) slice.from = toISODateTime(from, timezone);
  if (to) slice.to = toISODateTime(to, timezone);
  return slice;
}

/**
 * Footer / list total from projected stats.
 * When a status filter is active, returns that bucket's count; otherwise `stats.total`.
 */
export function resolveProcessStatsTotal(
  stats: ProcessListStats,
  status?: string | null,
): number {
  if (!status || status === "All" || status.toLowerCase() === "all") {
    return stats.total;
  }
  const key = classifyProcessState(status);
  return key ? stats[key] : stats.total;
}

export function resolveDecisionStatsTotal(
  stats: DecisionListStats,
  status?: string | null,
): number {
  if (!status || status === "All" || status.toLowerCase() === "all") {
    return stats.total;
  }
  return stats[classifyDecisionState(status)];
}
