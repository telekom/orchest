package io.telekom.orchest.connectors;

import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;

/**
 * Functional interface for parsing connector-specific configuration from BPMN service tasks.
 * Implementations extract connector metadata (e.g., REST endpoint, authentication) from the BPMN
 * model and populate the corresponding BaseNode with connector-specific properties.
 */
@FunctionalInterface
public interface ConnectorParser {
  /**
   * Parses connector configuration from a BPMN service task element.
   *
   * @param serviceTask The BPMN FlowNode representing the service task.
   * @param node The BaseNode to populate with parsed connector configuration.
   */
  void parse(FlowNode serviceTask, BaseNode node);
}
