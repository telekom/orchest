
import { DiagramControlBar } from "@/shared/diagram/components/DiagramControlBar/DiagramControlBar";
import { DiagramViewer, useDiagramViewer } from "@/shared/diagram/hooks/useDiagramViewer";
import { useExport } from "@/shared/diagram/hooks/useExport";
import { DiagramType } from "@/shared/enums";
import DmnJS from "dmn-js/dist/dmn-navigated-viewer.production.min";
import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import styles from "./DmnViewer.module.css";

interface DmnViewerProps {
  xml: string;
  matchedRuleId?: string | string[];
  className?: string;
  showControls?: boolean;
}

const DmnViewer: React.FC<DmnViewerProps> = ({
  xml,
  matchedRuleId,
  className,
  showControls = true,
}) => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const viewerRef = useRef<DiagramViewer | null>(null);
  const [currentViewType, setCurrentViewType] = useState<"decisionTable" | "drd" | null>(null);

  const highlightMatchedRuleId = useCallback(() => {
    if (!matchedRuleId) return;

    const ruleIds = Array.isArray(matchedRuleId)
      ? matchedRuleId
      : matchedRuleId.split(',').map(id => id.trim()).filter(Boolean);

    const applyHighlight = () => {
      document.querySelectorAll(".dmn-highlighted-row").forEach(el =>
        el.classList.remove("dmn-highlighted-row")
      );

      return ruleIds.some(ruleId => {
        const selectors = [
          `tr[data-row-id="${ruleId}"]`,
          `tr[data-element-id="${ruleId}"]`,
          `[data-row-id="${ruleId}"]`,
          `[data-element-id="${ruleId}"]`
        ];

        for (const selector of selectors) {
          const elements = document.querySelectorAll(selector);

          if (elements.length) {
            elements.forEach(el => {
              if (el instanceof HTMLElement) {
                el.classList.add("dmn-highlighted-row");
                el.querySelectorAll("td").forEach(cell =>
                  cell.classList.add("dmn-highlighted-row")
                );
                if (el.tagName === "TD" && el.parentElement) {
                  el.parentElement.classList.add("dmn-highlighted-row");
                  el.parentElement.querySelectorAll("td").forEach(cell =>
                    cell.classList.add("dmn-highlighted-row")
                  );
                }
              }
            });
            return true;
          }
        }
        return false;
      });
    };

    const retry = (attempt = 1) => {
      setTimeout(() => {
        const success = applyHighlight();
        if (!success && attempt < 3) retry(attempt + 1);
      }, attempt * 100);
    };

    retry();
  }, [matchedRuleId]);

  const viewerOptions = useMemo(() => ({
    height: "100%",
    width: "100%",
    disableAdjustOrigin: true,
    headerVisible: false,
    labelVisible: false,
  }), []);

  const onLoaded = useCallback(async (loadedViewer: DiagramViewer) => {
    const views = loadedViewer.getViews?.() || [];
    const drdView = views.find(v => v.type === "drd");
    const decisionTableView = views.find(v => v.type === "decisionTable");
    const viewToOpen = drdView || decisionTableView;

    if (viewToOpen && loadedViewer.open) {
      await loadedViewer.open(viewToOpen);
      setCurrentViewType(viewToOpen.type as "drd" | "decisionTable");
      highlightMatchedRuleId();
    }
  }, [highlightMatchedRuleId]);

  const { viewer, isLoaded, handleZoomReset } = useDiagramViewer({
    containerRef,
    xml,
    engine: "dmn",
    ViewerClass: DmnJS as unknown as new (options?: Record<string, unknown>) => DiagramViewer,
    viewerOptions,
    onLoaded,
  });

  useEffect(() => {
    viewerRef.current = viewer;
  }, [viewer]);

  const { handleExport: exportDiagram } = useExport({
    modelerRef: viewerRef,
    diagramType: DiagramType.DMN,
  });

  useEffect(() => {
    if (!viewer?.on || !viewer?.off || !isLoaded) return;

    const handleViewsChanged = () => {
      const activeView = viewer.getActiveView?.();
      const viewType = activeView?.type;

      if (viewType === "drd" || viewType === "decisionTable") {
        setCurrentViewType(viewType);
        if (viewType === "drd") {
          setTimeout(handleZoomReset, 150);
        } else {
          highlightMatchedRuleId();
        }
      }
    };

    viewer.on("views.changed", handleViewsChanged);
    return () => viewer.off?.("views.changed", handleViewsChanged);
  }, [viewer, isLoaded, handleZoomReset, highlightMatchedRuleId]);

  useEffect(() => {
    if (showControls && isLoaded && viewer) handleZoomReset();
  }, [showControls, isLoaded, viewer, handleZoomReset]);

  return (
    <div className={`${styles.container} ${className || ""}`}>
      <div ref={containerRef} className={`dmn-viewer ${styles.viewerContainer}`} />
      {isLoaded && showControls && (
        <DiagramControlBar
          onZoomReset={handleZoomReset}
          onExport={exportDiagram}
          showZoomButton={currentViewType === "drd"}
        />
      )}
    </div>
  );
};

export default React.memo(DmnViewer);
