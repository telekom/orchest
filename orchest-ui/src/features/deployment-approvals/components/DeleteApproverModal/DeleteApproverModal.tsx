import { WarningModal } from "@/shared/components";
import React from "react";
import styles from './DeleteApproverModal.module.css';

interface DeleteApproverModalProps {
  approverEmail: string;
  processName: string;
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

export const DeleteApproverModal: React.FC<DeleteApproverModalProps> = ({
  approverEmail,
  processName,
  isOpen,
  onClose,
  onConfirm,
}) => {
  return (
    <WarningModal
      isOpen={isOpen}
      onClose={onClose}
      onConfirm={onConfirm}
      title="Remove Approver"
      description="This action cannot be undone"
      warningTitle="Are you sure you want to remove this approver?"
      warningMessage={
        <>
          Approver <span className={styles.codeText}>"{approverEmail}"</span> will be removed from{" "}
          <span className={styles.codeText}>"{processName}"</span> and will no longer be able to approve deployments for this process.
        </>
      }
      confirmText="Remove Approver"
      cancelText="Cancel"
    />
  );
};
