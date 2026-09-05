import React from "react";
import styles from "./EmptyState.module.css";
import clsx from "clsx";

export interface EmptyStateProps {
  title: string;
  description?: string;
  icon?: React.ReactNode;
  className?: string;
  variant?: 'default' | 'dark-text';
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  title,
  description,
  icon,
  className = "",
  variant = 'default',
}) => {
  return (
    <div className={clsx(
      styles.container,
      variant === 'dark-text' && styles.darkText,
      className
    )}>
      <div className={styles.contentWrapper}>
        {icon && <div className={styles.iconWrapper}>{icon}</div>}
        <p className={styles.title}>{title}</p>
        {description && <p className={styles.description}>{description}</p>}
      </div>
    </div>
  );
};
