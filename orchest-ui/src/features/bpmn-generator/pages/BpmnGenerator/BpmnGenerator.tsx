import { aiService as api } from "@/api/external";
import { toast } from "@/design-system/components/ui/sonner";
import { TOAST_MESSAGES } from "@/shared/constants";
import OrchestBpmnThemeModule from "@/shared/diagram/bpmn-theme";
import { getBpmnRendererConfig, getDiagramTheme } from "@/shared/diagram/lib/DiagramTheme";
import { useIsDarkMode } from "@/shared/stores/uiStore";
import { logger } from '@/shared/utils/logger';
import { Button } from "@/design-system/components/ui/button";
import BpmnModeler from "bpmn-js/lib/Modeler";
import clsx from "clsx";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import BpmnGeneratorChat from "../../components/BpmnGeneratorChat/BpmnGeneratorChat";
import type { BpmnCanvas, BpmnJsonData, BpmnProcess } from "../../types/bpmn.types";
import styles from "./BpmnGenerator.module.css";

// Pure functions for BPMN modeler management
const createModeler = (container: HTMLDivElement, isDarkMode: boolean): BpmnModeler => {
  return new BpmnModeler({
    container,
    bpmnRenderer: getBpmnRendererConfig(isDarkMode),
    additionalModules: [OrchestBpmnThemeModule],
  });
};

const fitViewport = (modeler: BpmnModeler | null) => {
  const canvas = modeler?.get("canvas") as BpmnCanvas | undefined;
  canvas?.zoom("fit-viewport");
};

const processDiagram = async (bpmnDiagram: string) => {
  try {
    const result = await api.processBpmnDiagram(bpmnDiagram);
    return result.layoutedXml;
  } catch (error) {
    logger.error("Failed to process the diagram:", error);
    return null;
  }
};

const createBpmnJson = async (xml: string) => {
  try {
    const json = await api.createBpmnJson(xml);
    toast.success(TOAST_MESSAGES.SUCCESS.BPMN_UPLOADED);
    return json as unknown as BpmnJsonData;
  } catch (error) {
    logger.error("Error creating BPMN JSON:", error);
    toast.error(TOAST_MESSAGES.ERROR.LOADING_BPMN_FILE);
    return null;
  }
};

const BpmnGenerator: React.FC = () => {
  const navigate = useNavigate();
  const canvasRef = useRef<HTMLDivElement>(null);
  const modelerRef = useRef<BpmnModeler | null>(null);
  const [bpmnXml, setBpmnXml] = useState("");
  const [process, setProcess] = useState<BpmnProcess | BpmnJsonData | null>(null);
  const [canvasExpanded, setCanvasExpanded] = useState(false);
  const isDarkMode = useIsDarkMode();
  const diagramTheme = getDiagramTheme(isDarkMode);

  useEffect(() => {
    if (canvasRef.current) {
      modelerRef.current?.destroy();
      modelerRef.current = createModeler(canvasRef.current, isDarkMode);
    }

    return () => {
      modelerRef.current?.destroy();
    };
  }, [isDarkMode]);

  const handleDrop = async (event: React.DragEvent) => {
    event.preventDefault();
    const items = event.dataTransfer.items;
    if (!items) return;

    for (let i = 0; i < items.length; i++) {
      if (items[i].kind === "file") {
        const file = items[i].getAsFile();
        if (file && file.name.endsWith(".bpmn")) {
          const reader = new FileReader();
          reader.onload = async (e) => {
            const xmlContent = e.target?.result as string;
            try {
              await modelerRef.current?.importXML(xmlContent);
              fitViewport(modelerRef.current);
              setBpmnXml(xmlContent);
              const json = await createBpmnJson(xmlContent);
              if (json) {
                setProcess(json);
              }
            } catch (err) {
              logger.error("Failed to import BPMN diagram:", err);
            }
          };
          reader.readAsText(file);
        }
      }
    }
  };

  // Memoized to prevent unnecessary re-renders of BpmnGeneratorChat
  // Only depends on isDarkMode which changes infrequently
  const handleBpmnXml = useCallback(
    async (bpmnXmlValue: string) => {
      if (bpmnXmlValue === "") {
        modelerRef.current?.destroy();
        if (canvasRef.current) {
          modelerRef.current = createModeler(canvasRef.current, isDarkMode);
        }
        return;
      }

      try {
        const layoutedXml = await processDiagram(bpmnXmlValue);
        if (!layoutedXml) throw new Error("Failed to layout BPMN diagram");
        setBpmnXml(layoutedXml);

        await modelerRef.current?.importXML(layoutedXml);
        fitViewport(modelerRef.current);
      } catch (error) {
        logger.error("Error handling BPMN XML:", error);
      }
    },
    [isDarkMode]
  );

  const downloadBpmnFile = async () => {
    const result = await modelerRef.current?.saveXML();
    if (!result || !result.xml) {
      logger.error("Failed to save XML");
      return;
    }
    const { xml } = result;
    const blob = new Blob([xml], { type: "text/xml" });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "diagram.bpmn";
    a.click();
  };

  const handleEditBpmnXml = () => {
    navigate("/modeler", { state: { bpmnXml } });
  };

  const toggleCanvasExpanded = () => {
    setCanvasExpanded((prev) => !prev);
  };

  return (
    <div className={styles.container}>
      <div className={styles.gridLayout}>
        <div className={styles.chatColumn}>
          <BpmnGeneratorChat
            onBpmnXmlReceived={handleBpmnXml}
            onBpmnJsonReceived={setProcess}
            onDownload={downloadBpmnFile}
            isDownloadReady={!!bpmnXml}
            process={process || {}}
          />
        </div>

        <div className={styles.previewPanel}>
          <div className={styles.previewHeader}>
            <div className={styles.headerTitle}>
              <span className={styles.title}>
                Preview
              </span>
            </div>

            <div className={styles.headerActions}>
              <Button
                variant="ghost"
                size="sm"
                buttonType="iconOnly"
                buttonIcon="maximize-screen"
                onClick={toggleCanvasExpanded}
                className={styles.odsIconButton}
                aria-label="Expand preview"
              />
              <Button
                className={clsx(styles.editButton)}
                variant="default"
                size="sm"
                label="Edit"
                buttonIcon="edit"
                onClick={handleEditBpmnXml}
              />
            </div>
          </div>

          {canvasExpanded && (
            <div
              className={styles.modalOverlay}
              aria-labelledby="dialog-title"
              role="dialog"
              aria-modal="true"
            >
              <div
                className={styles.backdrop}
                aria-hidden="true"
              />
            </div>
          )}

          <div
            className={
              canvasExpanded
                ? styles.canvasWrapperExpanded
                : styles.canvasWrapper
            }
          >
            {canvasExpanded && (
              <Button
                className={clsx(styles.closeButton)}
                onClick={toggleCanvasExpanded}
                variant="ghost"
                size="sm"
                buttonType="iconOnly"
                buttonIcon="close"
                aria-label="Close preview"
              />
            )}
            <div
              ref={canvasRef}
              id="canvas"
              className={`canvas-container bpmn-preview ${styles.canvas} ${canvasExpanded ? styles.canvasExpanded : ''}`}
              style={{ backgroundColor: diagramTheme.container }} // INLINE STYLE: Dynamic theme background color
              onDrop={handleDrop}
              onDragOver={(e) => e.preventDefault()}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default BpmnGenerator;
