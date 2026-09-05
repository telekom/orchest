import { StandardModal } from "@/shared/components/StandardModal/StandardModal";
import { Button } from "@/design-system/components/ui/button";
import styles from "./ModificationDialogs.module.css";

interface ActionDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  selectedActivityId: string | null;
  elementName: string;
  elementStatus: string;
  onMoveInstance: () => void;
  onCancel: () => void;
}

export const ActionDialog = ({
  open,
  onOpenChange,
  selectedActivityId,
  elementName,
  elementStatus,
  onMoveInstance,
  onCancel,
}: ActionDialogProps) => {
  return (
    <StandardModal
      isOpen={open}
      onClose={() => onOpenChange(false)}
      title="Activity Actions"
      description="Select an action for this activity"
      size="md"
      showFooter={false}
    >
      {selectedActivityId && (
        <>
          <div className={styles.infoBox}>
            <div className={styles.infoRow}>
              <strong>Name:</strong> {elementName || "–"}
            </div>
            <div className={styles.infoRow}>
              <strong>ID:</strong> {selectedActivityId}
            </div>
            <div className={styles.infoRow}>
              <strong>Status:</strong> {elementStatus}
            </div>
          </div>
          <div className={styles.actionGrid}>
            <Button onClick={onMoveInstance} variant="primary" size="sm" label="Move Instance" />
            <Button onClick={onCancel} variant="outline" size="sm" label="Cancel" />
          </div>
        </>
      )}
    </StandardModal>
  );
};

interface ConfirmModificationDialogProps {
  open: boolean;
  sourceActivityId: string;
  targetActivityId: string;
  onConfirm: () => void;
  onCancel: () => void;
}

export const ConfirmModificationDialog = ({
  open,
  sourceActivityId,
  targetActivityId,
  onConfirm,
  onCancel,
}: ConfirmModificationDialogProps) => {
  return (
    <StandardModal
      isOpen={open}
      onClose={onCancel}
      title="Confirm Instance Modification"
      
      size="md"
      onConfirm={onConfirm}
      confirmText="Confirm"
    >
      <div className={styles.confirmText}>
        Move instance from <strong>{sourceActivityId}</strong> to <strong>{targetActivityId}</strong>?
      </div>
    </StandardModal>
  );
};
