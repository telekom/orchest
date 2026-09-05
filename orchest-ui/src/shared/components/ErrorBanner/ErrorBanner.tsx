import { AlertCircle } from "lucide-react";
import React from "react";
import styles from './ErrorBanner.module.css';

export interface ErrorBannerProps {
  message: string;
  className?: string;
}

/**
 * Centralized error banner component for displaying errors.
 * For data loading errors, users can simply reload the page.
 */
export const ErrorBanner: React.FC<ErrorBannerProps> = ({
  message,
  className,
}) => {
  return (
    <div className={className || styles.pageContainer}>
      <div className={styles.errorBanner}>
        <AlertCircle className={styles.errorIcon} />
        <span className={styles.errorText}>{message}</span>
      </div>
    </div>
  );
};

export default ErrorBanner;
