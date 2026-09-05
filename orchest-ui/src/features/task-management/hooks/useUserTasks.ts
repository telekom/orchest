import { userTaskService } from '@/api/domains';
import { UserTaskDTO, UserTaskQueryParams } from '@/api/types/orchest-api';
import { queryKeys } from '@/shared/constants/queryKeys';
import { useApiQuery } from '@/shared/hooks';

/**
 * Sort tasks client-side to ensure stable ordering after claim/unclaim
 * operations, which can cause the API to return tasks in a different order
 * (e.g. due to lastModifiedAt changes).
 */
function sortTasks(tasks: UserTaskDTO[], sort?: string): UserTaskDTO[] {
  if (!sort || tasks.length === 0) return tasks;

  const isDescending = sort.startsWith('-');
  const field = sort.replace(/^[+-]/, '') as keyof UserTaskDTO;

  return [...tasks].sort((a, b) => {
    const valA = a[field];
    const valB = b[field];

    if (valA == null && valB == null) return 0;
    if (valA == null) return 1;
    if (valB == null) return -1;

    const comparison = String(valA).localeCompare(String(valB));
    return isDescending ? -comparison : comparison;
  });
}

export const useUserTasks = (
  params?: UserTaskQueryParams,
  options?: {
    enabled?: boolean;
    staleTime?: number;
  }
) => {
  const queryKey = queryKeys.userTasks.list(params);

  return useApiQuery(
    queryKey,
    async () => {
      const response = await userTaskService.getUserTasks(params);
      return sortTasks(response.content, params?.sort);
    },
    {
      staleTime: options?.staleTime ?? 30000,
      showErrorToast: true,
      retryOnError: true,
      enabled: options?.enabled ?? true,
    }
  );
};

export type UseUserTasksReturn = ReturnType<typeof useUserTasks>;
