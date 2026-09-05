import { userTaskService } from '@/api/domains';
import { AssignTaskRequest } from '@/api/types/orchest-api';
import { queryKeys } from '@/shared/constants/queryKeys';
import { useApiMutation, useQueryClient } from '@/shared/hooks';
import { TASK_MESSAGES } from '../constants/messages';

export interface AssignTaskParams {
  taskId: string;
  assignee: string;
}

export const useAssignTask = () => {
  const queryClient = useQueryClient();

  return useApiMutation(
    ({ taskId, assignee }: AssignTaskParams) => {
      const request: AssignTaskRequest = { assignee };
      return userTaskService.assignTask(taskId, request);
    },
    {
      showSuccessToast: true,
      successMessage: TASK_MESSAGES.SUCCESS.TASK_ASSIGNED,
      showErrorToast: true,
      onSuccess: async (updatedTask, params) => {
        queryClient.setQueryData(queryKeys.userTasks.detail(params.taskId), updatedTask);
        await queryClient.invalidateQueries({ queryKey: queryKeys.userTasks.all });
      },
    }
  );
};

export type UseAssignTaskReturn = ReturnType<typeof useAssignTask>;
