package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.NodeType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a Business Rule Task node in a BPMN process. Business Rule Tasks execute DMN decision
 * tables to make decisions during process execution.
 */
@Getter
@Setter
@NoArgsConstructor
public class BusinessRuleTaskNode extends ActivityNode {
  /** The ID of the DMN decision definition to evaluate. */
  private String decisionId;

  /** The variable name where the decision result will be stored. */
  private String resultVariable;

  /**
   * Constructs a new BusinessRuleTaskNode with the specified ID and name.
   *
   * @param id The unique identifier for the node.
   * @param name The human-readable name of the node.
   */
  public BusinessRuleTaskNode(String id, String name) {
    super(id, name, NodeType.BUSINESS_RULE_TASK);
  }
}
