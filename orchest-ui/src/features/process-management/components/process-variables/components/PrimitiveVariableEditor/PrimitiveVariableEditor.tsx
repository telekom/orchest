import React from "react";
import { NameField, TypeField, ValueField, VariableHeader } from "../VariableFieldComponents/VariableFieldComponents";
import styles from "./PrimitiveVariableEditor.module.css";

interface PrimitiveVariableEditorProps {
  name: string;
  type: string;
  scope: string;
  valueState: string;
  readOnly?: boolean;
  errors?: {
    name?: string;
    value?: string;
  };
  onNameChange?: (value: string) => void;
  onTypeChange?: (value: string) => void;
  onValueChange?: (value: string) => void;
}

export const PrimitiveVariableEditor: React.FC<PrimitiveVariableEditorProps> = ({
  name,
  type,
  scope,
  valueState,
  readOnly = false,
  errors = {},
  onNameChange,
  onTypeChange,
  onValueChange,
}) => (
  <div className={styles.container}>
    <div className={styles.header}>
      <VariableHeader name={name} type={type} scope={scope} />
    </div>
    <NameField
      name={name}
      readOnly={readOnly}
      error={errors.name}
      onNameChange={onNameChange}
    />
    <TypeField
      type={type}
      readOnly={readOnly}
      onTypeChange={onTypeChange}
    />
    <ValueField
      type={type}
      value={valueState}
      readOnly={readOnly}
      error={errors.value}
      onValueChange={onValueChange}
    />
  </div>
);
