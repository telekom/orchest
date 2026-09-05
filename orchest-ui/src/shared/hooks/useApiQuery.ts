import { QUERY_CONFIG } from '@/shared/constants/apiConfig';
import {
    QueryKey,
    useMutation,
    UseMutationOptions,
    useQuery,
    useQueryClient,
    UseQueryOptions,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { useCallback, useEffect, useRef } from 'react';
import { toast } from '@/design-system/components/ui/sonner';
import { useStableCallback } from './useStableCallback';

const RETRYABLE_STATUS_CODES = [408, 429];
const DEFAULT_SUCCESS_MESSAGE = 'Operation completed successfully';

const getToastId = (queryKey: QueryKey): string => JSON.stringify(queryKey);

const dismissToastIfNeeded = (queryKey: QueryKey, shouldDismiss: boolean) => {
  if (shouldDismiss) {
    toast.dismiss(getToastId(queryKey));
  }
};

const shouldRetryRequest = (failureCount: number, error: unknown): boolean => {
  if (error && typeof error === 'object' && 'response' in error) {
    const status = (error as AxiosError).response?.status;
    if (status) {
      if (status >= 400 && status < 500 && !RETRYABLE_STATUS_CODES.includes(status)) {
        return false;
      }
      if (status >= 500 && status < 600) {
        return failureCount < QUERY_CONFIG.RETRY_ATTEMPTS;
      }
    }
  }
  return failureCount < QUERY_CONFIG.RETRY_ATTEMPTS;
};

const getRetryDelay = (attemptIndex: number, customDelay?: number): number => {
  if (customDelay !== undefined) return customDelay;
  return Math.min(1000 * Math.pow(2, attemptIndex), 10000);
};


export interface ApiQueryOptions<TData, TError = AxiosError>
  extends Omit<UseQueryOptions<TData, TError>, 'queryFn' | 'queryKey'> {
  errorContext?: Record<string, unknown>;
  showErrorToast?: boolean;
  retryOnError?: boolean;
  showLoadingToast?: boolean;
  onError?: (error: TError) => void;
  onSuccess?: (data: TData) => void;
  retryDelay?: number;
}

export interface ApiMutationOptions<TData, TError = AxiosError, TVariables = unknown>
  extends Omit<UseMutationOptions<TData, TError, TVariables>, 'mutationFn'> {
  errorContext?: Record<string, unknown>;
  showErrorToast?: boolean;
  showSuccessToast?: boolean;
  successMessage?: string;
  invalidateQueries?: QueryKey[];
}


export function useApiQuery<TData>(
  queryKey: QueryKey,
  queryFn: () => Promise<TData>,
  options: ApiQueryOptions<TData> = {}
) {
  const {
    retryOnError = true,
    showLoadingToast = false,
    retryDelay,
    onError: customOnError,
    onSuccess: customOnSuccess,
    ...queryOptions
  } = options;

  const queryKeyRef = useRef(queryKey);
  queryKeyRef.current = queryKey;

  const enhancedQueryFn = useCallback(async (): Promise<TData> => {
    const key = queryKeyRef.current;
    if (showLoadingToast) {
      toast.loading('Loading...', { id: getToastId(key), duration: Infinity });
    }

    try {
      const result = await queryFn();
      dismissToastIfNeeded(key, showLoadingToast);
      return result;
    } catch (error) {
      dismissToastIfNeeded(key, showLoadingToast);
      throw error;
    }
  }, [queryFn, showLoadingToast]);

  const retryConfig = retryOnError ? shouldRetryRequest : false;

  const result = useQuery({
    queryKey,
    queryFn: enhancedQueryFn,
    retry: retryConfig,
    retryDelay: (attemptIndex) => getRetryDelay(attemptIndex, retryDelay),
    ...queryOptions
  });

  const stableOnError = useStableCallback(customOnError);
  const stableOnSuccess = useStableCallback(customOnSuccess);

  useEffect(() => {
    if (result.error && stableOnError) {
      stableOnError(result.error);
    }
  }, [result.error, stableOnError]);

  useEffect(() => {
    if (result.data && result.isSuccess && stableOnSuccess) {
      stableOnSuccess(result.data);
    }
  }, [result.data, result.isSuccess, stableOnSuccess]);

  return result;
}


export function useApiMutation<TData, TVariables = unknown>(
  mutationFn: (variables: TVariables) => Promise<TData>,
  options: ApiMutationOptions<TData, AxiosError, TVariables> = {}
) {
  const {
    showSuccessToast = false,
    successMessage = DEFAULT_SUCCESS_MESSAGE,
    invalidateQueries = [],
    ...mutationOptions
  } = options;

  const queryClient = useQueryClient();

  const enhancedMutationFn = useCallback(async (variables: TVariables): Promise<TData> => {
    try {
      const result = await mutationFn(variables);

      if (showSuccessToast) {
        toast.success(successMessage);
      }

      if (invalidateQueries.length > 0) {
        invalidateQueries.forEach(queryKey => {
          queryClient.invalidateQueries({ queryKey });
        });
      }

      return result;
    } catch (error) {
      throw error;
    }
  }, [mutationFn, showSuccessToast, successMessage, invalidateQueries, queryClient]);

  return useMutation({
    mutationFn: enhancedMutationFn,
    ...mutationOptions
  });
}


export {
    useQueryClient,
    type QueryKey, type UseMutationOptions, type UseQueryOptions
};

