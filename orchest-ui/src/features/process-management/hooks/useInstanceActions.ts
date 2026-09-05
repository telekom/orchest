import { processInstanceService } from "@/api/domains";
import {
  CancelInstanceRequest,
  CompensateInstanceRequest,
  RetryProcessEvent,
} from "@/api/types/orchest-api";
import { queryKeys } from "@/shared/constants/queryKeys";
import { useApiMutation } from "@/shared/hooks";

/**
 * Custom hook providing mutation operations for process instance actions.
 *
 * Provides:
 * - cancelInstance: Cancels one or more process instances
 * - retryInstance: Retries a failed/incident process instance from a specific activity
 * - compensateInstance: Starts a compensation process for an incident instance
 *
 * All mutations automatically:
 * - Display success/error toasts (except compensateInstance — caller shows toast with link)
 * - Invalidate relevant queries to refresh UI
 * - Handle errors via global error handler
 */
export const useInstanceActions = () => {
  const cancelInstance = useApiMutation(
    (request: CancelInstanceRequest) => processInstanceService.cancelInstance(request),
    {
      showSuccessToast: true,
      successMessage: 'Process instance cancelled successfully',
      invalidateQueries: [queryKeys.processInstances.all],
      showErrorToast: true,
    }
  );

  const retryInstance = useApiMutation(
    (request: RetryProcessEvent) => processInstanceService.retryInstance(request),
    {
      showSuccessToast: true,
      successMessage: 'Process instance retry initiated successfully',
      invalidateQueries: [queryKeys.processInstances.all],
      showErrorToast: true,
    }
  );

  const compensateInstance = useApiMutation(
    (request: CompensateInstanceRequest) => processInstanceService.compensate(request),
    {
      showSuccessToast: false,
      invalidateQueries: [queryKeys.processInstances.all],
      showErrorToast: true,
    }
  );

  return {
    cancelInstance,
    retryInstance,
    compensateInstance,
  };
};

export type UseInstanceActionsReturn = ReturnType<typeof useInstanceActions>;
