import {
  processEnvVariablesService,
  type EnvVariableType,
  type ProcessEnvVariable,
} from '@/api/domains/process-env-variables';
import { processDefinitionService } from '@/api/domains';
import { useApiMutation, useApiQuery } from '@/shared/hooks';
import { useCallback, useMemo, useState } from 'react';

export function useEnvVariables(variableType: EnvVariableType) {
  const [selectedProcessId, setSelectedProcessId] = useState<string>('');

  const { data: definitionsResponse } = useApiQuery(
    ['processDefinitions', 'envVarSelector'],
    () => processDefinitionService.getProcessDefinitions({ size: 1000 }),
    { staleTime: 60000 }
  );

  const uniqueProcessIds = useMemo(() => {
    const content = definitionsResponse?.content ?? definitionsResponse?.data ?? [];
    if (!Array.isArray(content)) return [];
    const ids = content.map((d: Record<string, unknown>) => String(d.definitionId || d.processDefinitionId || d.id || ''));
    return [...new Set(ids)].filter(Boolean).sort();
  }, [definitionsResponse]);

  const { data: variables = [], isLoading: loadingVariables, refetch } = useApiQuery(
    ['processEnvVariables', selectedProcessId, variableType],
    () => processEnvVariablesService.getByProcessDefinitionId(selectedProcessId),
    {
      enabled: !!selectedProcessId,
      showErrorToast: true,
      staleTime: 15000,
    }
  );

  const filteredVariables = useMemo(() => {
    return variables.filter((v: ProcessEnvVariable) => v.type === variableType);
  }, [variables, variableType]);

  const { mutateAsync: addVariable, isPending: adding } = useApiMutation(
    (variable: ProcessEnvVariable) => processEnvVariablesService.add(variable),
    {
      invalidateQueries: [['processEnvVariables', selectedProcessId, variableType]],
      showSuccessToast: true,
      successMessage: 'Variable added successfully',
    }
  );

  const { mutateAsync: updateVariable, isPending: updating } = useApiMutation(
    (variable: ProcessEnvVariable) => processEnvVariablesService.update(variable),
    {
      invalidateQueries: [['processEnvVariables', selectedProcessId, variableType]],
      showSuccessToast: true,
      successMessage: 'Variable updated successfully',
    }
  );

  const { mutateAsync: deleteVariable, isPending: deleting } = useApiMutation(
    (variable: ProcessEnvVariable) => processEnvVariablesService.remove(variable),
    {
      invalidateQueries: [['processEnvVariables', selectedProcessId, variableType]],
      showSuccessToast: true,
      successMessage: 'Variable deleted successfully',
    }
  );

  const handleProcessChange = useCallback((processId: string) => {
    setSelectedProcessId(processId);
  }, []);

  return {
    selectedProcessId,
    uniqueProcessIds,
    filteredVariables,
    loadingVariables,
    adding,
    updating,
    deleting,
    handleProcessChange,
    addVariable,
    updateVariable,
    deleteVariable,
    refetch,
  };
}
