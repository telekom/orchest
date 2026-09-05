import { decisionInstanceService } from '@/api/domains';
import { DecisionInstanceScrollDTO } from '@/api/types/orchest-api';
import { toast } from '@/design-system/components/ui/sonner';
import { QUERY_CONFIG } from '@/shared/constants/apiConfig';
import { queryKeys } from '@/shared/constants/queryKeys';
import { mapDomainToContextDecisionInstance } from '@/shared/utils/typeMapping';
import { useInfiniteQuery } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { useCallback, useEffect, useMemo } from 'react';
import type { DecisionFilters, DecisionInstance } from '../types';

const RETRYABLE_STATUS_CODES = new Set([408, 429]);
const ERROR_TOAST_ID = 'infinite-decision-instances-error';

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

export interface InfiniteDecisionInstancesResult {
  instances: DecisionInstance[];
  isLoading: boolean;
  isFetchingNextPage: boolean;
  hasNextPage: boolean;
  totalLoaded: number;
  loadMore: () => void;
  refetch: () => void;
  error?: Error;
}

interface UseInfiniteDecisionInstancesOptions {
  filters: DecisionFilters;
  pageSize: number;
  /** Sort param like "-executedAt"; defaults to "-executedAt" when omitted. */
  sort?: string;
}

const DEFAULT_SORT = '-executedAt';

export function useInfiniteDecisionInstances({
  filters,
  pageSize,
  sort,
}: UseInfiniteDecisionInstancesOptions): InfiniteDecisionInstancesResult {
  const sortParam = sort ?? DEFAULT_SORT;

  const queryKey = queryKeys.decisionInstances.scroll({
    decisionId: filters.decisionId || null,
    version: filters.version || null,
    status: filters.status || null,
    searchText: filters.searchText || null,
    from: filters.from || null,
    to: filters.to || null,
    timezone: filters.timezone || null,
    pageSize,
    sort: sortParam,
  });

  const query = useInfiniteQuery<DecisionInstanceScrollDTO>({
    queryKey,
    initialPageParam: 0,
    queryFn: async ({ pageParam }) => {
      const offset = pageParam as number;
      return decisionInstanceService.scrollDecisionInstances({
        decisionId: filters.decisionId || undefined,
        version: filters.version ? Number.parseInt(filters.version, 10) : undefined,
        state: (filters.status as 'EVALUATED' | 'FAILED' | 'UNKNOWN') || undefined,
        searchText: filters.searchText || undefined,
        executedFrom: filters.from || undefined,
        executedTo: filters.to || undefined,
        timezone: filters.timezone || undefined,
        from: offset,
        to: offset + pageSize,
        sort: sortParam,
      });
    },
    getNextPageParam: (lastPage) => (lastPage?.hasNext ? lastPage.to : undefined),
    retry: shouldRetryRequest,
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

  const instances = useMemo<DecisionInstance[]>(() => {
    if (!data?.pages?.length) return [];
    const result: DecisionInstance[] = [];
    for (const page of data.pages) {
      const content = page?.content ?? [];
      for (const item of content) {
        result.push(mapDomainToContextDecisionInstance(item));
      }
    }
    return result;
  }, [data]);

  const totalLoaded = instances.length;

  useEffect(() => {
    if (!error) return;
    toast.error('Failed to load decision instances. Please try again.', { id: ERROR_TOAST_ID });
  }, [error]);

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
    totalLoaded,
    loadMore,
    refetch: wrappedRefetch,
    error: error as Error | undefined,
  };
}
