/**
 * Extracts child instance information from sequence executions
 */

import type { SequenceExecution } from "@/api/types/orchest-api";

export interface ChildInstanceRef {
  instanceId: string;
  childType: "process" | "decision";
}

export interface ChildInstanceInfo {
  hasChild: boolean;
  children: ChildInstanceRef[];
}

function normalizeChildProcessIds(meta?: SequenceExecution["metaData"]): string[] {
  if (!meta) return [];
  const plural = meta.childProcessInstanceIds;
  if (Array.isArray(plural) && plural.length > 0) {
    return plural;
  }
  const single = meta.childProcessInstanceId;
  if (typeof single === "string" && single.length > 0) {
    return [single];
  }
  return [];
}

function normalizeDecisionIds(meta?: SequenceExecution["metaData"]): string[] {
  if (!meta) return [];
  const fromIds = meta.decisionInstanceIds;
  if (Array.isArray(fromIds) && fromIds.length > 0) {
    return fromIds;
  }
  const fromId = meta.decisionInstanceId;
  if (Array.isArray(fromId) && fromId.length > 0) {
    return fromId;
  }
  return [];
}

/**
 * Extracts child instance information for a given element from sequence executions.
 * Prefers child process IDs (childProcessInstanceIds, then childProcessInstanceId); otherwise uses decision instance IDs.
 * Aggregates and deduplicates across all matching executions.
 */
export const extractChildInstanceInfo = (
  elementId: string,
  sequenceExecutions?: Record<string, SequenceExecution>
): ChildInstanceInfo => {
  const empty: ChildInstanceInfo = {
    hasChild: false,
    children: [],
  };

  if (!sequenceExecutions) return empty;

  const seen = new Set<string>();
  const children: ChildInstanceRef[] = [];

  const pushUnique = (instanceId: string, childType: "process" | "decision") => {
    const key = `${childType}:${instanceId}`;
    if (seen.has(key)) return;
    seen.add(key);
    children.push({ instanceId, childType });
  };

  for (const execution of Object.values(sequenceExecutions)) {
    if (execution.nodeId !== elementId) continue;

    const processIds = normalizeChildProcessIds(execution.metaData);
    if (processIds.length > 0) {
      for (const id of processIds) {
        pushUnique(id, "process");
      }
      continue;
    }

    const decisionIds = normalizeDecisionIds(execution.metaData);
    for (const id of decisionIds) {
      pushUnique(id, "decision");
    }
  }

  return {
    hasChild: children.length > 0,
    children,
  };
};

/**
 * Extracts execution status for a given element
 */
export const extractExecutionStatus = (
  elementId: string,
  sequenceExecutions?: Record<string, SequenceExecution>
): string => {
  if (!sequenceExecutions) return "Unknown";

  for (const execution of Object.values(sequenceExecutions)) {
    if (execution.nodeId === elementId) {
      return execution.state || "Unknown";
    }
  }

  return "Unknown";
};

function normalizeUserTaskIds(meta?: SequenceExecution["metaData"]): string[] {
  if (!meta) return [];

  const candidates = [meta.userTaskId, meta.userTaskIds];
  const ids: string[] = [];

  for (const value of candidates) {
    if (typeof value === "string" && value.length > 0) {
      ids.push(value);
      continue;
    }
    if (Array.isArray(value)) {
      for (const id of value) {
        if (typeof id === "string" && id.length > 0) {
          ids.push(id);
        }
      }
    }
  }

  // Case-insensitive fallback for alternate backend key spellings
  for (const [key, value] of Object.entries(meta)) {
    if (key.toLowerCase() !== "usertaskid" && key.toLowerCase() !== "usertaskids") {
      continue;
    }
    if (typeof value === "string" && value.length > 0) {
      ids.push(value);
    } else if (Array.isArray(value)) {
      for (const id of value) {
        if (typeof id === "string" && id.length > 0) {
          ids.push(id);
        }
      }
    }
  }

  return [...new Set(ids)];
}

const isUserTaskNodeType = (nodeType?: string): boolean => {
  if (!nodeType) return false;
  const normalized = nodeType.toUpperCase().replace(/^BPMN:/, "");
  return normalized === "USER_TASK" || normalized === "USERTASK";
};

/**
 * Extracts user task IDs from sequence execution metadata for a BPMN element.
 * Reads `metaData.userTaskId` (string or string[]).
 */
export const extractUserTaskIds = (
  elementId: string,
  sequenceExecutions?: Record<string, SequenceExecution>
): string[] => {
  if (!sequenceExecutions) return [];

  const seen = new Set<string>();
  const ids: string[] = [];

  for (const execution of Object.values(sequenceExecutions)) {
    if (execution.nodeId !== elementId) continue;
    for (const id of normalizeUserTaskIds(execution.metaData)) {
      if (seen.has(id)) continue;
      seen.add(id);
      ids.push(id);
    }
  }

  return ids;
};

/**
 * Returns all unique userTaskId values found across sequence executions.
 */
export const extractAllUserTaskIds = (
  sequenceExecutions?: Record<string, SequenceExecution>
): string[] => {
  if (!sequenceExecutions) return [];

  const seen = new Set<string>();
  const ids: string[] = [];

  for (const execution of Object.values(sequenceExecutions)) {
    for (const id of normalizeUserTaskIds(execution.metaData)) {
      if (seen.has(id)) continue;
      seen.add(id);
      ids.push(id);
    }
  }

  return ids;
};

/** True when any sequence execution is a user task or includes a userTaskId. */
export const hasUserTasksInSequenceExecutions = (
  sequenceExecutions?: Record<string, SequenceExecution>
): boolean => {
  if (!sequenceExecutions) return false;

  for (const execution of Object.values(sequenceExecutions)) {
    if (isUserTaskNodeType(execution.nodeType)) return true;
    if (normalizeUserTaskIds(execution.metaData).length > 0) return true;
  }

  return false;
};
