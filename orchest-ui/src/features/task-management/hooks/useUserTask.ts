import { userTaskService } from '@/api/domains';
import { queryKeys } from '@/shared/constants/queryKeys';
import { useApiQuery } from '@/shared/hooks';

export const useUserTask = (
  taskId: string,
  options?: {
    enabled?: boolean;
    staleTime?: number;
  }
) => {
  const queryKey = queryKeys.userTasks.detail(taskId);

  return useApiQuery(queryKey, () => userTaskService.getUserTask(taskId), {
    staleTime: options?.staleTime ?? 10000,
    showErrorToast: true,
    retryOnError: true,
    enabled: options?.enabled ?? !!taskId,
  });
};

export type UseUserTaskReturn = ReturnType<typeof useUserTask>;
