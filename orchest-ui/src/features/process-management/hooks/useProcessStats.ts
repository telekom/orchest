import { statsService } from "@/api/domains";
import { useApiQuery } from "@/shared/hooks";
import { queryKeys } from "@/shared/constants/queryKeys";
import { useMemo } from "react";
import {
  buildListStatsQuerySlice,
  DEFAULT_PROCESS_LIST_STATS,
  projectProcessListStats,
  type ProcessListStats,
  type ProcessListStatsFilters,
} from "../utils/projectListStats";

export type ProcessStats = ProcessListStats;

export interface UseProcessStatsOptions {
  filters?: ProcessListStatsFilters;
  refetchInterval?: number | false;
  refetchIntervalInBackground?: boolean;
  showErrorToast?: boolean;
}

export function useProcessStats(options: UseProcessStatsOptions = {}) {
  const {
    filters = {},
    refetchInterval,
    refetchIntervalInBackground,
    showErrorToast = true,
  } = options;

  const statsQuery = useMemo(
    () => buildListStatsQuerySlice(filters),
    [filters.from, filters.to, filters.timezone],
  );

  const {
    data: dto,
    isLoading,
    isFetching,
    dataUpdatedAt,
    refetch,
    error,
  } = useApiQuery(
    queryKeys.orchest.stats(statsQuery),
    async () => statsService.getOrchestStats(statsQuery),
    {
      showErrorToast,
      enabled: true,
      refetchInterval,
      refetchIntervalInBackground,
    },
  );

  const stats = useMemo(
    () =>
      projectProcessListStats(dto, {
        process: filters.process,
        version: filters.version,
      }),
    [dto, filters.process, filters.version],
  );

  return {
    stats: stats ?? DEFAULT_PROCESS_LIST_STATS,
    isLoading,
    isFetching,
    dataUpdatedAt,
    refetch,
    error,
  };
}
