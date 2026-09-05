import { TIMING } from "@/shared/constants";
import {
    BPMN_VIEWER_DEFAULTS,
    ERROR_SEVERITY,
    KEYBOARD_CONFIG,
} from "@/shared/diagram/constants";
import { globalErrorHandler } from "@/shared/error/globalErrorHandler";
import { useStableCallback } from "@/shared/hooks/useStableCallback";
import { useIsDarkMode } from "@/shared/stores/uiStore";
import { RefObject, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { applyDiagramTheme } from "../lib/DiagramTheme";
import { zoomIn, zoomOut, zoomToFit } from "../lib/DiagramZoom";
import type { DiagramViewer, ElementRegistryService, EventBusService, OverlaysService } from "../types";
import { createFlowSkinBPMN, setTheme as setFlowSkinTheme } from "@flowskin-bpmn/flowskin-bpmn";
import "@flowskin-bpmn/flowskin-bpmn/styles";
import "../styles/flowskin-override.css";

// Re-export types for convenience
export type { DiagramViewer, ElementRegistryService, EventBusService, OverlaysService };

type DiagramViewerClass = new (options?: Record<string, unknown>) => DiagramViewer;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type FlowSkinInstance = ReturnType<typeof createFlowSkinBPMN> & { [key: string]: any };

interface DiagramViewerConfig {
  containerRef: RefObject<HTMLDivElement | null>;
  xml: string;
  engine: "bpmn" | "dmn";
  ViewerClass: DiagramViewerClass;
  viewerOptions?: Record<string, unknown>;
  onLoaded?: (viewer: DiagramViewer) => void;
  onError?: (error: Error) => void;
  /** Use @flowskin-bpmn renderer instead of raw bpmn-js. Only applies when engine="bpmn". */
  useFlowSkin?: boolean;
  /** Skip custom theme modules — use stock bpmn-js rendering. Only applies when useFlowSkin=false. */
  vanilla?: boolean;
}

const getEngineDefaultOptions = (engine: "bpmn" | "dmn") => {
  if (engine === "bpmn") {
    return BPMN_VIEWER_DEFAULTS;
  }

  return {};
};

const createViewer = (
  ViewerClass: DiagramViewerClass,
  container: HTMLDivElement,
  options: Record<string, unknown>
) => {
  return new ViewerClass({
    container,
    keyboard: KEYBOARD_CONFIG,
    ...options,
  });
};

const handleImportError = (error: Error, engine: string, onErrorCallback?: (err: Error) => void) => {
  globalErrorHandler.handleError(error, 'runtime', {
    context: `diagram-viewer-import-${engine}`,
    severity: ERROR_SEVERITY.HIGH
  });
  onErrorCallback?.(error);
};

export const useDiagramViewer = ({
  containerRef,
  xml,
  engine,
  ViewerClass,
  viewerOptions = {},
  onLoaded,
  onError,
  useFlowSkin = false,
  vanilla = false,
}: DiagramViewerConfig) => {
  const isDarkMode = useIsDarkMode();
  const isDarkModeRef = useRef(isDarkMode);
  isDarkModeRef.current = isDarkMode;
  const viewerRef = useRef<DiagramViewer | null>(null);
  const flowSkinRef = useRef<FlowSkinInstance | null>(null);
  const [isLoaded, setIsLoaded] = useState(false);
  const [viewer, setViewer] = useState<DiagramViewer | null>(null);
  const [flowSkin, setFlowSkin] = useState<FlowSkinInstance | null>(null);
  const [error, setError] = useState<Error | null>(null);

  const stableOnLoaded = useStableCallback(onLoaded);
  const stableOnError = useStableCallback(onError);

  const defaultViewerOptions = useMemo(() => vanilla ? {} : getEngineDefaultOptions(engine), [engine, vanilla]);

  const mergedViewerOptions = useMemo(
    () => ({
      ...defaultViewerOptions,
      ...viewerOptions,
      ...(defaultViewerOptions.additionalModules || viewerOptions.additionalModules
        ? {
            additionalModules: [
              ...(defaultViewerOptions.additionalModules ?? []),
              ...((viewerOptions.additionalModules as unknown[] | undefined) ?? []),
            ],
          }
        : {}),
      ...(defaultViewerOptions.moddleExtensions || viewerOptions.moddleExtensions
        ? {
            moddleExtensions: {
              ...(defaultViewerOptions.moddleExtensions as Record<string, unknown> | undefined),
              ...(viewerOptions.moddleExtensions as Record<string, unknown> | undefined),
            },
          }
        : {}),
    }),
    [defaultViewerOptions, viewerOptions]
  );

  const viewerOptionsKey = JSON.stringify(mergedViewerOptions);
  const stableViewerOptions = useMemo(() => mergedViewerOptions, [viewerOptionsKey]); // eslint-disable-line react-hooks/exhaustive-deps -- viewerOptionsKey is a JSON serialization of mergedViewerOptions

  useEffect(() => {
    if (!containerRef.current || !xml) return;

    let mounted = true;

    const timeoutId = setTimeout(() => {
      if (!mounted || !containerRef.current) return;
      const { offsetWidth, offsetHeight } = containerRef.current;
      if (offsetWidth === 0 || offsetHeight === 0) return;

      if (useFlowSkin && engine === "bpmn") {
        const initialTheme = isDarkModeRef.current ? 'dark' : 'light';
        setFlowSkinTheme(initialTheme);
        const renderer = createFlowSkinBPMN({
          container: containerRef.current,
          theme: initialTheme,
          hoverCard: false,
        }) as FlowSkinInstance;

        flowSkinRef.current = renderer;
        const innerViewer = renderer.getViewer() as unknown as DiagramViewer;
        viewerRef.current = innerViewer;

        if (mounted) {
          setViewer(innerViewer);
          setFlowSkin(renderer);
        }

        renderer.loadXml(xml)
          .then(() => {
            if (!mounted) return;
            setIsLoaded(true);
            setError(null);
            stableOnLoaded?.(innerViewer);
          })
          .catch((err: Error) => {
            if (!mounted) return;
            handleImportError(err, engine, stableOnError);
            setError(err);
          });
      } else {
        // Classic view — set data-theme so FlowSkin CSS vars apply correct mode
        document.documentElement.setAttribute('data-theme', isDarkModeRef.current ? '' : 'light');

        const newViewer = createViewer(ViewerClass, containerRef.current, stableViewerOptions);

        viewerRef.current = newViewer;

        if (mounted) {
          setViewer(newViewer);
        }

        newViewer
          .importXML(xml)
          .then(() => {
            if (!mounted) return;
            zoomToFit(newViewer);
            setIsLoaded(true);
            setError(null);
            stableOnLoaded?.(newViewer);
          })
          .catch((err: Error) => {
            if (!mounted) return;
            handleImportError(err, engine, stableOnError);
            setError(err);
          });
      }
    }, TIMING.DIAGRAM_INIT_DELAY);

    return () => {
      mounted = false;
      clearTimeout(timeoutId);
      if (flowSkinRef.current) {
        flowSkinRef.current.destroy();
        flowSkinRef.current = null;
        setIsLoaded(false);
        setViewer(null);
        setFlowSkin(null);
        viewerRef.current = null;
      } else if (viewerRef.current) {
        viewerRef.current.destroy();
        setIsLoaded(false);
        setViewer(null);
        viewerRef.current = null;
      }
    };
  }, [xml, engine, ViewerClass, stableViewerOptions, containerRef, stableOnLoaded, stableOnError, useFlowSkin]);

  // Theme changes — update in place without recreating the viewer
  useEffect(() => {
    if (!viewer || !isLoaded) return;
    if (useFlowSkin && flowSkinRef.current) {
      setFlowSkinTheme(isDarkMode ? 'dark' : 'light');
      flowSkinRef.current.setTheme?.(isDarkMode ? 'dark' : 'light');
    } else if (engine === 'dmn') {
      applyDiagramTheme(viewer, isDarkMode);
    } else {
      // Classic BPMN — sync data-theme so FlowSkin CSS vars pick up correct mode
      document.documentElement.setAttribute('data-theme', isDarkMode ? '' : 'light');
    }
  }, [isDarkMode, isLoaded, viewer, useFlowSkin, engine]);

  // Re-center diagram when container resizes (e.g., sidebar toggle, filter bar expansion)
  // Works for BPMN diagrams and DMN DRD views (skips DMN decision tables to avoid errors)
  useEffect(() => {
    if (!containerRef.current || !viewer || !isLoaded) return;

    let resizeTimeout: NodeJS.Timeout;
    const resizeObserver = new ResizeObserver(() => {
      // Debounce resize events
      clearTimeout(resizeTimeout);
      resizeTimeout = setTimeout(() => {
        // For DMN viewers, only resize if current view is DRD (not decision table)
        if (engine === "dmn" && viewer.getActiveView) {
          const activeView = viewer.getActiveView();
          if (activeView?.type !== "drd") return;
        }
        zoomToFit(viewer);
      }, TIMING.DIAGRAM_INIT_DELAY);
    });

    resizeObserver.observe(containerRef.current);

    return () => {
      clearTimeout(resizeTimeout);
      resizeObserver.disconnect();
    };
  }, [containerRef, viewer, isLoaded, engine]);

  const handleZoomReset = useCallback(() => {
    if (!viewer) return;
    zoomToFit(viewer);
  }, [viewer]);

  const handleZoomIn = useCallback(() => {
    if (!viewer) return;
    zoomIn(viewer);
  }, [viewer]);

  const handleZoomOut = useCallback(() => {
    if (!viewer) return;
    zoomOut(viewer);
  }, [viewer]);

  return {
    viewer,
    isLoaded,
    error,
    flowSkin,
    handleZoomReset,
    handleZoomIn,
    handleZoomOut,
  };
};
