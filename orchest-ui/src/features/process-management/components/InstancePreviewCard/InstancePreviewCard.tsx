import { processInstanceService } from "@/api/domains";
import type { ProcessInstanceDTO } from "@/api/types/orchest-api";
import * as Popover from "@radix-ui/react-popover";
import { format } from "date-fns";
import React, { useCallback, useEffect, useRef, useState } from "react";
import styles from "./InstancePreviewCard.module.css";

interface InstancePreviewCardProps {
  processInstanceId: string;
  children: React.ReactNode;
}

const STATE_COLORS: Record<string, string> = {
  RUNNING: "var(--color-green-500)",
  ACTIVE: "var(--color-green-500)",
  COMPLETED: "var(--color-blue-500)",
  FAILED: "var(--color-red-500)",
  INCIDENT: "var(--color-orange-500)",
  HOLD: "var(--color-yellow-500)",
  CANCELLED: "var(--color-gray-400)",
};

const MAX_VARIABLES = 4;

export const InstancePreviewCard: React.FC<InstancePreviewCardProps> = ({
  processInstanceId,
  children,
}) => {
  const [open, setOpen] = useState(false);
  const [data, setData] = useState<ProcessInstanceDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);
  const hoverTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const closeTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const fetchData = useCallback(async () => {
    if (data) return;
    setLoading(true);
    setError(false);
    try {
      const result = await processInstanceService.getProcessInstance(processInstanceId);
      setData(result);
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }, [processInstanceId, data]);

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

  const bpmnRefCallback = useCallback((node: HTMLDivElement | null) => {
    if (!node || !data?.bpmnXML) return;

    (async () => {
      const { createFlowSkinBPMN, setTheme } = await import("@flowskin-bpmn/flowskin-bpmn");
      if (!node.isConnected) return;

      const theme = document.documentElement.classList.contains("dark") ? "dark" : "light";
      setTheme(theme);
      const renderer = createFlowSkinBPMN({ container: node, theme, hoverCard: false });

      try {
        await renderer.loadXml(data.bpmnXML);
      } catch {
        // silently handle render errors
      }
    })();
  }, [data]);

  useEffect(() => {
    return () => {
      if (hoverTimerRef.current) clearTimeout(hoverTimerRef.current);
      if (closeTimerRef.current) clearTimeout(closeTimerRef.current);
    };
  }, []);

  const variables = data?.variables ? Object.entries(data.variables) : [];
  const visibleVars = variables.slice(0, MAX_VARIABLES);
  const hiddenCount = Math.max(0, variables.length - MAX_VARIABLES);

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
              <div className={styles.skelBlock} />
              <div className={styles.skelLine} style={{ width: "60%" }} />
              <div className={styles.skelLine} style={{ width: "40%" }} />
            </div>
          )}

          {error && (
            <div className={styles.errorState}>
              Failed to load instance details
            </div>
          )}

          {data && !loading && (
            <>
              {/* Header */}
              <div className={styles.header}>
                <div className={styles.headerMain}>
                  <span className={styles.processName}>
                    {data.processDefinitionId}
                  </span>
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
                    ID: {data.processInstanceId?.slice(0, 12)}...
                  </span>
                  {data.createdAt && (
                    <span className={styles.metaItem}>
                      Started: {format(new Date(data.createdAt), "MMM d, yyyy HH:mm")}
                    </span>
                  )}
                </div>
              </div>

              {/* BPMN Preview */}
              {data.bpmnXML && (
                <div className={styles.bpmnSection}>
                  <div
                    ref={bpmnRefCallback}
                    className={styles.bpmnContainer}
                  />
                </div>
              )}

              {/* Variables */}
              {variables.length > 0 && (
                <div className={styles.variablesSection}>
                  <span className={styles.variablesTitle}>
                    Variables ({variables.length})
                  </span>
                  <div className={styles.variablesList}>
                    {visibleVars.map(([key, value]) => (
                      <div key={key} className={styles.variableRow}>
                        <span className={styles.variableKey}>{key}</span>
                        <span className={styles.variableValue}>
                          {typeof value === "object" ? JSON.stringify(value) : String(value ?? "")}
                        </span>
                      </div>
                    ))}
                    {hiddenCount > 0 && (
                      <span className={styles.variablesMore}>+{hiddenCount} more</span>
                    )}
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
