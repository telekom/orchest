import { userTaskService } from '@/api/domains';
import { queryKeys } from '@/shared/constants/queryKeys';
import { useApiQuery } from '@/shared/hooks';

/**
 * Custom hook for fetching all user tasks for a specific process instance
 *
 * Features:
 * - Automatic query caching and deduplication
 * - Stale time of 20 seconds
 * - Error handling with toast notifications
 * - Automatic retry on network failures
 *
 * @param processInstanceId - ID of the process instance
 * @param options - Additional query options
 * @returns Query result with tasks array for the process
 *
 * @example
 * ```typescript
 * const { data: tasks, isLoading } = useUserTasksByProcess('process-123');
 *
 * // Conditional fetching
 * const { data: tasks } = useUserTasksByProcess(processInstanceId, {
 *   enabled: !!processInstanceId
 * });
 * ```
 */
export const useUserTasksByProcess = (
  processInstanceId: string,
  options?: {
    enabled?: boolean;
    staleTime?: number;
  }
) => {
  const queryKey = queryKeys.userTasks.byProcess(processInstanceId);

  return useApiQuery(
    queryKey,
    () => userTaskService.getUserTasksByProcess(processInstanceId),
    {
      staleTime: options?.staleTime ?? 20000, // 20 seconds
      showErrorToast: true,
      retryOnError: true,
      enabled: options?.enabled ?? !!processInstanceId,
    }
  );
};

export type UseUserTasksByProcessReturn = ReturnType<typeof useUserTasksByProcess>;
