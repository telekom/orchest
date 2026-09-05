import type { SequenceExecution } from "@/api/types/orchest-api";
import type { ProcessInstanceState } from "@/shared/enums";
import { DiagramViewer } from "../../hooks/useDiagramViewer";
import { generateStatusBadge, getStatusRingMarkerClass } from "./BadgeGenerator";

export interface BadgeContext {
  processState: ProcessInstanceState;
  lastActivityAt: string;
  activeElements?: string[];
  sequenceExecutions: Record<string, SequenceExecution>;
}

type AppliedStatus = {
  overlayId: string;
  markerClass: string;
};

export interface StatusOverlaysManager {
  applied: Map<string, AppliedStatus>;
  overlayIds: Map<string, string>;
  add: (viewer: DiagramViewer, context: BadgeContext) => void;
  clear: (viewer: DiagramViewer) => void;
}

type CanvasService = {
  addMarker?: (elementId: string, className: string) => void;
  removeMarker?: (elementId: string, className: string) => void;
};

type ElementRegistry = {
  get: (elementId: string) => unknown;
};

type OverlaysService = {
  add: (id: string, config: Record<string, unknown>) => void;
  remove: (id: string) => void;
};

export const createStatusOverlaysManager = (): StatusOverlaysManager => {
  const applied = new Map<string, AppliedStatus>();
  const overlayIds = new Map<string, string>();

  const clear = (viewer: DiagramViewer) => {
    const overlays = viewer.get("overlays") as OverlaysService | undefined;
    const canvas = viewer.get("canvas") as CanvasService | undefined;

    applied.forEach(({ overlayId, markerClass }, nodeId) => {
      try {
        overlays?.remove(overlayId);
      } catch {
        // Overlay may already be gone
      }
      try {
        canvas?.removeMarker?.(nodeId, markerClass);
      } catch {
        // Marker may already be gone
      }
    });

    applied.clear();
    overlayIds.clear();
  };

  const addBadge = (
    overlays: OverlaysService,
    canvas: CanvasService | undefined,
    elementRegistry: ElementRegistry,
    nodeId: string,
    executions: Record<string, SequenceExecution>,
    stateOverride?: string
  ) => {
    const exec = Object.values(executions).find(e => e.nodeId === nodeId);
    if (!exec?.state || !elementRegistry.get(exec.nodeId)) return;

    const badgeState = stateOverride ?? exec.state;
    const badge = generateStatusBadge(exec.nodeType, badgeState);
    const markerClass = getStatusRingMarkerClass(badgeState);
    if (!badge || !markerClass) return;

    const overlayId = `${exec.nodeId}-status-badge`;

    try {
      canvas?.addMarker?.(exec.nodeId, markerClass);
      overlays.add(exec.nodeId, {
        position: { top: -4, right: -4 },
        html: badge,
        type: "status-badge",
        id: overlayId,
      });
      applied.set(exec.nodeId, { overlayId, markerClass });
      overlayIds.set(exec.nodeId, badgeState);
    } catch {
      // Ignore individual overlay/marker failures
    }
  };

  return {
    applied,
    overlayIds,

    add: (viewer, { processState, lastActivityAt, activeElements, sequenceExecutions }) => {
      const overlays = viewer.get("overlays") as OverlaysService | undefined;
      const elementRegistry = viewer.get("elementRegistry") as ElementRegistry | undefined;
      const canvas = viewer.get("canvas") as CanvasService | undefined;

      if (!overlays || !elementRegistry) return;

      const upperState = processState?.toUpperCase() ?? "";
      const isIncident = upperState === "INCIDENT";
      const isHold = upperState === "HOLD";
      const useActiveElements =
        activeElements?.length &&
        (upperState === "RUNNING" ||
          upperState === "ACTIVE" ||
          upperState === "STARTED" ||
          upperState === "TRIGGERED" ||
          upperState === "HOLD" ||
          upperState === "PENDING" ||
          isIncident);

      let elements: string[] = [];
      if (useActiveElements) {
        elements = activeElements ?? [];
      } else if (lastActivityAt) {
        elements = [lastActivityAt];
      }

      // Process-level hold/incident wins over the activity's sequence-execution state
      // (active tokens often report as PENDING/ACTIVE even while the instance is on hold).
      const stateOverride = isIncident ? "INCIDENT" : isHold ? "HOLD" : undefined;
      elements.forEach(id =>
        addBadge(overlays, canvas, elementRegistry, id, sequenceExecutions, stateOverride)
      );
    },

    clear,
  };
};
