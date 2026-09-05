import { Button } from "@/design-system/components/ui/button";
import clsx from "clsx";
import { ArrowLeft } from "lucide-react";
import React from "react";
import styles from './BackButton.module.css';

export interface BackButtonProps {
  onClick: () => void;
  ariaLabel?: string;
}

/**
 * Centralized back button component for navigation.
 * Memoized to prevent unnecessary re-renders.
 */
export const BackButton: React.FC<BackButtonProps> = React.memo(({
  onClick,
  ariaLabel = "Go back"
}) => {
  return (
    <Button
      variant="ghost"
      size="icon"
      onClick={onClick}
      className={clsx(styles.backButton)}
      aria-label={ariaLabel}
    >
      <ArrowLeft size={16} />
    </Button>
  );
});

BackButton.displayName = "BackButton";

export default BackButton;
