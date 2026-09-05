import { useEffect, useRef } from "react";
import { useApiQuery } from "./useApiQuery";
import { useStableCallback } from "./useStableCallback";

/**
 * Type-safe identity function for when no mapping is needed
 * Avoids unsafe type assertions
 */
function identityMap<T>(item: T): T {
  return item;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: {
    totalPages: number;
    totalElements: number;
  };
}

export interface InstancesListConfig<TData, TFilters, TMapped = TData> {
  queryKey: readonly unknown[];
  fetchFn: (params: FetchParams<TFilters>) => Promise<PaginatedResponse<TData>>;
  filters: TFilters;
  pageNumber: number;
  pageSize: number;
  mapFn?: (data: TData) => TMapped;
  onPaginationUpdate?: (totalPages: number, totalElements: number) => void;
  showErrorToast?: boolean;
  enabled?: boolean;
}

export interface FetchParams<TFilters> {
  filters: TFilters;
  pageNumber: number;
  pageSize: number;
}

export interface InstancesListResult<T> {
  instances: T[];
  isLoading: boolean;
  /** True when loading a new page while previous data exists (for pagination UX) */
  isPaginationLoading: boolean;
  totalPages: number;
  totalElements: number;
  refetch: () => void;
  error?: unknown;
}

/**
 * Generic hook for fetching paginated lists of instances
 * Standardizes data fetching patterns across Process and Decision management
 *
 * @example
 * const { instances, isLoading, totalPages, refetch } = useInstancesList({
 *   queryKey: queryKeys.processInstances.list({ ...filters, page, size }),
 *   fetchFn: async ({ filters, pageNumber, pageSize }) => {
 *     return await service.getInstances({ ...filters, page: pageNumber, size: pageSize });
 *   },
 *   filters: processFilters,
 *   pageNumber,
 *   pageSize,
 *   mapFn: (data) => mapDomainToContextInstance(data),
 *   onPaginationUpdate: setTotalPages,
 * });
 */
export function useInstancesList<TData, TFilters = Record<string, unknown>, TMapped = TData>({
  queryKey,
  fetchFn,
  filters,
  pageNumber,
  pageSize,
  mapFn,
  onPaginationUpdate,
  showErrorToast = true,
  enabled = true,
}: InstancesListConfig<TData, TFilters, TMapped>): InstancesListResult<TMapped> {
  const stableOnPaginationUpdate = useStableCallback(onPaginationUpdate);
  const previousDataRef = useRef<TMapped[]>([]);

  const { data, isLoading, refetch, error } = useApiQuery(
    queryKey,
    async () => {
      const response = await fetchFn({ filters, pageNumber, pageSize });

      if (!response) {
        return {
          content: [],
          totalPages: 0,
          totalElements: 0,
        };
      }

      const content = response.content || [];
      const totalPages = response.page.totalPages;
      const totalElements = response.page.totalElements;

      const effectiveMapFn = mapFn ?? (identityMap as (data: TData) => TMapped);
      const mappedContent = content.map(effectiveMapFn);

      return {
        content: mappedContent,
        totalPages,
        totalElements,
      };
    },
    {
      showErrorToast,
      enabled,
      placeholderData: (previousData) => previousData,
    }
  );

  const activeData = data || {
    content: [],
    totalPages: 0,
    totalElements: 0,
  };

  // Track previous data for pagination detection
  useEffect(() => {
    if (!isLoading && activeData.content.length > 0) {
      previousDataRef.current = activeData.content;
    }
  }, [isLoading, activeData.content]);

  // isPaginationLoading: loading a new page while we have previous data
  const isPaginationLoading = isLoading && previousDataRef.current.length > 0;

  useEffect(() => {
    if (stableOnPaginationUpdate && activeData && !isLoading) {
      stableOnPaginationUpdate(activeData.totalPages, activeData.totalElements);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps -- Only react to specific property changes, not the full activeData reference
  }, [activeData.totalPages, activeData.totalElements, isLoading, stableOnPaginationUpdate]);

  return {
    instances: activeData.content,
    isLoading,
    isPaginationLoading,
    totalPages: activeData.totalPages,
    totalElements: activeData.totalElements,
    refetch,
    error,
  };
}
