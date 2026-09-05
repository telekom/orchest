import { Button } from "@/design-system/components/ui/button";
import clsx from "clsx";
import { LayoutPanelLeft } from "lucide-react";
import React from "react";
import styles from "./PropertiesPanelToggle.module.css";

interface PropertiesPanelToggleProps {
  showPropertiesPanel: boolean;
  setShowPropertiesPanel: (show: boolean) => void;
  disabled?: boolean;
}

/**
 * Icon button for showing/hiding the properties panel
 */
export const PropertiesPanelToggle: React.FC<PropertiesPanelToggleProps> = ({
  showPropertiesPanel,
  setShowPropertiesPanel,
  disabled = false,
}) => (
  <Button
    variant="ghost"
    onClick={() => setShowPropertiesPanel(!showPropertiesPanel)}
    size="icon"
    disabled={disabled}
    className={clsx(styles.button, styles.subtleWhite, {
      [styles.active]: showPropertiesPanel,
    })}
    aria-label={showPropertiesPanel ? "Hide properties panel" : "Show properties panel"}
    aria-pressed={showPropertiesPanel}
  >
    <LayoutPanelLeft size={16} />
  </Button>
);
