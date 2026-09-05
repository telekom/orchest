import { processInstanceService } from "@/api/domains";
import { queryKeys } from "@/shared/constants/queryKeys";
import { useApiQuery } from "@/shared/hooks";
import { Loader2 } from "lucide-react";
import React, { useMemo } from "react";
import { parseBpmnContainment } from "../../utils/bpmnContainment";
import { buildHistoryTree } from "../../utils/historyTree";
import HistoryTreeRow from "./HistoryTreeRow";
import styles from "./InstanceHistory.module.css";

const INDENT_PER_LEVEL_REM = 1.25;

interface ChildInstanceSubtreeProps {
  instanceId: string;
  depth: number;
  showDates: boolean;
  selectedNodeId: string | null;
  onSelect: (nodeId: string) => void;
  isExpanded: (nodeKey: string, expandable: boolean) => boolean;
  onToggleExpand: (nodeKey: string) => void;
}

/**
 * Lazily loads a child process instance (spawned by a call activity) and renders
 * its execution history as a nested subtree.
 */
const ChildInstanceSubtree: React.FC<ChildInstanceSubtreeProps> = ({
  instanceId,
  depth,
  showDates,
  selectedNodeId,
  onSelect,
  isExpanded,
  onToggleExpand,
}) => {
  const { data, isLoading, error } = useApiQuery(
    queryKeys.processInstances.detail(instanceId),
    () => processInstanceService.getProcessInstance(instanceId),
    { showErrorToast: false, retryOnError: false }
  );

  const tree = useMemo(() => {
    if (!data) return null;
    const containment = parseBpmnContainment(data.bpmnXML);
    return buildHistoryTree(data.sequenceExecutions, containment, {
      id: containment.processId || data.processDefinitionId || instanceId,
      name: containment.processName || data.processDefinitionId || instanceId,
      state: data.state,
      startTime: data.createdAt,
      endTime: data.completedAt,
    });
  }, [data, instanceId]);

  const indentStyle = { paddingLeft: `${depth * INDENT_PER_LEVEL_REM}rem` };

  if (isLoading) {
    return (
      <div className={styles.subtreeMessage} style={indentStyle}>
        <Loader2 className={styles.spinner} />
        <span>Loading child instance…</span>
      </div>
    );
  }

  if (error || !tree) {
    return (
      <div className={styles.subtreeMessage} style={indentStyle}>
        <span className={styles.subtreeError}>Unable to load child instance</span>
      </div>
    );
  }

  if (tree.children.length === 0) {
    return (
      <div className={styles.subtreeMessage} style={indentStyle}>
        <span>No execution history</span>
      </div>
    );
  }

  return (
    <>
      {tree.children.map((child) => (
        <HistoryTreeRow
          key={child.key}
          node={child}
          depth={depth}
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

export default ChildInstanceSubtree;
