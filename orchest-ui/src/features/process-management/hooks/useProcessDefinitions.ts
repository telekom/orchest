import { processDefinitionService } from "@/api/domains";
import { queryKeys } from "@/shared/constants/queryKeys";
import { useDefinitions } from "@/shared/hooks/useDefinitions";

export function useProcessDefinitions() {
  const { definitions, isLoading, refetch, error } = useDefinitions({
    queryKey: queryKeys.processDefinitions.list({ size: 1000 }),
    fetchFn: () => processDefinitionService.getProcessDefinitions({ size: 1000 }),
    showErrorToast: false,
    enabled: true,
  });

  return {
    processDefinitions: definitions,
    isLoading,
    refetch,
    error,
  };
}
