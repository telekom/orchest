import { toast } from "@/design-system/components/ui/sonner";
import { useAuth, UserRoles } from "@/shared/auth";
import { ConfirmationDialog } from "@/shared/components";
import DiagramDiffModal from "@/shared/diagram/components/DiagramDiffModal/DiagramDiffModal";
import { useDefinitionLoader } from "@/shared/diagram/hooks/useDefinitionLoader";
import { DiagramViewer } from "@/shared/diagram/hooks/useDiagramViewer";
import { useExport } from "@/shared/diagram/hooks/useExport";
import { useFileImport } from "@/shared/diagram/hooks/useFileImport";
import { useModelerActions } from "@/shared/diagram/hooks/useModelerActions";
import { useUIStore } from "@/shared/stores/uiStore";
import { logger } from "@/shared/utils/logger";
import React, { ReactNode, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { ACCEPTED_FILE_TYPES, DIALOG_MESSAGES, EXPORT_FORMATS } from "../../constants/modelerConfig";
import { useModelerContext } from "../../context/ModelerContext";
import { useValidation } from "../../hooks/useValidation";
import { CodeViewer } from "../CodeViewer/CodeViewer";
import { DmnEvaluateModal } from "../DmnEvaluateModal/DmnEvaluateModal";
import { ValidationPanel } from "../ValidationPanel/ValidationPanel";
import ModelerToolbar from "../ModelerToolbar/ModelerToolbar";
import styles from "./BaseModeler.module.css";

export type DiagramType = "bpmn" | "dmn" | "form";
export type ItemType = "process" | "decision" | "form";

interface DefinitionDTO {
  definitionId: string;
  version?: number | string;
  resourceXML?: string;
  [key: string]: unknown;
}

interface DefinitionResponse {
  content: DefinitionDTO[];
  [key: string]: unknown;
}

interface DefinitionWithXML {
  resourceXML?: string;
  [key: string]: unknown;
}

export interface DefinitionService {
  getDefinitions?: (params: { size: number }) => Promise<DefinitionResponse>;
  getProcessDefinitions?: (params: { size: number }) => Promise<DefinitionResponse>;
  getDecisionDefinitions?: (params: { size: number }) => Promise<DefinitionResponse>;
  getProcessDefinition?: (definitionId: string, version: number) => Promise<DefinitionWithXML>;
  getProcessDefinitionByVersion?: (definitionId: string, version: number) => Promise<DefinitionWithXML>;
  getDecisionDefinitionByVersion?: (definitionId: string, version: number) => Promise<DefinitionWithXML>;
  [key: string]: unknown;
}

interface BaseModelerProps {
  diagramType: DiagramType;
  itemType: ItemType;
  modelerRef: React.RefObject<DiagramViewer | null>;
  emptyTemplate: string | object;
  definitionService?: DefinitionService;
  onSave?: (xml: string) => void;
  onImportSuccess?: () => void;
  onImportStart?: () => void;
  onImportError?: () => void;
  onGenerateCode?: () => void;
  isGenerating?: boolean;
  customExportFormats?: Array<{ value: string; label: string }>;
  disablePropertiesPanel?: boolean;
  activeModeler?: string;
  onModelerTypeChange?: (value: string) => void;
  onImportXml?: (xml: string) => Promise<void>;
  children: ReactNode;
}

const getDiagramConfig = (type: DiagramType) => ({
  exportFormats: EXPORT_FORMATS[type.toUpperCase() as keyof typeof EXPORT_FORMATS],
  dialogMessages: DIALOG_MESSAGES.IMPORT[type.toUpperCase() as keyof typeof DIALOG_MESSAGES.IMPORT],
  acceptedFileTypes: ACCEPTED_FILE_TYPES[type.toUpperCase() as keyof typeof ACCEPTED_FILE_TYPES],
});

const BaseModeler: React.FC<BaseModelerProps> = ({
  diagramType,
  itemType,
  modelerRef,
  emptyTemplate,
  definitionService,
  onSave,
  onImportSuccess,
  onImportStart,
  onImportError,
  onGenerateCode,
  isGenerating = false,
  customExportFormats,
  disablePropertiesPanel = false,
  activeModeler,
  onModelerTypeChange,
  onImportXml,
  children,
}) => {
  const { hasRole } = useAuth();
  const canDeploy = hasRole(UserRoles.ADMIN, true);

  const {
    selectedDefinition,
    selectedVersion,
    setSelectedVersion,
    isDiffModalOpen,
    openDiffModal,
    closeDiffModal,
    isUploadDialogOpen,
    openUploadDialog,
    closeUploadDialog,
    isEvaluateModalOpen,
    openEvaluateModal,
    closeEvaluateModal,
    showPropertiesPanel,
    setShowPropertiesPanel,
    showCodeView,
    setShowCodeView,
    isDeploying,
    setIsDeploying,
    currentXml,
    setCurrentXml,
    handleDefinitionChange,
    handleVersionChange,
    resetState,
  } = useModelerContext();

  const lastDiagramXmlRef = useRef<string>("");

  const hasDefinitionService = definitionService && itemType !== "form";

  const fetchService = useMemo(() => {
    if (!hasDefinitionService || !definitionService) return null;

    return {
      getDefinitions: (params: { size: number }): Promise<DefinitionResponse> => {
        if (itemType === "process") {
          return definitionService.getProcessDefinitions?.(params) ?? Promise.resolve({ content: [] });
        }
        if (itemType === "decision") {
          return definitionService.getDecisionDefinitions?.(params) ?? Promise.resolve({ content: [] });
        }
        return definitionService.getDefinitions!(params);
      },
    };
  }, [hasDefinitionService, definitionService, itemType]);

  const { definitions, versions, loadingDefinitions } = useDefinitionLoader({
    type: itemType,
    fetchService,
    enabled: hasDefinitionService,
    selectedDefinition,
  });

  const { fileInputRef, handleFileSelect, handleDragOver, handleDragLeave, handleDrop } =
    useFileImport({
      modelerRef,
      fileType: diagramType,
      onImportSuccess: () => {
        closeUploadDialog();
        onImportSuccess?.();
      },
      onImportStart,
      onImportError,
    });

  const { handleExport: exportDiagram } = useExport({
    modelerRef,
    diagramType,
  });

  const isNewDialogOpen = useUIStore((state) => state.modals.newDiagramDialog);
  const openNewDialog = useUIStore((state) => state.openModal);
  const closeNewDialog = useUIStore((state) => state.closeModal);

  const { handleUndo, handleRedo, handleNew, getCurrentXml, handleDeploy } = useModelerActions({
    modelerRef,
    diagramType,
    emptyTemplate,
    onSave,
    setSelectedVersion,
  });

  const { issues: validationIssues, hasErrors: validationHasErrors, validate, clearValidation } = useValidation();
  const [showValidationPanel, setShowValidationPanel] = useState(false);
  const [compensateFlow, setCompensateFlow] = useState(false);
  const [isCompensateFlowConfirmOpen, setIsCompensateFlowConfirmOpen] = useState(false);

  const handleSave = async () => {
    const xml = await getCurrentXml();
    if (!xml) return;
    setCurrentXml(xml);

    const issues = await validate(modelerRef.current, diagramType, xml);
    if (issues.some((i) => i.severity === 'error')) {
      setShowValidationPanel(true);
      return;
    }
    if (issues.length > 0) {
      setShowValidationPanel(true);
      return;
    }
    openDiffModal();
  };

  const handleDismissValidation = useCallback(() => {
    setShowValidationPanel(false);
  }, []);

  const handleDeployAnyway = useCallback(() => {
    setShowValidationPanel(false);
    clearValidation();
    openDiffModal();
  }, [clearValidation, openDiffModal]);

  const handleCompensateFlowToggleRequest = useCallback((next: boolean) => {
    if (next) {
      setIsCompensateFlowConfirmOpen(true);
      return;
    }
    setCompensateFlow(false);
  }, []);

  const handleItemChange = useCallback(
    (value: string) => {
      setCompensateFlow(false);
      handleDefinitionChange(value);
    },
    [handleDefinitionChange]
  );

  const handleDiffConfirm = async () => {
    setIsDeploying(true);
    try {
      await handleDeploy(
        currentXml,
        diagramType === "bpmn" ? { compensateFlow } : undefined
      );
      setCompensateFlow(false);
      closeDiffModal();
    } catch (error) {
      logger.error("Failed to deploy:", error);
    } finally {
      setIsDeploying(false);
    }
  };

  const handleToggleCodeView = useCallback(async () => {
    if (!showCodeView) {
      const xml = await getCurrentXml();
      if (xml) {
        lastDiagramXmlRef.current = xml;
        setCurrentXml(xml);
      }
    } else {
      const modeler = modelerRef.current;
      if (modeler && (diagramType === "bpmn" || diagramType === "dmn")) {
        const hasChanges = currentXml !== lastDiagramXmlRef.current;

        if (hasChanges && currentXml) {
          try {
            await modeler.importXML(currentXml);
            toast.success("XML changes applied successfully");
          } catch (error) {
            logger.error("Error importing XML from code view:", error);
            toast.error("Failed to apply XML changes. Check console for details.");
          }
        }
      }
    }
    setShowCodeView(!showCodeView);
  }, [
    showCodeView,
    getCurrentXml,
    modelerRef,
    diagramType,
    currentXml,
    setCurrentXml,
    setShowCodeView
  ]);

  const handleCodeViewXmlChange = useCallback((xml: string) => {
    setCurrentXml(xml);
  }, [setCurrentXml]);

  const handleVersionSelect = useCallback(async (version: string) => {

    if (!version || !selectedDefinition || !definitionService) {
      logger.warn('handleVersionSelect: missing parameters', { version, selectedDefinition, definitionService: !!definitionService });
      return;
    }

    logger.info(`Loading ${itemType} ${selectedDefinition} version ${version}`);
    handleVersionChange(version);

    try {
      const versionNum = parseInt(version, 10);
      const response = itemType === "process"
        ? await definitionService.getProcessDefinition?.(selectedDefinition, versionNum)
        : await definitionService.getDecisionDefinitionByVersion?.(selectedDefinition, versionNum);

      if (response?.resourceXML) {
        logger.info('Loading definition XML...');

        if (diagramType === "bpmn" || diagramType === "dmn") {
          try {
            // Use the specialized importXml function if available (prevents double-import issues)
            if (onImportXml) {
              await onImportXml(response.resourceXML);
            } else {
              // Fallback to direct modeler access (shouldn't happen with new setup)
              const modeler = modelerRef.current;
              if (!modeler) {
                logger.error("Modeler not initialized when trying to load definition");
                toast.error("Modeler is still loading. Please try again in a moment.");
                return;
              }

              if (!('importXML' in modeler) || typeof modeler.importXML !== 'function') {
                throw new Error('Modeler does not have importXML method');
              }

              await modeler.importXML(response.resourceXML);

              // For DMN, need to open the first available view after import
              if (diagramType === "dmn" && 'getViews' in modeler && 'open' in modeler) {
                try {
                  const views = (modeler as any).getViews();
                  if (views && views.length > 0) {
                    await (modeler as any).open(views[0]);
                  }
                } catch (dmnError) {
                  logger.warn('Could not open DMN view:', dmnError);
                }
              }

              // Zoom to fit the diagram in viewport (only for BPMN)
              if (diagramType === "bpmn") {
                try {
                  if ('get' in modeler && typeof modeler.get === 'function') {
                    const canvas = modeler.get('canvas');
                    if (canvas && typeof canvas.zoom === 'function') {
                      canvas.zoom('fit-viewport');
                    }
                  }
                } catch (zoomError) {
                  logger.warn('Could not zoom canvas:', zoomError);
                }
              }
            }

            // Persist the loaded XML to store for state preservation
            setCurrentXml(response.resourceXML);

            const itemLabel = itemType === "process" ? "Process" : "Decision";
            toast.success(`${itemLabel} ${selectedDefinition} version ${version} loaded successfully`);

            logger.info(`Successfully loaded ${diagramType.toUpperCase()} definition`);
          } catch (importError) {
            logger.error(`Error importing ${diagramType.toUpperCase()}:`, importError);
            toast.error(`Failed to import ${diagramType.toUpperCase()}. Check console for details.`);
          }
        }
      } else {
        toast.error(`No ${diagramType.toUpperCase()} XML found for this definition`);
      }
    } catch (error) {
      logger.error("Error loading version:", error);
      toast.error("Failed to load version");
    }
  }, [
    selectedDefinition,
    definitionService,
    handleVersionChange,
    itemType,
    modelerRef,
    diagramType,
    setCurrentXml,
    onImportXml
  ]);

  // Auto-load first version when definitions are loaded and no version is selected
  useEffect(() => {
    // Only auto-load if modeler is ready
    if (versions.length > 0 && !selectedVersion && selectedDefinition && modelerRef.current) {
      // Small delay to ensure modeler is fully initialized
      const timer = setTimeout(() => {
        handleVersionSelect(versions[0]);
      }, 100);
      return () => clearTimeout(timer);
    }
  }, [versions, selectedVersion, selectedDefinition, modelerRef, handleVersionSelect]);

  const config = getDiagramConfig(diagramType);
  const exportFormats = customExportFormats || config.exportFormats;
  const { TITLE: dialogTitle, DRAG_DROP_TEXT: dragDropText } = config.dialogMessages;

  return (
    <div className={styles.container}>
      {hasDefinitionService && (diagramType === "bpmn" || diagramType === "dmn") && (
        <DiagramDiffModal
          isOpen={isDiffModalOpen}
          onClose={closeDiffModal}
          onConfirm={handleDiffConfirm}
          diagramType={diagramType}
          currentXml={currentXml}
          isDeploying={isDeploying}
        />
      )}

      {diagramType === "dmn" && selectedDefinition && (
        <DmnEvaluateModal
          isOpen={isEvaluateModalOpen}
          onClose={closeEvaluateModal}
          decisionId={selectedDefinition}
          version={selectedVersion ? parseInt(selectedVersion, 10) : 1}
        />
      )}

      <ConfirmationDialog
        open={isNewDialogOpen}
        onOpenChange={(open) => (open ? openNewDialog('newDiagramDialog') : closeNewDialog('newDiagramDialog'))}
        title="Create New Diagram?"
        description="Are you sure you want to create a new diagram? Any unsaved changes will be lost."
        onConfirm={() => {
          resetState();
          setCompensateFlow(false);
          handleNew();
          closeNewDialog('newDiagramDialog');
        }}
        confirmText="Create New"
        cancelText="Cancel"
      />

      <ConfirmationDialog
        open={isCompensateFlowConfirmOpen}
        onOpenChange={setIsCompensateFlowConfirmOpen}
        title="Deploy as compensation flow?"
        description="This will not change your existing flow. It will create a complete new process with the same process ID, but as a compensation flow."
        onConfirm={() => setCompensateFlow(true)}
        confirmText="Confirm"
        cancelText="Cancel"
      />

      <ModelerToolbar
        onNew={() => openNewDialog('newDiagramDialog')}
        onSave={handleSave}
        onUndo={handleUndo}
        onRedo={handleRedo}
        onExport={exportDiagram}
        exportFormats={exportFormats}
        isUploadDialogOpen={isUploadDialogOpen}
        setIsUploadDialogOpen={(open) => (open ? openUploadDialog() : closeUploadDialog())}
        onFileSelect={handleFileSelect}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        fileInputRef={fileInputRef}
        acceptedFileTypes={config.acceptedFileTypes}
        dialogTitle={dialogTitle}
        dragDropText={dragDropText}
        selectedItem={selectedDefinition}
        selectedVersion={selectedVersion}
        items={definitions || []}
        versions={versions || []}
        loadingItems={loadingDefinitions}
        onItemChange={handleItemChange}
        onVersionChange={handleVersionSelect}
        itemType={itemType}
        showPropertiesPanel={showPropertiesPanel}
        setShowPropertiesPanel={setShowPropertiesPanel}
        disablePropertiesPanel={disablePropertiesPanel}
        showCodeView={showCodeView}
        onToggleCodeView={handleToggleCodeView}
        onGenerateCode={onGenerateCode}
        isGenerating={isGenerating}
        canDeploy={canDeploy}
        showCompensateFlowToggle={diagramType === "bpmn"}
        compensateFlow={compensateFlow}
        onCompensateFlowToggleRequest={handleCompensateFlowToggleRequest}
        activeModeler={activeModeler}
        onModelerTypeChange={onModelerTypeChange}
        onEvaluate={diagramType === "dmn" && selectedDefinition ? openEvaluateModal : undefined}
      />

      {showValidationPanel && validationIssues.length > 0 && (
        <ValidationPanel
          issues={validationIssues}
          onDismiss={handleDismissValidation}
          onDeployAnyway={!validationHasErrors ? handleDeployAnyway : undefined}
          hasErrors={validationHasErrors}
        />
      )}

      <div className={showCodeView ? styles.hidden : styles.diagramView}>
        {children}
      </div>

      {showCodeView && (
        <div className={styles.codeView}>
          <CodeViewer
            xml={currentXml}
            onXmlChange={handleCodeViewXmlChange}
            readOnly={false}
          />
        </div>
      )}
    </div>
  );
};

export default BaseModeler;
