import { ScrollArea } from "@/design-system/components/ui/scroll-area";
import { Switch } from "@/design-system/components/ui/switch";
import { Button } from "@/design-system/components/ui/button";
import { EMPTY_STATE_MESSAGES } from "@/shared/constants";
import type { SequenceExecution } from "@/api/types/orchest-api";
import clsx from "clsx";
import { FoldVertical, UnfoldVertical } from "lucide-react";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import { parseBpmnContainment } from "../../utils/bpmnContainment";
import {
  buildHistoryTree,
  collectDefaultExpandedKeys,
} from "../../utils/historyTree";
import HistoryTreeRow from "./HistoryTreeRow";
import styles from "./InstanceHistory.module.css";

export interface InstanceHistoryProps {
  sequenceExecutions: Record<string, SequenceExecution>;
  bpmnXML: string;
  processName: string;
  status: string;
  startDate?: string;
  endDate?: string | null;
  isLoading: boolean;
  /** Currently selected node id (highlighted in the diagram viewer). */
  selectedNodeId: string | null;
  /** Called when a node row is clicked. */
  onSelectNode: (nodeId: string) => void;
}

type GlobalExpandMode = "default" | "all" | "none";

const LOADING_ROWS = 5;

const InstanceHistory: React.FC<InstanceHistoryProps> = ({
  sequenceExecutions,
  bpmnXML,
  processName,
  status,
  startDate,
  endDate,
  isLoading,
  selectedNodeId,
  onSelectNode,
}) => {
  const [showDates, setShowDates] = useState(false);
  const [expandedKeys, setExpandedKeys] = useState<Set<string>>(() => new Set());
  const [globalExpand, setGlobalExpand] = useState<GlobalExpandMode>("default");

  const tree = useMemo(() => {
    const containment = parseBpmnContainment(bpmnXML);
    return buildHistoryTree(sequenceExecutions, containment, {
      id: containment.processId || "root",
      name: containment.processName || processName || "Process",
      state: status,
      startTime: startDate,
      endTime: endDate ?? undefined,
    });
  }, [sequenceExecutions, bpmnXML, processName, status, startDate, endDate]);

  useEffect(() => {
    setExpandedKeys(collectDefaultExpandedKeys(tree));
    setGlobalExpand("default");
  }, [tree]);

  const isNodeExpanded = useCallback(
    (nodeKey: string, expandable: boolean) => {
      if (!expandable) return false;
      if (globalExpand === "all") return true;
      if (globalExpand === "none") return false;
      return expandedKeys.has(nodeKey);
    },
    [expandedKeys, globalExpand]
  );

  const toggleExpand = useCallback((nodeKey: string) => {
    setGlobalExpand("default");
    setExpandedKeys((prev) => {
      const next = new Set(prev);
      if (next.has(nodeKey)) next.delete(nodeKey);
      else next.add(nodeKey);
      return next;
    });
  }, []);

  const expandAll = useCallback(() => setGlobalExpand("all"), []);
  const collapseAll = useCallback(() => setGlobalExpand("none"), []);

  const hasHistory = tree.children.length > 0;

  const renderBody = () => {
    if (isLoading) {
      return (
        <div className={styles.loading}>
          {Array.from({ length: LOADING_ROWS }, (_, i) => (
            <div
              key={i}
              className={styles.loadingRow}
              style={{ marginLeft: `${(i % 3) * 1.25}rem` }}
            />
          ))}
        </div>
      );
    }

    if (!hasHistory) {
      return (
        <div className={styles.empty}>
          <p className={clsx(styles.emptyText)}>{EMPTY_STATE_MESSAGES.NO_HISTORY}</p>
        </div>
      );
    }

    return (
      <div className={styles.tree}>
        <HistoryTreeRow
          node={tree}
          depth={0}
          showDates={showDates}
          selectedNodeId={selectedNodeId}
          onSelect={onSelectNode}
          isExpanded={isNodeExpanded}
          onToggleExpand={toggleExpand}
        />
      </div>
    );
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h3 className={styles.title}>Instance History</h3>
        <div className={styles.headerActions}>
          {hasHistory && !isLoading && (
            <div className={styles.expandActions}>
              <Button
                type="button"
                variant="ghost"
                size="sm"
                className={styles.expandButton}
                onClick={expandAll}
                aria-label="Expand all"
              >
                <UnfoldVertical className={styles.expandButtonIcon} aria-hidden="true" />
                Expand all
              </Button>
              <Button
                type="button"
                variant="ghost"
                size="sm"
                className={styles.expandButton}
                onClick={collapseAll}
                aria-label="Collapse all"
              >
                <FoldVertical className={styles.expandButtonIcon} aria-hidden="true" />
                Collapse all
              </Button>
            </div>
          )}
          <label className={styles.toggle}>
            <Switch checked={showDates} onCheckedChange={setShowDates} />
            <span className={styles.toggleLabel}>Show Dates</span>
          </label>
        </div>
      </div>

      <ScrollArea className={styles.scrollArea}>{renderBody()}</ScrollArea>
    </div>
  );
};

export default React.memo(InstanceHistory);
