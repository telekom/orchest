import { toast } from "@/design-system/components/ui/sonner";
import { StandardModal } from "@/shared/components/StandardModal/StandardModal";
import { VariableAction } from "@/shared/enums";
import { useFormValidation, useJsonFormatter, type ValidationRules } from "@/shared/hooks";
import { convertValueByType, formatJsonValue, getVariableModalContentClassName, getVariableModalSize, isValidJson } from "@/shared/utils/variableUtils";
import React, { useEffect, useMemo, useState } from "react";
import { ProcessVariable } from "@/features/process-management/types/processInstance";
import { VariableModalContent } from "../VariableModalContent/VariableModalContent";

interface EditVariableModalProps {
  variable: ProcessVariable;
  isOpen: boolean;
  onClose: () => void;
  variableOperation: (params: {
    action: VariableAction;
    variable: { name: string; value: unknown; type?: string; scope?: string };
  }) => void;
}

export const EditVariableModal: React.FC<EditVariableModalProps> = ({
  variable,
  isOpen,
  onClose,
  variableOperation,
}) => {
  const [name, setName] = useState(variable.name);
  const [type, setType] = useState<"string" | "number" | "boolean" | "object">(variable.type as "string" | "number" | "boolean" | "object" || "string");
  const [valueState, setValueState] = useState<string>(variable.value || "");
  const [isHeaderOpen, setIsHeaderOpen] = useState(true);

  const { isValid: isValidJsonValue } = useJsonFormatter({
    value: valueState,
    type,
    indent: 2,
  });

  const validationRules = useMemo<ValidationRules<{ name: string; value: string }>>(
    () => ({
      name: (value: string) => {
        if (!value.trim()) {
          return "Variable name is required";
        }
        return "";
      },
      value: (value: string) => {
        if (!value.trim()) {
          return "Variable value is required";
        }
        if (type === "object" && !isValidJsonValue) {
          return "Please provide valid JSON for object type";
        }
        return "";
      },
    }),
    [type, isValidJsonValue]
  );

  const { errors, validate, validateField, clearErrors } = useFormValidation({
    rules: validationRules,
  });

  useEffect(() => {
    if (isOpen) {
      setName(variable.name);
      setType(variable.type || "string");

      if (variable.type === "object") {
        setValueState(formatJsonValue(variable.value, 2));
      } else {
        setValueState(variable.value || "");
      }

      clearErrors();
    }
  }, [variable, isOpen, clearErrors]);

  const handleTypeChange = (newType: string) => {
    setType(newType as "string" | "number" | "boolean" | "object");

    if (newType === "object") {
      if (!valueState || valueState.trim() === "" || !isValidJson(valueState)) {
        setValueState("{\n  \n}");
      } else {
        setValueState(formatJsonValue(valueState, 2));
      }
    }
  };

  const handleNameChange = (newName: string) => {
    setName(newName);
    validateField('name', newName);
  };

  const handleValueChange = (newValue: string) => {
    setValueState(newValue);
    validateField('value', newValue);
  };

  const handleSave = () => {
    const validationErrors = validate({ name, value: valueState });

    if (Object.keys(validationErrors).length > 0) {
      const firstError = Object.values(validationErrors)[0];
      if (firstError) {
        toast.error(firstError);
      }
      return;
    }

    const conversion = convertValueByType(valueState, type);

    if (!conversion.success) {
      toast.error(conversion.errorMessage || "Conversion failed");
      return;
    }

    variableOperation({
      action: variable.name === "" ? VariableAction.ADD : VariableAction.UPDATE,
      variable: {
        name,
        value: conversion.value,
        type,
        scope: variable.scope,
      },
    });

    onClose();
  };

  const isAddMode = variable.name === "";

  const isSaveDisabled = useMemo(() => {
    if (!name.trim() || !valueState.trim()) {
      return true;
    }
    if (type === "object" && !isValidJsonValue) {
      return true;
    }
    return false;
  }, [name, valueState, type, isValidJsonValue]);

  return (
    <StandardModal
      isOpen={isOpen}
      onClose={onClose}
      title={isAddMode ? "Add Variable" : "Edit Variable"}
      size={getVariableModalSize(type)}
      onConfirm={handleSave}
      confirmText={isAddMode ? "Add Variable" : "Save"}
      confirmDisabled={isSaveDisabled}
      contentWrapperClassName={getVariableModalContentClassName(type, "edit")}
    >
      <VariableModalContent
        name={name}
        type={type}
        scope={variable.scope}
        valueState={valueState}
        isHeaderOpen={isHeaderOpen}
        readOnly={false}
        errors={{
          name: errors.name,
          value: errors.value,
        }}
        onNameChange={handleNameChange}
        onTypeChange={handleTypeChange}
        onValueChange={handleValueChange}
        onHeaderToggle={setIsHeaderOpen}
      />
    </StandardModal>
  );
};
