import { decisionInstanceService, feelPlaygroundService, processInstanceService } from "@/api/domains";
import type { SequenceExecution } from "@/api/types/orchest-api";
import { DiagramViewer } from "@/shared/diagram/hooks/useDiagramViewer";
import { generateModificationBadge } from "@/shared/diagram/lib/overlays/BadgeGenerator";
import { addInvokedInstanceLinks, addUserTaskLinks, createInfoCard } from "@/shared/diagram/utils/bpmnDomHelpers";
import {
  extractChildInstanceInfo,
  extractExecutionStatus,
  extractUserTaskIds,
} from "@/shared/diagram/utils/childInstanceExtractor";
import {
  extractBpmnElementDetails,
  type BpmnBusinessObject,
} from "@/shared/diagram/utils/extractBpmnElementDetails";
import {
  formatFeelEvaluationResult,
  normalizeFeelExpression,
} from "@/shared/diagram/utils/feelMappingEval";
import { addTrackedOverlay, clearOverlaySet } from "@/shared/diagram/utils/overlayManager";
import { useApiMutation } from "@/shared/hooks";
import { logger } from "@/shared/utils/logger";
import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

interface ModificationState {
  isModifying: boolean;
  sourceActivityId: string | null;
  targetActivityId: string | null;
  scopeId: string | null;
}

export interface UseBpmnModificationProps {
  viewer: DiagramViewer | null;
  showInstanceActions: boolean;
  onModification?: (sourceId: string, targetId: string) => Promise<void>;
  sequenceExecutions?: Record<string, SequenceExecution>;
  variableContext?: Record<string, unknown>;
}

const DISABLED_ELEMENT_TYPES = ["bpmn:DataStoreReference", "bpmn:TextAnnotation"];
const NO_MOVE_ELEMENT_TYPES = new Set([
  "bpmn:SequenceFlow",
  "bpmn:MessageFlow",
  "bpmn:Association",
  "bpmn:DataInputAssociation",
  "bpmn:DataOutputAssociation",
  "bpmn:DataStoreReference",
  "bpmn:TextAnnotation",
]);

/** CSS marker class applied to a node highlighted from the history panel. */
const HIGHLIGHT_MARKER = "history-selected";

const INITIAL_MODIFICATION_STATE: ModificationState = {
  isModifying: false,
  sourceActivityId: null,
  targetActivityId: null,
  scopeId: null,
};

export const useBpmnModification = ({
  viewer,
  showInstanceActions,
  onModification,
  sequenceExecutions,
  variableContext,
}: UseBpmnModificationProps) => {
  const navigate = useNavigate();
  const [modification, setModification] = useState<ModificationState>(INITIAL_MODIFICATION_STATE);
  const badgeOverlayIds = useRef<Set<string>>(new Set());
  const infoOverlayIds = useRef<Set<string>>(new Set());
  const highlightedElementId = useRef<string | null>(null);
  const variableContextRef = useRef(variableContext);
  variableContextRef.current = variableContext;

  const navigateToProcess = useApiMutation(
    (instanceId: string) => processInstanceService.getProcessInstance(instanceId),
    {
      showErrorToast: false,
      onSuccess: (_, variables) => navigate(`/processes/${variables}`),
      onError: (error) => logger.error('process instance not found:', error),
    }
  );

  const navigateToDecision = useApiMutation(
    (instanceId: string) => decisionInstanceService.getDecisionInstance(instanceId),
    {
      showErrorToast: false,
      onSuccess: (_, variables) => navigate(`/decisions/${variables}`),
      onError: (error) => logger.error('decision instance not found:', error),
    }
  );

  useEffect(() => {
    if (!showInstanceActions) {
      setModification(INITIAL_MODIFICATION_STATE);

      const overlays = viewer?.get("overlays");
      if (overlays && badgeOverlayIds.current.size > 0) {
        clearOverlaySet(overlays, badgeOverlayIds.current);
      }
    }
  }, [showInstanceActions, viewer]);

  const handleChildInstanceNavigation = useCallback((instanceId: string, type: "process" | "decision") => {
    if (type === "decision") {
      navigateToDecision.mutate(instanceId);
    } else {
      navigateToProcess.mutate(instanceId);
    }
  }, [navigateToDecision, navigateToProcess]);

  const handleUserTaskNavigation = useCallback((taskId: string) => {
    navigate(`/tasks/${taskId}`);
  }, [navigate]);

  const evaluateMappingExpression = useCallback(async (expression: string) => {
    const normalized = normalizeFeelExpression(expression);
    if (!normalized) {
      return { ok: false, text: "Empty expression" };
    }
    try {
      const dto = await feelPlaygroundService.evaluate(
        normalized,
        variableContextRef.current ?? {}
      );
      const text = formatFeelEvaluationResult(dto);
      const ok = dto.success !== false && !dto.error;
      return { ok, text };
    } catch (error) {
      logger.error("Failed to evaluate mapping expression:", error);
      return {
        ok: false,
        text: error instanceof Error ? error.message : "Evaluation request failed",
      };
    }
  }, []);

  const showElementInfo = useCallback((
    elementId: string,
    elementName: string | undefined,
    overlays: ReturnType<DiagramViewer["get"]>,
    onSettingsClick?: () => void,
    businessObject?: BpmnBusinessObject | null
  ) => {
    const status = extractExecutionStatus(elementId, sequenceExecutions);
    const childInfo = extractChildInstanceInfo(elementId, sequenceExecutions);
    const userTaskIds = extractUserTaskIds(elementId, sequenceExecutions);
    const details = extractBpmnElementDetails(businessObject);

    const cardContainer = createInfoCard(elementId, elementName, status, {
      onSettingsClick,
      details,
      onEvaluateExpression: evaluateMappingExpression,
    });

    if (childInfo.hasChild && childInfo.children.length > 0) {
      addInvokedInstanceLinks(cardContainer, childInfo.children, handleChildInstanceNavigation);
    }

    if (userTaskIds.length > 0) {
      addUserTaskLinks(cardContainer, userTaskIds, handleUserTaskNavigation);
    }

    addTrackedOverlay(overlays, infoOverlayIds.current, elementId, {
      position: { bottom: 0, left: 0 },
      html: cardContainer,
      type: "info-card",
      scale: false,
    });
  }, [sequenceExecutions, handleChildInstanceNavigation, handleUserTaskNavigation, evaluateMappingExpression]);

  /**
   * Highlights and centers a node in the diagram and shows its info card.
   * Passing `null` clears the current highlight and info card.
   */
  const focusElement = useCallback((elementId: string | null) => {
    if (!viewer) return;

    const overlays = viewer.get("overlays");
    const canvas = viewer.get("canvas") as {
      addMarker?: (id: string, cls: string) => void;
      removeMarker?: (id: string, cls: string) => void;
      scrollToElement?: (element: unknown, padding?: unknown) => void;
    } | undefined;
    const elementRegistry = viewer.get("elementRegistry");

    if (highlightedElementId.current) {
      canvas?.removeMarker?.(highlightedElementId.current, HIGHLIGHT_MARKER);
      highlightedElementId.current = null;
    }
    if (overlays) clearOverlaySet(overlays, infoOverlayIds.current);

    if (!elementId) return;

    const element = elementRegistry?.get(elementId);
    if (!element || !overlays) return;

    canvas?.addMarker?.(elementId, HIGHLIGHT_MARKER);
    highlightedElementId.current = elementId;

    try {
      canvas?.scrollToElement?.(element);
    } catch (error) {
      logger.error("Failed to scroll to element:", error);
    }

    showElementInfo(
      elementId,
      element.businessObject?.name,
      overlays,
      undefined,
      element.businessObject as BpmnBusinessObject | undefined
    );
  }, [viewer, showElementInfo]);

  const startInstanceMove = useCallback((elementId: string) => {
    if (!viewer || !showInstanceActions) return;

    const overlays = viewer.get("overlays");
    if (!overlays) return;

    clearOverlaySet(overlays, infoOverlayIds.current);
    clearOverlaySet(overlays, badgeOverlayIds.current);

    setModification({
      isModifying: true,
      sourceActivityId: elementId,
      targetActivityId: null,
      scopeId: null,
    });

    addTrackedOverlay(overlays, badgeOverlayIds.current, elementId, {
      position: { top: -10, right: -10 },
      html: generateModificationBadge("source"),
      type: "mod-badge",
    });
  }, [viewer, showInstanceActions]);

  const handleModificationSelection = useCallback((
    elementId: string,
    parentId: string | undefined,
    overlays: ReturnType<DiagramViewer["get"]>
  ) => {
    setModification(prev => {
      if (!prev.sourceActivityId) {
        clearOverlaySet(overlays, badgeOverlayIds.current);
        return {
          ...prev,
          sourceActivityId: elementId,
          scopeId: parentId || null,
        };
      }

      if (!prev.targetActivityId) {
        addTrackedOverlay(overlays, badgeOverlayIds.current, elementId, {
          position: { top: -10, right: -10 },
          html: generateModificationBadge("target"),
          type: "mod-badge",
        });

        return {
          ...prev,
          scopeId: parentId || null,
          targetActivityId: elementId,
        };
      }

      return prev;
    });
  }, []);

  const handleModificationClick = useCallback((event: {
    element: {
      id?: string;
      type?: string;
      parent?: { id?: string };
      businessObject?: BpmnBusinessObject & { name?: string };
    };
  }) => {
    if (!viewer) return;

    const overlays = viewer.get("overlays");
    if (!overlays) return;

    const element = event.element;
    const elementId = element.id;
    const parentId = element.parent?.id;

    if (!elementId || element.type === "root" || element.type === "bpmn:Process") return;

    clearOverlaySet(overlays, infoOverlayIds.current);

    if (modification.isModifying) {
      // Move-instance selection only applies to activity/event nodes, not edges
      if (!NO_MOVE_ELEMENT_TYPES.has(element.type ?? "")) {
        handleModificationSelection(elementId, parentId, overlays);
      }
      return;
    }

    if (!DISABLED_ELEMENT_TYPES.includes(element.type ?? "")) {
      const canMove =
        showInstanceActions && !NO_MOVE_ELEMENT_TYPES.has(element.type ?? "");
      const onSettingsClick = canMove ? () => startInstanceMove(elementId) : undefined;
      showElementInfo(
        elementId,
        element.businessObject?.name,
        overlays,
        onSettingsClick,
        element.businessObject
      );
    }
  }, [viewer, modification.isModifying, showInstanceActions, handleModificationSelection, startInstanceMove, showElementInfo]);

  const resetModification = useCallback(() => {
    if (!viewer) return;

    const overlays = viewer.get("overlays");
    if (!overlays) return;

    setModification(INITIAL_MODIFICATION_STATE);
    clearOverlaySet(overlays, badgeOverlayIds.current);
  }, [viewer]);

  const confirmModification = useCallback(async () => {
    if (modification.sourceActivityId && modification.targetActivityId && onModification) {
      await onModification(modification.sourceActivityId, modification.targetActivityId);
      resetModification();
    }
  }, [modification, onModification, resetModification]);

  return {
    modification,
    handleModificationClick,
    startInstanceMove,
    resetModification,
    confirmModification,
    focusElement,
  };
};
