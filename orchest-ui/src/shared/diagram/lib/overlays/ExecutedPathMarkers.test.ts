import type { SequenceExecution } from "@/api/types/orchest-api";
import { NodeType, ProcessInstanceState } from "@/shared/enums";
import { describe, expect, it, vi } from "vitest";
import {
  EXECUTED_PATH_MARKER_LIVE,
  collectExecutedSequenceFlowIds,
  createExecutedPathMarkersManager,
  getExecutedPathMarkerClass,
} from "./ExecutedPathMarkers";

const makeExecution = (
  overrides: Partial<SequenceExecution> & Pick<SequenceExecution, "nodeId">
): SequenceExecution => ({
  nodeName: overrides.nodeName ?? overrides.nodeId,
  nodeType: overrides.nodeType ?? NodeType.SERVICE_TASK,
  state: overrides.state ?? ProcessInstanceState.COMPLETED,
  stateChanges: overrides.stateChanges ?? [],
  ...overrides,
});

describe("collectExecutedSequenceFlowIds", () => {
  it("returns unique non-empty sequence flow ids across executions", () => {
    const executions: Record<string, SequenceExecution> = {
      a: makeExecution({
        nodeId: "Task_A",
        sequenceFlowIds: ["Flow_1", "Flow_2", ""],
      }),
      b: makeExecution({
        nodeId: "Task_B",
        sequenceFlowIds: ["Flow_2", "Flow_3"],
      }),
      c: makeExecution({
        nodeId: "Task_C",
      }),
    };

    expect(collectExecutedSequenceFlowIds(executions)).toEqual([
      "Flow_1",
      "Flow_2",
      "Flow_3",
    ]);
  });

  it("returns an empty array when there are no flow ids", () => {
    expect(collectExecutedSequenceFlowIds({})).toEqual([]);
    expect(
      collectExecutedSequenceFlowIds({
        a: makeExecution({ nodeId: "Task_A", sequenceFlowIds: [] }),
      })
    ).toEqual([]);
  });
});

describe("getExecutedPathMarkerClass", () => {
  it("returns live marker for non-terminal states", () => {
    expect(getExecutedPathMarkerClass("RUNNING")).toBe(EXECUTED_PATH_MARKER_LIVE);
    expect(getExecutedPathMarkerClass("incident")).toBe(EXECUTED_PATH_MARKER_LIVE);
    expect(getExecutedPathMarkerClass("HOLD")).toBe(EXECUTED_PATH_MARKER_LIVE);
  });

  it("returns live marker for terminal states too (always animated)", () => {
    expect(getExecutedPathMarkerClass("COMPLETED")).toBe(EXECUTED_PATH_MARKER_LIVE);
    expect(getExecutedPathMarkerClass("FAILED")).toBe(EXECUTED_PATH_MARKER_LIVE);
    expect(getExecutedPathMarkerClass("cancelled")).toBe(EXECUTED_PATH_MARKER_LIVE);
    expect(getExecutedPathMarkerClass("TERMINATED")).toBe(EXECUTED_PATH_MARKER_LIVE);
  });
});

describe("createExecutedPathMarkersManager", () => {
  const createViewerMock = (knownIds: string[]) => {
    const addMarker = vi.fn();
    const removeMarker = vi.fn();
    const known = new Set(knownIds);

    return {
      addMarker,
      removeMarker,
      viewer: {
        get: (moduleName: string) => {
          if (moduleName === "canvas") {
            return { addMarker, removeMarker };
          }
          if (moduleName === "elementRegistry") {
            return {
              get: (id: string) => (known.has(id) ? { type: "bpmn:SequenceFlow" } : undefined),
            };
          }
          return undefined;
        },
      },
    };
  };

  it("applies live markers for known flows on a running instance", () => {
    const { viewer, addMarker, removeMarker } = createViewerMock(["Flow_1", "Flow_2"]);
    const manager = createExecutedPathMarkersManager();

    manager.apply(viewer as never, {
      state: "RUNNING",
      sequenceExecutions: {
        a: makeExecution({
          nodeId: "Task_A",
          sequenceFlowIds: ["Flow_1", "Flow_2", "Flow_Missing"],
        }),
      },
    });

    expect(addMarker).toHaveBeenCalledWith("Flow_1", EXECUTED_PATH_MARKER_LIVE);
    expect(addMarker).toHaveBeenCalledWith("Flow_2", EXECUTED_PATH_MARKER_LIVE);
    expect(addMarker).not.toHaveBeenCalledWith("Flow_Missing", expect.anything());
    expect(removeMarker).not.toHaveBeenCalled();
  });

  it("clears previous markers before re-applying", () => {
    const { viewer, addMarker, removeMarker } = createViewerMock(["Flow_1", "Flow_2"]);
    const manager = createExecutedPathMarkersManager();

    manager.apply(viewer as never, {
      state: "RUNNING",
      sequenceExecutions: {
        a: makeExecution({ nodeId: "Task_A", sequenceFlowIds: ["Flow_1"] }),
      },
    });

    manager.apply(viewer as never, {
      state: "COMPLETED",
      sequenceExecutions: {
        a: makeExecution({ nodeId: "Task_A", sequenceFlowIds: ["Flow_1", "Flow_2"] }),
      },
    });

    expect(removeMarker).toHaveBeenCalledWith("Flow_1", EXECUTED_PATH_MARKER_LIVE);
    expect(addMarker).toHaveBeenLastCalledWith("Flow_2", EXECUTED_PATH_MARKER_LIVE);
    expect(addMarker).toHaveBeenCalledWith("Flow_1", EXECUTED_PATH_MARKER_LIVE);
  });

  it("clear removes all applied markers", () => {
    const { viewer, removeMarker } = createViewerMock(["Flow_1"]);
    const manager = createExecutedPathMarkersManager();

    manager.apply(viewer as never, {
      state: "RUNNING",
      sequenceExecutions: {
        a: makeExecution({ nodeId: "Task_A", sequenceFlowIds: ["Flow_1"] }),
      },
    });

    manager.clear(viewer as never);

    expect(removeMarker).toHaveBeenCalledWith("Flow_1", EXECUTED_PATH_MARKER_LIVE);
  });
});
