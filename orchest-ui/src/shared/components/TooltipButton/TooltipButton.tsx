import {
  Tooltip,
  TooltipArrow,
  TooltipContent,
  TooltipTrigger,
} from "@/design-system/components/ui/tooltip/tooltip";
import styles from "./TooltipButton.module.css";
import React from "react";

interface TooltipButtonProps {
  tooltip: string;
  children: React.ReactNode;
  side?: "top" | "right" | "bottom" | "left";
  className?: string;
}

export const TooltipButton: React.FC<TooltipButtonProps> = ({
  tooltip,
  children,
  side = "top",
  className = "max-w-sm break-words z-50",
}) => {
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <div className={styles.container}>{children}</div>
      </TooltipTrigger>
      <TooltipContent side={side} className={className}>
        {tooltip}
        <TooltipArrow className={styles.arrow} />
      </TooltipContent>
    </Tooltip>
  );
};
