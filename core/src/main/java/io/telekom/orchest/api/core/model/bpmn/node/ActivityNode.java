package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.MultiInstanceLoopCharacteristics;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an activity node in a BPMN process. Activities are nodes that represent work being
 * performed in a process.
 *
 * <p>This class serves as the base class for all activity types including:
 *
 * <ul>
 *   <li>Tasks (ServiceTask, UserTask, ScriptTask, etc.)
 *   <li>Subprocesses (SubProcess, CallActivity)
 * </ul>
 *
 * <p>Activities can have boundary events attached to them, which can interrupt or not interrupt the
 * activity execution based on the event type and configuration.
 *
 * <p>Boundary events are stored as IDs to avoid circular references during serialization.
 */
@Getter
@Setter
@NoArgsConstructor
public class ActivityNode extends BaseNode {
  /**
   * List of boundary event IDs attached to this activity. Boundary events can be attached to
   * activities to handle events that occur during activity execution (e.g., errors, timers,
   * messages).
   *
   * <p>Stored as IDs rather than object references to avoid circular dependencies and serialization
   * issues.
   */
  private List<String> boundaryEventIds = new ArrayList<>();

  private MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics;

  /**
   * Constructs a new ActivityNode with the specified ID, name, and type.
   *
   * @param id The unique identifier of the activity node
   * @param name The name of the activity node
   * @param type The type of the activity node (e.g., SERVICE_TASK, USER_TASK, SUB_PROCESS)
   */
  public ActivityNode(String id, String name, NodeType type) {
    super(id, name, type);
    this.boundaryEventIds = new ArrayList<>();
  }

  /**
   * Adds a boundary event ID to this activity.
   *
   * <p>Boundary events are attached to activities and can be triggered during activity execution.
   * This method only stores the ID to avoid circular references in the object graph.
   *
   * <p>If the boundary event ID is null or already exists in the list, the method does nothing.
   *
   * @param boundaryEventId The ID of the boundary event attached to this activity. Must not be null
   *     for the event to be added.
   */
  public void addBoundaryEvent(String boundaryEventId) {
    if (boundaryEventId != null && !boundaryEventIds.contains(boundaryEventId)) {
      boundaryEventIds.add(boundaryEventId);
    }
  }
}
