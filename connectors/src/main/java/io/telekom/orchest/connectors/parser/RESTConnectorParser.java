package io.telekom.orchest.connectors.parser;

import static io.telekom.orchest.connectors.BpmnConnectorParser.mapIoMappings;

import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.connectors.ConnectorParser;

/**
 * Connector parser for REST-based service tasks. Delegates entirely to I/O mapping extraction since
 * REST connectors require no additional connector-specific configuration.
 */
public class RESTConnectorParser implements ConnectorParser {
  /** {@inheritDoc} */
  @Override
  public void parse(FlowNode flowNode, BaseNode node) {
    mapIoMappings(flowNode, node);
  }
}
