import { AlertTriangle, Info, XCircle, CheckCircle } from "lucide-react";
import React from "react";
import clsx from "clsx";
import styles from "./ModalBanner.module.css";

export type ModalBannerType = "warning" | "info" | "error" | "success";

export interface ModalBannerProps {
  type: ModalBannerType;
  title?: string;
  message: React.ReactNode;
  className?: string;
}

const BANNER_CONFIG = {
  warning: {
    icon: AlertTriangle,
    containerClass: styles.warningBox,
    iconClass: styles.warningIcon,
    titleClass: styles.warningTitle,
    textClass: styles.warningText,
  },
  info: {
    icon: Info,
    containerClass: styles.infoBox,
    iconClass: styles.infoIcon,
    titleClass: styles.infoTitle,
    textClass: styles.infoText,
  },
  error: {
    icon: XCircle,
    containerClass: styles.errorBox,
    iconClass: styles.errorIcon,
    titleClass: styles.errorTitle,
    textClass: styles.errorText,
  },
  success: {
    icon: CheckCircle,
    containerClass: styles.successBox,
    iconClass: styles.successIcon,
    titleClass: styles.successTitle,
    textClass: styles.successText,
  },
} as const;

export const ModalBanner: React.FC<ModalBannerProps> = ({
  type,
  title,
  message,
  className,
}) => {
  const config = BANNER_CONFIG[type];
  const Icon = config.icon;

  return (
    <div className={clsx(config.containerClass, className)}>
      <Icon className={config.iconClass} />
      <div className={styles.content}>
        {title && <h4 className={config.titleClass}>{title}</h4>}
        <div className={config.textClass}>{message}</div>
      </div>
    </div>
  );
};
