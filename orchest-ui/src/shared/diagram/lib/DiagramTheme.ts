import type { DiagramViewer } from "../types";
import { getDiagramTheme, getRendererConfig } from "./DiagramColors";

export { getDiagramTheme, getRendererConfig } from "./DiagramColors";

export function getBpmnRendererConfig(isDarkMode: boolean) {
  return getRendererConfig(isDarkMode);
}

export function getDmnRendererConfig(isDarkMode: boolean) {
  return getRendererConfig(isDarkMode);
}

export function applyDiagramTheme(
  viewer: DiagramViewer,
  isDarkMode: boolean
): void {
  try {
    const canvas = viewer.get ? viewer.get("canvas") : viewer.getActiveViewer?.()?.get("canvas");
    if (!canvas) return;

    const container = canvas.getContainer();
    if (!container) return;

    const theme = getDiagramTheme(isDarkMode);
    container.style.backgroundColor = theme.container;
  } catch {
    // Silently fail - not critical
  }
}
