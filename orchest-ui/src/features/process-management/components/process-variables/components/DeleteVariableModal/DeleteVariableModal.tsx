import { WarningModal } from "@/shared/components";
import React from "react";
import { ProcessVariable } from "@/features/process-management/types/processInstance";
import styles from "./DeleteVariableModal.module.css";

interface DeleteVariableModalProps {
  variable: ProcessVariable;
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

export const DeleteVariableModal: React.FC<DeleteVariableModalProps> = ({
  variable,
  isOpen,
  onClose,
  onConfirm,
}) => {
  return (
    <WarningModal
      isOpen={isOpen}
      onClose={onClose}
      onConfirm={onConfirm}
      title="Delete Variable"
      description="This action cannot be undone"
      warningTitle="Are you sure you want to delete this variable?"
      warningMessage={
        <>
          Variable <span className={styles.variableName}>"{variable.name}"</span> will be permanently removed from this process instance.
        </>
      }
      confirmText="Delete Variable"
      cancelText="Cancel"
    />
  );
};
