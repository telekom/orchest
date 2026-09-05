import { SpinnerLoader } from "@/shared/components/Loader/Loader";
import GenericDiagramViewer from "@/shared/diagram/components/GenericDiagramViewer/GenericDiagramViewer";
import React, { lazy, Suspense } from "react";
import styles from "./ProcessDiagramViewer.module.css";
import "./ProcessDiagramViewer.css";

const BpmnViewer = lazy(
  () => import("@/features/process-management/components/BpmnViewer/BpmnViewer")
);

export interface ProcessDiagramViewerProps {
  processDefinitionId: string | null;
  diagramXml: string;
  isLoading?: boolean;
  height?: string;
  minHeight?: string;
}

const ProcessDiagramViewer: React.FC<ProcessDiagramViewerProps> = ({
  processDefinitionId,
  diagramXml,
  isLoading = false,
  height = "min(50vh, 600px)",
  minHeight = "300px",
}) => {
  return (
    <GenericDiagramViewer
        id={processDefinitionId}
        diagramXml={diagramXml}
        isLoading={isLoading}
        height={height}
        minHeight={minHeight}
        loadingText={`Loading BPMN diagram for ${processDefinitionId}...`}
        emptyStateConfig={{
          noSelection: {
            title: "Please select process in filterbar",
            description: "Choose a process from the sidebar to view its BPMN diagram",
          },
          noData: {
            title: "BPMN diagram not available",
            description: (id) => `No BPMN diagram found for process: ${id}`,
          },
        }}
        viewerClassName={`${styles.viewerContainer} bpmn-container process-list-bpmn`}
      >
        <Suspense fallback={<div className={styles.loadingContainer}><SpinnerLoader size="lg" /></div>}>
          <BpmnViewer
            xml={diagramXml}
            state="ACTIVE"
            processInstanceId={null}
            processDefinitionId={processDefinitionId}
            sequenceExecutions={{}}
            showInstanceActions={false}
            className="process-diagram-viewer-wrapper"
          />
        </Suspense>
      </GenericDiagramViewer>  
  );
};

export default React.memo(ProcessDiagramViewer);
