import { LoadingIcon } from "@/design-system/components/ui/loading-icon";

import { Button } from "@/design-system/components/ui/button";
import clsx from "clsx";
import React from "react";
import styles from "./ToolbarButton.module.css";

export interface ToolbarButtonProps {
  /** ODS icon name (e.g., "circle-add", "publish", "upload") */
  icon?: ODSIconName;
  label: string;
  onClick: () => void;
  isLoading?: boolean;
  className?: string;
}

/**
 * Reusable toolbar button with optional loading state
 * Used for primary toolbar actions (New, Deploy, Generate Code, etc.)
 */
export const ToolbarButton: React.FC<ToolbarButtonProps> = ({
  icon: _icon,
  label,
  onClick,
  isLoading,
  className = "",
}) => {
  return (
    <Button
      variant="ghost"
      size="sm"
      onClick={onClick}
      label={label}
      className={clsx(styles.button, className)}
    >
      {isLoading && (
        <div className={styles.loadingContainer}>
          <LoadingIcon size={15} className={styles.loadingIcon} />
        </div>
      )}
    </Button>
  );
};
