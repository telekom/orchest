import {
  sensitiveVariablesService,
  type ProcessSensitiveVariables,
  type SensitiveVariable,
} from '@/api/domains/sensitive-variables';
import { processDefinitionService } from '@/api/domains';
import { useApiMutation, useApiQuery } from '@/shared/hooks';
import { useCallback, useMemo, useState } from 'react';

export function useSensitiveVariables() {
  const [selectedProcessId, setSelectedProcessId] = useState<string>('');

  const { data: definitionsResponse } = useApiQuery(
    ['processDefinitions', 'sensitiveVarSelector'],
    () => processDefinitionService.getProcessDefinitions({ size: 1000 }),
    { staleTime: 60000 }
  );

  const uniqueProcessIds = useMemo(() => {
    const content = definitionsResponse?.content ?? definitionsResponse?.data ?? [];
    if (!Array.isArray(content)) return [];
    const ids = content.map((d: Record<string, unknown>) => String(d.definitionId || d.processDefinitionId || d.id || ''));
    return [...new Set(ids)].filter(Boolean).sort();
  }, [definitionsResponse]);

  const { data: processConfigs = [], isLoading: loadingVariables, refetch } = useApiQuery(
    ['sensitiveVariables', selectedProcessId],
    () => sensitiveVariablesService.getByProcessDefinitionId(selectedProcessId),
    {
      enabled: !!selectedProcessId,
      showErrorToast: true,
      staleTime: 15000,
    }
  );

  const currentConfig = useMemo(() => {
    return processConfigs.length > 0 ? processConfigs[0] : null;
  }, [processConfigs]);

  const variables = useMemo(() => {
    return currentConfig?.variables ?? [];
  }, [currentConfig]);

  const { mutateAsync: saveConfig, isPending: saving } = useApiMutation(
    (data: ProcessSensitiveVariables) => {
      if (data.id) {
        return sensitiveVariablesService.update(data);
      }
      return sensitiveVariablesService.add(data);
    },
    {
      invalidateQueries: [['sensitiveVariables', selectedProcessId]],
      showSuccessToast: true,
      successMessage: 'Sensitive variables saved successfully',
    }
  );

  const { mutateAsync: deleteConfig, isPending: deleting } = useApiMutation(
    (data: ProcessSensitiveVariables) => sensitiveVariablesService.delete(data),
    {
      invalidateQueries: [['sensitiveVariables', selectedProcessId]],
      showSuccessToast: true,
      successMessage: 'Sensitive variable configuration deleted',
    }
  );

  const addVariable = useCallback(async (variable: SensitiveVariable) => {
    const updatedVariables = [...variables, variable];
    const payload: ProcessSensitiveVariables = {
      ...(currentConfig?.id ? { id: currentConfig.id } : {}),
      processDefinitionId: selectedProcessId,
      variables: updatedVariables,
      enabled: currentConfig?.enabled ?? true,
    };
    await saveConfig(payload);
  }, [variables, currentConfig, selectedProcessId, saveConfig]);

  const updateVariable = useCallback(async (index: number, variable: SensitiveVariable) => {
    const updatedVariables = [...variables];
    updatedVariables[index] = variable;
    const payload: ProcessSensitiveVariables = {
      ...(currentConfig?.id ? { id: currentConfig.id } : {}),
      processDefinitionId: selectedProcessId,
      variables: updatedVariables,
      enabled: currentConfig?.enabled ?? true,
    };
    await saveConfig(payload);
  }, [variables, currentConfig, selectedProcessId, saveConfig]);

  const deleteVariable = useCallback(async (index: number) => {
    const updatedVariables = variables.filter((_, i) => i !== index);
    if (updatedVariables.length === 0 && currentConfig) {
      await deleteConfig(currentConfig);
    } else {
      const payload: ProcessSensitiveVariables = {
        ...(currentConfig?.id ? { id: currentConfig.id } : {}),
        processDefinitionId: selectedProcessId,
        variables: updatedVariables,
        enabled: currentConfig?.enabled ?? true,
      };
      await saveConfig(payload);
    }
  }, [variables, currentConfig, selectedProcessId, saveConfig, deleteConfig]);

  const toggleEnabled = useCallback(async () => {
    if (!currentConfig) return;
    const payload: ProcessSensitiveVariables = {
      ...currentConfig,
      enabled: !currentConfig.enabled,
    };
    await saveConfig(payload);
  }, [currentConfig, saveConfig]);

  const handleProcessChange = useCallback((processId: string) => {
    setSelectedProcessId(processId);
  }, []);

  return {
    selectedProcessId,
    uniqueProcessIds,
    variables,
    currentConfig,
    loadingVariables,
    saving,
    deleting,
    handleProcessChange,
    addVariable,
    updateVariable,
    deleteVariable,
    toggleEnabled,
    refetch,
  };
}