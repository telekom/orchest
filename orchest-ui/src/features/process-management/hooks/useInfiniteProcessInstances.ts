import { processInstanceService } from "@/api/domains";
import { ProcessInstanceDTO } from "@/api/types/orchest-api";
import type { SortConfig } from "@/shared/components/DataTable/types";
import { toMultiSortParam } from "@/shared/components/DataTable/types";
import { queryKeys } from "@/shared/constants/queryKeys";
import { useInfiniteInstancesList } from "@/shared/hooks/useInfiniteInstancesList";
import { ProcessInstance } from "@/shared/types";
import type { ProcessFilters } from "./useProcessInstances";

export interface InfiniteProcessInstancesResult {
  instances: ProcessInstance[];
  isLoading: boolean;
  isFetchingNextPage: boolean;
  hasNextPage: boolean;
  totalElements: number;
  totalLoaded: number;
  loadMore: () => void;
  refetch: () => void;
  error?: Error;
}

interface UseInfiniteProcessInstancesOptions {
  filters: ProcessFilters;
  pageSize: number;
  sortConfig?: SortConfig | SortConfig[];
}

const mapDtoToProcessInstance = (instance: ProcessInstanceDTO): ProcessInstance => ({
  processId: instance.processInstanceId,
  processName: instance.processDefinitionId || "Unknown Process",
  processVersion: instance.version?.toString() || "1",
  status: (instance.state as ProcessInstance["status"]) || "ACTIVE",
  startDate: instance.createdAt,
  endDate: instance.completedAt ?? null,
  parentProcessId: instance.parentProcessInstanceId ?? "",
  sequenceExecutions: instance.sequenceExecutions || {},
  incidentMessage: instance.incidentMessage ?? undefined,
});

/**
 * Infinite-scroll variant of {@link useProcessInstances}.
 *
 * Loads process instances page-by-page on demand, accumulating the result set
 * in memory. Whenever filters, sort, or page size change, the cache is reset
 * (via the query key) and fetching restarts from page 0.
 */
export function useInfiniteProcessInstances({
  filters,
  pageSize,
  sortConfig,
}: UseInfiniteProcessInstancesOptions): InfiniteProcessInstancesResult {
  let sortsArray: SortConfig[] = [];
  if (Array.isArray(sortConfig)) {
    sortsArray = sortConfig;
  } else if (sortConfig) {
    sortsArray = [sortConfig];
  }
  const sortParam = sortsArray.length > 0 ? toMultiSortParam(sortsArray) : undefined;

  // Build a stable query key that captures everything except the page index;
  // useInfiniteQuery appends pageParam internally so we keep page out of the key
  // intentionally to enable proper cache reuse across pages.
  const queryKey = queryKeys.processInstances.list({
    process: filters.process,
    version: filters.version,
    searchText: filters.searchText || '',
    status: filters.status,
    from: filters.from,
    to: filters.to,
    timezone: filters.timezone || null,
    page: -1,
    size: pageSize,
    sort: sortParam,
  });

  const result = useInfiniteInstancesList<ProcessInstanceDTO, ProcessFilters, ProcessInstance>({
    queryKey,
    pageSize,
    filters,
    fetchFn: async ({ filters, pageNumber, pageSize }) => {
      const response = await processInstanceService.getProcessInstances({
        processDefinitionId: filters.process || undefined,
        version: filters.version ? Number.parseInt(filters.version, 10) : undefined,
        searchText: filters.searchText || undefined,
        state:
          filters.status && filters.status !== "All"
            ? filters.status
            : undefined,
        from: filters.from || undefined,
        to: filters.to || undefined,
        timezone: filters.timezone || undefined,
        page: pageNumber,
        size: pageSize,
        sort: sortParam,
      });

      if (!response?.content) {
        throw new Error("Invalid data received from API");
      }

      return response;
    },
    mapFn: mapDtoToProcessInstance,
    showErrorToast: true,
    enabled: true,
  });

  return {
    ...result,
    error: result.error as Error | undefined,
  };
}
