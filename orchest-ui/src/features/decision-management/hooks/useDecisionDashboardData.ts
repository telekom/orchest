import { decisionInstanceService } from "@/api/domains";
import { DecisionInstanceDTO } from "@/api/types/orchest-api";
import { useApiQuery } from "@/shared/hooks";

export type DecisionStateKey = "evaluated" | "failed" | "unknown";

export interface DecisionVersionBreakdown {
  version: number;
  total: number;
  counts: Record<DecisionStateKey, number>;
}

export interface DecisionBreakdownRow {
  decisionId: string;
  total: number;
  counts: Record<DecisionStateKey, number>;
  versions: DecisionVersionBreakdown[];
}

export interface DecisionDashboardAggregates {
  total: number;
  counts: Record<DecisionStateKey, number>;
  byDecision: DecisionBreakdownRow[];
  scannedCount: number;
  scannedPages: number;
  truncated: boolean;
}

const EMPTY_COUNTS: Record<DecisionStateKey, number> = {
  evaluated: 0,
  failed: 0,
  unknown: 0,
};

const PAGE_SIZE = 100;
const MAX_PAGES = 20;

const classify = (state: string | undefined): DecisionStateKey => {
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

const aggregate = (instances: DecisionInstanceDTO[]): DecisionBreakdownRow[] => {
  const map = new Map<string, DecisionBreakdownRow>();
  const versionMaps = new Map<string, Map<number, DecisionVersionBreakdown>>();

  for (const instance of instances) {
    const id = instance.decisionId || "Unknown Decision";
    const version =
      typeof instance.version === "number" && Number.isFinite(instance.version)
        ? instance.version
        : 1;

    let row = map.get(id);
    if (!row) {
      row = {
        decisionId: id,
        total: 0,
        counts: { ...EMPTY_COUNTS },
        versions: [],
      };
      map.set(id, row);
      versionMaps.set(id, new Map());
    }
    row.total += 1;
    const bucket = classify(instance.state);
    row.counts[bucket] += 1;

    const versionMap = versionMaps.get(id)!;
    let vRow = versionMap.get(version);
    if (!vRow) {
      vRow = { version, total: 0, counts: { ...EMPTY_COUNTS } };
      versionMap.set(version, vRow);
    }
    vRow.total += 1;
    vRow.counts[bucket] += 1;
  }

  for (const [id, row] of map.entries()) {
    const versionMap = versionMaps.get(id);
    if (versionMap) {
      row.versions = Array.from(versionMap.values()).sort(
        (a, b) => b.version - a.version
      );
    }
  }

  return Array.from(map.values()).sort((a, b) => b.total - a.total);
};

const sumCounts = (
  rows: DecisionBreakdownRow[]
): Record<DecisionStateKey, number> => {
  const out = { ...EMPTY_COUNTS };
  for (const row of rows) {
    (Object.keys(out) as DecisionStateKey[]).forEach((k) => {
      out[k] += row.counts[k];
    });
  }
  return out;
};

export interface UseDecisionDashboardDataOptions {
  refetchInterval?: number | false;
  refetchIntervalInBackground?: boolean;
}

export function useDecisionDashboardData(
  options: UseDecisionDashboardDataOptions = {}
) {
  const { refetchInterval, refetchIntervalInBackground } = options;

  return useApiQuery<DecisionDashboardAggregates>(
    ["dashboard", "decision-aggregates", "v1"],
    async () => {
      const first = await decisionInstanceService.getDecisionInstances({
        page: 0,
        size: PAGE_SIZE,
      });

      const totalPages = first?.page?.totalPages ?? 1;
      const collected: DecisionInstanceDTO[] = [...(first?.content ?? [])];
      const pagesToFetch = Math.min(totalPages, MAX_PAGES);

      if (pagesToFetch > 1) {
        const promises = [];
        for (let i = 1; i < pagesToFetch; i++) {
          promises.push(
            decisionInstanceService.getDecisionInstances({
              page: i,
              size: PAGE_SIZE,
            })
          );
        }
        const rest = await Promise.all(promises);
        for (const r of rest) {
          if (r?.content) collected.push(...r.content);
        }
      }

      const byDecision = aggregate(collected);
      const counts = sumCounts(byDecision);

      return {
        total: first?.page?.totalElements ?? collected.length,
        counts,
        byDecision,
        scannedCount: collected.length,
        scannedPages: pagesToFetch,
        truncated: totalPages > MAX_PAGES,
      };
    },
    {
      showErrorToast: false,
      enabled: true,
      staleTime: 0,
      refetchInterval,
      refetchIntervalInBackground,
    }
  );
}
