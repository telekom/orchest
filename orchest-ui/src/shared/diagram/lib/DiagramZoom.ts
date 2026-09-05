import { globalErrorHandler } from "@/shared/error/globalErrorHandler";
import type { DiagramViewer } from "../types";

interface Canvas {
  zoom(level?: number | string, center?: string): number;
  viewbox(box?: { x: number; y: number; width: number; height: number }): { x: number; y: number; width: number; height: number; inner: { x: number; y: number; width: number; height: number }; outer: { width: number; height: number } };
}

const getCanvas = (viewer: DiagramViewer) => {
  const activeViewer = viewer.getActiveViewer ? viewer.getActiveViewer() : viewer;
  return activeViewer.get("canvas") as Canvas | undefined;
};

const PADDING = 20;

export const zoomToFit = (viewer: DiagramViewer): void => {
  try {
    const canvas = getCanvas(viewer);
    if (!canvas || typeof canvas.zoom !== "function") return;

    const vbox = canvas.viewbox();
    const inner = vbox.inner;
    const outer = vbox.outer;

    if (!inner || !outer || inner.width === 0 || inner.height === 0) {
      canvas.zoom("fit-viewport", "auto");
      return;
    }

    const scaleX = (outer.width - PADDING * 2) / inner.width;
    const scaleY = (outer.height - PADDING * 2) / inner.height;
    const newZoom = Math.min(scaleX, scaleY);

    canvas.viewbox({
      x: inner.x - (outer.width / newZoom - inner.width) / 2,
      y: inner.y - (outer.height / newZoom - inner.height) / 2,
      width: outer.width / newZoom,
      height: outer.height / newZoom,
    });
  } catch (error) {
    globalErrorHandler.handleError(
      error instanceof Error ? error : new Error(String(error)),
      "runtime",
      {
        context: "diagram-zoom-fit",
        severity: "low",
      }
    );
  }
};

export const zoomIn = (viewer: DiagramViewer, step: number = 0.1): void => {
  try {
    const canvas = getCanvas(viewer);
    if (canvas && typeof canvas.zoom === "function") {
      const currentZoom = canvas.zoom();
      canvas.zoom(currentZoom + step);
    }
  } catch (error) {
    globalErrorHandler.handleError(
      error instanceof Error ? error : new Error(String(error)),
      "runtime",
      {
        context: "diagram-zoom-in",
        severity: "low",
      }
    );
  }
};

export const zoomOut = (viewer: DiagramViewer, step: number = 0.1): void => {
  try {
    const canvas = getCanvas(viewer);
    if (canvas && typeof canvas.zoom === "function") {
      const currentZoom = canvas.zoom();
      canvas.zoom(Math.max(0.1, currentZoom - step));
    }
  } catch (error) {
    globalErrorHandler.handleError(
      error instanceof Error ? error : new Error(String(error)),
      "runtime",
      {
        context: "diagram-zoom-out",
        severity: "low",
      }
    );
  }
};

export const getActiveViewer = (viewer: DiagramViewer): unknown => {
  if (viewer.getActiveViewer && typeof viewer.getActiveViewer === "function") {
    return viewer.getActiveViewer();
  }
  return viewer;
};
