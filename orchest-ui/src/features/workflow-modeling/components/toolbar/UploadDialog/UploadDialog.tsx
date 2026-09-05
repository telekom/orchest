import { Button } from "@/design-system/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogFooter, DialogTitle } from "@/design-system/components/ui/dialog";
import { FileUp } from "lucide-react";
import React from "react";
import styles from "./UploadDialog.module.css";

interface UploadDialogProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  onDragOver: (event: React.DragEvent<HTMLDivElement>) => void;
  onDragLeave: (event: React.DragEvent<HTMLDivElement>) => void;
  onDrop: (event: React.DragEvent<HTMLDivElement>) => void;
  fileInputRef: React.RefObject<HTMLInputElement | null>;
  acceptedFileTypes: string;
  onFileSelect: (event: React.ChangeEvent<HTMLInputElement>) => void;
  dialogTitle: string;
  dragDropText: string;
  itemTypeLabel: string;
}

/**
 * Upload dialog with drag-and-drop support
 * Used for importing BPMN/DMN/Form files into the modeler
 */
export const UploadDialog: React.FC<UploadDialogProps> = ({
  isOpen,
  onOpenChange,
  onDragOver,
  onDragLeave,
  onDrop,
  fileInputRef,
  acceptedFileTypes,
  onFileSelect,
  dialogTitle,
  dragDropText,
  itemTypeLabel,
}) => (
  <Dialog open={isOpen} onOpenChange={onOpenChange} dismissOnBackdropClick={true}>
    <DialogContent
      headerText={dialogTitle}
      bodyText={`Upload a ${itemTypeLabel} file to import`}
      showCloseButton={true}
      closeButtonProps={{
        onClick: () => onOpenChange(false),
        'aria-label': 'Close upload dialog'
      }}
    >
      <div className={styles.uploadArea}>
        <div
          className={styles.dropZone}
          onDragOver={onDragOver}
          onDragLeave={onDragLeave}
          onDrop={onDrop}
        >
          <FileUp className={styles.uploadIcon} />
          <p className={styles.uploadText}>
            {dragDropText}
          </p>
          <p className={styles.separatorText}>or</p>
          <div className={styles.buttonContainer}>
            <label htmlFor="file-upload" className={styles.fileLabel}>
              <Button
                variant="primary"
                size="sm"
                label="Browse Files"
                onClick={() => fileInputRef.current?.click()}
              />
              <input
                ref={fileInputRef}
                id="file-upload"
                type="file"
                className={styles.fileInput}
                accept={acceptedFileTypes}
                onChange={onFileSelect}
              />
            </label>
          </div>
        </div>
      </div>
    </DialogContent>
  </Dialog>
);
