import type { SequenceExecution } from "@/api/types/orchest-api";
import { collectExecutedSequenceFlowIds } from "./ExecutedPathMarkers";
import type { DiagramViewer } from "../../hooks/useDiagramViewer";

export const ORCHEST_PATH_HIDDEN_MARKER = "orchest-path-hidden";

const CONTAINER_TYPES = new Set([
  "bpmn:Process",
  "bpmn:Participant",
  "bpmn:Lane",
  "bpmn:Collaboration",
  "label",
]);

type CanvasService = {
  addMarker?: (elementId: string, className: string) => void;
  removeMarker?: (elementId: string, className: string) => void;
};

type DiagramElement = {
  id: string;
  type?: string;
  waypoints?: unknown[];
  businessObject?: { $type?: string };
  source?: { id?: string };
  target?: { id?: string };
  labelTarget?: { id?: string } | null;
};

type ElementRegistry = {
  get: (elementId: string) => DiagramElement | undefined;
  getAll?: () => DiagramElement[];
};

export interface ExecutedPathFocusContext {
  sequenceExecutions: Record<string, SequenceExecution>;
  activeElements?: string[];
}

export interface ExecutedPathFocusManager {
  apply: (viewer: DiagramViewer, context: ExecutedPathFocusContext) => void;
  clear: (viewer: DiagramViewer) => void;
}

const isConnection = (el: DiagramElement): boolean =>
  Array.isArray(el.waypoints) ||
  el.type === "bpmn:SequenceFlow" ||
  el.type === "bpmn:MessageFlow" ||
  el.type === "bpmn:Association";

const isContainer = (el: DiagramElement): boolean => {
  if (el.type && CONTAINER_TYPES.has(el.type)) return true;
  const boType = el.businessObject?.$type;
  return Boolean(boType && CONTAINER_TYPES.has(boType));
};

/**
 * Active flows = taken sequenceFlowIds.
 * Active nodes = execution nodeIds + sourceNodeIds + endpoints of active flows + activeElements.
 */
export const collectActivePathIds = (
  sequenceExecutions: Record<string, SequenceExecution>,
  elementRegistry: ElementRegistry,
  activeElements: string[] = []
): { activeFlowIds: Set<string>; activeNodeIds: Set<string> } => {
  const activeFlowIds = new Set(collectExecutedSequenceFlowIds(sequenceExecutions));
  const activeNodeIds = new Set<string>();

  for (const exec of Object.values(sequenceExecutions)) {
    if (exec.nodeId) activeNodeIds.add(exec.nodeId);
    if (exec.sourceNodeId) activeNodeIds.add(exec.sourceNodeId);
  }

  for (const id of activeElements) {
    if (id) activeNodeIds.add(id);
  }

  for (const flowId of activeFlowIds) {
    const flow = elementRegistry.get(flowId);
    if (!flow) continue;
    if (flow.source?.id) activeNodeIds.add(flow.source.id);
    if (flow.target?.id) activeNodeIds.add(flow.target.id);
  }

  return { activeFlowIds, activeNodeIds };
};

export const collectHiddenElementIds = (
  sequenceExecutions: Record<string, SequenceExecution>,
  elementRegistry: ElementRegistry,
  activeElements: string[] = []
): string[] => {
  const { activeFlowIds, activeNodeIds } = collectActivePathIds(
    sequenceExecutions,
    elementRegistry,
    activeElements
  );

  // No executed path yet — keep the full diagram visible.
  if (activeFlowIds.size === 0 && activeNodeIds.size === 0) {
    return [];
  }

  const elements = elementRegistry.getAll?.() ?? [];
  const hidden: string[] = [];

  for (const el of elements) {
    if (!el?.id) continue;

    // Hide labels whose host is hidden.
    if (el.labelTarget?.id) {
      const hostId = el.labelTarget.id;
      const hostHidden =
        !activeNodeIds.has(hostId) && !activeFlowIds.has(hostId);
      if (hostHidden) hidden.push(el.id);
      continue;
    }

    if (isContainer(el)) continue;

    if (isConnection(el)) {
      if (!activeFlowIds.has(el.id)) hidden.push(el.id);
      continue;
    }

    // Shape not on the executed path (only linked via inactive edges, or not linked at all).
    if (!activeNodeIds.has(el.id)) {
      hidden.push(el.id);
    }
  }

  return hidden;
};

export const createExecutedPathFocusManager = (): ExecutedPathFocusManager => {
  const applied = new Set<string>();

  const clear = (viewer: DiagramViewer) => {
    const canvas = viewer.get("canvas") as CanvasService | undefined;
    if (!canvas?.removeMarker) {
      applied.clear();
      return;
    }

    applied.forEach((id) => {
      try {
        canvas.removeMarker?.(id, ORCHEST_PATH_HIDDEN_MARKER);
      } catch {
        // Element may already be gone
      }
    });
    applied.clear();
  };

  const apply = (
    viewer: DiagramViewer,
    { sequenceExecutions, activeElements }: ExecutedPathFocusContext
  ) => {
    clear(viewer);

    const canvas = viewer.get("canvas") as CanvasService | undefined;
    const elementRegistry = viewer.get("elementRegistry") as ElementRegistry | undefined;
    if (!canvas?.addMarker || !elementRegistry) return;

    const hiddenIds = collectHiddenElementIds(
      sequenceExecutions,
      elementRegistry,
      activeElements
    );

    for (const id of hiddenIds) {
      try {
        canvas.addMarker(id, ORCHEST_PATH_HIDDEN_MARKER);
        applied.add(id);
      } catch {
        // Ignore individual failures
      }
    }
  };

  return { apply, clear };
};
