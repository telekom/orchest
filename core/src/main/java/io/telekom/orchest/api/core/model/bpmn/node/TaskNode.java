package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.NodeType;
import lombok.NoArgsConstructor;

/** Represents a generic BPMN task node, the simplest form of work within a process. */
@NoArgsConstructor
public class TaskNode extends BaseNode {
  /**
   * Constructs a new TaskNode with the specified ID and name.
   *
   * @param id the unique identifier for the node
   * @param name the human-readable name of the node
   */
  public TaskNode(String id, String name) {
    super(id, name, NodeType.TASK);
  }
}
