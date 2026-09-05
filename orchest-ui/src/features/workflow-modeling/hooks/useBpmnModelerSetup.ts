import { connectorService } from "@/api/domains";
import { toast } from "@/design-system/components/ui/sonner";
import ConnectorsExtensionModule from "@/features/workflow-modeling/BpmnConnectorConfiguration";
import { TOAST_MESSAGES } from "@/shared/constants";
import { logger } from "@/shared/utils/logger";
import { useIsDarkMode } from "@/shared/stores/uiStore";
import TemplateIconRendererModule from "@bpmn-io/element-templates-icons-renderer";
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  ZeebePropertiesProviderModule,
} from "bpmn-js-properties-panel";
import {
  CloudElementTemplatesPropertiesProviderModule,
} from "bpmn-js-element-templates";
import CamundaBehaviorsModule from "camunda-bpmn-js-behaviors/lib/camunda-cloud";
import BpmnJS from "bpmn-js/lib/Modeler";
import { useCallback, useEffect, useRef, useState } from "react";
import ZeebeBpmnModdle from "zeebe-bpmn-moddle/resources/zeebe.json";
import { createFlowSkinModeler } from "@flowskin-bpmn/flowskin-bpmn/modeler";
import { setTheme as setFlowSkinTheme } from "@flowskin-bpmn/flowskin-bpmn";
import "@flowskin-bpmn/flowskin-bpmn/styles";
import type { FlowSkinModeler } from "@flowskin-bpmn/flowskin-bpmn/modeler";

interface ConnectorExtension {
  loadTemplates: (connectorData: unknown[]) => void;
}

interface UseBpmnModelerSetupOptions {
  initialXml: string;
  containerRef: React.RefObject<HTMLDivElement>;
  propertiesPanelRef: React.RefObject<HTMLDivElement>;
  onDiagramChange?: (xml: string) => void;
}

// Extract the BeatRenderer module from a throwaway FlowSkin modeler instance
let beatModule: Record<string, unknown> | null = null;

function getBeatModule() {
  if (beatModule) return beatModule;

  const tempContainer = document.createElement('div');
  tempContainer.style.cssText = 'position:absolute;width:1px;height:1px;overflow:hidden;opacity:0';
  document.body.appendChild(tempContainer);

  const temp = createFlowSkinModeler({ container: tempContainer, hoverCard: false, keyboard: false });
  const modeler = temp.getModeler() as { get?: (name: string) => unknown };

  if (modeler?.get) {
    const renderer = modeler.get('beatRenderer') as { constructor?: unknown };
    if (renderer?.constructor) {
      beatModule = {
        __init__: ['beatRenderer'],
        beatRenderer: ['type', renderer.constructor],
      };
    }
  }

  temp.destroy();
  document.body.removeChild(tempContainer);

  return beatModule;
}

export function useBpmnModelerSetup({
  initialXml,
  containerRef,
  propertiesPanelRef,
  onDiagramChange,
}: UseBpmnModelerSetupOptions) {
  const isDarkMode = useIsDarkMode();
  const modelerRef = useRef<InstanceType<typeof BpmnJS> | null>(null);
  const [isLoaded, setIsLoaded] = useState(false);
  const autoSaveTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const initializedRef = useRef(false);
  const lastImportedXmlRef = useRef<string>("");

  const onDiagramChangeRef = useRef(onDiagramChange);
  useEffect(() => {
    if (onDiagramChange) {
      onDiagramChangeRef.current = onDiagramChange;
    }
  }, [onDiagramChange]);

  // Create modeler only once on mount
  useEffect(() => {
    if (!containerRef.current || !propertiesPanelRef.current || initializedRef.current) {
      return;
    }

    initializedRef.current = true;
    setFlowSkinTheme(isDarkMode ? 'dark' : 'light');

    // Build modules: FlowSkin BeatRenderer + all existing modules
    const flowSkinRenderer = getBeatModule();
    const additionalModules = [
      ConnectorsExtensionModule,
      BpmnPropertiesPanelModule,
      BpmnPropertiesProviderModule,
      ZeebePropertiesProviderModule,
      CloudElementTemplatesPropertiesProviderModule,
      CamundaBehaviorsModule,
      TemplateIconRendererModule,
      ...(flowSkinRenderer ? [flowSkinRenderer] : []),
    ];

    const modeler = new BpmnJS({
      container: containerRef.current,
      propertiesPanel: { parent: propertiesPanelRef.current },
      additionalModules,
      moddleExtensions: { zeebe: ZeebeBpmnModdle },
      keyboard: { bindTo: document },
      exporter: { name: "connectors-modeling-demo", version: "0.0.0" },
      connectorsExtension: { appendAnything: false },
    });

    modelerRef.current = modeler;

    const initializeModeler = async () => {
      try {
        // Load connector templates (non-blocking)
        try {
          const templates = await connectorService.getConnectorTemplates();
          if (templates?.content?.length > 0) {
            const mappedTemplates = templates.content
              .map((template) => template.connectorData)
              .filter(Boolean);
            if (mappedTemplates.length > 0) {
              (modeler.get("connectorsExtension") as ConnectorExtension).loadTemplates(mappedTemplates);
            }
          }
        } catch (connectorErr) {
          logger.warn("Failed to load connector templates:", connectorErr);
        }

        // Setup auto-save
        if (onDiagramChangeRef.current) {
          const eventBus = modeler.get('eventBus');
          eventBus.on('commandStack.changed', () => {
            if (autoSaveTimeoutRef.current) {
              clearTimeout(autoSaveTimeoutRef.current);
            }
            autoSaveTimeoutRef.current = setTimeout(async () => {
              try {
                const { xml } = await modeler.saveXML({ format: true });
                if (xml && onDiagramChangeRef.current) {
                  onDiagramChangeRef.current(xml);
                }
              } catch (error) {
                logger.error("Auto-save diagram error:", error);
              }
            }, 1000);
          });
        }

        // Import initial XML
        const { warnings } = (await modeler.importXML(initialXml)) as { warnings: unknown[] };
        lastImportedXmlRef.current = initialXml;

        if (warnings.length) {
          logger.warn("BPMN import warnings:", warnings);
        }

        setIsLoaded(true);
      } catch (err) {
        logger.error("BPMN import error:", err);
        toast.error(TOAST_MESSAGES.ERROR.LOADING_BPMN_DIAGRAM);
        setIsLoaded(true);
      }
    };

    initializeModeler();

    return () => {
      if (autoSaveTimeoutRef.current) {
        clearTimeout(autoSaveTimeoutRef.current);
      }
      if (modelerRef.current) {
        modelerRef.current.destroy();
        modelerRef.current = null;
      }
      initializedRef.current = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Theme sync — re-import XML to force BeatRenderer redraw
  useEffect(() => {
    if (!isLoaded) return;
    const modeler = modelerRef.current;
    if (!modeler) return;

    setFlowSkinTheme(isDarkMode ? 'dark' : 'light');

    const rerender = async () => {
      try {
        const { xml } = await modeler.saveXML({ format: true });
        if (xml) {
          await modeler.importXML(xml);
          lastImportedXmlRef.current = xml;
        }
      } catch (e) {
        logger.warn("Theme switch re-render failed:", e);
      }
    };
    rerender();
  }, [isDarkMode, isLoaded]);

  // Handle external XML changes
  useEffect(() => {
    const modeler = modelerRef.current;
    if (!modeler || !isLoaded || !initialXml) return;
    if (lastImportedXmlRef.current === initialXml) return;

    const importXml = async () => {
      try {
        const { warnings } = (await modeler.importXML(initialXml)) as { warnings: unknown[] };
        lastImportedXmlRef.current = initialXml;
        if (warnings.length) {
          logger.warn("BPMN import warnings:", warnings);
        }
        try {
          const canvas = modeler.get('canvas');
          if (canvas && typeof canvas.zoom === 'function') {
            canvas.zoom('fit-viewport');
          }
        } catch (zoomError) {
          logger.warn('Could not zoom canvas:', zoomError);
        }
      } catch (err) {
        logger.error("BPMN XML import error:", err);
        toast.error("Failed to load BPMN diagram");
      }
    };

    importXml();
  }, [initialXml, isLoaded]);

  // Expose importXml for external use
  const importXml = useCallback(async (xml: string) => {
    const modeler = modelerRef.current;
    if (!modeler) {
      toast.error("Modeler is not initialized. Please refresh the page.");
      throw new Error('Modeler not initialized');
    }

    if (!isLoaded) {
      const maxWait = 5000;
      const startTime = Date.now();
      while (!isLoaded && (Date.now() - startTime) < maxWait) {
        await new Promise(resolve => setTimeout(resolve, 100));
      }
      if (!isLoaded) {
        toast.error("Modeler is still loading. Please try again.");
        throw new Error('Modeler initialization timeout');
      }
    }

    try {
      const { warnings } = (await modeler.importXML(xml)) as { warnings: unknown[] };
      lastImportedXmlRef.current = xml;
      if (warnings.length) {
        logger.warn("BPMN import warnings:", warnings);
      }
      try {
        const canvas = modeler.get('canvas');
        if (canvas && typeof canvas.zoom === 'function') {
          canvas.zoom('fit-viewport');
        }
      } catch (zoomError) {
        logger.warn('Could not zoom canvas:', zoomError);
      }
    } catch (err) {
      logger.error("BPMN XML import error:", err);
      toast.error("Failed to load BPMN diagram");
      throw err;
    }
  }, [isLoaded]);

  return { modelerRef, isLoaded, importXml };
}
