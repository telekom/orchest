import * as DialogPrimitive from "@radix-ui/react-dialog";
import clsx from "clsx";
import { X } from "lucide-react";
import * as React from "react";
import styles from "./dialog.module.css";

const Dialog: React.FC<React.ComponentProps<typeof DialogPrimitive.Root> & { dismissOnBackdropClick?: boolean }> = ({ dismissOnBackdropClick: _d, ...props }) => (
  <DialogPrimitive.Root {...props} />
);
const DialogTrigger = DialogPrimitive.Trigger;
const DialogPortal = DialogPrimitive.Portal;
const DialogClose = DialogPrimitive.Close;

const DialogOverlay = React.forwardRef<
  React.ComponentRef<typeof DialogPrimitive.Overlay>,
  React.ComponentPropsWithoutRef<typeof DialogPrimitive.Overlay>
>(({ className, ...props }, ref) => (
  <DialogPrimitive.Overlay
    ref={ref}
    className={clsx(styles.overlay, className)}
    {...props}
  />
));
DialogOverlay.displayName = DialogPrimitive.Overlay.displayName;

interface DialogContentProps extends React.ComponentPropsWithoutRef<typeof DialogPrimitive.Content> {
  /** @deprecated ODS compat */
  headerText?: string;
  /** @deprecated ODS compat */
  bodyText?: string;
  /** @deprecated ODS compat */
  contentSlot?: React.ReactNode;
  /** @deprecated ODS compat */
  actionSlot?: React.ReactNode;
  /** @deprecated ODS compat */
  actionsLayout?: string;
  /** @deprecated ODS compat */
  showCloseButton?: boolean;
  /** @deprecated ODS compat */
  closeButtonProps?: Record<string, unknown>;
  /** @deprecated ODS compat */
  stickyButton?: boolean;
}

const DialogContent = React.forwardRef<
  React.ComponentRef<typeof DialogPrimitive.Content>,
  DialogContentProps
>(({ className, children, headerText, bodyText, contentSlot, actionSlot, actionsLayout: _al, showCloseButton: _scb, closeButtonProps: _cbp, stickyButton: _sb, ...props }, ref) => (
  <DialogPortal>
    <DialogOverlay />
    <DialogPrimitive.Content
      ref={ref}
      className={clsx(styles.content, className)}
      {...props}
    >
      {headerText && (
        <div className={styles.header}>
          <DialogPrimitive.Title className={styles.title}>{headerText}</DialogPrimitive.Title>
          {bodyText && <DialogPrimitive.Description className={styles.description}>{bodyText}</DialogPrimitive.Description>}
        </div>
      )}
      {contentSlot}
      {children}
      {actionSlot}
      <DialogPrimitive.Close className={styles.closeButton}>
        <X size={16} />
        <span className="sr-only">Close</span>
      </DialogPrimitive.Close>
    </DialogPrimitive.Content>
  </DialogPortal>
));
DialogContent.displayName = DialogPrimitive.Content.displayName;

const DialogHeader: React.FC<React.HTMLAttributes<HTMLDivElement>> = ({
  className,
  ...props
}) => <div className={clsx(styles.header, className)} {...props} />;
DialogHeader.displayName = "DialogHeader";

const DialogFooter: React.FC<React.HTMLAttributes<HTMLDivElement>> = ({
  className,
  ...props
}) => <div className={clsx(styles.footer, className)} {...props} />;
DialogFooter.displayName = "DialogFooter";

const DialogTitle = React.forwardRef<
  React.ComponentRef<typeof DialogPrimitive.Title>,
  React.ComponentPropsWithoutRef<typeof DialogPrimitive.Title>
>(({ className, ...props }, ref) => (
  <DialogPrimitive.Title
    ref={ref}
    className={clsx(styles.title, className)}
    {...props}
  />
));
DialogTitle.displayName = DialogPrimitive.Title.displayName;

const DialogDescription = React.forwardRef<
  React.ComponentRef<typeof DialogPrimitive.Description>,
  React.ComponentPropsWithoutRef<typeof DialogPrimitive.Description>
>(({ className, ...props }, ref) => (
  <DialogPrimitive.Description
    ref={ref}
    className={clsx(styles.description, className)}
    {...props}
  />
));
DialogDescription.displayName = DialogPrimitive.Description.displayName;

export {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogOverlay,
  DialogPortal,
  DialogTitle,
  DialogTrigger,
};
