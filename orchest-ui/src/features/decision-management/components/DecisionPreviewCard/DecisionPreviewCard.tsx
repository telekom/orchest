import { decisionInstanceService } from "@/api/domains";
import type { DecisionInstanceDTO } from "@/api/types/orchest-api";
import * as Popover from "@radix-ui/react-popover";
import { format } from "date-fns";
import React, { useCallback, useEffect, useRef, useState } from "react";
import styles from "./DecisionPreviewCard.module.css";

interface DecisionPreviewCardProps {
  decisionInstanceId: string;
  children: React.ReactNode;
}

const STATE_COLORS: Record<string, string> = {
  EVALUATED: "var(--color-blue-500)",
  FAILED: "var(--color-red-500)",
  UNKNOWN: "var(--color-gray-400)",
};

const MAX_VARIABLES = 4;

export const DecisionPreviewCard: React.FC<DecisionPreviewCardProps> = ({
  decisionInstanceId,
  children,
}) => {
  const [open, setOpen] = useState(false);
  const [data, setData] = useState<DecisionInstanceDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);
  const hoverTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const closeTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const fetchData = useCallback(async () => {
    if (data) return;
    setLoading(true);
    setError(false);
    try {
      const result = await decisionInstanceService.getDecisionInstance(decisionInstanceId);
      setData(result);
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }, [decisionInstanceId, data]);

  const handleMouseEnter = () => {
    if (closeTimerRef.current) {
      clearTimeout(closeTimerRef.current);
      closeTimerRef.current = null;
    }
    hoverTimerRef.current = setTimeout(() => {
      setOpen(true);
      fetchData();
    }, 300);
  };

  const handleMouseLeave = () => {
    if (hoverTimerRef.current) {
      clearTimeout(hoverTimerRef.current);
      hoverTimerRef.current = null;
    }
    closeTimerRef.current = setTimeout(() => {
      setOpen(false);
    }, 150);
  };

  const handleContentMouseEnter = () => {
    if (closeTimerRef.current) {
      clearTimeout(closeTimerRef.current);
      closeTimerRef.current = null;
    }
  };

  const handleContentMouseLeave = () => {
    closeTimerRef.current = setTimeout(() => {
      setOpen(false);
    }, 150);
  };

  useEffect(() => {
    return () => {
      if (hoverTimerRef.current) clearTimeout(hoverTimerRef.current);
      if (closeTimerRef.current) clearTimeout(closeTimerRef.current);
    };
  }, []);

  const inputVars = data?.inputVariables ? Object.entries(data.inputVariables) : [];
  const outputVars = data?.outputVariables ? Object.entries(data.outputVariables) : [];
  const visibleInputs = inputVars.slice(0, MAX_VARIABLES);
  const visibleOutputs = outputVars.slice(0, MAX_VARIABLES);

  return (
    <Popover.Root open={open} onOpenChange={setOpen}>
      <Popover.Anchor asChild>
        <span
          onMouseEnter={handleMouseEnter}
          onMouseLeave={handleMouseLeave}
          style={{ display: "inline-flex" }}
        >
          {children}
        </span>
      </Popover.Anchor>
      <Popover.Portal>
        <Popover.Content
          side="left"
          align="center"
          sideOffset={8}
          className={styles.card}
          onMouseEnter={handleContentMouseEnter}
          onMouseLeave={handleContentMouseLeave}
          onOpenAutoFocus={(e) => e.preventDefault()}
        >
          {loading && (
            <div className={styles.loadingState}>
              <div className={styles.skelLine} style={{ width: "70%" }} />
              <div className={styles.skelLine} style={{ width: "50%" }} />
              <div className={styles.skelLine} style={{ width: "60%" }} />
              <div className={styles.skelLine} style={{ width: "40%" }} />
            </div>
          )}

          {error && (
            <div className={styles.errorState}>
              Failed to load decision details
            </div>
          )}

          {data && !loading && (
            <>
              {/* Header */}
              <div className={styles.header}>
                <div className={styles.headerMain}>
                  <span className={styles.decisionName}>{data.decisionId}</span>
                  <span className={styles.version}>v{data.version}</span>
                  <span
                    className={styles.stateBadge}
                    style={{ color: STATE_COLORS[data.state?.toUpperCase()] || "var(--color-gray-500)" }}
                  >
                    <span className={styles.stateDot} style={{ backgroundColor: STATE_COLORS[data.state?.toUpperCase()] || "var(--color-gray-500)" }} />
                    {data.state}
                  </span>
                </div>
                <div className={styles.headerMeta}>
                  <span className={styles.metaItem}>
                    ID: {data.decisionInstanceId?.slice(0, 12)}...
                  </span>
                  {data.executedAt && (
                    <span className={styles.metaItem}>
                      Executed: {format(new Date(data.executedAt), "MMM d, yyyy HH:mm")}
                    </span>
                  )}
                  {data.processInstanceId && (
                    <span className={styles.metaItem}>
                      Process: {data.processInstanceId.slice(0, 12)}...
                    </span>
                  )}
                </div>
              </div>

              {/* Input Variables */}
              {inputVars.length > 0 && (
                <div className={styles.variablesSection}>
                  <span className={styles.variablesTitle}>
                    Inputs ({inputVars.length})
                  </span>
                  <div className={styles.variablesList}>
                    {visibleInputs.map(([key, value]) => (
                      <div key={key} className={styles.variableRow}>
                        <span className={styles.variableKey}>{key}</span>
                        <span className={styles.variableValue}>
                          {typeof value === "object" ? JSON.stringify(value) : String(value ?? "")}
                        </span>
                      </div>
                    ))}
                    {inputVars.length > MAX_VARIABLES && (
                      <span className={styles.variablesMore}>+{inputVars.length - MAX_VARIABLES} more</span>
                    )}
                  </div>
                </div>
              )}

              {/* Output Variables */}
              {outputVars.length > 0 && (
                <div className={styles.variablesSection}>
                  <span className={styles.variablesTitle}>
                    Outputs ({outputVars.length})
                  </span>
                  <div className={styles.variablesList}>
                    {visibleOutputs.map(([key, value]) => (
                      <div key={key} className={styles.variableRow}>
                        <span className={styles.variableKey}>{key}</span>
                        <span className={styles.variableValue}>
                          {typeof value === "object" ? JSON.stringify(value) : String(value ?? "")}
                        </span>
                      </div>
                    ))}
                    {outputVars.length > MAX_VARIABLES && (
                      <span className={styles.variablesMore}>+{outputVars.length - MAX_VARIABLES} more</span>
                    )}
                  </div>
                </div>
              )}

              {/* Matched Rules */}
              {data.matchedRuleIds && data.matchedRuleIds.length > 0 && (
                <div className={styles.rulesSection}>
                  <span className={styles.variablesTitle}>
                    Matched Rules ({data.matchedRuleIds.length})
                  </span>
                  <div className={styles.rulesList}>
                    {data.matchedRuleIds.map((ruleId) => (
                      <span key={ruleId} className={styles.ruleBadge}>{ruleId}</span>
                    ))}
                  </div>
                </div>
              )}
            </>
          )}
        </Popover.Content>
      </Popover.Portal>
    </Popover.Root>
  );
};
