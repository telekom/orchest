import React from "react";
import { StandardModal } from "@/shared/components/StandardModal/StandardModal";
import { ModalBanner } from "@/shared/components/ModalBanner/ModalBanner";
import styles from "./WarningModal.module.css";

export interface WarningModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  description?: string;
  warningTitle: string;
  warningMessage: React.ReactNode;
  confirmText?: string;
  cancelText?: string;
  confirmLoading?: boolean;
}

export const WarningModal: React.FC<WarningModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  title,
  description,
  warningTitle,
  warningMessage,
  confirmText = "Confirm",
  cancelText = "Cancel",
  confirmLoading = false,
}) => {
  const handleConfirm = () => {
    onConfirm();
    onClose();
  };

  return (
    <StandardModal
      isOpen={isOpen}
      onClose={onClose}
      title={title}
      description={description}
      size="md"
      showFooter={true}
      onConfirm={handleConfirm}
      confirmText={confirmText}
      cancelText={cancelText}
      confirmLoading={confirmLoading}
    >
      <div className={styles.container}>
        <ModalBanner
          type="warning"
          title={warningTitle}
          message={warningMessage}
        />
      </div>
    </StandardModal>
  );
};
