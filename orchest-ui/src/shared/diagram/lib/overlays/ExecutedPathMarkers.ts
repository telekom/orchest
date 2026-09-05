import type { SequenceExecution } from "@/api/types/orchest-api";
import type { DiagramViewer } from "../../hooks/useDiagramViewer";

export const EXECUTED_PATH_MARKER_LIVE = "executed-path-live";
export const EXECUTED_PATH_MARKER_STATIC = "executed-path-static";

export interface ExecutedPathContext {
  sequenceExecutions: Record<string, SequenceExecution>;
  state: string;
}

export interface ExecutedPathMarkersManager {
  apply: (viewer: DiagramViewer, context: ExecutedPathContext) => void;
  clear: (viewer: DiagramViewer) => void;
}

type CanvasService = {
  addMarker?: (elementId: string, className: string) => void;
  removeMarker?: (elementId: string, className: string) => void;
};

type ElementRegistry = {
  get: (elementId: string) => unknown;
};

export const collectExecutedSequenceFlowIds = (
  sequenceExecutions: Record<string, SequenceExecution>
): string[] => {
  const ids = new Set<string>();

  for (const execution of Object.values(sequenceExecutions)) {
    for (const flowId of execution.sequenceFlowIds ?? []) {
      if (flowId) {
        ids.add(flowId);
      }
    }
  }

  return Array.from(ids);
};

export const getExecutedPathMarkerClass = (_state: string): string =>
  EXECUTED_PATH_MARKER_LIVE;

export const createExecutedPathMarkersManager = (): ExecutedPathMarkersManager => {
  const applied = new Map<string, string>();

  const clear = (viewer: DiagramViewer) => {
    const canvas = viewer.get("canvas") as CanvasService | undefined;
    if (!canvas?.removeMarker) {
      applied.clear();
      return;
    }

    applied.forEach((markerClass, flowId) => {
      try {
        canvas.removeMarker?.(flowId, markerClass);
      } catch {
        // Element may already be gone after diagram reload
      }
    });
    applied.clear();
  };

  const apply = (viewer: DiagramViewer, { sequenceExecutions, state }: ExecutedPathContext) => {
    clear(viewer);

    const canvas = viewer.get("canvas") as CanvasService | undefined;
    const elementRegistry = viewer.get("elementRegistry") as ElementRegistry | undefined;
    if (!canvas?.addMarker || !elementRegistry) return;

    const markerClass = getExecutedPathMarkerClass(state);
    const flowIds = collectExecutedSequenceFlowIds(sequenceExecutions);

    for (const flowId of flowIds) {
      if (!elementRegistry.get(flowId)) continue;

      try {
        canvas.addMarker(flowId, markerClass);
        applied.set(flowId, markerClass);
      } catch {
        // Ignore marker failures for individual flows
      }
    }
  };

  return { apply, clear };
};
