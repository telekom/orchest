import type { SequenceExecution } from "@/api/types/orchest-api";
import { NodeType, ProcessInstanceState } from "@/shared/enums";
import { describe, expect, it } from "vitest";
import {
  collectActivePathIds,
  collectHiddenElementIds,
} from "./ExecutedPathFocus";

const makeExec = (
  overrides: Partial<SequenceExecution> & Pick<SequenceExecution, "nodeId">
): SequenceExecution => ({
  nodeName: overrides.nodeName ?? overrides.nodeId,
  nodeType: overrides.nodeType ?? NodeType.SERVICE_TASK,
  state: overrides.state ?? ProcessInstanceState.COMPLETED,
  stateChanges: [],
  ...overrides,
});

describe("collectActivePathIds", () => {
  it("includes execution nodes, sources, flow endpoints, and active elements", () => {
    const registry = {
      get: (id: string) => {
        if (id === "Flow_1") {
          return { id, type: "bpmn:SequenceFlow", waypoints: [], source: { id: "Start" }, target: { id: "Task_A" } };
        }
        return undefined;
      },
    };

    const { activeFlowIds, activeNodeIds } = collectActivePathIds(
      {
        a: makeExec({
          nodeId: "Task_A",
          sourceNodeId: "Start",
          sequenceFlowIds: ["Flow_1"],
        }),
      },
      registry,
      ["Task_B"]
    );

    expect(activeFlowIds.has("Flow_1")).toBe(true);
    expect(activeNodeIds.has("Task_A")).toBe(true);
    expect(activeNodeIds.has("Start")).toBe(true);
    expect(activeNodeIds.has("Task_B")).toBe(true);
  });
});

describe("collectHiddenElementIds", () => {
  it("hides inactive flows and nodes only connected via inactive edges", () => {
    const elements = [
      { id: "Process_1", type: "bpmn:Process" },
      { id: "Start", type: "bpmn:StartEvent" },
      { id: "Task_A", type: "bpmn:ServiceTask" },
      { id: "Task_Unused", type: "bpmn:ServiceTask" },
      {
        id: "Flow_1",
        type: "bpmn:SequenceFlow",
        waypoints: [],
        source: { id: "Start" },
        target: { id: "Task_A" },
      },
      {
        id: "Flow_Unused",
        type: "bpmn:SequenceFlow",
        waypoints: [],
        source: { id: "Task_A" },
        target: { id: "Task_Unused" },
      },
      { id: "Label_Unused", type: "label", labelTarget: { id: "Task_Unused" } },
    ];

    const registry = {
      get: (id: string) => elements.find((e) => e.id === id),
      getAll: () => elements,
    };

    const hidden = collectHiddenElementIds(
      {
        a: makeExec({
          nodeId: "Task_A",
          sourceNodeId: "Start",
          sequenceFlowIds: ["Flow_1"],
        }),
      },
      registry
    );

    expect(hidden).toContain("Flow_Unused");
    expect(hidden).toContain("Task_Unused");
    expect(hidden).toContain("Label_Unused");
    expect(hidden).not.toContain("Flow_1");
    expect(hidden).not.toContain("Task_A");
    expect(hidden).not.toContain("Start");
    expect(hidden).not.toContain("Process_1");
  });

  it("hides nothing when there is no executed path data", () => {
    const registry = {
      get: () => undefined,
      getAll: () => [
        { id: "Task_A", type: "bpmn:ServiceTask" },
        { id: "Flow_1", type: "bpmn:SequenceFlow", waypoints: [] },
      ],
    };

    expect(collectHiddenElementIds({}, registry)).toEqual([]);
  });
});
