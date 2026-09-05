import { decisionInstanceService } from "@/api/domains";
import { DecisionInstanceDTO } from "@/api/types/orchest-api";
import { queryKeys } from "@/shared/constants/queryKeys";
import { useInstancesList } from "@/shared/hooks/useInstancesList";
import { mapDomainToContextDecisionInstance } from "@/shared/utils/typeMapping";
import type { DecisionFilters, DecisionInstance, DecisionInstancesResult } from "../types";

interface UseDecisionInstancesOptions {
  filters: DecisionFilters;
  pageNumber: number;
  pageSize: number;
  onPaginationUpdate?: (totalPages: number, totalElements: number) => void;
}

export function useDecisionInstances({
  filters,
  pageNumber,
  pageSize,
  onPaginationUpdate,
}: UseDecisionInstancesOptions): DecisionInstancesResult {
  const queryKey = queryKeys.decisionInstances.list({
    decisionId: filters.decisionId || null,
    version: filters.version || null,
    status: filters.status || null,
    searchText: filters.searchText || null,
    from: filters.from || null,
    to: filters.to || null,
    page: pageNumber,
    size: pageSize,
  });

  return useInstancesList<DecisionInstanceDTO, DecisionFilters, DecisionInstance>({
    queryKey,
    fetchFn: async ({ filters, pageNumber, pageSize }) => {
      const response = await decisionInstanceService.getDecisionInstances({
        decisionId: filters.decisionId || undefined,
        version: filters.version ? parseInt(filters.version) : undefined,
        state: filters.status as "EVALUATED" | "FAILED" | "UNKNOWN",
        searchText: filters.searchText || undefined,
        from: filters.from || undefined,
        to: filters.to || undefined,
        page: pageNumber,
        size: pageSize,
      });

      return response;
    },
    filters,
    pageNumber,
    pageSize,
    mapFn: (instance): DecisionInstance => mapDomainToContextDecisionInstance(instance),
    onPaginationUpdate,
    showErrorToast: true,
    enabled: true,
  });
}
