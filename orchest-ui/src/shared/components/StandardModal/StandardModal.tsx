import { Button } from "@/design-system/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogFooter, DialogTitle, DialogDescription } from "@/design-system/components/ui/dialog";
import React from "react";
import styles from "./StandardModal.module.css";

export interface StandardModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: React.ReactNode;
  description?: string;
  children: React.ReactNode;

  size?: "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | "4xl" | "5xl" | "full" | "fullscreen";

  showFooter?: boolean;
  cancelText?: string;
  confirmText?: string;
  onConfirm?: () => void;
  confirmDisabled?: boolean;
  confirmLoading?: boolean;
  confirmVariant?: "primary" | "secondary" | "outline" | "ghost" | "destructive";

  customFooter?: React.ReactNode;
  contentClassName?: string;
  contentWrapperClassName?: string;
  maxHeight?: string;
}

const sizeClasses = {
  sm: { maxWidth: "400px" },
  md: { maxWidth: "500px" },
  lg: { maxWidth: "600px" },
  xl: { maxWidth: "700px" },
  "2xl": { maxWidth: "42rem" },
  "3xl": { maxWidth: "48rem" },
  "4xl": { maxWidth: "56rem" },
  "5xl": { maxWidth: "64rem" },
  full: { maxWidth: "90vw" },
  fullscreen: { width: "calc(100vw - 32px)", maxWidth: "calc(100vw - 32px)" },
} as const;

export const StandardModal: React.FC<StandardModalProps> = ({
  isOpen,
  onClose,
  title,
  description,
  children,
  size = "md",
  showFooter = true,
  cancelText = "Cancel",
  confirmText = "Save",
  onConfirm,
  confirmDisabled = false,
  confirmLoading = false,
  confirmVariant = "primary",
  customFooter,
  contentClassName = "",
  contentWrapperClassName = "",
  maxHeight = "90vh",
}) => {
  const sizeStyle = sizeClasses[size];
  const heightStyle = size === "fullscreen"
    ? { height: "calc(100vh - 32px)", maxHeight: "calc(100vh - 32px)" }
    : { maxHeight };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className={contentClassName} style={{ ...sizeStyle, ...heightStyle }}>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          {description && <DialogDescription>{description}</DialogDescription>}
        </DialogHeader>

        <div className={contentWrapperClassName || styles.contentWrapper}>
          {children}
        </div>

        {showFooter && (
          <DialogFooter>
            {customFooter ?? (
              <>
                <Button variant="outline" size="sm" onClick={onClose} disabled={confirmLoading}>
                  {cancelText}
                </Button>
                {onConfirm && (
                  <Button
                    variant={confirmVariant}
                    size="sm"
                    onClick={onConfirm}
                    disabled={confirmDisabled || confirmLoading}
                  >
                    {confirmLoading ? "Loading..." : confirmText}
                  </Button>
                )}
              </>
            )}
          </DialogFooter>
        )}
      </DialogContent>
    </Dialog>
  );
};
