import { processInstanceService } from "@/api/domains";
import { ProcessInstanceDTO } from "@/api/types/orchest-api";
import { useApiQuery } from "@/shared/hooks";
import { ProcessStatus } from "@/shared/constants/status";

export type DashboardStateKey =
  | "running"
  | "completed"
  | "failed"
  | "incident"
  | "hold"
  | "cancelled";

export interface VersionBreakdown {
  version: number;
  total: number;
  counts: Record<DashboardStateKey, number>;
}

export interface ProcessBreakdownRow {
  processDefinitionId: string;
  total: number;
  counts: Record<DashboardStateKey, number>;
  versions: VersionBreakdown[];
}

export interface DashboardAggregates {
  total: number;
  counts: Record<DashboardStateKey, number>;
  byProcess: ProcessBreakdownRow[];
  scannedCount: number;
  scannedPages: number;
  truncated: boolean;
}

const EMPTY_COUNTS: Record<DashboardStateKey, number> = {
  running: 0,
  completed: 0,
  failed: 0,
  incident: 0,
  hold: 0,
  cancelled: 0,
};

const PAGE_SIZE = 100;
const MAX_PAGES = 20;

const classify = (state: string | undefined): DashboardStateKey | null => {
  if (!state) return null;
  const upper = state.toUpperCase();
  switch (upper) {
    case ProcessStatus.ACTIVE:
    case ProcessStatus.RUNNING:
    case ProcessStatus.STARTED:
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

const aggregate = (instances: ProcessInstanceDTO[]): ProcessBreakdownRow[] => {
  const map = new Map<string, ProcessBreakdownRow>();
  const versionMaps = new Map<string, Map<number, VersionBreakdown>>();

  for (const instance of instances) {
    const id = instance.processDefinitionId || "Unknown Process";
    const version =
      typeof instance.version === "number" && Number.isFinite(instance.version)
        ? instance.version
        : 1;

    let row = map.get(id);
    if (!row) {
      row = {
        processDefinitionId: id,
        total: 0,
        counts: { ...EMPTY_COUNTS },
        versions: [],
      };
      map.set(id, row);
      versionMaps.set(id, new Map());
    }
    row.total += 1;
    const bucket = classify(instance.state as string);
    if (bucket) {
      row.counts[bucket] += 1;
    }

    const versionMap = versionMaps.get(id)!;
    let vRow = versionMap.get(version);
    if (!vRow) {
      vRow = {
        version,
        total: 0,
        counts: { ...EMPTY_COUNTS },
      };
      versionMap.set(version, vRow);
    }
    vRow.total += 1;
    if (bucket) {
      vRow.counts[bucket] += 1;
    }
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
  rows: ProcessBreakdownRow[]
): Record<DashboardStateKey, number> => {
  const out = { ...EMPTY_COUNTS };
  for (const row of rows) {
    (Object.keys(out) as DashboardStateKey[]).forEach((k) => {
      out[k] += row.counts[k];
    });
  }
  return out;
};

export interface UseDashboardDataOptions {
  refetchInterval?: number | false;
  refetchIntervalInBackground?: boolean;
}

export function useDashboardData(options: UseDashboardDataOptions = {}) {
  const { refetchInterval, refetchIntervalInBackground } = options;

  return useApiQuery<DashboardAggregates>(
    ["dashboard", "process-aggregates", "v1"],
    async () => {
      const first = await processInstanceService.getProcessInstances({
        page: 0,
        size: PAGE_SIZE,
        sort: "createdAt,desc",
      });

      const totalPages = first?.page?.totalPages ?? 1;
      const collected: ProcessInstanceDTO[] = [...(first?.content ?? [])];
      const pagesToFetch = Math.min(totalPages, MAX_PAGES);

      if (pagesToFetch > 1) {
        const promises = [];
        for (let i = 1; i < pagesToFetch; i++) {
          promises.push(
            processInstanceService.getProcessInstances({
              page: i,
              size: PAGE_SIZE,
              sort: "createdAt,desc",
            })
          );
        }
        const rest = await Promise.all(promises);
        for (const r of rest) {
          if (r?.content) collected.push(...r.content);
        }
      }

      const byProcess = aggregate(collected);
      const counts = sumCounts(byProcess);

      return {
        total: first?.page?.totalElements ?? collected.length,
        counts,
        byProcess,
        scannedCount: collected.length,
        scannedPages: pagesToFetch,
        truncated: totalPages > MAX_PAGES,
      };
    },
    {
      showErrorToast: true,
      enabled: true,
      staleTime: 0,
      refetchInterval,
      refetchIntervalInBackground,
    }
  );
}
