import { processInstanceService } from "@/api/domains";
import { ProcessInstanceDTO } from "@/api/types/orchest-api";
import type { SortConfig } from "@/shared/components/DataTable/types";
import { toMultiSortParam } from "@/shared/components/DataTable/types";
import { queryKeys } from "@/shared/constants/queryKeys";
import { useInstancesList } from "@/shared/hooks/useInstancesList";
import { ProcessInstance } from "@/shared/types";

export interface ProcessFilters {
  process: string | null;
  version: string | null;
  searchText: string | null;
  status: string | null;
  from: string | null;
  to: string | null;
  timezone?: string | null;
}

export interface ProcessInstancesResult {
  instances: ProcessInstance[];
  isLoading: boolean;
  totalPages: number;
  totalElements: number;
  refetch: () => void;
  error?: Error;
}

interface UseProcessInstancesOptions {
  filters: ProcessFilters;
  pageNumber: number;
  pageSize: number;
  sortConfig?: SortConfig | SortConfig[];
  onPaginationUpdate?: (totalPages: number, totalElements: number) => void;
}

export function useProcessInstances({
  filters,
  pageNumber,
  pageSize,
  sortConfig,
  onPaginationUpdate,
}: UseProcessInstancesOptions): ProcessInstancesResult {
  const sorts = sortConfig
    ? (Array.isArray(sortConfig) ? sortConfig : [sortConfig])
    : [];
  const sortParam = sorts.length > 0 ? toMultiSortParam(sorts) : undefined;

  const queryKey = queryKeys.processInstances.list({
    process: filters.process,
    version: filters.version,
    searchText: filters.searchText || '',
    status: filters.status,
    from: filters.from,
    to: filters.to,
    page: pageNumber,
    size: pageSize,
    sort: sortParam,
  });

  const result = useInstancesList<ProcessInstanceDTO, ProcessFilters, ProcessInstance>({
    queryKey,
    fetchFn: async ({ filters, pageNumber, pageSize }) => {
      const response = await processInstanceService.getProcessInstances({
        processDefinitionId: filters.process || undefined,
        version: filters.version ? parseInt(filters.version) : undefined,
        searchText: filters.searchText || undefined,
        state:
          filters.status && filters.status !== "All"
            ? filters.status
            : undefined,
        from: filters.from || undefined,
        to: filters.to || undefined,
        page: pageNumber,
        size: pageSize,
        sort: sortParam,
      });

      if (!response || !response.content) {
        throw new Error("Invalid data received from API");
      }

      return response;
    },
    filters,
    pageNumber,
    pageSize,
    mapFn: (instance): ProcessInstance => ({
      processId: instance.processInstanceId,
      processName: instance.processDefinitionId || "Unknown Process",
      processVersion: instance.version?.toString() || "1",
      status: (instance.state as ProcessInstance["status"]) || "ACTIVE",
      startDate: instance.createdAt,
      endDate: instance.completedAt ?? null,
      parentProcessId: instance.parentProcessInstanceId ?? "",
      sequenceExecutions: instance.sequenceExecutions || {},
      incidentMessage: instance.incidentMessage ?? undefined,
    }),
    onPaginationUpdate,
    showErrorToast: true,
    enabled: true,
  });

  return {
    ...result,
    error: result.error as Error | undefined,
  };
}
