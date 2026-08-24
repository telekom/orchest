package io.telekom.orchest.connectors.parser;

import static io.telekom.orchest.connectors.BpmnConnectorParser.mapIoMappings;

import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.connectors.ConnectorParser;
import java.util.Collection;

/**
 * Connector parser for Microsoft Teams service tasks. Extracts I/O mappings and retry configuration
 * from the BPMN model.
 */
public class MSTeamConnectorParser implements ConnectorParser {
  /** {@inheritDoc} */
  @Override
  public void parse(FlowNode flowNode, BaseNode node) {
    mapIoMappings(flowNode, node);

    Collection<ZeebeTaskDefinition> taskDefs =
        flowNode
            .getExtensionElements()
            .getElementsQuery()
            .filterByType(ZeebeTaskDefinition.class)
            .list();
    if (!taskDefs.isEmpty()) {
      node.getProperties().put("retries", taskDefs.iterator().next().getRetries());
    }
  }
}
