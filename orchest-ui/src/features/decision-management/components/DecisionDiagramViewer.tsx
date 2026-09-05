import GenericDiagramViewer from "@/shared/diagram/components/GenericDiagramViewer/GenericDiagramViewer";
import DmnViewer from "@/shared/diagram/components/DmnViewer/DmnViewer";
import React from "react";

export interface DecisionDiagramViewerProps {
  decisionId: string | null;
  version: string | null;
  diagramXml: string;
  isLoading?: boolean;
  height?: string;
  minHeight?: string;
}

const DecisionDiagramViewer: React.FC<DecisionDiagramViewerProps> = ({
  decisionId,
  version,
  diagramXml,
  isLoading = false,
  height = "min(50vh, 600px)",
  minHeight = "400px",
}) => {
  return (
    <GenericDiagramViewer
      id={decisionId}
      diagramXml={diagramXml}
      isLoading={isLoading}
      height={height}
      minHeight={minHeight}
      loadingText="Loading DMN diagram..."
      emptyStateConfig={{
        noSelection: {
          title: "Please select DMN ID in filterbar",
          description: "Choose a Version from the sidebar to view its DMN",
        },
        noData: {
          title: "DMN diagram not available",
          description: (id) => `No DMN diagram found for decision: ${id} (v${version})`,
        },
      }}
      viewerClassName="w-full h-full"
      containerClassName="flex-grow"
    >
      <DmnViewer xml={diagramXml} />
    </GenericDiagramViewer>
  );
};

export default React.memo(DecisionDiagramViewer);
