import { ExportFormat } from "@/shared/enums";
import { DiagramTypeValue } from "@/shared/enums/DiagramType";
import { globalErrorHandler } from "@/shared/error/globalErrorHandler";
import { MIME_TYPES } from "@/shared/constants";
import { MutableRefObject, useCallback } from "react";
import { toast } from "@/design-system/components/ui/sonner";
import { downloadFile } from "../lib/DiagramExport";
import { DiagramViewer } from "./useDiagramViewer";

interface UseExportOptions {
  modelerRef: MutableRefObject<DiagramViewer | null>;
  diagramType: DiagramTypeValue;
}

export const useExport = ({
  modelerRef,
  diagramType
}: UseExportOptions) => {
  const handleExport = useCallback(async (format: ExportFormat) => {
    const modeler = modelerRef.current;
    if (!modeler) {
      toast.error("Modeler not initialized");
      return;
    }

    try {
      switch (format) {
        case ExportFormat.XML: {
          if (!modeler.saveXML) {
            toast.error("XML export not supported");
            return;
          }

          const { xml } = await modeler.saveXML({ format: true });

          if (!xml) {
            toast.error("Failed to export diagram XML");
            return;
          }

          const fileName = `diagram.${diagramType}`;
          const mimeType = MIME_TYPES.XML;

          downloadFile(xml, fileName, mimeType);
          toast.success(`${diagramType.toUpperCase()} file downloaded`);
          break;
        }

        case ExportFormat.SVG: {
          if (diagramType === "bpmn" && modeler.saveSVG) {
            const { svg } = await modeler.saveSVG();
            downloadFile(svg, "diagram.svg", MIME_TYPES.SVG);
            toast.success("SVG diagram downloaded");
          } else if (diagramType === "dmn") {
            const activeViewer = modeler.getActiveViewer?.() as DiagramViewer | undefined;
            const canvas = activeViewer?.get("canvas") as { _svg?: SVGElement } | undefined;
            const svgElement = canvas?._svg;

            if (svgElement) {
              const serializer = new XMLSerializer();
              const svgString = serializer.serializeToString(svgElement);
              downloadFile(svgString, "diagram.svg", MIME_TYPES.SVG);
              toast.success("SVG diagram downloaded");
            } else {
              toast.error("Failed to export SVG: canvas not available");
            }
          }
          break;
        }

        case ExportFormat.PNG: {
          if (diagramType !== "bpmn") {
            toast.error("PNG export only supported for BPMN diagrams");
            return;
          }

          if (!modeler.saveSVG) {
            toast.error("PNG export not supported");
            return;
          }

          const { svg } = await modeler.saveSVG();
          const canvas = document.createElement("canvas");
          const ctx = canvas.getContext("2d");

          if (!ctx) {
            const error = new Error("Failed to create canvas context for PNG export");
            globalErrorHandler.handleError(error, 'runtime', {
              context: 'diagram-export-png',
              severity: 'high'
            });
            toast.error("Failed to create canvas context");
            return;
          }

          const img = new Image();

          await new Promise<void>((resolve, reject) => {
            img.onload = () => {
              canvas.width = img.width;
              canvas.height = img.height;
              ctx.drawImage(img, 0, 0);

              const dataUrl = canvas.toDataURL(MIME_TYPES.PNG);
              downloadFile(dataUrl, "diagram.png", MIME_TYPES.PNG);
              toast.success("PNG diagram downloaded");
              resolve();
            };

            img.onerror = () => {
              const error = new Error("Image conversion failed");
              globalErrorHandler.handleError(error, 'runtime', {
                context: 'diagram-export-png-conversion',
                severity: 'medium'
              });
              toast.error("Failed to convert diagram to PNG");
              reject(error);
            };

            img.src = "data:image/svg+xml;base64," + btoa(unescape(encodeURIComponent(svg)));
          });
          break;
        }

        default:
          toast.error(`Unsupported export format: ${format}`);
      }
    } catch (error) {
      globalErrorHandler.handleError(
        error instanceof Error ? error : new Error(String(error)),
        'runtime',
        {
          context: `diagram-export-${format}`,
          diagramType,
          severity: 'medium'
        }
      );
      toast.error(`Failed to export diagram as ${format.toUpperCase()}`);
    }
  }, [modelerRef, diagramType]);

  return { handleExport };
};
