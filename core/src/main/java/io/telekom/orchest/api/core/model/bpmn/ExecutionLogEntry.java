package io.telekom.orchest.api.core.model.bpmn;

import io.telekom.orchest.api.core.request.StateChange;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Records the execution history of a single BPMN node within a process instance. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionLogEntry {
  /** The BPMN element ID of the node. */
  private String nodeId;

  /** The human-readable name of the node. */
  private String nodeName;

  /** The BPMN type of this node. */
  private NodeType nodeType;

  /** The ID of the node that preceded this one in execution. */
  private String sourceNodeId;

  /** IDs of sequence flows traversed to reach this node. */
  @Builder.Default private Set<String> sequenceFlowIds = new HashSet<>();

  /** Ordered list of state transitions for this node. */
  @Builder.Default private List<StateChange> stateChanges = new ArrayList<>();

  /** Additional metadata associated with the execution of this node. */
  @Builder.Default private Map<String, Object> metaData = new HashMap<>();

  /**
   * Adds a state change, deduplicating consecutive identical states.
   *
   * @param stateChange the state transition to record
   */
  public void addStateChange(StateChange stateChange) {
    if (stateChange == null || stateChange.getState() == null) {
      return;
    }
    // Idempotent against consecutive duplicates so boundary merges + engine-side
    // terminal writes (e.g. COMPLETED from resumeActivity after the worker's history
    // was already merged in) don't produce redundant timeline entries.
    if (!stateChanges.isEmpty() && stateChanges.getLast().getState() == stateChange.getState()) {
      return;
    }
    stateChanges.add(stateChange);
  }

  /**
   * Adds a sequence flow ID to the set of traversed flows.
   *
   * @param sequenceFlowId the sequence flow identifier
   */
  public void addSequenceFlowId(String sequenceFlowId) {
    sequenceFlowIds.add(sequenceFlowId);
  }

  /**
   * Returns the current (most recent) state of this node.
   *
   * @return the latest node state, or null if no state changes exist
   */
  public NodeState getState() {
    if (stateChanges == null) return null;
    return stateChanges.getLast().getState();
  }
}
