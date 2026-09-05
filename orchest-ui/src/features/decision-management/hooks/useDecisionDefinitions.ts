import { decisionInstanceService } from "@/api/domains";
import { queryKeys } from "@/shared/constants/queryKeys";
import { useDefinitions } from "@/shared/hooks/useDefinitions";

export function useDecisionDefinitions() {
  const { definitions, isLoading, refetch, error } = useDefinitions({
    queryKey: [...queryKeys.decisionDefinitions.list({ page: 0, size: 1000 })],
    fetchFn: () => decisionInstanceService.getDecisionIDs(),
    showErrorToast: false,
    enabled: true,
  });

  return {
    decisionDefinitions: definitions,
    isLoading,
    refetch,
    error,
  };
}
