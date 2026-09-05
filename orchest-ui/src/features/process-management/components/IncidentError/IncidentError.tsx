import {
    Collapsible,
    CollapsibleContent,
    CollapsibleTrigger,
} from "@/design-system/components/ui/collapsible";
import { Check, ChevronDown, Copy } from "lucide-react";
import React from "react";
import styles from "./IncidentError.module.css";

interface IncidentErrorProps {
  incidentMessage: string;
  copiedItems: Record<string, boolean>;
  onCopyToClipboard: (text: string, itemId: string) => void;
}

const IncidentError: React.FC<IncidentErrorProps> = ({
  incidentMessage,
  copiedItems,
  onCopyToClipboard,
}) => {
  const [expanded, setExpanded] = React.useState(false);

  return (
    <div className={styles.container}>
      {incidentMessage ? (
        <Collapsible className={styles.collapsible}>
          <CollapsibleTrigger asChild>
            <div className={styles.trigger} onClick={() => setExpanded(prev => !prev)}>
              <div className={styles.triggerContent}>
                <span className={styles.errorLabel}>Error:</span>
                <p className={`${styles.errorMessage} ${expanded ? styles.errorMessageExpanded : ""}`}>
                  {incidentMessage.split("\n")[0]}
                </p>
              </div>
              <div className={styles.actions}>
                <button
                  type="button"
                  className={styles.iconButton}
                  onClick={(e: React.MouseEvent) => {
                    e.stopPropagation();
                    onCopyToClipboard(incidentMessage, "incident");
                  }}
                  aria-label="Copy error message to clipboard"
                >
                  {copiedItems["incident"] ? (
                    <Check className={styles.icon} />
                  ) : (
                    <Copy className={styles.icon} />
                  )}
                </button>
                <span className={styles.chevronWrapper}>
                  <ChevronDown className={styles.chevronIcon} />
                </span>
              </div>
            </div>
          </CollapsibleTrigger>
          <CollapsibleContent className={styles.content}>
            <pre className={styles.errorDetail}>
              {incidentMessage.split("\n").slice(1).join("\n")}
            </pre>
          </CollapsibleContent>
        </Collapsible>
      ) : (
        <p className={styles.fallbackMessage}>
          An incident has occurred in this process instance.
        </p>
      )}
    </div>
  );
};

export default React.memo(IncidentError);
