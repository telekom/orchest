import { DiagramControlBar } from "@/shared/diagram/components/DiagramControlBar/DiagramControlBar";
import { ExportFormat } from "@/shared/enums";
import { memo, ReactNode } from "react";

interface BpmnControlBarProps {
  showCancelButton?: boolean;
  showRetryButton?: boolean;
  showRaiseIncidentButton?: boolean;
  showCompensateButton?: boolean;
  onZoomReset: () => void;
  onZoomIn?: () => void;
  onZoomOut?: () => void;
  onExport: (format: ExportFormat) => void;
  cancelButton?: ReactNode;
  retryButton?: ReactNode;
  raiseIncidentButton?: ReactNode;
  compensateButton?: ReactNode;
}

const BpmnControlBarComponent = ({
  showCancelButton,
  showRetryButton,
  showRaiseIncidentButton,
  showCompensateButton,
  onZoomReset,
  onZoomIn,
  onZoomOut,
  onExport,
  cancelButton,
  retryButton,
  raiseIncidentButton,
  compensateButton,
}: BpmnControlBarProps) => {
  const additionalButtons = (
    <>
      {showRaiseIncidentButton && raiseIncidentButton}

      {showCompensateButton && compensateButton}

      {showCancelButton && cancelButton}

      {showRetryButton && retryButton}
    </>
  );

  return (
    <DiagramControlBar
      onZoomReset={onZoomReset}
      onZoomIn={onZoomIn}
      onZoomOut={onZoomOut}
      onExport={onExport}
      additionalButtons={additionalButtons}
      className="bpmn-controls"
    />
  );
};

export const BpmnControlBar = memo(BpmnControlBarComponent);
