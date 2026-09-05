import { Badge } from "@/design-system/components/ui/badge/badge";
import { Input } from "@/design-system/components/ui/input";
import { Label } from "@/design-system/components/ui/label";
import { Textarea } from "@/design-system/components/ui/textarea";
import { getBadgeStyleForVariableScope, getBadgeStyleForVariableType } from "@/shared/utils/badgeUtils";
import { Select } from "@/design-system/components/ui/select";
import clsx from "clsx";
import { Code, FileText, LucideIcon, Type } from "lucide-react";
import React from "react";
import styles from "./VariableFieldComponents.module.css";

// eslint-disable-next-line react-refresh/only-export-components
export const VARIABLE_TYPES = ["string", "object", "number", "boolean"] as const;

const TYPE_OPTIONS = [
  { id: "string", label: "string", value: "string" },
  { id: "object", label: "object", value: "object" },
  { id: "number", label: "number", value: "number" },
  { id: "boolean", label: "boolean", value: "boolean" },
];

const BOOLEAN_OPTIONS = [
  { id: "true", label: "true", value: "true" },
  { id: "false", label: "false", value: "false" },
];

interface FieldLabelProps {
  icon: LucideIcon;
  label: string;
  required?: boolean;
}

export const FieldLabel: React.FC<FieldLabelProps> = ({ icon: Icon, label, required }) => (
  <div className={styles.fieldLabel}>
    <Icon className={styles.fieldIcon} />
    <Label className={styles.labelText}>
      {label} {required && <span className={styles.required}>*</span>}
    </Label>
  </div>
);

interface ReadOnlyFieldProps {
  value: string;
  isLongText?: boolean;
}

export const ReadOnlyField: React.FC<ReadOnlyFieldProps> = ({ value, isLongText }) => (
  <div className={clsx(styles.readOnlyField, isLongText && styles.readOnlyFieldLong)}>
    {isLongText ? (
      <pre className={styles.readOnlyText}>{value}</pre>
    ) : (
      <span className={styles.readOnlyText}>{value}</span>
    )}
  </div>
);

interface VariableHeaderProps {
  name: string;
  type: string;
  scope: string;
}

export const VariableHeader: React.FC<VariableHeaderProps> = ({ name, type, scope }) => {
  const displayName = name || "Unnamed Variable";
  const typeStyle = getBadgeStyleForVariableType(type);
  const scopeStyle = getBadgeStyleForVariableScope(scope);
  const TypeIcon = typeStyle.icon;
  const ScopeIcon = scopeStyle.icon;

  return (
    <div className={styles.variableHeader}>
      <span className={styles.variableName}>{displayName}</span>
      <Badge
        variant={typeStyle.variant}
        icon={TypeIcon ? <TypeIcon /> : undefined}
        className={typeStyle.className}
      >
        {type}
      </Badge>
      <Badge
        variant={scopeStyle.variant}
        icon={ScopeIcon ? <ScopeIcon /> : undefined}
        className={scopeStyle.className}
      >
        {scope}
      </Badge>
    </div>
  );
};

interface TypeSelectorProps {
  value: string;
  onChange?: (value: string) => void;
  readOnly?: boolean;
}

export const TypeSelector: React.FC<TypeSelectorProps> = ({ value, onChange, readOnly }) => {
  const handleChange = (value: string) => {
    
    if (value && onChange) {
      onChange(value);
    }
  };

  return readOnly ? (
    <ReadOnlyField value={value} />
  ) : (
    <div className={styles.selectWrapper}>
      <Select
        label=""
        value={value}
        items={TYPE_OPTIONS}
        onValueChange={handleChange}
        size="sm"
        className={styles.odsSelect}
      />
    </div>
  );
};

interface NameFieldProps {
  name: string;
  readOnly?: boolean;
  error?: string;
  onNameChange?: (value: string) => void;
}

export const NameField: React.FC<NameFieldProps> = ({ name, readOnly, error, onNameChange }) => (
  <div className={styles.fieldContainer}>
    <FieldLabel icon={Type} label="Name" required={!readOnly} />
    {readOnly ? (
      <ReadOnlyField value={name} />
    ) : (
      <>
        <Input
          value={name}
          onChange={(e) => onNameChange?.(e.target.value)}
          placeholder="Variable name"
          errorMessage={error}
        />
      </>
    )}
  </div>
);

interface TypeFieldProps {
  type: string;
  readOnly?: boolean;
  onTypeChange?: (value: string) => void;
}

export const TypeField: React.FC<TypeFieldProps> = ({ type, readOnly, onTypeChange }) => (
  <div className={styles.fieldContainer}>
    <FieldLabel icon={Code} label="Type" required={!readOnly} />
    <TypeSelector value={type} onChange={onTypeChange} readOnly={readOnly} />
  </div>
);

interface ValueFieldProps {
  type: string;
  value: string;
  readOnly?: boolean;
  error?: string;
  onValueChange?: (value: string) => void;
}

export const ValueField: React.FC<ValueFieldProps> = ({ type, value, readOnly, error, onValueChange }) => {
  const isLongString = type === "string" && value.length > 100;

  const handleBooleanChange = (value: string) => {
    
    if (value && onValueChange) {
      onValueChange(value);
    }
  };

  return (
    <div className={styles.valueFieldContainer}>
      <FieldLabel icon={FileText} label="Value" required={!readOnly} />
      {readOnly ? (
        <ReadOnlyField value={value} isLongText={isLongString} />
      ) : (
        <>
          {type === "boolean" ? (
            <div className={styles.selectWrapper}>
              <Select
                label=""
                value={value}
                items={BOOLEAN_OPTIONS}
                onValueChange={handleBooleanChange}
                size="sm"
                className={styles.odsSelect}
              />
            </div>
          ) : isLongString ? (
            <>
              <Textarea
                value={value}
                onChange={(e) => onValueChange?.(e.target.value)}
                placeholder="Enter value"
                className={clsx(styles.textareaError, error && styles.inputError)}
              />
              {error && (
                <p className={styles.errorText}>{error}</p>
              )}
            </>
          ) : (
            <Input
              value={value}
              onChange={(e) => onValueChange?.(e.target.value)}
              placeholder="Enter value"
              type={type === "number" ? "number" : "text"}
              errorMessage={error}
            />
          )}
        </>
      )}
    </div>
  );
};
