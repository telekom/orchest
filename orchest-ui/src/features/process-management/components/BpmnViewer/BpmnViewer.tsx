import type { SequenceExecution } from "@/api/types/orchest-api";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/design-system/components/ui/dropdown-menu";
import { Switch } from "@/design-system/components/ui/switch";
import { TelekomLogoMark } from "@/shared/components/OrchLogo";
import { TooltipButton } from "@/shared/components/TooltipButton/TooltipButton";
import { TIMING } from "@/shared/constants";
import { isTerminalProcessStatus } from "@/shared/constants/status";
import { useDiagramViewer } from "@/shared/diagram/hooks/useDiagramViewer";
import { useHoverZoom } from "@/shared/diagram/hooks/useHoverZoom";
import { useExport } from "@/shared/diagram/hooks/useExport";
import { createExecutedPathFocusManager } from "@/shared/diagram/lib/overlays/ExecutedPathFocus";
import { collectExecutedSequenceFlowIds } from "@/shared/diagram/lib/overlays/ExecutedPathMarkers";
import { useIsDarkMode } from "@/shared/stores/uiStore";
import { logger } from "@/shared/utils/logger";
import clsx from "clsx";
import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { extractChildInstanceInfo } from "@/shared/diagram/utils/childInstanceExtractor";
import { animateEdges, setNodeStates as flowSkinSetNodeStates, clearNodeStates as flowSkinClearNodeStates } from "@flowskin-bpmn/flowskin-bpmn";
import BpmnNavigatedViewer from "bpmn-js/lib/NavigatedViewer";
import ZeebeBpmnModdle from "zeebe-bpmn-moddle/resources/zeebe.json";
import { useBpmnModification } from "../../hooks/useBpmnModification";
import { useCompleteJourney } from "../../hooks/useCompleteJourney";
import { ConfirmModificationDialog } from "../bpmn/ModificationDialogs/ModificationDialogs";
import { BpmnControlBar } from "../ui/BpmnControlBar";
import { CancelInstanceButton, CompensateInstanceButton, RaiseIncidentButton, RetryInstanceButton } from "../ui/InstanceActionButtons";
import styles from "./BpmnViewer.module.css";

interface BpmnViewerProps {
  xml: string;
  sequenceExecutions: Record<string, SequenceExecution>;
  state: string;
  lastActivityAt?: string;
  activeElements?: string[];
  processInstanceId: string | null;
  processDefinitionId: string | null;
  onInstanceModification?: (sourceId: string, targetId: string) => Promise<void>;
  onInstanceCancellation?: (activityId: string) => Promise<void>;
  showInstanceActions?: boolean;
  /** Show Telekom mark in the canvas top-right (process detail). */
  showBrandMark?: boolean;
  /** Hide inactive edges and off-path nodes (process detail only). */
  focusExecutedPath?: boolean;
  className?: string;
  /** Node id to highlight (and show info card for), driven from the history panel. */
  selectedNodeId?: string | null;
  /** Process instance variables used to evaluate mapping FEEL expressions. */
  variableContext?: Record<string, unknown>;
}

const RETRYABLE_STATES = ["hold", "incident"];

type FlowSkinNodeState = 'running' | 'completed' | 'incident' | 'hold';

function mapExecutionStateToFlowSkin(execState: string, processStateOverride?: string): FlowSkinNodeState | null {
  const s = (processStateOverride ?? execState).toUpperCase();
  if (s === "RUNNING" || s === "ACTIVE" || s === "STARTED" || s === "TRIGGERED" || s === "PENDING") return "running";
  if (s === "COMPLETED") return "completed";
  if (s === "INCIDENT" || s === "FAILED") return "incident";
  if (s === "HOLD") return "hold";
  return null;
}

const RUNNING_PROCESS_STATES = new Set([
  "RUNNING", "ACTIVE", "STARTED", "TRIGGERED", "HOLD", "PENDING", "INCIDENT",
]);

function buildNodeStatesMap(
  sequenceExecutions: Record<string, SequenceExecution>,
  processState: string,
  activeElements?: string[],
  lastActivityAt?: string,
): Record<string, FlowSkinNodeState> {
  const upperProcess = processState.toUpperCase();
  const isIncident = upperProcess === "INCIDENT";
  const isHold = upperProcess === "HOLD";

  // Same logic as original StatusOverlays: only badge active/recent nodes
  const useActiveElements =
    activeElements?.length && RUNNING_PROCESS_STATES.has(upperProcess);

  let targetNodeIds: string[] = [];
  if (useActiveElements) {
    targetNodeIds = activeElements ?? [];
  } else if (lastActivityAt) {
    targetNodeIds = [lastActivityAt];
  }

  if (targetNodeIds.length === 0) return {};

  // Process-level hold/incident wins over the activity's execution state
  const stateOverride = isIncident ? "INCIDENT" : isHold ? "HOLD" : undefined;

  const result: Record<string, FlowSkinNodeState> = {};

  for (const nodeId of targetNodeIds) {
    const exec = Object.values(sequenceExecutions).find(e => e.nodeId === nodeId);
    if (!exec?.state) continue;

    const badge = mapExecutionStateToFlowSkin(exec.state, stateOverride);
    if (badge) result[exec.nodeId] = badge;
  }

  return result;
}

const BpmnViewer = React.memo<BpmnViewerProps>(({
  xml,
  sequenceExecutions,
  processInstanceId,
  processDefinitionId,
  state,
  lastActivityAt,
  activeElements,
  onInstanceModification,
  onInstanceCancellation,
  showInstanceActions = false,
  showBrandMark = false,
  focusExecutedPath = false,
  className,
  selectedNodeId,
  variableContext,
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const executedPathFocusRef = useRef(createExecutedPathFocusManager());
  const isDarkMode = useIsDarkMode();
  const [executedPathOnly, setExecutedPathOnly] = useState(false);
  const [journeyEnabled, setJourneyEnabled] = useState(false);
  const [hoverZoom, setHoverZoom] = useState(false);
  const [classicView, setClassicViewState] = useState(() => localStorage.getItem('orchest:classicView') === 'true');
  const setClassicView = useCallback((v: boolean) => {
    setClassicViewState(v);
    localStorage.setItem('orchest:classicView', String(v));
  }, []);
  const pathFocusActive = focusExecutedPath && executedPathOnly && !journeyEnabled;

  const hasChildInstances = useMemo(() => {
    return Object.values(sequenceExecutions).some(
      exec => extractChildInstanceInfo(exec.nodeId, sequenceExecutions).hasChild
    );
  }, [sequenceExecutions]);

  const { loading: journeyLoading, flattenedXml, allExecutedEdgeIds } = useCompleteJourney(
    xml,
    sequenceExecutions,
    journeyEnabled,
  );
  const diagramXml = (journeyEnabled && flattenedXml) ? flattenedXml : xml;

  const classicViewerOptions = useMemo(() => classicView ? { moddleExtensions: { zeebe: ZeebeBpmnModdle } } : {}, [classicView]);

  const { viewer, isLoaded, flowSkin, handleZoomReset, handleZoomIn, handleZoomOut } = useDiagramViewer({
    containerRef,
    xml: diagramXml,
    engine: "bpmn",
    ViewerClass: BpmnNavigatedViewer as unknown as new (options?: Record<string, unknown>) => import('@/shared/diagram/hooks/useDiagramViewer').DiagramViewer,
    useFlowSkin: !classicView,
    vanilla: classicView,
    viewerOptions: classicViewerOptions,
  });

  useHoverZoom(containerRef, viewer, hoverZoom && isLoaded);

  const { handleExport } = useExport({
    modelerRef: { current: viewer },
    diagramType: "bpmn",
  });

  const {
    modification,
    handleModificationClick,
    resetModification,
    confirmModification,
    focusElement,
  } = useBpmnModification({
    viewer,
    showInstanceActions,
    onModification: onInstanceModification,
    sequenceExecutions,
    variableContext,
  });

  // FlowSkin loading animation on executed edges
  useEffect(() => {
    if (!viewer || !isLoaded) return;
    if (!flowSkin && !classicView) return;

    const flowIds = journeyEnabled && allExecutedEdgeIds.length > 0
      ? allExecutedEdgeIds
      : collectExecutedSequenceFlowIds(sequenceExecutions);
    if (flowIds.length === 0) return;

    let stopAnim: (() => void) | undefined;
    const timeoutId = setTimeout(() => {
      try {
        const opts = { type: 'loading' as const, ...(isDarkMode ? {} : { color: '#ff0080' }) };
        stopAnim = flowSkin
          ? flowSkin.animateEdges(flowIds, opts)
          : animateEdges(viewer, flowIds, opts);
      } catch (error) {
        logger.error("Error animating executed path edges:", error);
      }
    }, TIMING.DIAGRAM_INIT_DELAY);

    return () => {
      clearTimeout(timeoutId);
      stopAnim?.();
    };
  }, [viewer, isLoaded, flowSkin, classicView, sequenceExecutions, journeyEnabled, allExecutedEdgeIds, isDarkMode]);

  // FlowSkin node state badges
  useEffect(() => {
    if (!viewer || !isLoaded) return;
    if (!flowSkin && !classicView) return;

    const nodeStates = buildNodeStatesMap(sequenceExecutions, state, activeElements, lastActivityAt);

    const timeoutId = setTimeout(() => {
      try {
        if (flowSkin) {
          flowSkin.clearNodeStates();
          if (Object.keys(nodeStates).length > 0) {
            flowSkin.setNodeStates(nodeStates);
          }
        } else {
          flowSkinClearNodeStates(viewer);
          if (Object.keys(nodeStates).length > 0) {
            flowSkinSetNodeStates(viewer, nodeStates);
          }
        }
      } catch (error) {
        logger.error("Error setting node state badges:", error);
      }
    }, TIMING.DIAGRAM_INIT_DELAY);

    return () => clearTimeout(timeoutId);
  }, [viewer, isLoaded, flowSkin, classicView, sequenceExecutions, state, activeElements, lastActivityAt]);

  // Executed path focus toggle — hide off-path nodes
  useEffect(() => {
    if (!viewer || !isLoaded || !focusExecutedPath) return;

    const timeoutId = setTimeout(() => {
      try {
        if (pathFocusActive) {
          executedPathFocusRef.current.apply(viewer, {
            sequenceExecutions,
            activeElements,
          });
        } else {
          executedPathFocusRef.current.clear(viewer);
        }
      } catch (error) {
        logger.error("Error applying executed path focus:", error);
      }
    }, TIMING.DIAGRAM_INIT_DELAY);

    return () => {
      clearTimeout(timeoutId);
      try {
        executedPathFocusRef.current.clear(viewer);
      } catch (error) {
        logger.error("Error clearing executed path focus:", error);
      }
    };
  }, [viewer, isLoaded, focusExecutedPath, pathFocusActive, sequenceExecutions, activeElements]);

  // Click handler for card overlay + move-instance interaction
  useEffect(() => {
    if (!viewer || !isLoaded) return;

    const eventBus = viewer.get("eventBus");
    eventBus.on("element.click", handleModificationClick as (event: unknown) => void);

    return () => {
      eventBus.off("element.click", handleModificationClick as (event: unknown) => void);
    };
  }, [isLoaded, handleModificationClick, viewer]);

  // External node selection (from history panel)
  useEffect(() => {
    if (!viewer || !isLoaded) return;

    const timeoutId = setTimeout(() => {
      focusElement(selectedNodeId ?? null);
    }, TIMING.DIAGRAM_INIT_DELAY);

    return () => clearTimeout(timeoutId);
  }, [viewer, isLoaded, selectedNodeId, focusElement]);

  const activeViewOptions = Number(executedPathOnly) + Number(journeyEnabled) + Number(hoverZoom);

  const isRetryable = RETRYABLE_STATES.includes(state.toLowerCase());
  const showCancelButton = !isTerminalProcessStatus(state) && showInstanceActions;
  const showRetryButton = isRetryable && showInstanceActions;

  const cancelButton = showCancelButton && processInstanceId && processDefinitionId && (
    <TooltipButton tooltip="Cancel Instance">
      <CancelInstanceButton
        processInstanceId={processInstanceId}
        processDefinitionId={processDefinitionId}
        onSuccess={onInstanceCancellation ? () => onInstanceCancellation(processInstanceId) : undefined}
      />
    </TooltipButton>
  );

  const retryButton = showRetryButton && processInstanceId && (
    <TooltipButton tooltip="Retry Instance">
      <RetryInstanceButton
        processInstanceId={processInstanceId}
        sequenceExecutions={sequenceExecutions}
        processInstanceState={state}
      />
    </TooltipButton>
  );

  const showRaiseIncident = showInstanceActions && !isTerminalProcessStatus(state) && state.toLowerCase() !== 'incident';
  const raiseIncidentButton = showRaiseIncident && processInstanceId && (
    <TooltipButton tooltip="Raise Incident">
      <RaiseIncidentButton
        processInstanceId={processInstanceId}
      />
    </TooltipButton>
  );

  const showCompensateButton = showInstanceActions && state.toLowerCase() === 'incident';
  const compensateButton = showCompensateButton && processInstanceId && (
    <TooltipButton tooltip="Compensate">
      <CompensateInstanceButton
        processInstanceId={processInstanceId}
      />
    </TooltipButton>
  );

  return (
    <div className={clsx("bpmn-container", styles.bpmnContainer, className)}>
<div ref={containerRef} className={clsx("bpmn-viewer-container", styles.bpmnViewerContainer)} data-tour="bpmn-diagram"/>
      {isLoaded && (
        <div className={styles.viewOptionsChrome} data-tour="executed-path-toggle">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button className={styles.viewOptionsBtn} aria-label="View options">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round">
                  <circle cx="8" cy="8" r="1.5" /><circle cx="8" cy="3" r="1.5" /><circle cx="8" cy="13" r="1.5" />
                </svg>
                <span>View</span>
                {activeViewOptions > 0 && (
                  <span className={styles.viewOptionsBadge}>{activeViewOptions}</span>
                )}
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="start" sideOffset={6} onCloseAutoFocus={(e) => e.preventDefault()}>
              <DropdownMenuLabel>Display options</DropdownMenuLabel>
              <DropdownMenuSeparator />
              {focusExecutedPath && (
                <label className={styles.menuToggleItem} data-disabled={journeyEnabled || undefined}>
                  <span className={styles.menuToggleLabel}>Focus path</span>
                  <Switch
                    checked={executedPathOnly}
                    onCheckedChange={setExecutedPathOnly}
                    disabled={journeyEnabled}
                    className={styles.menuToggleSwitch}
                  />
                </label>
              )}
              {hasChildInstances && (
                <label className={styles.menuToggleItem} data-disabled={journeyLoading || undefined}>
                  <span className={styles.menuToggleLabel}>{journeyLoading ? "Loading…" : "Full journey"}</span>
                  <Switch
                    checked={journeyEnabled}
                    onCheckedChange={setJourneyEnabled}
                    disabled={journeyLoading}
                    className={styles.menuToggleSwitch}
                  />
                </label>
              )}
              <label className={styles.menuToggleItem}>
                <span className={styles.menuToggleLabel}>Hover zoom</span>
                <Switch
                  checked={hoverZoom}
                  onCheckedChange={setHoverZoom}
                  className={styles.menuToggleSwitch}
                />
              </label>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      )}

      {showBrandMark && (
        <div className={styles.brandMarkStandalone}>
          <TelekomLogoMark size={28} variant="letter" />
        </div>
      )}

      {isLoaded && (
        <button
          className={clsx(styles.classicViewBtn, classicView && styles.classicViewBtnActive)}
          onClick={() => setClassicView(!classicView)}
          aria-label="Toggle classic view"
          title={classicView ? "Switch to Modern View" : "Switch to Classic View"}
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M17 1l4 4-4 4" /><path d="M3 11V9a4 4 0 0 1 4-4h14" /><path d="M7 23l-4-4 4-4" /><path d="M21 13v2a4 4 0 0 1-4 4H3" />
          </svg>
          <span>{classicView ? "Modern View" : "Classic View"}</span>
        </button>
      )}

      {isLoaded && (
        <BpmnControlBar
          showCancelButton={showCancelButton}
          showRetryButton={showRetryButton}
          showRaiseIncidentButton={showRaiseIncident}
          showCompensateButton={showCompensateButton}
          onZoomReset={handleZoomReset}
          onZoomIn={handleZoomIn}
          onZoomOut={handleZoomOut}
          onExport={handleExport}
          cancelButton={cancelButton}
          retryButton={retryButton}
          raiseIncidentButton={raiseIncidentButton}
          compensateButton={compensateButton}
        />
      )}

      {modification.sourceActivityId && modification.targetActivityId && showInstanceActions && (
        <ConfirmModificationDialog
          open={true}
          sourceActivityId={modification.sourceActivityId}
          targetActivityId={modification.targetActivityId}
          onConfirm={confirmModification}
          onCancel={resetModification}
        />
      )}
    </div>
  );
});

BpmnViewer.displayName = 'BpmnViewer';

export default BpmnViewer;
