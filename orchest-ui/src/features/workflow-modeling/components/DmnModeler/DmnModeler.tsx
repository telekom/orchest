import { decisionDefinitionService } from "@/api/domains";
import { toast } from "@/design-system/components/ui/sonner";
import { getDiagramTheme, getDmnRendererConfig } from "@/shared/diagram/lib/DiagramTheme";
import { useIsDarkMode } from "@/shared/stores/uiStore";
import { logger } from "@/shared/utils/logger";
import clsx from "clsx";
import {
    DmnPropertiesPanelModule,
    DmnPropertiesProviderModule,
} from "dmn-js-properties-panel";
import DmnJS from "dmn-js/lib/Modeler";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { useLocation } from "react-router-dom";
import { MODELER_CONSTANTS } from "../../constants";
import { ModelerProvider, useModelerContext } from "../../context/ModelerContext";
import { emptyDmn } from "../../templates/emptyDmn";
import BaseModeler from "../BaseModeler/BaseModeler";
import styles from "./DmnModeler.module.css";

interface DmnModelerProps {
  onSave?: (xml: string) => void;
  activeModeler?: string;
  onModelerTypeChange?: (value: string) => void;
}

interface DmnView {
  type: string;
  id?: string;
}

interface EventBus {
  on: (event: string, callback: (e: unknown) => void) => void;
}

const VIEW_TYPES = {
  DECISION_TABLE: "decisionTable",
  DRD: "drd",
} as const;

const INPUT_TAGS = ["INPUT", "TEXTAREA"];
const EXPORT_FORMATS = [
  { value: "xml", label: "DMN XML" },
  { value: "svg", label: "SVG Image" },
];

const isEventBus = (obj: unknown): obj is EventBus => {
  return typeof obj === "object" && obj !== null && "on" in obj && typeof (obj as { on: unknown }).on === "function";
};

const handleError = (message: string, error: unknown) => {
  logger.error(message, error);
  toast.error(message);
};

const DmnModelerContent: React.FC<DmnModelerProps> = ({ onSave, activeModeler, onModelerTypeChange }) => {
  const location = useLocation();
  const isDarkMode = useIsDarkMode();
  const { showPropertiesPanel } = useModelerContext();
  const diagramTheme = getDiagramTheme(isDarkMode);

  // Priority: 1. Route state XML (from navigation), 2. Empty template
  // NO persistence - always start fresh or from route
  const initialXml = (location.state as { dmnXml?: string } | null)?.dmnXml || emptyDmn;

  const containerRef = useRef<HTMLDivElement>(null);
  const propertiesPanelRef = useRef<HTMLDivElement>(null);
  const dmnModelerRef = useRef<InstanceType<typeof DmnJS> | null>(null);
  const initializedRef = useRef(false);
  const autoSaveTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const lastImportedXmlRef = useRef<string>("");

  const [isViewOnly] = useState(false);
  const [currentViewType, setCurrentViewType] = useState<string | null>(null);
  const [isModelerLoaded, setIsModelerLoaded] = useState(false);

  // Initialize modeler only once on mount
  useEffect(() => {

    if (!containerRef.current || !propertiesPanelRef.current || initializedRef.current) {
      return;
    }

    initializedRef.current = true;

    const modeler = new DmnJS({
      container: containerRef.current,
      keyboard: { bindTo: document },
      drd: {
        propertiesPanel: { parent: propertiesPanelRef.current },
        additionalModules: [DmnPropertiesPanelModule, DmnPropertiesProviderModule],
      },
      decisionTable: {
        debounceInput: true,
        debounceTimeout: MODELER_CONSTANTS.DMN.DEBOUNCE_TIMEOUT,
        editorScroll: false,
        optimizer: { batching: true },
        editingAllowed: !isViewOnly,
      },
      common: {
        renderer: getDmnRendererConfig(isDarkMode),
      },
    });

    dmnModelerRef.current = modeler;

    // Setup error handling
    try {
      const eventBus = modeler.get?.("eventBus");
      if (isEventBus(eventBus)) {
        eventBus.on("error", (e: unknown) => {
          logger.error("dmn-js error:", e);
        });
      }
    } catch (err) {
      logger.error("Failed to setup eventBus:", err);
    }

    // Handle input blur and auto-save on command stack changes
    modeler.on("commandStack.changed", () => {
      // Handle input blur for DMN tables
      setTimeout(() => {
        const activeEl = document.activeElement;
        if (activeEl && INPUT_TAGS.includes(activeEl.tagName)) {
          activeEl.addEventListener("blur", () => {}, { once: true });
        }
      }, 0);

      // No auto-save - persistence disabled
    });

    // Import initial XML and open appropriate view
    const importAndOpen = async () => {
      try {
        await modeler.importXML(initialXml);
        lastImportedXmlRef.current = initialXml;

        const views = modeler.getViews() as DmnView[];
        const decisionTableView = views.find((v) => v.type === VIEW_TYPES.DECISION_TABLE);
        const drdView = views.find((v) => v.type === VIEW_TYPES.DRD);

        // Open view and track type in callback to avoid polling
        if (drdView) {
          modeler.open(drdView, () => {
            setCurrentViewType(VIEW_TYPES.DRD);
            setIsModelerLoaded(true);
          });
        } else if (decisionTableView) {
          modeler.open(decisionTableView, () => {
            setCurrentViewType(VIEW_TYPES.DECISION_TABLE);
            setIsModelerLoaded(true);
          });
        } else {
          setIsModelerLoaded(true);
        }

        // Optimize table rendering performance
        if (containerRef.current) {
          const tableContainers = containerRef.current.querySelectorAll(".tjs-table-container");
          tableContainers.forEach((el) => {
            if (el instanceof HTMLElement) {
              el.style.willChange = "transform";
            }
          });
        }
      } catch (err) {
        handleError("Failed to load DMN diagram", err);
        setIsModelerLoaded(true); // Set loaded even on error to prevent stuck state
      }
    };

    importAndOpen();

    return () => {
      // Clear auto-save timeout
      if (autoSaveTimeoutRef.current) {
        clearTimeout(autoSaveTimeoutRef.current);
      }

      try {
        dmnModelerRef.current?.destroy();
      } catch (e) {
        logger.error("Error destroying modeler:", e);
      } finally {
        dmnModelerRef.current = null;
        initializedRef.current = false;
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // ONLY run once on mount - no dependencies to prevent re-initialization!

  // Separate effect to handle XML changes (without recreating the modeler)
  useEffect(() => {
    const modeler = dmnModelerRef.current;
    if (!modeler || !isModelerLoaded || !initialXml) return;

    // Skip if this is the same XML we just imported
    if (lastImportedXmlRef.current === initialXml) return;

    // Skip the initial import (already handled in the modeler initialization)
    // Only import when XML actually changes after initialization
    const importXml = async () => {
      try {
        await modeler.importXML(initialXml);
        lastImportedXmlRef.current = initialXml;

        const views = modeler.getViews() as DmnView[];
        const decisionTableView = views.find((v) => v.type === VIEW_TYPES.DECISION_TABLE);
        const drdView = views.find((v) => v.type === VIEW_TYPES.DRD);

        // Open the appropriate view after import
        if (drdView) {
          await new Promise<void>((resolve) => {
            modeler.open(drdView, () => {
              setCurrentViewType(VIEW_TYPES.DRD);
              resolve();
            });
          });
        } else if (decisionTableView) {
          await new Promise<void>((resolve) => {
            modeler.open(decisionTableView, () => {
              setCurrentViewType(VIEW_TYPES.DECISION_TABLE);
              resolve();
            });
          });
        }

        // Optimize table rendering performance
        if (containerRef.current) {
          const tableContainers = containerRef.current.querySelectorAll(".tjs-table-container");
          tableContainers.forEach((el) => {
            if (el instanceof HTMLElement) {
              el.style.willChange = "transform";
            }
          });
        }
      } catch (err) {
        logger.error("DMN XML import error:", err);
        toast.error("Failed to load DMN diagram");
      }
    };

    // Only import if the modeler is already initialized and XML has changed
    if (initializedRef.current && isModelerLoaded) {
      importXml();
    }
  }, [initialXml, isModelerLoaded]);

  // Expose importXml function for external use (e.g., when loading definitions from dropdown)
  const importXmlExternal = useCallback(async (xml: string) => {
    const modeler = dmnModelerRef.current;
    if (!modeler) {
      logger.error('Cannot import XML: DMN modeler not initialized');
      toast.error("Modeler is not initialized. Please refresh the page.");
      throw new Error('DMN modeler not initialized');
    }

    // Wait for modeler to be ready (with timeout)
    if (!isModelerLoaded) {
      logger.info('Waiting for DMN modeler to initialize...');
      const maxWait = 5000; // 5 seconds max
      const startTime = Date.now();

      while (!isModelerLoaded && (Date.now() - startTime) < maxWait) {
        await new Promise(resolve => setTimeout(resolve, 100));
      }

      if (!isModelerLoaded) {
        logger.error('DMN modeler initialization timeout');
        toast.error("Modeler is still loading. Please try again.");
        throw new Error('DMN modeler initialization timeout');
      }
    }

    try {
      await modeler.importXML(xml);
      lastImportedXmlRef.current = xml; // Track to prevent double-import

      const views = modeler.getViews() as DmnView[];
      const decisionTableView = views.find((v) => v.type === VIEW_TYPES.DECISION_TABLE);
      const drdView = views.find((v) => v.type === VIEW_TYPES.DRD);

      // Open the appropriate view after import
      if (drdView) {
        await new Promise<void>((resolve) => {
          modeler.open(drdView, () => {
            setCurrentViewType(VIEW_TYPES.DRD);
            resolve();
          });
        });
      } else if (decisionTableView) {
        await new Promise<void>((resolve) => {
          modeler.open(decisionTableView, () => {
            setCurrentViewType(VIEW_TYPES.DECISION_TABLE);
            resolve();
          });
        });
      }

      // Optimize table rendering performance
      if (containerRef.current) {
        const tableContainers = containerRef.current.querySelectorAll(".tjs-table-container");
        tableContainers.forEach((el) => {
          if (el instanceof HTMLElement) {
            el.style.willChange = "transform";
          }
        });
      }
    } catch (err) {
      logger.error("DMN XML import error:", err);
      toast.error("Failed to load DMN diagram");
      throw err;
    }
  }, [isModelerLoaded]);

  return (
    <BaseModeler
      diagramType="dmn"
      itemType="decision"
      modelerRef={dmnModelerRef as unknown as React.RefObject<import('@/shared/diagram/hooks/useDiagramViewer').DiagramViewer | null>}
      emptyTemplate={emptyDmn}
      definitionService={decisionDefinitionService as unknown as import("./BaseModeler").DefinitionService}
      onSave={onSave}
      customExportFormats={EXPORT_FORMATS}
      disablePropertiesPanel={currentViewType !== VIEW_TYPES.DRD}
      activeModeler={activeModeler}
      onModelerTypeChange={onModelerTypeChange}
      onImportXml={importXmlExternal}
    >
      <div className={styles.container}>
        <div className={styles.modelerWrapper}>
          <div
            ref={containerRef}
            className={clsx(styles.modelerContainer, 'dmn-modeler-container', 'modeler-container')}
            style={{ backgroundColor: diagramTheme.container }}
          />
        </div>

        <div
          ref={propertiesPanelRef}
          className={clsx(styles.propertiesPanel, 'properties-panel', {
            [styles.visible]: currentViewType === VIEW_TYPES.DRD && showPropertiesPanel,
            [styles.hidden]: !(currentViewType === VIEW_TYPES.DRD && showPropertiesPanel),
          })}
        />
      </div>
    </BaseModeler>
  );
};

const DmnModeler: React.FC<DmnModelerProps> = ({ onSave, activeModeler, onModelerTypeChange }) => {
  return (
    <ModelerProvider>
      <DmnModelerContent onSave={onSave} activeModeler={activeModeler} onModelerTypeChange={onModelerTypeChange} />
    </ModelerProvider>
  );
};

export default DmnModeler;