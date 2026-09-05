import { processInstanceService, ProcessInstanceState } from "@/api/domains";
import { queryKeys } from "@/shared/constants/queryKeys";
import { TIMING } from "@/shared/constants";
import { useApiQuery } from "@/shared/hooks";
import {
    getStringProperty,
    isObject,
    isStateChangeArray,
} from "@/shared/utils/typeGuards";
import { mapDomainToContextProcessInstance } from "@/shared/utils/typeMapping";
import { transformVariablesToArray } from "@/shared/utils/variableUtils";
import type { ProcessInstanceDTO, SequenceExecution } from "@/api/types/orchest-api";
import { useCallback, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import type { ProcessInstance, ProcessVariable, Task } from "../types/processInstance";

const STOP_POLLING_STATES = new Set([
  "COMPLETED",
  "CANCELLED",
  "TERMINATED",
]);

const shouldPollProcessDetails = (state: string | undefined): boolean => {
  if (!state) return true;
  return !STOP_POLLING_STATES.has(state.toUpperCase());
};

export const useProcessInstanceQuery = (processId: string | undefined) => {
  const navigate = useNavigate();

  const handleError = useCallback(() => {
    setTimeout(() => navigate("/"), TIMING.NAVIGATION_REDIRECT_DELAY);
  }, [navigate]);

  const { data: instanceData, isLoading: loading, error, refetch } = useApiQuery(
    queryKeys.processInstances.detail(processId || ''),
    async () => {
      if (!processId) throw new Error('No process ID');
      return await processInstanceService.getProcessInstance(processId);
    },
    {
      enabled: !!processId,
      showErrorToast: false,
      retryOnError: false,
      onError: handleError,
      refetchInterval: (query) => {
        const state = (query.state.data as ProcessInstanceDTO | undefined)?.state;
        return shouldPollProcessDetails(state)
          ? TIMING.PROCESS_DETAILS_POLL_INTERVAL
          : false;
      },
    }
  );

  const instance = useMemo<ProcessInstance | null>(() => {
    if (!instanceData) return null;

    const mappedSequenceExecutions = instanceData.sequenceExecutions
      ? Object.fromEntries(
          Object.entries(instanceData.sequenceExecutions).map(([key, value]) => {
            if (!value || typeof value !== "object") {
              return [
                key,
                {
                  nodeId: key,
                  nodeName: "Unknown",
                  nodeType: "UNKNOWN" as const,
                  state: "UNKNOWN" as const,
                  stateChanges: [],
                  metaData: {},
                },
              ];
            }

            const entry = value as Record<string, unknown>;
            const metaData =
              entry.metaData && typeof entry.metaData === "object"
                ? (entry.metaData as Record<string, unknown>)
                : {};

            return [
              key,
              {
                nodeId: (typeof entry.nodeId === "string" && entry.nodeId) || key,
                nodeName:
                  (typeof entry.nodeName === "string" && entry.nodeName) || "Unknown",
                nodeType: (entry.nodeType as SequenceExecution["nodeType"]) || ("UNKNOWN" as const),
                state: (entry.state as SequenceExecution["state"]) || ("UNKNOWN" as const),
                stateChanges: Array.isArray(entry.stateChanges) ? entry.stateChanges : [],
                metaData,
                sourceNodeId:
                  typeof entry.sourceNodeId === "string" ? entry.sourceNodeId : undefined,
                sequenceFlowIds: Array.isArray(entry.sequenceFlowIds)
                  ? (entry.sequenceFlowIds as string[])
                  : undefined,
              } satisfies SequenceExecution,
            ];
          })
        )
      : {};

    const domainInstance = {
      processInstanceId: instanceData.processInstanceId,
      parentProcessInstanceId: instanceData.parentProcessInstanceId,
      processDefinitionId: instanceData.processDefinitionId,
      version:
        typeof instanceData.version === "number"
          ? instanceData.version
          : Number(instanceData.version) || 1,
      state: instanceData.state as ProcessInstanceState,
      createdAt: instanceData.createdAt,
      completedAt: instanceData.completedAt ?? undefined,
      incidentMessage: instanceData.incidentMessage ?? undefined,
      sequenceExecutions: mappedSequenceExecutions,
      variables: instanceData.variables || {},
      bpmnXML: instanceData.bpmnXML,
      lastActivityAt: instanceData.lastActivityAt,
      activeElements: instanceData.activeElements,
    };

    return mapDomainToContextProcessInstance(domainInstance);
  }, [instanceData]);

  const variables = useMemo<ProcessVariable[]>(() => {
    if (!instanceData?.variables) return [];
    return transformVariablesToArray(instanceData.variables, "global") as ProcessVariable[];
  }, [instanceData]);

  const tasks = useMemo<Task[]>(() => {
    if (!instanceData?.sequenceExecutions) return [];

    const history: Task[] = [];
    const seqExec = instanceData.sequenceExecutions;

    Object.entries(seqExec).forEach(([key, value]) => {
      if (!isObject(value)) return;

      const nodeName = getStringProperty(value, 'nodeName');
      const nodeType = getStringProperty(value, 'nodeType');
      const stateChanges = isStateChangeArray(value.stateChanges)
        ? value.stateChanges
        : undefined;
      const state = getStringProperty(value, 'state');

      history.push({
        taskId: String(key),
        taskName: nodeName || "Unknown Task",
        status: state || "RUNNING",
        nodeType,
        timestamp: stateChanges && stateChanges.length > 0
          ? stateChanges
          : [{ timestamp: new Date().toISOString(), state: "RUNNING" }],
      });
    });

    return history;
  }, [instanceData]);

  const diagramXml = useMemo(() => instanceData?.bpmnXML || "", [instanceData]);

  const variableContext = useMemo<Record<string, unknown>>(
    () => (instanceData?.variables && typeof instanceData.variables === "object"
      ? (instanceData.variables as Record<string, unknown>)
      : {}),
    [instanceData]
  );

  return {
    instance,
    loading,
    error,
    diagramXml,
    tasks,
    variables,
    variableContext,
    fetchInstance: refetch,
  };
};
