import { useDiagrams } from "@/shared/context";
import React, { Suspense } from "react";

import { ModelerType } from "../constants";
import { useDiagramId, useSaveWithErrorHandling } from "../hooks";
import { MODELER_REGISTRY } from "../config/modelerRegistry";
import { ModelerLoader } from "../components";
import { useActiveModelerType, useModelerStore } from "../stores";
import styles from "./ModelerPage.module.css";

const ModelerPage: React.FC = () => {
  const { saveDiagram } = useDiagrams();
  const activeModeler = useActiveModelerType();
  const setActiveModelerType = useModelerStore((state) => state.setActiveModelerType);
  const currentDiagramId = useDiagramId();
  const handleSaveDiagram = useSaveWithErrorHandling(saveDiagram, currentDiagramId);
  const ActiveModelerComponent = MODELER_REGISTRY[activeModeler];

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <Suspense fallback={<ModelerLoader type={activeModeler} />}>
          <ActiveModelerComponent
            key={`${activeModeler}-modeler`}
            onSave={handleSaveDiagram}
            activeModeler={activeModeler}
            onModelerTypeChange={(value) => setActiveModelerType(value as ModelerType)}
          />
        </Suspense>
      </div>
    </div>
  );
};

export default ModelerPage;
