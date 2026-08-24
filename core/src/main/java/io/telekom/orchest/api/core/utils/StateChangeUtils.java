package io.telekom.orchest.api.core.utils;

import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.request.StateChange;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for creating and managing {@link StateChange} objects. Provides helper methods to
 * build state changes for BPMN node state transitions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StateChangeUtils {

  /**
   * Creates a state change object with the specified node state.
   *
   * @param state The node state to set.
   * @return A new StateChange instance with the specified state.
   */
  public static StateChange buildStateChanges(NodeState state) {
    return new StateChange(state);
  }

  /**
   * Creates a state change object with the specified node state and start timestamp.
   *
   * @param state The node state to set.
   * @param start The start timestamp for the state change.
   * @return A new StateChange instance with the specified state and start time.
   */
  public static StateChange buildStateChanges(NodeState state, OffsetDateTime start) {
    return new StateChange(state, start);
  }

  /**
   * Creates a list containing all standard state changes (TRIGGERED, STARTED, COMPLETED). Useful
   * for testing or initializing state change sequences.
   *
   * @return A list containing StateChange objects for TRIGGERED, STARTED, and COMPLETED states.
   */
  public static List<StateChange> getAllStates() {
    List<StateChange> stateChanges = new ArrayList<>();
    stateChanges.add(StateChangeUtils.buildStateChanges(NodeState.TRIGGERED));
    stateChanges.add(StateChangeUtils.buildStateChanges(NodeState.STARTED));
    stateChanges.add(StateChangeUtils.buildStateChanges(NodeState.COMPLETED));
    return stateChanges;
  }
}
