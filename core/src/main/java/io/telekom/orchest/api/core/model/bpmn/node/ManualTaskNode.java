package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.NodeType;
import lombok.NoArgsConstructor;

/**
 * Represents a Manual Task node in a BPMN process. Manual tasks represent work that must be
 * performed by a human without any system support.
 */
@NoArgsConstructor
public class ManualTaskNode extends ActivityNode {
  /**
   * Constructs a new ManualTaskNode with the specified ID and name.
   *
   * @param id The unique identifier for the node.
   * @param name The human-readable name of the node.
   */
  public ManualTaskNode(String id, String name) {
    super(id, name, NodeType.MANUAL_TASK);
  }
}
