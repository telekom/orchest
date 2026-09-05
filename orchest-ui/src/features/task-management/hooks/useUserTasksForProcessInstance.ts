import { userTaskService } from '@/api/domains';
import { UserTaskDTO } from '@/api/types/orchest-api';
import { queryKeys } from '@/shared/constants/queryKeys';
import { useApiQuery } from '@/shared/hooks';
import { logger } from '@/shared/utils/logger';

/**
 * Fetches user tasks by explicit IDs (from sequence execution metaData.userTaskId).
 * Falls back to process-instance list when no IDs are provided.
 */
export const useUserTasksForProcessInstance = (
  processInstanceId: string,
  userTaskIds: string[] = [],
  options?: {
    enabled?: boolean;
    staleTime?: number;
  }
) => {
  const sortedIds = [...userTaskIds].sort();
  const queryKey = [
    ...queryKeys.userTasks.byProcess(processInstanceId),
    'byIds',
    sortedIds,
  ] as const;

  return useApiQuery(
    queryKey,
    async (): Promise<UserTaskDTO[]> => {
      if (sortedIds.length > 0) {
        const results = await Promise.allSettled(
          sortedIds.map((taskId) => userTaskService.getUserTask(taskId))
        );

        const tasks: UserTaskDTO[] = [];
        results.forEach((result, index) => {
          if (result.status === 'fulfilled' && result.value) {
            tasks.push(result.value);
            return;
          }
          logger.error(
            `Failed to load user task ${sortedIds[index]}:`,
            result.status === 'rejected' ? result.reason : undefined
          );
        });

        if (tasks.length > 0) {
          return tasks;
        }
      }

      // Fallback when IDs are missing or individual fetches failed
      return userTaskService.getUserTasksByProcess(processInstanceId);
    },
    {
      staleTime: options?.staleTime ?? 20000,
      showErrorToast: true,
      retryOnError: true,
      enabled: options?.enabled ?? !!processInstanceId,
    }
  );
};

export type UseUserTasksForProcessInstanceReturn = ReturnType<
  typeof useUserTasksForProcessInstance
>;
