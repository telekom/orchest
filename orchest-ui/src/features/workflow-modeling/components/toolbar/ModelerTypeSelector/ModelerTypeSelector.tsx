import React from "react";
import { ModelerTypes } from "../../../constants";
import styles from "./ModelerTypeSelector.module.css";

interface ModelerTypeSelectorProps {
  activeModeler: string;
  onModelerTypeChange: (value: string) => void;
}

export const ModelerTypeSelector: React.FC<ModelerTypeSelectorProps> = ({
  activeModeler,
  onModelerTypeChange,
}) => {
  return (
    <div className={styles.segmentedControl} role="radiogroup" aria-label="Modeler type">
      <button
        type="button"
        role="radio"
        aria-checked={activeModeler === ModelerTypes.BPMN}
        className={`${styles.segment} ${activeModeler === ModelerTypes.BPMN ? styles.active : ''}`}
        onClick={() => onModelerTypeChange(ModelerTypes.BPMN)}
      >
        BPMN
      </button>
      <button
        type="button"
        role="radio"
        aria-checked={activeModeler === ModelerTypes.DMN}
        className={`${styles.segment} ${activeModeler === ModelerTypes.DMN ? styles.active : ''}`}
        onClick={() => onModelerTypeChange(ModelerTypes.DMN)}
      >
        DMN
      </button>
    </div>
  );
};
