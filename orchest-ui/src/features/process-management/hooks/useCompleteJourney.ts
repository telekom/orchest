import { processInstanceService } from "@/api/domains";
import type { ProcessInstanceDTO, SequenceExecution } from "@/api/types/orchest-api";
import { extractChildInstanceInfo } from "@/shared/diagram/utils/childInstanceExtractor";
import { collectExecutedSequenceFlowIds } from "@/shared/diagram/lib/overlays/ExecutedPathMarkers";
import { flattenBpmn } from "@flowskin-bpmn/flowskin-bpmn";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

const MAX_DEPTH = 4;

interface ChildDef {
  processId: string;
  name: string;
  xml: string;
  executedEdgeIds: string[];
  children?: ChildDef[];
}

export interface CompleteJourneyResult {
  loading: boolean;
  flattenedXml: string | null;
  /** All executed edge IDs across parent + children (for animation). */
  allExecutedEdgeIds: string[];
  error: string | null;
}

async function buildChildDefs(
  sequenceExecutions: Record<string, SequenceExecution>,
  depth: number,
  visited: Set<string>,
  collectedEdgeIds: string[] = [],
): Promise<ChildDef[]> {
  if (depth > MAX_DEPTH) return [];

  const callActivityNodeIds = new Set<string>();
  for (const exec of Object.values(sequenceExecutions)) {
    const info = extractChildInstanceInfo(exec.nodeId, sequenceExecutions);
    if (info.hasChild) callActivityNodeIds.add(exec.nodeId);
  }

  const defs: ChildDef[] = [];

  for (const nodeId of callActivityNodeIds) {
    const info = extractChildInstanceInfo(nodeId, sequenceExecutions);
    for (const child of info.children) {
      if (child.childType !== "process") continue;
      if (visited.has(child.instanceId)) continue;
      visited.add(child.instanceId);

      let childInstance: ProcessInstanceDTO;
      try {
        childInstance = await processInstanceService.getProcessInstance(child.instanceId);
      } catch {
        continue;
      }

      const childSeqExecs = childInstance.sequenceExecutions ?? {};
      const childEdgeIds = collectExecutedSequenceFlowIds(childSeqExecs);
      collectedEdgeIds.push(...childEdgeIds);

      const nestedChildren = Object.keys(childSeqExecs).length > 0
        ? await buildChildDefs(childSeqExecs, depth + 1, visited, collectedEdgeIds)
        : [];

      defs.push({
        processId: childInstance.processDefinitionId,
        name: childInstance.processDefinitionId,
        xml: childInstance.bpmnXML ?? "",
        executedEdgeIds: childEdgeIds,
        children: nestedChildren.length > 0 ? nestedChildren : undefined,
      });
    }
  }

  return defs;
}

export const useCompleteJourney = (
  parentXml: string | undefined,
  sequenceExecutions: Record<string, SequenceExecution> | undefined,
  enabled: boolean,
): CompleteJourneyResult => {
  const [loading, setLoading] = useState(false);
  const [flattenedXml, setFlattenedXml] = useState<string | null>(null);
  const [allExecutedEdgeIds, setAllExecutedEdgeIds] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);
  const abortRef = useRef(false);

  const parentEdgeIds = useMemo(() => {
    if (!sequenceExecutions) return [];
    return collectExecutedSequenceFlowIds(sequenceExecutions);
  }, [sequenceExecutions]);

  const buildAndFlatten = useCallback(async () => {
    if (!enabled || !parentXml || !sequenceExecutions) {
      setFlattenedXml(null);
      setAllExecutedEdgeIds([]);
      return;
    }

    setLoading(true);
    setError(null);
    abortRef.current = false;

    try {
      const visited = new Set<string>();
      const childEdgeIds: string[] = [];
      const children = await buildChildDefs(sequenceExecutions, 1, visited, childEdgeIds);

      if (abortRef.current) return;

      const xml = await flattenBpmn({
        parent: { xml: parentXml, executedEdgeIds: parentEdgeIds },
        children,
      });

      if (!abortRef.current) {
        setFlattenedXml(xml);
        setAllExecutedEdgeIds([...parentEdgeIds, ...childEdgeIds]);
      }
    } catch (e) {
      if (!abortRef.current) {
        setError(e instanceof Error ? e.message : "Failed to flatten journey");
        setFlattenedXml(null);
      }
    } finally {
      if (!abortRef.current) {
        setLoading(false);
      }
    }
  }, [enabled, parentXml, sequenceExecutions, parentEdgeIds]);

  useEffect(() => {
    buildAndFlatten();
    return () => { abortRef.current = true; };
  }, [buildAndFlatten]);

  return { loading, flattenedXml, allExecutedEdgeIds, error };
};
