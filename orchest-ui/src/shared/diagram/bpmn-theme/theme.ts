/**
 * Orchest BPMN visual theme tokens (Phase 1).
 * Canvas stays light; accents distinguish element types.
 */
export const ORCHEST_BPMN_THEME = {
  surface: "#F8FAFC",
  surfaceStrong: "#FFFFFF",
  stroke: "#0F172A",
  strokeMuted: "#334155",
  glassHighlight: "rgba(255, 255, 255, 0.85)",
  accent: "#FF0080",
  /** Slightly softened rectangle — not pill-like. */
  taskRadius: 4,
  iconBadgeSize: 20,
  iconBadgeRadius: 3,
  iconBadgeOffset: 4,
  iconStrokeWidth: 1.5,
  strokeWidth: 1.75,
  callActivityStrokeWidth: 3.5,
  colors: {
    service: "#0284C7",
    user: "#2563EB",
    script: "#7C3AED",
    businessRule: "#D97706",
    send: "#0D9488",
    receive: "#0D9488",
    manual: "#64748B",
    callActivity: "#4F46E5",
    message: "#0D9488",
    timer: "#D97706",
    error: "#DC2626",
    noneEvent: "#0F172A",
    exclusive: "#E11D48",
    parallel: "#0D9488",
    inclusive: "#D97706",
    eventGateway: "#7C3AED",
  },
  /** Center mark size relative to gateway width (0–1). */
  gatewayMarkScale: 0.42,
  gatewayStrokeWidth: 2.25,
} as const;

export type OrchestAccentKey = keyof typeof ORCHEST_BPMN_THEME.colors;
