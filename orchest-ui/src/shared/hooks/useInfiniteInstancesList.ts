import { QUERY_CONFIG } from '@/shared/constants/apiConfig';
import { toast } from '@/design-system/components/ui/sonner';
import { useInfiniteQuery } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { useCallback, useEffect, useMemo } from 'react';
import { useStableCallback } from './useStableCallback';
import type { PaginatedResponse } from './useInstancesList';

const RETRYABLE_STATUS_CODES = new Set([408, 429]);

function identityMap<T>(item: T): T {
  return item;
}

function shouldRetryRequest(failureCount: number, error: unknown): boolean {
  if (error && typeof error === 'object' && 'response' in error) {
    const status = (error as AxiosError).response?.status;
    if (status) {
      if (status >= 400 && status < 500 && !RETRYABLE_STATUS_CODES.has(status)) {
        return false;
      }
      if (status >= 500 && status < 600) {
        return failureCount < QUERY_CONFIG.RETRY_ATTEMPTS;
      }
    }
  }
  return failureCount < QUERY_CONFIG.RETRY_ATTEMPTS;
}

export interface InfiniteFetchParams<TFilters> {
  filters: TFilters;
  pageNumber: number;
  pageSize: number;
}

export interface InfiniteInstancesListConfig<TData, TFilters, TMapped = TData> {
  /**
   * Query key. The page number is appended internally by `useInfiniteQuery`,
   * so callers should NOT include `page` in this key.
   */
  queryKey: readonly unknown[];
  fetchFn: (params: InfiniteFetchParams<TFilters>) => Promise<PaginatedResponse<TData>>;
  filters: TFilters;
  pageSize: number;
  mapFn?: (data: TData) => TMapped;
  /** Notified whenever the loaded set or totals change. */
  onPaginationUpdate?: (totalLoaded: number, totalElements: number) => void;
  showErrorToast?: boolean;
  enabled?: boolean;
}

export interface InfiniteInstancesListResult<T> {
  /** Flattened list of all loaded instances across pages. */
  instances: T[];
  /** True on the very first fetch when nothing is loaded yet. */
  isLoading: boolean;
  /** True while a follow-up page is being fetched (we already have data). */
  isFetchingNextPage: boolean;
  /** True if more pages can be loaded from the server. */
  hasNextPage: boolean;
  /** Total number of records that match the current filters on the server. */
  totalElements: number;
  /** Total number of records currently loaded into the client. */
  totalLoaded: number;
  /** Imperatively load the next page (no-op if not possible). */
  loadMore: () => void;
  /** Refetch from page 0 and reset the accumulated cache. */
  refetch: () => void;
  error?: unknown;
}

const ERROR_TOAST_ID = 'infinite-instances-list-error';

/**
 * Generic hook for infinite-scroll style paginated lists.
 *
 * Wraps TanStack Query's `useInfiniteQuery` so that pages are accumulated as the
 * user scrolls, while preserving the same fetcher contract used by
 * {@link useInstancesList}.
 *
 * The query key automatically includes the current page index, so callers should
 * pass a key that captures everything *except* the page number (filters, sort,
 * page size, etc.). When that key changes, the cache is reset and fetching
 * resumes from page 0 — which is exactly the behavior we want when filters or
 * sort change.
 */
export function useInfiniteInstancesList<TData, TFilters = Record<string, unknown>, TMapped = TData>({
  queryKey,
  fetchFn,
  filters,
  pageSize,
  mapFn,
  onPaginationUpdate,
  showErrorToast = true,
  enabled = true,
}: InfiniteInstancesListConfig<TData, TFilters, TMapped>): InfiniteInstancesListResult<TMapped> {
  const stableOnPaginationUpdate = useStableCallback(onPaginationUpdate);

  const query = useInfiniteQuery({
    queryKey,
    initialPageParam: 0,
    queryFn: async ({ pageParam }) => {
      const response = await fetchFn({
        filters,
        pageNumber: pageParam,
        pageSize,
      });

      if (!response) {
        return {
          content: [],
          page: { totalPages: 0, totalElements: 0 },
        };
      }

      return response;
    },
    getNextPageParam: (lastPage, allPages) => {
      const totalPages = lastPage?.page?.totalPages ?? 0;
      const nextPageIndex = allPages.length;
      return nextPageIndex < totalPages ? nextPageIndex : undefined;
    },
    retry: shouldRetryRequest,
    enabled,
  });

  const {
    data,
    isLoading,
    isFetchingNextPage,
    hasNextPage,
    fetchNextPage,
    refetch,
    error,
  } = query;

  const instances = useMemo<TMapped[]>(() => {
    if (!data?.pages?.length) return [];
    const effectiveMapFn = mapFn ?? (identityMap as (data: TData) => TMapped);
    const result: TMapped[] = [];
    for (const page of data.pages) {
      const content = page?.content ?? [];
      for (const item of content) {
        result.push(effectiveMapFn(item));
      }
    }
    return result;
  }, [data, mapFn]);

  const totalElements = data?.pages?.[0]?.page?.totalElements ?? 0;
  const totalLoaded = instances.length;

  useEffect(() => {
    if (!isLoading && data) {
      stableOnPaginationUpdate?.(totalLoaded, totalElements);
    }
  }, [data, isLoading, totalLoaded, totalElements, stableOnPaginationUpdate]);

  useEffect(() => {
    if (!error || !showErrorToast) return;
    toast.error('Failed to load data. Please try again.', { id: ERROR_TOAST_ID });
  }, [error, showErrorToast]);

  const loadMore = useCallback(() => {
    if (!hasNextPage || isFetchingNextPage || isLoading) return;
    void fetchNextPage();
  }, [hasNextPage, isFetchingNextPage, isLoading, fetchNextPage]);

  const wrappedRefetch = useCallback(() => {
    void refetch();
  }, [refetch]);

  return {
    instances,
    isLoading,
    isFetchingNextPage,
    hasNextPage: !!hasNextPage,
    totalElements,
    totalLoaded,
    loadMore,
    refetch: wrappedRefetch,
    error,
  };
}
