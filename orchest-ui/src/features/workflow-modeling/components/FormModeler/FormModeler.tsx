import { FormEditor } from "@bpmn-io/form-js";
import { toast } from "@/design-system/components/ui/sonner";
import { logger } from '@/shared/utils/logger';
import clsx from "clsx";
import React, { useCallback, useEffect, useRef } from "react";
import { useLocation } from "react-router-dom";
import { ModelerProvider, useModelerContext } from "../../context/ModelerContext";
import { emptyForm } from "../../templates/emptyForm";
import BaseModeler from "../BaseModeler/BaseModeler";
import styles from "./FormModeler.module.css";

import "@bpmn-io/form-js/dist/assets/form-js.css";
import "@bpmn-io/form-js/dist/assets/form-js-editor.css";
import "@bpmn-io/form-js/dist/assets/form-js-playground.css";

interface FormModelerProps {
  onSave?: (schema: string) => void;
  activeModeler?: string;
  onModelerTypeChange?: (value: string) => void;
}

const FormModelerContent: React.FC<FormModelerProps> = ({ onSave, activeModeler, onModelerTypeChange }) => {
  const location = useLocation();
  const { showPropertiesPanel } = useModelerContext();

  // Priority: 1. Route state schema (from navigation), 2. Empty template
  // NO persistence - always start fresh or from route
  const initialSchema = (location.state as { formSchema?: string | object } | null)?.formSchema || emptyForm;

  const containerRef = useRef<HTMLDivElement>(null);
  const propertiesPanelRef = useRef<HTMLDivElement>(null);
  const formEditorRef = useRef<FormEditor | null>(null);
  const autoSaveTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const initFormEditor = useCallback(() => {
    if (containerRef.current && propertiesPanelRef.current) {
      if (formEditorRef.current) {
        formEditorRef.current.destroy();
        formEditorRef.current = null;
      }

      const formEditor = new FormEditor({
        container: containerRef.current,
        propertiesPanel: {
          parent: propertiesPanelRef.current,
        },
      });

      formEditorRef.current = formEditor;

      const importAndSetup = async () => {
        try {
          await formEditor.importSchema(initialSchema);
          // No auto-save - persistence disabled
        } catch (err) {
          logger.error("Form import error:", err);
          toast.error("Failed to load form schema");
        }
      };

      importAndSetup();
    }
  }, [initialSchema]);

  useEffect(() => {
    initFormEditor();

    return () => {
      // Clear auto-save timeout
      if (autoSaveTimeoutRef.current) {
        clearTimeout(autoSaveTimeoutRef.current);
      }

      if (formEditorRef.current) {
        formEditorRef.current.destroy();
        formEditorRef.current = null;
      }
    };
  }, [initFormEditor]);

  const handleSaveForm = useCallback(async () => {
    const formEditor = formEditorRef.current;
    if (!formEditor) return;

    try {
      const schema = formEditor.saveSchema();
      const schemaJson = JSON.stringify(schema, null, 2);

      onSave?.(schemaJson);
      toast.success("Form saved successfully");
    } catch (err) {
      logger.error("Save error:", err);
      toast.error("Failed to save form");
    }
  }, [onSave]);

  return (
    <BaseModeler
      diagramType="form"
      itemType="form"
      modelerRef={formEditorRef}
      emptyTemplate={emptyForm}
      definitionService={undefined}
      onSave={handleSaveForm}
      activeModeler={activeModeler}
      onModelerTypeChange={onModelerTypeChange}
    >
      <div className={styles.container}>
        <div
          ref={containerRef}
          className={clsx(styles.modelerContainer, 'form-modeler-container', 'modeler-container')}
        ></div>

        <div
          ref={propertiesPanelRef}
          className={clsx(styles.propertiesPanel, 'properties-panel', {
            [styles.hidden]: !showPropertiesPanel
          })}
        ></div>
      </div>
    </BaseModeler>
  );
};

const FormModeler: React.FC<FormModelerProps> = ({ onSave, activeModeler, onModelerTypeChange }) => {
  return (
    <ModelerProvider>
      <FormModelerContent onSave={onSave} activeModeler={activeModeler} onModelerTypeChange={onModelerTypeChange} />
    </ModelerProvider>
  );
};

export default FormModeler;
