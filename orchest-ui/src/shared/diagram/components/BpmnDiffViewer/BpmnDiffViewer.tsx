import { toast } from "@/design-system/components/ui/sonner";
import OrchestBpmnThemeModule from "@/shared/diagram/bpmn-theme";
import { getDiagramTheme } from "@/shared/diagram/lib/DiagramTheme";
import { DIFF_STYLES } from "@/shared/diagram/lib/diff/diffStyles";
import { applyDiffMarkers, fitViewports, syncViewboxes } from "@/shared/diagram/lib/diff/diffUtils";
import { useIsDarkMode } from "@/shared/stores/uiStore";
import { diff } from "bpmn-js-differ";
import BpmnJS from "bpmn-js/lib/NavigatedViewer";
import React, { useEffect, useRef, useState } from "react";
import { DiffStats } from "../DiffStats/DiffStats";
import styles from "./BpmnDiffViewer.module.css";

interface BpmnDiffViewerProps {
  oldXml: string;
  newXml: string;
  height?: string;
}

const BpmnDiffViewer: React.FC<BpmnDiffViewerProps> = ({ oldXml, newXml, height = "500px" }) => {
  const oldContainerRef = useRef<HTMLDivElement>(null);
  const newContainerRef = useRef<HTMLDivElement>(null);
  const isDarkMode = useIsDarkMode();
  const diagramTheme = getDiagramTheme(isDarkMode);

  const [stats, setStats] = useState({ added: 0, removed: 0, changed: 0, layoutChanged: 0 });

  useEffect(() => {
    if (!oldContainerRef.current || !newContainerRef.current) return;

    const viewerOptions = { additionalModules: [OrchestBpmnThemeModule] };
    const oldViewer = new BpmnJS({ container: oldContainerRef.current, ...viewerOptions });
    const newViewer = new BpmnJS({ container: newContainerRef.current, ...viewerOptions });

    const loadDiff = async () => {
      try {
        await Promise.all([oldViewer.importXML(oldXml), newViewer.importXML(newXml)]);

        const oldDefs = (oldViewer as { _definitions?: unknown })._definitions;
        const newDefs = (newViewer as { _definitions?: unknown })._definitions;

        if (!oldDefs || !newDefs) {
          toast.error("Failed to parse BPMN diagrams");
          return;
        }

        const differences = diff(oldDefs, newDefs);
        const oldCanvas = oldViewer.get("canvas");
        const newCanvas = newViewer.get("canvas");

        const diffStats = applyDiffMarkers(
          differences,
          oldCanvas,
          newCanvas,
          oldViewer.get("elementRegistry"),
          newViewer.get("elementRegistry")
        );

        setStats(diffStats);
        syncViewboxes(oldViewer, newViewer);
        fitViewports(oldCanvas, newCanvas);
      } catch {
        toast.error("Failed to load BPMN diff");
      }
    };

    loadDiff();

    return () => {
      oldViewer.destroy();
      newViewer.destroy();
    };
  }, [oldXml, newXml]);

  return (
    <div className={styles.wrapper} style={{ height }}>
      <style>{DIFF_STYLES}</style>
      <DiffStats {...stats} />
      <div className={styles.grid}>
        <div className={styles.column}>
          <h3 className={styles.title}>Original Version</h3>
          {/* INLINE STYLE: theme background color */}
          <div
            ref={oldContainerRef}
            className={styles.diagramContainer}
            style={{ backgroundColor: diagramTheme.container }}
          />
        </div>
        <div className={styles.column}>
          <h3 className={styles.title}>New Version</h3>
          {/* INLINE STYLE: theme background color */}
          <div
            ref={newContainerRef}
            className={styles.diagramContainer}
            style={{ backgroundColor: diagramTheme.container }}
          />
        </div>
      </div>
    </div>
  );
};

export default React.memo(BpmnDiffViewer);
