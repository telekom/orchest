import { InfoModal } from "@/shared/components";
import { getVariableModalSize, getVariableModalContentClassName } from "@/shared/utils/variableUtils";
import React, { useState } from "react";
import { ProcessVariable } from "@/features/process-management/types/processInstance";
import { VariableModalContent } from "../VariableModalContent/VariableModalContent";

interface ViewVariableModalProps {
  variable: ProcessVariable;
  isOpen: boolean;
  onClose: () => void;
}

export const ViewVariableModal: React.FC<ViewVariableModalProps> = ({
  variable,
  isOpen,
  onClose,
}) => {
  const [isHeaderOpen, setIsHeaderOpen] = useState(true);

  return (
    <InfoModal
      isOpen={isOpen}
      onClose={onClose}
      title="View Variable"
      size={getVariableModalSize(variable.type)}
      cancelText="Close"
      contentWrapperClassName={getVariableModalContentClassName(variable.type, "view")}
      bannerType="info"
      bannerMessage="This variable is read-only. Use Edit to make changes."
    >
      <VariableModalContent
        name={variable.name}
        type={variable.type}
        scope={variable.scope}
        valueState={variable.value}
        isHeaderOpen={isHeaderOpen}
        readOnly={true}
        onHeaderToggle={setIsHeaderOpen}
      />
    </InfoModal>
  );
};
