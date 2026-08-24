package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.NodeType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Represents a BPMN receive task that waits for an incoming message to continue execution. */
@Getter
@Setter
@NoArgsConstructor
public class ReceiveTaskNode extends ActivityNode {
  /** The message name this task waits for. */
  private String messageRef;

  /**
   * Constructs a new ReceiveTaskNode with the specified ID and name.
   *
   * @param id the unique identifier for the node
   * @param name the human-readable name of the node
   */
  public ReceiveTaskNode(String id, String name) {
    super(id, name, NodeType.RECEIVE_TASK);
  }
}
