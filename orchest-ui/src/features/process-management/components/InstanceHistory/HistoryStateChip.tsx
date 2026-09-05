import clsx from "clsx";
import styles from "./InstanceHistory.module.css";

export type StatusCategory = "running" | "completed" | "incident" | "hold" | "neutral";

const RUNNING_STATES = new Set([
  "RUNNING",
  "STARTED",
  "ACTIVE",
  "TRIGGERED",
  "REGISTERED",
  "PENDING",
]);

const COMPLETED_STATES = new Set([
  "COMPLETED",
  "CANCELLED",
  "TERMINATED",
  "SKIPPED",
]);

const INCIDENT_STATES = new Set(["INCIDENT", "FAILED"]);

/** Maps a raw execution state to a visual category for history indicators. */
export const getStatusCategory = (state: string): StatusCategory => {
  const upper = (state || "").toUpperCase();
  if (RUNNING_STATES.has(upper)) return "running";
  if (COMPLETED_STATES.has(upper)) return "completed";
  if (INCIDENT_STATES.has(upper)) return "incident";
  if (upper === "HOLD") return "hold";
  return "neutral";
};

interface HistoryStateChipProps {
  state: string;
}

/** Compact, edgy state chip used in the transitions detail list. */
export const HistoryStateChip: React.FC<HistoryStateChipProps> = ({ state }) => {
  if (!state) return null;

  const category = getStatusCategory(state);

  return (
    <span
      className={clsx(styles.stateChip, styles[`stateChip_${category}`])}
      title={state}
    >
      {state}
    </span>
  );
};
