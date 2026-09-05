import { userTaskService } from '@/api/domains';
import { queryKeys } from '@/shared/constants/queryKeys';
import { useApiMutation, useQueryClient } from '@/shared/hooks';
import { TASK_MESSAGES } from '../constants/messages';

export const useUnclaimTask = () => {
  const queryClient = useQueryClient();

  return useApiMutation(
    (taskId: string) => userTaskService.unclaimTask(taskId),
    {
      showSuccessToast: true,
      successMessage: TASK_MESSAGES.SUCCESS.TASK_UNCLAIMED,
      showErrorToast: true,
      onSuccess: async (updatedTask, taskId) => {
        queryClient.setQueryData(queryKeys.userTasks.detail(taskId), updatedTask);
        await queryClient.invalidateQueries({ queryKey: queryKeys.userTasks.all });
      },
    }
  );
};

export type UseUnclaimTaskReturn = ReturnType<typeof useUnclaimTask>;
