import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/design-system/components/ui/collapsible";
import { ChevronDown, ChevronUp, FileText } from "lucide-react";
import { SpinnerLoader } from "@/shared/components/Loader/Loader";
import React, { lazy, Suspense } from "react";
import { FieldLabel, NameField, TypeField, VariableHeader } from "../VariableFieldComponents/VariableFieldComponents";
import styles from "./ObjectVariableEditor.module.css";

const JsonEditor = lazy(() => import("../JsonEditor/JsonEditor"));

interface ObjectVariableEditorProps {
  name: string;
  type: string;
  scope: string;
  valueState: string;
  isHeaderOpen: boolean;
  readOnly?: boolean;
  errors?: {
    name?: string;
    value?: string;
  };
  onNameChange?: (value: string) => void;
  onTypeChange?: (value: string) => void;
  onValueChange?: (value: string) => void;
  onHeaderToggle?: (open: boolean) => void;
}

export const ObjectVariableEditor: React.FC<ObjectVariableEditorProps> = ({
  name,
  type,
  scope,
  valueState,
  isHeaderOpen,
  readOnly = false,
  errors = {},
  onNameChange,
  onTypeChange,
  onValueChange,
  onHeaderToggle,
}) => {
  // Render chevron icon content
  const chevronUpContent = (
    <>
      <VariableHeader name={name} type={type} scope={scope} />
      <ChevronUp className={styles.chevronIcon} />
    </>
  );
  const chevronDownContent = (
    <>
      <VariableHeader name={name} type={type} scope={scope} />
      <ChevronDown className={styles.chevronIcon} />
    </>
  );

  return (
    <div className={styles.container}>
      <Collapsible open={isHeaderOpen} onOpenChange={onHeaderToggle} className={styles.collapsibleContainer}>
        <div className={styles.collapsibleHeader}>
          <CollapsibleTrigger asChild>
            <button
              className={styles.triggerButton}
              onClick={() => onHeaderToggle?.(!isHeaderOpen)}
            >
              {isHeaderOpen ? chevronUpContent : chevronDownContent}
            </button>
          </CollapsibleTrigger>

        <CollapsibleContent>
          <div className={styles.collapsibleContent}>
            <div className={styles.fieldsGrid}>
              <div className={styles.fieldCol6}>
                <NameField
                  name={name}
                  readOnly={readOnly}
                  error={errors.name}
                  onNameChange={onNameChange}
                />
              </div>
              <div className={styles.fieldCol6}>
                <TypeField
                  type={type}
                  readOnly={readOnly}
                  onTypeChange={onTypeChange}
                />
              </div>
            </div>
          </div>
          </CollapsibleContent>
        </div>
      </Collapsible>

      <div className={styles.valueContainer}>
        <FieldLabel icon={FileText} label="Value" required={!readOnly} />
        <div className={styles.editorBorder}>
          <Suspense
            fallback={
              <div className={styles.editorFallback}>
                <SpinnerLoader size="md" />
              </div>
            }
          >
            <JsonEditor
              value={valueState}
              onChange={onValueChange || (() => {})}
              mode="text"
              readOnly={readOnly}
              height="100%"
            />
          </Suspense>
        </div>
      </div>
    </div>
  );
};
