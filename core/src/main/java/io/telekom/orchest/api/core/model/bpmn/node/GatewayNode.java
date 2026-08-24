package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.NodeType;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a Gateway node in a BPMN process. Gateways control the flow of execution by splitting
 * or merging paths based on conditions. Examples include exclusive gateways, parallel gateways, and
 * event-based gateways.
 */
@Getter
@Setter
@NoArgsConstructor
public class GatewayNode extends BaseNode {
  /** Map of target node IDs to their condition expressions (FEEL expressions). */
  private Map<String, String> conditionExpressions = new HashMap<>();

  /** The ID of the default outgoing node (used when no conditions match). */
  private String defaultNode;

  /**
   * Constructs a new GatewayNode with the specified ID, name, and type.
   *
   * @param id The unique identifier for the node.
   * @param name The human-readable name of the node.
   * @param type The gateway type (EXCLUSIVE_GATEWAY, PARALLEL_GATEWAY, etc.).
   */
  public GatewayNode(String id, String name, NodeType type) {
    super(id, name, type);
  }

  /**
   * Sets a condition expression for a target node.
   *
   * @param targetNodeId The ID of the target node.
   * @param condition The FEEL condition expression.
   */
  public void setCondition(String targetNodeId, String condition) {
    conditionExpressions.put(targetNodeId, condition);
  }

  /**
   * Gets the condition expression for a target node.
   *
   * @param targetNodeId The ID of the target node.
   * @return The condition expression, or null if not set.
   */
  public String getCondition(String targetNodeId) {
    return conditionExpressions.get(targetNodeId);
  }

  /**
   * Gets all condition expressions.
   *
   * @return A map of target node IDs to their condition expressions.
   */
  public Map<String, String> getConditions() {
    return conditionExpressions;
  }
}
