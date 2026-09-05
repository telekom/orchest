import { ChevronDown, ChevronRight } from "lucide-react";
import clsx from "clsx";
import React from "react";
import { isExpandableNode, type HistoryNode } from "../../utils/historyTree";
import { HistoryStateChip } from "./HistoryStateChip";
import { NodeTypeIcon, StatusIcon } from "./historyIcons";
import ChildInstanceSubtree from "./ChildInstanceSubtree";
import styles from "./InstanceHistory.module.css";

const INDENT_PER_LEVEL_REM = 1.25;

const formatTime = (value?: string) =>
  value ? new Date(value).toLocaleString() : "—";

interface HistoryTreeRowProps {
  node: HistoryNode;
  depth: number;
  showDates: boolean;
  selectedNodeId: string | null;
  onSelect: (nodeId: string) => void;
  isExpanded: (nodeKey: string, expandable: boolean) => boolean;
  onToggleExpand: (nodeKey: string) => void;
}

const HistoryTreeRow: React.FC<HistoryTreeRowProps> = ({
  node,
  depth,
  showDates,
  selectedNodeId,
  onSelect,
  isExpanded,
  onToggleExpand,
}) => {
  const hasEmbedded = node.children.length > 0;
  const hasCallChild = node.childInstanceIds.length > 0;
  const expandable = isExpandableNode(node);
  const expanded = isExpanded(node.key, expandable);

  const isSelected = selectedNodeId === node.nodeId;

  return (
    <>
      <div
        className={clsx(styles.row, isSelected && styles.rowSelected)}
        style={{ paddingLeft: `${depth * INDENT_PER_LEVEL_REM}rem` }}
      >
        {expandable ? (
          <button
            type="button"
            className={styles.chevronButton}
            onClick={() => onToggleExpand(node.key)}
            aria-expanded={expanded}
            aria-label={expanded ? "Collapse" : "Expand"}
          >
            {expanded ? (
              <ChevronDown className={styles.chevron} />
            ) : (
              <ChevronRight className={styles.chevron} />
            )}
          </button>
        ) : (
          <span className={styles.chevronSpacer} />
        )}

        <StatusIcon state={node.state} />

        <button
          type="button"
          className={styles.rowMain}
          onClick={() => onSelect(node.nodeId)}
          aria-pressed={isSelected}
        >
          <NodeTypeIcon nodeType={node.nodeType} />

          <span className={styles.nodeName} title={node.name}>
            {node.name}
          </span>

          {showDates && (
            <span className={styles.dates}>
              <span className={styles.dateValue}>{formatTime(node.startTime)}</span>
              <span className={styles.dateSeparator}>→</span>
              <span className={clsx(styles.dateValue, !node.endTime && styles.dateMuted)}>
                {formatTime(node.endTime)}
              </span>
            </span>
          )}
        </button>
      </div>

      {isSelected && (
        <div
          className={styles.transitions}
          style={{ paddingLeft: `${(depth + 1) * INDENT_PER_LEVEL_REM + 0.5}rem` }}
        >
          {node.stateChanges.length > 0 ? (
            node.stateChanges.map((change, index) => (
              <div className={styles.transitionRow} key={`${change.state}-${change.timestamp}-${index}`}>
                <HistoryStateChip state={change.state || ""} />
                <span className={styles.transitionTime}>{formatTime(change.timestamp)}</span>
              </div>
            ))
          ) : (
            <span className={styles.transitionEmpty}>No state transitions</span>
          )}
        </div>
      )}

      {expanded && hasEmbedded &&
        node.children.map((child) => (
          <HistoryTreeRow
            key={child.key}
            node={child}
            depth={depth + 1}
            showDates={showDates}
            selectedNodeId={selectedNodeId}
            onSelect={onSelect}
            isExpanded={isExpanded}
            onToggleExpand={onToggleExpand}
          />
        ))}

      {expanded && hasCallChild &&
        node.childInstanceIds.map((instanceId) => (
          <ChildInstanceSubtree
            key={instanceId}
            instanceId={instanceId}
            depth={depth + 1}
            showDates={showDates}
            selectedNodeId={selectedNodeId}
            onSelect={onSelect}
            isExpanded={isExpanded}
            onToggleExpand={onToggleExpand}
          />
        ))}
    </>
  );
};

export default HistoryTreeRow;
