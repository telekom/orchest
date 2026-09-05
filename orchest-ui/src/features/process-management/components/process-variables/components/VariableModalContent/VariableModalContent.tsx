import React from "react";
import { ObjectVariableEditor } from "../ObjectVariableEditor/ObjectVariableEditor";
import { PrimitiveVariableEditor } from "../PrimitiveVariableEditor/PrimitiveVariableEditor";

interface VariableModalContentProps {
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

export const VariableModalContent: React.FC<VariableModalContentProps> = (props) => {
  const isObjectType = props.type === "object";

  if (isObjectType) {
    return <ObjectVariableEditor {...props} />;
  }

  return <PrimitiveVariableEditor {...props} />;
};
