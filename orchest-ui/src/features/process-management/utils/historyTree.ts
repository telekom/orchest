import type { SequenceExecution } from "@/api/types/orchest-api";
import type { BpmnContainment } from "./bpmnContainment";

export interface HistoryNode {
  /** Stable react key. */
  key: string;
  nodeId: string;
  name: string;
  nodeType: string;
  state: string;
  startTime?: string;
  endTime?: string;
  /** All raw state transitions for this node, sorted ascending by time. */
  stateChanges: StateChange[];
  /** Nodes nested inside this one via an embedded subprocess. */
  children: HistoryNode[];
  /** Child process instance ids spawned by a call activity (lazy drill-down). */
  childInstanceIds: string[];
}

export interface HistoryRoot {
  id: string;
  name: string;
  state: string;
  startTime?: string;
  endTime?: string;
}

/** States that mean a node has finished (used to derive an end time). */
const TERMINAL_STATES = new Set([
  "COMPLETED",
  "CANCELLED",
  "TERMINATED",
  "FAILED",
  "SKIPPED",
]);

export type StateChange = { timestamp: string; state: string };

const isStateChange = (value: unknown): value is StateChange =>
  !!value &&
  typeof value === "object" &&
  typeof (value as StateChange).timestamp === "string";

const byTimestamp = (a: StateChange, b: StateChange) => {
  if (a.timestamp === b.timestamp) return 0;
  return a.timestamp < b.timestamp ? -1 : 1;
};

/** Returns the valid state transitions, sorted ascending by timestamp. */
function collectStateChanges(raw: unknown): StateChange[] {
  if (!Array.isArray(raw)) return [];
  return raw.filter(isStateChange).sort(byTimestamp);
}

function deriveTimes(changes: StateChange[]): { start?: string; end?: string } {
  let start: string | undefined;
  let end: string | undefined;

  for (const { timestamp, state } of changes) {
    if (!start || timestamp < start) start = timestamp;
    if (TERMINAL_STATES.has(state) && (!end || timestamp > end)) {
      end = timestamp;
    }
  }

  return { start, end };
}

function normalizeChildInstanceIds(meta: SequenceExecution["metaData"]): string[] {
  if (!meta) return [];
  const plural = meta.childProcessInstanceIds;
  if (Array.isArray(plural)) return plural.filter((id): id is string => typeof id === "string");
  const single = meta.childProcessInstanceId;
  return typeof single === "string" && single.length > 0 ? [single] : [];
}

function minTime(a?: string, b?: string): string | undefined {
  if (!a) return b;
  if (!b) return a;
  return a < b ? a : b;
}

function maxTime(a?: string, b?: string): string | undefined {
  if (!a) return b;
  if (!b) return a;
  return a > b ? a : b;
}

/**
 * Walks up the containment chain until it finds a container that actually has
 * an execution node, so nodes inside non-executed scopes still attach sensibly.
 */
function resolveParentKey(
  nodeId: string,
  parentOf: Map<string, string>,
  nodeMap: Map<string, HistoryNode>
): string | null {
  let parent = parentOf.get(nodeId);
  const guard = new Set<string>();
  while (parent && !guard.has(parent)) {
    if (nodeMap.has(parent)) return parent;
    guard.add(parent);
    parent = parentOf.get(parent);
  }
  return null;
}

function sortTree(node: HistoryNode) {
  node.children.sort((a, b) => {
    if (a.startTime && b.startTime && a.startTime !== b.startTime) {
      return a.startTime < b.startTime ? -1 : 1;
    }
    return a.name.localeCompare(b.name);
  });
  node.children.forEach(sortTree);
}

/** Adds a single execution to the node map, merging repeated node executions. */
function upsertExecution(nodeMap: Map<string, HistoryNode>, exec: SequenceExecution) {
  if (!exec || typeof exec !== "object" || !exec.nodeId) return;

  const changes = collectStateChanges(exec.stateChanges);
  const { start, end } = deriveTimes(changes);
  const childInstanceIds = normalizeChildInstanceIds(exec.metaData);
  const existing = nodeMap.get(exec.nodeId);

  if (existing) {
    // Merge repeated executions of the same node (loops / multi-instance).
    existing.startTime = minTime(existing.startTime, start);
    existing.endTime = maxTime(existing.endTime, end);
    existing.stateChanges = [...existing.stateChanges, ...changes].sort(byTimestamp);
    if (exec.state) existing.state = exec.state;
    for (const id of childInstanceIds) {
      if (!existing.childInstanceIds.includes(id)) existing.childInstanceIds.push(id);
    }
    return;
  }

  nodeMap.set(exec.nodeId, {
    key: exec.nodeId,
    nodeId: exec.nodeId,
    name: exec.nodeName || exec.nodeId,
    nodeType: exec.nodeType,
    state: exec.state,
    startTime: start,
    endTime: end,
    stateChanges: changes,
    children: [],
    childInstanceIds,
  });
}

export function buildHistoryTree(
  sequenceExecutions: Record<string, SequenceExecution> | undefined,
  containment: BpmnContainment,
  root: HistoryRoot
): HistoryNode {
  const nodeMap = new Map<string, HistoryNode>();

  for (const exec of Object.values(sequenceExecutions ?? {})) {
    upsertExecution(nodeMap, exec);
  }

  const rootNode: HistoryNode = {
    key: root.id || "root",
    nodeId: root.id,
    name: root.name || "Process",
    nodeType: "PROCESS",
    state: root.state,
    startTime: root.startTime,
    endTime: root.endTime ?? undefined,
    stateChanges: [],
    children: [],
    childInstanceIds: [],
  };

  for (const node of nodeMap.values()) {
    const parentKey = resolveParentKey(node.nodeId, containment.parentOf, nodeMap);
    if (parentKey) {
      nodeMap.get(parentKey)!.children.push(node);
    } else {
      rootNode.children.push(node);
    }
  }

  sortTree(rootNode);
  return rootNode;
}

/** Whether a history node can be expanded (has children or call-activity drill-down). */
export const isExpandableNode = (node: HistoryNode): boolean =>
  node.children.length > 0 || node.childInstanceIds.length > 0;

/** Default expanded keys: only the root process node is open. */
export const collectDefaultExpandedKeys = (node: HistoryNode): Set<string> => {
  const keys = new Set<string>();
  if (isExpandableNode(node)) keys.add(node.key);
  return keys;
};
