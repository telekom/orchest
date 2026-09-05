import { Button } from "@/design-system/components/ui/button";
import clsx from "clsx";
import { Code, Eye } from "lucide-react";
import React from "react";
import styles from "./CodeViewToggle.module.css";

interface CodeViewToggleProps {
  showCodeView: boolean;
  onToggle: () => void;
}

export const CodeViewToggle: React.FC<CodeViewToggleProps> = ({
  showCodeView,
  onToggle,
}) => {
  return (
    <Button
      variant="ghost"
      onClick={onToggle}
      size="icon"
      className={clsx(styles.button, styles.subtleWhite)}
      aria-label={showCodeView ? "Show Diagram" : "Show Code"}
    >
      {showCodeView ? <Eye size={16} /> : <Code size={16} />}
    </Button>
  );
};
