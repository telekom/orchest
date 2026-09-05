import { processDefinitionService } from "@/api/domains";
import { useIsDarkMode } from "@/shared/stores/uiStore";
import { DiagramViewer } from "@/shared/diagram/hooks/useDiagramViewer";
import { getDiagramTheme } from "@/shared/diagram/lib/DiagramTheme";
import clsx from "clsx";
import React, { useRef } from "react";
import { useLocation } from "react-router-dom";
import { ModelerProvider, useModelerContext } from "../../context/ModelerContext";
import { useBpmnModelerSetup } from "../../hooks/useBpmnModelerSetup";
import { useCodeGeneration } from "../../hooks/useCodeGeneration";
import { emptyBpmn } from "../../templates/emptyBpmn";
import BaseModeler from "../BaseModeler/BaseModeler";
import styles from "./BpmnModeler.module.css";

interface BpmnModelerProps {
  onSave?: (xml: string) => void;
  activeModeler?: string;
  onModelerTypeChange?: (value: string) => void;
}

const BpmnModelerContent: React.FC<BpmnModelerProps> = ({ onSave, activeModeler, onModelerTypeChange }) => {
  const location = useLocation();
  const isDarkMode = useIsDarkMode();
  const { showPropertiesPanel } = useModelerContext();

  // Priority: 1. Route state XML (from navigation), 2. Empty template
  // NO persistence - always start fresh or from route
  const initialXml = location.state?.bpmnXml || emptyBpmn;

  const containerRef = useRef<HTMLDivElement>(null);
  const propertiesPanelRef = useRef<HTMLDivElement>(null);

  // Initialize BPMN modeler (NO auto-save, no persistence)
  const { modelerRef: bpmnModelerRef, importXml } = useBpmnModelerSetup({
    initialXml,
    containerRef,
    propertiesPanelRef,
  });

  const { generateCode, isGenerating } = useCodeGeneration({
    modelerRef: bpmnModelerRef,
  });

  const diagramTheme = getDiagramTheme(isDarkMode);

  return (
    <BaseModeler
      diagramType="bpmn"
      itemType="process"
      modelerRef={bpmnModelerRef as React.RefObject<DiagramViewer | null>}
      emptyTemplate={emptyBpmn}
      definitionService={processDefinitionService as unknown as import("./BaseModeler").DefinitionService}
      onSave={onSave}
      onGenerateCode={generateCode}
      isGenerating={isGenerating}
      activeModeler={activeModeler}
      onModelerTypeChange={onModelerTypeChange}
      onImportXml={importXml}
    >
      <div className={styles.container}>
        <div
          ref={containerRef}
          className={clsx(styles.modelerContainer, 'bpmn-modeler-container', 'modeler-container')}
          style={{ backgroundColor: diagramTheme.container }}
        />
        <div
          ref={propertiesPanelRef}
          className={clsx(styles.propertiesPanel, 'properties-panel', {
            [styles.hidden]: !showPropertiesPanel
          })}
        />
      </div>
    </BaseModeler>
  );
};

const BpmnModeler: React.FC<BpmnModelerProps> = ({ onSave, activeModeler, onModelerTypeChange }) => {
  return (
    <ModelerProvider>
      <BpmnModelerContent onSave={onSave} activeModeler={activeModeler} onModelerTypeChange={onModelerTypeChange} />
    </ModelerProvider>
  );
};

export default BpmnModeler;
