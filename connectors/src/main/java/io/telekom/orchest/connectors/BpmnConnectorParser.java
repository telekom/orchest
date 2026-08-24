package io.telekom.orchest.connectors;

import io.camunda.zeebe.model.bpmn.impl.instance.zeebe.ZeebeIoMappingImpl;
import io.camunda.zeebe.model.bpmn.impl.instance.zeebe.ZeebeTaskHeadersImpl;
import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import io.telekom.orchest.api.core.model.bpmn.DataMapping;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.connectors.parser.MSTeamConnectorParser;
import io.telekom.orchest.connectors.parser.RESTConnectorParser;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Central registry and utility for parsing BPMN connector service tasks into OrchesT node
 * representations. Maps connector type identifiers to their respective parsers and handles I/O
 * mapping extraction.
 */
@Slf4j
public class BpmnConnectorParser {

  public static final String IS_CONNECTOR = "isConnector";
  public static final String CONNECTOR_TYPE = "connectorType";
  public static final String IS_MULTI_INSTANCE = "isMultiInstance";
  public static final Map<String, ConnectorParser> CONNECTORS_REGISTRY =
      Map.ofEntries(
          Map.entry("io.orchest.http-json:1", new RESTConnectorParser()),
          Map.entry("io.orchest.connector-microsoft-teams:1", new MSTeamConnectorParser()),
          Map.entry("io.orchest.soap:1", new RESTConnectorParser()),
          Map.entry("io.orchest.connector-kafka:1", new RESTConnectorParser()),
          Map.entry("io.orchest.whatsapp:1", new RESTConnectorParser()),
          Map.entry("io.orchest.slack:1", new RESTConnectorParser()),
          Map.entry("io.orchest.connector-jdbc:1", new RESTConnectorParser()),
          Map.entry("io.orchest.webhook:1", new RESTConnectorParser()),
          Map.entry("io.orchest.openai:1", new RESTConnectorParser()),
          Map.entry("io.orchest.azure-openai:1", new RESTConnectorParser()));

  /**
   * Checks whether the given connector name is registered in the connectors registry.
   *
   * @param connectorName the connector type identifier to look up
   * @return {@code true} if the connector is registered, {@code false} otherwise
   */
  public static boolean isConnector(String connectorName) {
    return CONNECTORS_REGISTRY.containsKey(connectorName);
  }

  /**
   * Parses a connector service task by delegating to the registered parser and setting connector
   * metadata properties on the node.
   *
   * @param connectorType the connector type identifier used to select the parser
   * @param flowNode the BPMN flow node representing the service task
   * @param node the target node to populate with connector configuration
   */
  public static void parseConnector(String connectorType, FlowNode flowNode, BaseNode node) {
    CONNECTORS_REGISTRY.get(connectorType).parse(flowNode, node);
    node.getProperties().put(IS_CONNECTOR, true);
    node.getProperties().put(CONNECTOR_TYPE, connectorType);
  }

  /**
   * Extracts Zeebe I/O mappings and task headers from the flow node's extension elements and
   * populates the node's input/output mappings accordingly.
   *
   * @param flowNode the BPMN flow node containing extension elements with I/O mappings
   * @param node the target node to populate with extracted data mappings
   */
  public static void mapIoMappings(FlowNode flowNode, BaseNode node) {
    Collection<ZeebeIoMappingImpl> ioMappings =
        flowNode
            .getExtensionElements()
            .getElementsQuery()
            .filterByType(ZeebeIoMappingImpl.class)
            .list();

    // io Mappings
    if (!ioMappings.isEmpty()) {
      ZeebeIoMappingImpl ioMapping = ioMappings.iterator().next();
      ioMapping
          .getInputs()
          .forEach(
              ioMappingInput ->
                  node.getInputMappings()
                      .add(
                          new DataMapping(ioMappingInput.getTarget(), ioMappingInput.getSource())));
      ioMapping
          .getOutputs()
          .forEach(
              ioMappingOutput ->
                  node.getOutputMappings()
                      .add(
                          new DataMapping(
                              ioMappingOutput.getTarget(), ioMappingOutput.getSource())));
    }

    // Headers
    Collection<ZeebeTaskHeadersImpl> taskHeaders =
        flowNode
            .getExtensionElements()
            .getElementsQuery()
            .filterByType(ZeebeTaskHeadersImpl.class)
            .list();

    if (!taskHeaders.isEmpty()) {
      ZeebeTaskHeadersImpl taskHeader = taskHeaders.iterator().next();
      taskHeader
          .getHeaders()
          .forEach(
              zeebeHeader -> {
                node.getOutputMappings()
                    .add(new DataMapping(zeebeHeader.getKey(), zeebeHeader.getValue()));
              });
    }
  }
}
