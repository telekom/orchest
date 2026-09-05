import { RefObject, useEffect, useRef } from "react";
import type { DiagramViewer } from "../types";

const ZOOM_LEVEL = 1.8;

/**
 * Hover-zoom: when enabled, zooms into the diagram and pans to follow the cursor.
 * Moving the mouse across the container scrolls through the zoomed diagram.
 */
export function useHoverZoom(
  containerRef: RefObject<HTMLDivElement | null>,
  viewer: DiagramViewer | null,
  enabled: boolean,
) {
  const savedViewboxRef = useRef<{ x: number; y: number; width: number; height: number } | null>(null);

  useEffect(() => {
    if (!viewer || !containerRef.current || !enabled) {
      // Restore original viewbox when disabled
      if (viewer && savedViewboxRef.current) {
        try {
          const canvas = (viewer.getActiveViewer?.() ?? viewer).get("canvas") as any;
          canvas?.viewbox(savedViewboxRef.current);
        } catch { /* ignore */ }
        savedViewboxRef.current = null;
      }
      return;
    }

    const container = containerRef.current;
    const canvas = (viewer.getActiveViewer?.() ?? viewer).get("canvas") as any;
    if (!canvas) return;

    // Save current viewbox so we can restore on disable
    const original = canvas.viewbox();
    savedViewboxRef.current = { x: original.x, y: original.y, width: original.width, height: original.height };

    const inner = original.inner;
    if (!inner || inner.width === 0 || inner.height === 0) return;

    // Compute zoomed viewbox dimensions
    const zoomedWidth = original.outer.width / ZOOM_LEVEL;
    const zoomedHeight = original.outer.height / ZOOM_LEVEL;

    // Pan range: how far the viewbox origin can move
    const panRangeX = inner.width - zoomedWidth;
    const panRangeY = inner.height - zoomedHeight;

    const handleMouseMove = (e: MouseEvent) => {
      const rect = container.getBoundingClientRect();
      const ratioX = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
      const ratioY = Math.max(0, Math.min(1, (e.clientY - rect.top) / rect.height));

      const x = inner.x + (panRangeX > 0 ? ratioX * panRangeX : -( zoomedWidth - inner.width) / 2);
      const y = inner.y + (panRangeY > 0 ? ratioY * panRangeY : -(zoomedHeight - inner.height) / 2);

      canvas.viewbox({ x, y, width: zoomedWidth, height: zoomedHeight });
    };

    // Apply initial zoom centered
    const initX = inner.x + (panRangeX > 0 ? panRangeX / 2 : -(zoomedWidth - inner.width) / 2);
    const initY = inner.y + (panRangeY > 0 ? panRangeY / 2 : -(zoomedHeight - inner.height) / 2);
    canvas.viewbox({ x: initX, y: initY, width: zoomedWidth, height: zoomedHeight });

    container.addEventListener("mousemove", handleMouseMove);

    return () => {
      container.removeEventListener("mousemove", handleMouseMove);
    };
  }, [viewer, containerRef, enabled]);
}
