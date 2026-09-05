import { statsService } from "@/api/domains";
import { queryKeys } from "@/shared/constants/queryKeys";
import { useApiQuery } from "@/shared/hooks";
import {
  buildListStatsQuerySlice,
  DEFAULT_DECISION_LIST_STATS,
  projectDecisionListStats,
  type DecisionListStats,
  type DecisionListStatsFilters,
} from "@/features/process-management/utils/projectListStats";
import { useMemo } from "react";

export type DecisionStats = DecisionListStats;

export interface UseDecisionStatsOptions {
  filters?: DecisionListStatsFilters;
  refetchInterval?: number | false;
  refetchIntervalInBackground?: boolean;
  showErrorToast?: boolean;
}

export function useDecisionStats(options: UseDecisionStatsOptions = {}) {
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
      projectDecisionListStats(dto, {
        decisionId: filters.decisionId,
        version: filters.version,
      }),
    [dto, filters.decisionId, filters.version],
  );

  return {
    stats: stats ?? DEFAULT_DECISION_LIST_STATS,
    isLoading,
    isFetching,
    dataUpdatedAt,
    refetch,
    error,
  };
}
