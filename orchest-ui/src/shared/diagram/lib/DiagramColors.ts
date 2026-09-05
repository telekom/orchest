/**
 * Central Diagram Theme Configuration
 * Single source of truth for all BPMN/DMN diagram container styling
 * Libraries handle their own element rendering
 */

export const DIAGRAM_COLORS = {
  MAGENTA: '#FF0080',
  MAGENTA_LIGHT: '#FF3399',
  MAGENTA_HIGHLIGHT: 'rgba(255, 0, 128, 0.15)',
  LIGHT_CANVAS: '#FFFFFF',
  DARK_CANVAS: '#0a0a0f',
} as const;

export interface DiagramTheme {
  container: string;
  canvas: string;
}

const LIGHT_THEME: DiagramTheme = {
  container: DIAGRAM_COLORS.LIGHT_CANVAS,
  canvas: DIAGRAM_COLORS.LIGHT_CANVAS,
};

const DARK_THEME: DiagramTheme = {
  container: DIAGRAM_COLORS.DARK_CANVAS,
  canvas: DIAGRAM_COLORS.DARK_CANVAS,
};

export function getDiagramTheme(isDarkMode: boolean): DiagramTheme {
  return isDarkMode ? DARK_THEME : LIGHT_THEME;
}

export function getSelectionColors(isDarkMode: boolean) {
  return {
    primary: DIAGRAM_COLORS.MAGENTA,
    secondary: isDarkMode ? '#FF66B2' : DIAGRAM_COLORS.MAGENTA_LIGHT,
    highlight: DIAGRAM_COLORS.MAGENTA_HIGHLIGHT,
  };
}

export function getRendererConfig(_isDarkMode: boolean) {
  // Returns empty config - we don't want to override element fill/stroke colors
  // Selection/highlight colors are applied via diagram-theme.css (--diagram-selection-*)
  return {};
}
