import { deploymentApprovalsService } from '@/api/domains';
import type { ProcessDefinitionDTO } from '@/api/types/orchest-api';
import { useAuth } from '@/shared/auth';
import { queryKeys } from '@/shared/constants/queryKeys';
import { useApiMutation, useApiQuery } from './useApiQuery';

export interface UseDeputyManagementReturn {
  ownedProcesses: ProcessDefinitionDTO[];
  isLoading: boolean;
  error: Error | null;
  refetch: () => void;
  addDeputy: (definitionId: string, deputyEmail: string) => Promise<void>;
  removeDeputy: (definitionId: string, deputyEmail: string) => Promise<void>;
  isAddingDeputy: boolean;
  isRemovingDeputy: boolean;
}

export const useDeputyManagement = (): UseDeputyManagementReturn => {
  const { user } = useAuth();

  const {
    data: ownedProcesses = [],
    isLoading,
    error,
    refetch,
  } = useApiQuery(
    queryKeys.deploymentApprovals.definitions(),
    () => deploymentApprovalsService.getDefinitions(),
    {
      enabled: !!user?.email,
      showErrorToast: true,
    }
  );

  const addDeputyMutation = useApiMutation(
    (variables: { definitionId: string; deputyEmail: string }) => {
      const process = ownedProcesses.find((p) => p.processId === variables.definitionId);
      if (!process) {
        throw new Error('Process definition not found');
      }
      const updatedProcess = {
        ...process,
        approvers: [...process.approvers, variables.deputyEmail],
      };
      return deploymentApprovalsService.addApprovers(updatedProcess);
    },
    {
      showSuccessToast: true,
      successMessage: 'Approver added successfully',
      invalidateQueries: [queryKeys.deploymentApprovals.definitions()],
      onSuccess: () => {
        refetch();
      },
    }
  );

  const removeDeputyMutation = useApiMutation(
    (variables: { definitionId: string; deputyEmail: string }) =>
      deploymentApprovalsService.removeDeputy({
        definitionId: variables.definitionId,
        deputyEmail: variables.deputyEmail,
      }),
    {
      showSuccessToast: true,
      successMessage: 'Approver removed successfully',
      invalidateQueries: [queryKeys.deploymentApprovals.definitions()],
      onSuccess: () => {
        refetch();
      },
    }
  );

  const addDeputy = async (definitionId: string, deputyEmail: string) => {
    await addDeputyMutation.mutateAsync({ definitionId, deputyEmail });
  };

  const removeDeputy = async (definitionId: string, deputyEmail: string) => {
    await removeDeputyMutation.mutateAsync({ definitionId, deputyEmail });
  };

  return {
    ownedProcesses,
    isLoading,
    error: error as Error | null,
    refetch,
    addDeputy,
    removeDeputy,
    isAddingDeputy: addDeputyMutation.isPending,
    isRemovingDeputy: removeDeputyMutation.isPending,
  };
};
