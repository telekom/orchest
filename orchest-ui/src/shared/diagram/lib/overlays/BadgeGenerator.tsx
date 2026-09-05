import { AlertCircle, Ban, Check, Pause } from "lucide-react";
import type { ReactElement } from "react";
import { renderToStaticMarkup } from "react-dom/server";

export type StatusRingMarkerClass =
  | "status-ring-running"
  | "status-ring-hold"
  | "status-ring-completed"
  | "status-ring-incident"
  | "status-ring-cancelled";

type StatusConfig = {
  markerClass: StatusRingMarkerClass;
  color: string;
  label: string;
  /** When set, used instead of a Lucide icon (e.g. blinking running dot). */
  customHtml?: string;
  icon?: ReactElement;
};

const BLINKING_DOT_HTML =
  '<span class="activity-status-blink-dot" aria-hidden="true"></span>';

const STATE_CONFIGS: Record<string, StatusConfig> = {
  INCIDENT: {
    markerClass: "status-ring-incident",
    color: "#DC2626",
    label: "Incident",
    icon: <AlertCircle color="#fff" size={12} />,
  },
  FAILED: {
    markerClass: "status-ring-incident",
    color: "#DC2626",
    label: "Failed",
    icon: <AlertCircle color="#fff" size={12} />,
  },
  CANCELLED: {
    markerClass: "status-ring-cancelled",
    color: "#374151",
    label: "Cancelled",
    icon: <Ban color="#fff" size={12} />,
  },
  TERMINATED: {
    markerClass: "status-ring-cancelled",
    color: "#374151",
    label: "Terminated",
    icon: <Ban color="#fff" size={12} />,
  },
  COMPLETED: {
    markerClass: "status-ring-completed",
    color: "#6B7280",
    label: "Completed",
    icon: <Check color="#fff" size={12} strokeWidth={3} />,
  },
  RUNNING: {
    markerClass: "status-ring-running",
    color: "#16A34A",
    label: "Running",
    customHtml: BLINKING_DOT_HTML,
  },
  ACTIVE: {
    markerClass: "status-ring-running",
    color: "#16A34A",
    label: "Running",
    customHtml: BLINKING_DOT_HTML,
  },
  STARTED: {
    markerClass: "status-ring-running",
    color: "#16A34A",
    label: "Running",
    customHtml: BLINKING_DOT_HTML,
  },
  TRIGGERED: {
    markerClass: "status-ring-running",
    color: "#16A34A",
    label: "Running",
    customHtml: BLINKING_DOT_HTML,
  },
  // Active wait tokens often arrive as PENDING from the API — treat like running (green + blink).
  PENDING: {
    markerClass: "status-ring-running",
    color: "#16A34A",
    label: "Running",
    customHtml: BLINKING_DOT_HTML,
  },
  HOLD: {
    markerClass: "status-ring-hold",
    color: "#EAB308",
    label: "On hold",
    icon: <Pause color="#fff" size={12} className="activity-status-blink-icon" />,
  },
};

const resolveConfig = (state: string): StatusConfig | null =>
  STATE_CONFIGS[state?.toUpperCase()] ?? null;

export const getStatusRingMarkerClass = (state: string): StatusRingMarkerClass | null =>
  resolveConfig(state)?.markerClass ?? null;

export const getStatusLabel = (state: string): string | null =>
  resolveConfig(state)?.label ?? null;

export const generateStatusBadge = (_: string | undefined, state: string): string | null => {
  const config = resolveConfig(state);
  if (!config) return null;

  const normalized = state.toUpperCase();
  const content = config.customHtml ?? (config.icon ? renderToStaticMarkup(config.icon) : "");

  return `<div class="activity-status-icon" style="background:${config.color}" title="${config.label}" data-status="${normalized}" data-testid="statistics-overlay" aria-label="${config.label}">${content}</div>`;
};

export const generateModificationBadge = (type: "source" | "target"): string => {
  const isSource = type === "source";
  return `<div style="background:${isSource ? "#ff4444" : "#14dd3a"};color:white;padding:2px 8px;border-radius:20px;font-weight:bold;position:relative;z-index:1001">${isSource ? "-1" : "+1"}</div>`;
};
