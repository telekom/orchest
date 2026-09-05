import { STORAGE_KEYS } from "@/shared/constants";
import { useUIStore } from "@/shared/stores/uiStore";
import React, { createContext, ReactNode, useCallback, useContext, useMemo, useState } from "react";

export type DiagramType = "bpmn" | "dmn" | "form";

const PROPERTIES_PANEL_STORAGE_KEY = STORAGE_KEYS.MODELER_PROPERTIES_PANEL;

const getStoredPropertiesPanelState = (defaultValue: boolean): boolean => {
  try {
    const stored = localStorage.getItem(PROPERTIES_PANEL_STORAGE_KEY);
    return stored !== null ? JSON.parse(stored) : defaultValue;
  } catch {
    return defaultValue;
  }
};

const setStoredPropertiesPanelState = (value: boolean): void => {
  try {
    localStorage.setItem(PROPERTIES_PANEL_STORAGE_KEY, JSON.stringify(value));
  } catch {
    // Ignore localStorage errors
  }
};

interface ModelerContextValue {
  selectedDefinition: string;
  setSelectedDefinition: (id: string) => void;
  selectedVersion: string;
  setSelectedVersion: (version: string) => void;

  isDiffModalOpen: boolean;
  openDiffModal: () => void;
  closeDiffModal: () => void;
  isUploadDialogOpen: boolean;
  openUploadDialog: () => void;
  closeUploadDialog: () => void;
  isEvaluateModalOpen: boolean;
  openEvaluateModal: () => void;
  closeEvaluateModal: () => void;

  showPropertiesPanel: boolean;
  setShowPropertiesPanel: (show: boolean) => void;

  showCodeView: boolean;
  setShowCodeView: (show: boolean) => void;

  isDeploying: boolean;
  setIsDeploying: (deploying: boolean) => void;

  currentXml: string;
  setCurrentXml: (xml: string) => void;

  handleDefinitionChange: (value: string) => void;
  handleVersionChange: (value: string) => void;
  resetState: () => void;
}

const ModelerContext = createContext<ModelerContextValue | undefined>(undefined);

interface ModelerProviderProps {
  children: ReactNode;
  defaultShowPropertiesPanel?: boolean;
}

export const ModelerProvider: React.FC<ModelerProviderProps> = ({
  children,
  defaultShowPropertiesPanel = true,
}) => {
  // Local state for definition/version selection (not persisted)
  const [selectedDefinition, setSelectedDefinition] = useState<string>("");
  const [selectedVersion, setSelectedVersion] = useState<string>("");
  const [currentXml, setCurrentXml] = useState<string>("");

  // Get modal states from UIStore
  const isDiffModalOpen = useUIStore((state) => state.modals.diffModal);
  const isUploadDialogOpen = useUIStore((state) => state.modals.uploadDialog);
  const isEvaluateModalOpen = useUIStore((state) => state.modals.evaluateDecisionModal);
  const openModal = useUIStore((state) => state.openModal);
  const closeModal = useUIStore((state) => state.closeModal);

  const [showPropertiesPanel, setShowPropertiesPanelState] = useState(() =>
    getStoredPropertiesPanelState(defaultShowPropertiesPanel)
  );

  const setShowPropertiesPanel = useCallback((show: boolean) => {
    setShowPropertiesPanelState(show);
    setStoredPropertiesPanelState(show);
  }, []);

  const [showCodeView, setShowCodeView] = useState(false);

  const [isDeploying, setIsDeploying] = useState(false);

  const handleDefinitionChange = useCallback((value: string) => {
    setSelectedDefinition(value);
    setSelectedVersion(""); // Reset version when definition changes
  }, []);

  const handleVersionChange = useCallback((value: string) => {
    setSelectedVersion(value);
  }, []);

  const handleSetCurrentXml = useCallback((xml: string) => {
    setCurrentXml(xml);
    // NO persistence - just update local state
  }, []);

  const openDiffModal = useCallback(() => {
    openModal('diffModal');
  }, [openModal]);

  const closeDiffModal = useCallback(() => {
    closeModal('diffModal');
  }, [closeModal]);

  const openUploadDialog = useCallback(() => {
    openModal('uploadDialog');
  }, [openModal]);

  const closeUploadDialog = useCallback(() => {
    closeModal('uploadDialog');
  }, [closeModal]);

  const openEvaluateModal = useCallback(() => {
    openModal('evaluateDecisionModal');
  }, [openModal]);

  const closeEvaluateModal = useCallback(() => {
    closeModal('evaluateDecisionModal');
  }, [closeModal]);

  const resetState = useCallback(() => {
    setSelectedDefinition("");
    setSelectedVersion("");
    setCurrentXml("");
    closeModal('diffModal');
    closeModal('uploadDialog');
    setIsDeploying(false);
    // NO persistence - nothing to clear
  }, [closeModal]);

  const value: ModelerContextValue = useMemo(() => ({
    selectedDefinition,
    setSelectedDefinition,
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
    setCurrentXml: handleSetCurrentXml,
    handleDefinitionChange,
    handleVersionChange,
    resetState,
  }), [
    selectedDefinition,
    selectedVersion,
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
    isDeploying,
    currentXml,
    handleSetCurrentXml,
    handleDefinitionChange,
    handleVersionChange,
    resetState,
  ]);

  return (
    <ModelerContext.Provider value={value}>
      {children}
    </ModelerContext.Provider>
  );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useModelerContext = (): ModelerContextValue => {
  const context = useContext(ModelerContext);

  if (context === undefined) {
    throw new Error("useModelerContext must be used within a ModelerProvider");
  }

  return context;
};
