import { userTaskService } from '@/api/domains';
import { CompleteTaskRequest } from '@/api/types/orchest-api';
import { queryKeys } from '@/shared/constants/queryKeys';
import { useApiMutation } from '@/shared/hooks';
import { TASK_MESSAGES } from '../constants/messages';

export interface CompleteTaskParams {
  taskId: string;
  variables?: Record<string, unknown>;
}

export const useCompleteTask = () => {
  return useApiMutation(
    ({ taskId, variables }: CompleteTaskParams) => {
      const request: CompleteTaskRequest = { variables };
      return userTaskService.completeTask(taskId, request);
    },
    {
      showSuccessToast: true,
      successMessage: TASK_MESSAGES.SUCCESS.TASK_COMPLETED,
      showErrorToast: true,
      invalidateQueries: [queryKeys.userTasks.all, queryKeys.processInstances.all],
    }
  );
};

export type UseCompleteTaskReturn = ReturnType<typeof useCompleteTask>;
