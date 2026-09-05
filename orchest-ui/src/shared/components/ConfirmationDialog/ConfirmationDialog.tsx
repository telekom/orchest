import { Button } from "@/design-system/components/ui/button";
import { Dialog, DialogTrigger, DialogContent, DialogHeader, DialogFooter, DialogTitle, DialogDescription } from "@/design-system/components/ui/dialog";
import { useCallback, type ReactNode } from "react";
import styles from "./ConfirmationDialog.module.css";

interface ConfirmationDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  trigger?: ReactNode;
  title: string;
  description: string;
  onConfirm: (e: React.MouseEvent<HTMLButtonElement>) => void | Promise<void>;
  confirmText?: string;
  cancelText?: string;
  confirmVariant?: "primary" | "secondary" | "outline" | "ghost" | "destructive" | "success";
  cancelVariant?: "primary" | "secondary" | "outline" | "ghost";
  affectedIds?: string[];
}

export const ConfirmationDialog = ({
  open,
  onOpenChange,
  trigger,
  title,
  description,
  onConfirm,
  confirmText = "Proceed",
  cancelText = "Cancel",
  confirmVariant = "primary",
  cancelVariant = "outline",
  affectedIds,
}: ConfirmationDialogProps) => {
  const handleConfirm = useCallback(async (e: React.MouseEvent<HTMLButtonElement>) => {
    await onConfirm(e);
    onOpenChange(false);
  }, [onConfirm, onOpenChange]);

  const handleCancel = useCallback((e: React.MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();
    onOpenChange(false);
  }, [onOpenChange]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      {trigger && (
        <DialogTrigger asChild>
          {trigger}
        </DialogTrigger>
      )}
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>

        {affectedIds && affectedIds.length > 0 && (
          <div className={styles.idsList}>
            <strong className={styles.idsLabel}>Affected IDs ({affectedIds.length}):</strong>
            <ul className={styles.idsListItems}>
              {affectedIds.map((id) => (
                <li key={id} className={styles.idItem}>
                  <code className={styles.idCode}>{id}</code>
                </li>
              ))}
            </ul>
          </div>
        )}

        <DialogFooter>
          <Button variant={cancelVariant} size="sm" onClick={handleCancel}>
            {cancelText}
          </Button>
          <Button variant={confirmVariant} size="sm" onClick={handleConfirm}>
            {confirmText}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
