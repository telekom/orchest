import { ExportFormat } from "@/shared/enums";
import { Button } from "@/design-system/components/ui/button";
import { Switch } from "@/design-system/components/ui/switch";
import clsx from "clsx";
import { Redo2, Undo2 } from "lucide-react";
import React from "react";
import {
    CodeViewToggle,
    ExportDropdown,
    ItemSelect,
    ModelerTypeSelector,
    PropertiesPanelToggle,
    SelectOption,
    ToolbarButton,
    UploadDialog,
} from "../toolbar";
import styles from "./ModelerToolbar.module.css";

// Item type display labels
const ITEM_TYPE_LABELS: Record<string, string> = {
  process: "process",
  decision: "decision",
  form: "form",
};

// Transforms items array into select options
const createItemOptions = (items: Array<{ id: string; name: string }>): SelectOption[] =>
  items.map((item) => ({
    value: item.name.trim(),
    label: item.name.trim(),
  }));

// Transforms versions array into select options
const createVersionOptions = (versions: string[]): SelectOption[] =>
  versions.map((v) => ({
    value: v,
    label: v,
  }));

export interface ModelerToolbarProps {
  // Core actions
  onNew: () => void;
  onSave: () => void;
  onUndo: () => void;
  onRedo: () => void;
  onExport: (format: ExportFormat) => void | Promise<void>;
  exportFormats: ReadonlyArray<{ readonly value: string; readonly label: string }>;

  // Upload dialog state
  isUploadDialogOpen: boolean;
  setIsUploadDialogOpen: (open: boolean) => void;
  onFileSelect: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onDragOver: (event: React.DragEvent<HTMLDivElement>) => void;
  onDragLeave: (event: React.DragEvent<HTMLDivElement>) => void;
  onDrop: (event: React.DragEvent<HTMLDivElement>) => void;
  fileInputRef: React.RefObject<HTMLInputElement | null>;
  acceptedFileTypes: string;
  dialogTitle: string;
  dragDropText: string;

  // Item selection state
  selectedItem: string;
  selectedVersion: string;
  items: Array<{ id: string; name: string }>;
  versions: string[];
  loadingItems: boolean;
  onItemChange: (value: string) => void;
  onVersionChange: (value: string) => void;
  itemType: "process" | "decision" | "form";

  // Properties panel state
  showPropertiesPanel: boolean;
  setShowPropertiesPanel: (show: boolean) => void;
  disablePropertiesPanel?: boolean;

  // Code view state
  showCodeView: boolean;
  onToggleCodeView: () => void;

  // Optional features
  onGenerateCode?: () => void;
  isGenerating?: boolean;
  canDeploy?: boolean;
  activeModeler?: string;
  onModelerTypeChange?: (value: string) => void;
  showCompensateFlowToggle?: boolean;
  compensateFlow?: boolean;
  onCompensateFlowToggleRequest?: (next: boolean) => void;

  /** Called when user clicks the Evaluate button (decision modeler only) */
  onEvaluate?: () => void;
}

/**
 * ModelerToolbar - Main toolbar for BPMN/DMN modeler
 *
 * Provides actions for:
 * - Creating new diagrams
 * - Selecting and loading existing diagrams
 * - Deploying diagrams
 * - Importing/exporting diagrams
 * - Undo/redo operations
 * - Toggling properties panel
 *
 * Composed from smaller sub-components for maintainability.
 */
const ModelerToolbar: React.FC<ModelerToolbarProps> = ({
  onNew,
  onSave,
  onUndo,
  onRedo,
  onExport,
  exportFormats,
  isUploadDialogOpen,
  setIsUploadDialogOpen,
  onFileSelect,
  onDragOver,
  onDragLeave,
  onDrop,
  fileInputRef,
  acceptedFileTypes,
  dialogTitle,
  dragDropText,
  selectedItem,
  selectedVersion,
  items,
  versions,
  loadingItems,
  onItemChange,
  onVersionChange,
  itemType,
  showPropertiesPanel,
  setShowPropertiesPanel,
  disablePropertiesPanel = false,
  showCodeView,
  onToggleCodeView,
  onGenerateCode: _onGenerateCode,
  isGenerating: _isGenerating = false,
  canDeploy = true,
  activeModeler,
  onModelerTypeChange,
  showCompensateFlowToggle = false,
  compensateFlow = false,
  onCompensateFlowToggleRequest,
  onEvaluate,
}) => {
  const itemTypeLabel = ITEM_TYPE_LABELS[itemType] || itemType;
  const itemOptions = createItemOptions(items);
  const versionOptions = createVersionOptions(versions);

  return (
    <div className={styles.toolbar}>
      {/* Left section: Primary actions */}
      <div className={styles.leftSection}>
        {/* BPMN/DMN switcher */}
        {activeModeler && onModelerTypeChange && (
          <ModelerTypeSelector
            activeModeler={activeModeler}
            onModelerTypeChange={onModelerTypeChange}
          />
        )}

        {/* New diagram */}
        <ToolbarButton icon="circle-add-type-standard" label="New" onClick={onNew} />

        {/* Item and version selection (not shown for forms) */}
        {itemType !== "form" && (
          <div className={styles.itemSelectionGroup}>
            <ItemSelect
              value={selectedItem}
              onValueChange={onItemChange}
              options={itemOptions}
              placeholder={`Select ${itemTypeLabel}`}
              variant="wide"
              loading={loadingItems}
              emptyMessage={`No ${itemTypeLabel}s found`}
              loadingMessage={`Loading ${itemTypeLabel}s...`}
              label={`Select ${itemTypeLabel}`}
              searchable
            />

            <ItemSelect
              value={selectedVersion}
              onValueChange={onVersionChange}
              options={versionOptions}
              placeholder="Version"
              variant="default"
              disabled={!selectedItem}
              emptyMessage={
                selectedItem ? "No versions found" : `Select ${itemTypeLabel} first`
              }
              loadingMessage="Loading..."
              label="Version"
            />
          </div>
        )}

        {canDeploy && showCompensateFlowToggle && (
          <div className={styles.compensateFlowToggle}>
            <Switch
              id="compensate-flow-toggle"
              checked={compensateFlow}
              onCheckedChange={(checked) => onCompensateFlowToggleRequest?.(checked)}
              aria-label="Compensate flow"
            />
            <label htmlFor="compensate-flow-toggle" className={styles.compensateFlowLabel}>
              Compensate flow
            </label>
          </div>
        )}

        {/* Deploy button */}
        {canDeploy && <ToolbarButton icon="publish-type-standard" label="Deploy" onClick={onSave} />}

        {/* Evaluate button (decision modeler only) */}
        {itemType === "decision" && onEvaluate && (
          <ToolbarButton
            label="Evaluate"
            onClick={onEvaluate}
          />
        )}

        {/* Export dropdown */}
        <ExportDropdown exportFormats={exportFormats} onExport={onExport} />

        {/* Import button */}
        <ToolbarButton
          icon="upload-type-standard"
          label="Import"
          onClick={() => setIsUploadDialogOpen(true)}
        />

        {/* Import dialog */}
        <UploadDialog
          isOpen={isUploadDialogOpen}
          onOpenChange={setIsUploadDialogOpen}
          onDragOver={onDragOver}
          onDragLeave={onDragLeave}
          onDrop={onDrop}
          fileInputRef={fileInputRef}
          acceptedFileTypes={acceptedFileTypes}
          onFileSelect={onFileSelect}
          dialogTitle={dialogTitle}
          dragDropText={dragDropText}
          itemTypeLabel={itemTypeLabel}
        />

        {/* Generate code (optional feature)
        {onGenerateCode && (
          <ToolbarButton
            icon="magic-wand-type-standard"
            label="Generate Code"
            onClick={onGenerateCode}
            isLoading={isGenerating}
          />
        )} */}
      </div>

      {/* Right section: History and panel controls */}
      <div className={styles.rightSection}>
        {/* Undo/Redo */}
        <Button
          variant="ghost"
          onClick={onUndo}
          size="icon"
          className={clsx(styles.historyButton, styles.subtleWhite)}
          aria-label="Undo"
        >
          <Undo2 size={16} />
        </Button>
        <Button
          variant="ghost"
          onClick={onRedo}
          size="icon"
          className={clsx(styles.historyButton, styles.subtleWhite)}
          aria-label="Redo"
        >
          <Redo2 size={16} />
        </Button>

        {/* Code view toggle */}
        <CodeViewToggle
          showCodeView={showCodeView}
          onToggle={onToggleCodeView}
        />

        {/* Properties panel toggle */}
        <PropertiesPanelToggle
          showPropertiesPanel={showPropertiesPanel}
          setShowPropertiesPanel={setShowPropertiesPanel}
          disabled={disablePropertiesPanel}
        />
      </div>
    </div>
  );
};

export default ModelerToolbar;
