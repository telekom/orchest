import { processInstanceService } from "@/api/domains";
import { ProcessInstance } from "@/shared/types";
import { EmptyState } from "@/shared/components/EmptyState/EmptyState";
import { SpinnerLoader } from "@/shared/components/Loader/Loader";
import { TIMING, TOAST_MESSAGES } from "@/shared/constants";
import { handleApiOperation } from "@/shared/utils/apiErrorUtils";
import React, { lazy, Suspense } from "react";
import styles from "./DiagramViewer.module.css";

const BpmnViewer = lazy(
  () => import("@/features/process-management/components/BpmnViewer/BpmnViewer")
);

interface DiagramViewerProps {
  diagramLoading: boolean;
  diagramXml: string;
  instance: ProcessInstance | null;
  showInstanceActions: boolean;
  onInstanceModified: () => void;
  selectedNodeId?: string | null;
  variableContext?: Record<string, unknown>;
}

const DiagramViewer: React.FC<DiagramViewerProps> = ({
  diagramLoading,
  diagramXml,
  instance,
  showInstanceActions,
  onInstanceModified,
  selectedNodeId,
  variableContext,
}) => {
  const handleInstanceModification = async (
    sourceId: string,
    targetId: string
  ) => {
    if (!instance) return;

    await handleApiOperation(
      () => processInstanceService.modifyInstance({
        processInstanceId: instance.processId,
        fromNodeId: sourceId,
        toNodeId: targetId,
      }),
      {
        operation: "move instance",
        successMessage: TOAST_MESSAGES.SUCCESS.UPDATE,
        errorMessage: TOAST_MESSAGES.ERROR.GENERIC,
      }
    );
  };

  const handleInstanceCancellation = async () => {
    if (!instance) return;

    const result = await handleApiOperation(
      () => processInstanceService.cancelInstance({
        instances: [
          {
            instanceId: instance.processId,
            processDefinitionId: instance.processName,
          },
        ],
      }),
      {
        operation: "cancel instance",
        successMessage: TOAST_MESSAGES.SUCCESS.DELETE,
        errorMessage: TOAST_MESSAGES.ERROR.GENERIC,
      }
    );

    if (result.success) {
      setTimeout(() => {
        onInstanceModified();
      }, TIMING.INSTANCE_MODIFICATION_DELAY);
    }
  };

  if (diagramLoading) {
    return (
      <div className={styles.diagramContainer}>
        <div className={styles.centerContent}>
          <SpinnerLoader size="lg" />
        </div>
      </div>
    );
  }

  if (!diagramXml) {
    return (
      <div className={styles.diagramContainer}>
        <EmptyState title="No diagram available" />
      </div>
    );
  }

  if (!instance) {
    return (
      <div className={styles.centerContent}>
        <SpinnerLoader size="lg" />
      </div>
    );
  }

  return (
    <div className={styles.relativeContainer}>
      <div className={styles.bpmnContainer}>
        <Suspense fallback={<div className={styles.centerContent}><SpinnerLoader size="lg" /></div>}>
          <BpmnViewer
            xml={diagramXml}
            state={instance.status}
            lastActivityAt={instance.lastActivityAt}
            activeElements={instance.activeElements}
            processInstanceId={instance.processId}
            processDefinitionId={instance.processName}
            sequenceExecutions={instance.sequenceExecutions}
            showInstanceActions={showInstanceActions}
            showBrandMark
            focusExecutedPath
            onInstanceModification={handleInstanceModification}
            onInstanceCancellation={handleInstanceCancellation}
            selectedNodeId={selectedNodeId}
            variableContext={variableContext}
          />
        </Suspense>
      </div>
    </div>
  );
};

export default React.memo(DiagramViewer);
