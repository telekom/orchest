import { StandardModal } from "@/shared/components/StandardModal/StandardModal";
import { AlertCircle, Info, FileCode } from "lucide-react";
import React, { useMemo } from "react";
import { useDefinitionDiff } from "../../hooks/useDefinitionDiff";
import { extractDefinitionId } from "../../utils/xmlHelpers";
import BpmnDiffViewer from "../BpmnDiffViewer/BpmnDiffViewer";
import styles from "./DiagramDiffModal.module.css";

interface DiagramDiffModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  diagramType: "bpmn" | "dmn";
  currentXml: string;
  isDeploying?: boolean;
}

const DiagramDiffModal: React.FC<DiagramDiffModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  diagramType,
  currentXml,
  isDeploying = false,
}) => {
  const currentDefinitionId = useMemo(
    () => extractDefinitionId(currentXml, diagramType),
    [currentXml, diagramType]
  );

  const { originalXml, error, hasOriginal, isLoading } = useDefinitionDiff({
    type: diagramType,
    selectedDefinition: currentDefinitionId || '',
    enabled: isOpen && !!currentDefinitionId,
  });

  const canShowDiff = hasOriginal && originalXml && !error;
  const isFirstVersion = !isLoading && !hasOriginal && !error;

  return (
    <StandardModal
      isOpen={isOpen}
      onClose={onClose}
      title={
        diagramType === "bpmn"
          ? "Review Changes Before Requesting Approval"
          : "Request Deployment Approval"
      }
      size="fullscreen"
      onConfirm={onConfirm}
      confirmText="Request Approval"
      confirmLoading={isDeploying}
      confirmDisabled={isDeploying}
    >
      <div className={styles.container}>
          {diagramType === "bpmn" && error && (
            <div className={`${styles.alert} ${styles.errorAlert}`}>
              <AlertCircle className={styles.errorIcon} />
              <div className={styles.alertContent}>
                <h4 className={styles.errorTitle}>
                  Failed to load original version for comparison
                </h4>
                <p className={styles.errorText}>
                  You can still deploy without viewing the diff
                </p>
              </div>
            </div>
          )}

          {diagramType === "bpmn" && isFirstVersion && (
            <div className={`${styles.alert} ${styles.infoAlert}`}>
              <Info className={styles.infoIcon} />
              <div className={styles.alertContent}>
                <h4 className={styles.infoTitle}>
                  No previous version available for comparison
                </h4>
                <p className={styles.infoText}>
                  This will be deployed as the first version of this process
                </p>
              </div>
            </div>
          )}

          {diagramType === "bpmn" && isLoading && (
            <div className={styles.loadingContainer}>
              <div className={styles.loadingContent}>
                <div className={styles.spinner}></div>
                <p className={styles.loadingText}>
                  Loading previous version for comparison...
                </p>
              </div>
            </div>
          )}

          {diagramType === "bpmn" && canShowDiff && (
            <div className={styles.diffViewerWrapper}>
              <BpmnDiffViewer
                oldXml={originalXml}
                newXml={currentXml}
                height="60vh"
              />
            </div>
          )}

          {diagramType === "dmn" && (
            <div className={styles.dmnCard}>
              <div className={styles.dmnContent}>
                <div className={styles.dmnIconWrapper}>
                  <FileCode className={styles.dmnIcon} />
                </div>
                <div className={styles.dmnTextContent}>
                  <h3 className={styles.dmnTitle}>
                    Deploy DMN Decision Model
                  </h3>
                  <p className={styles.dmnDescription}>
                    This will create a new version of your decision model in the system.
                  </p>
                </div>
              </div>
            </div>
          )}
      </div>
    </StandardModal>
  );
};

export default React.memo(DiagramDiffModal);
