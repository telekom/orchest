import { AlertCircle, Circle, CheckCircle2 } from "lucide-react";
import clsx from "clsx";
import { getBpmnNodeIcon } from "./BpmnNodeIcons";
import { getStatusCategory } from "./HistoryStateChip";
import styles from "./InstanceHistory.module.css";

/** Renders the left-hand status indicator for a history node. */
export const StatusIcon: React.FC<{ state: string }> = ({ state }) => {
  const category = getStatusCategory(state);
  const upper = (state || "").toUpperCase();

  const className = clsx(
    styles.statusIcon,
    styles[`status_${category}`],
    (category === "running" || category === "hold") && styles.statusBlink
  );

  switch (category) {
    case "completed":
      return <CheckCircle2 className={className} aria-label={upper} />;
    case "incident":
      return <AlertCircle className={className} aria-label={upper} />;
    default:
      return <Circle className={className} aria-label={upper || "Status"} />;
  }
};

/** Renders the theme-compatible BPMN node-type icon for a node. */
export const NodeTypeIcon: React.FC<{ nodeType: string }> = ({ nodeType }) => {
  const Icon = getBpmnNodeIcon(nodeType);
  return <Icon className={styles.nodeIcon} />;
};
