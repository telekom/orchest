package io.telekom.orchest.enginecore.bpmn.utils;

import static io.telekom.orchest.connectors.BpmnConnectorParser.IS_CONNECTOR;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.bpmndi.BpmnEdge;
import io.telekom.orchest.api.core.model.bpmn.ExecutionLogEntry;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for working with BPMN models using the Zeebe BPMN model API. Provides methods for
 * parsing, manipulating, and extracting information from BPMN XML.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BPMNUtils {
  private static final String EDGES_STROKE_COLOR_HEX_CODE = "#0f62fe";

  /**
   * Parses a UTF-8 string containing BPMN XML into a {@link BpmnModelInstance}.
   *
   * @param resourceUTF8XML The BPMN XML string.
   * @return The parsed BPMN model instance.
   */
  public static BpmnModelInstance getBpmnModelInstance(String resourceUTF8XML) {
    return Bpmn.readModelFromStream(
        new ByteArrayInputStream(resourceUTF8XML.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * Adds visual highlighting (strokes) to the BPMN diagram based on execution history. Used to
   * visualize the path taken by a process instance.
   *
   * @param bpmnModelInstance The BPMN model instance to modify.
   * @param sequenceExecutions A map of execution logs, used to identify executed sequence flows.
   * @return The modified BPMN XML string with styling applied.
   */
  public static String addStrokes(
      BpmnModelInstance bpmnModelInstance, Map<String, ExecutionLogEntry> sequenceExecutions) {
    Set<String> executedEdges =
        sequenceExecutions.values().stream()
            .map(ExecutionLogEntry::getSequenceFlowIds)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    bpmnModelInstance
        .getModelElementsByType(BpmnEdge.class)
        .forEach(
            edge -> {
              String edgeId = edge.getAttributeValue("bpmnElement");
              if (executedEdges.contains(edgeId)) {
                edge.setAttributeValueNs("bios", "stroke", EDGES_STROKE_COLOR_HEX_CODE);
              }
            });
    return Bpmn.convertToString(bpmnModelInstance).replaceAll("ns\\d:stroke", "stroke");
  }

  /**
   * Extracts the process ID from a BPMN model instance. Assumes the model contains at least one
   * process definition.
   *
   * @param bpmnModelInstance The BPMN model instance.
   * @return The ID of the first process found in the model.
   */
  public static String getProcessId(BpmnModelInstance bpmnModelInstance) {
    Process process = bpmnModelInstance.getModelElementsByType(Process.class).iterator().next();
    return process.getId();
  }

  /**
   * Checks if a node is configured as a connector task. Connector tasks are identified by the
   * presence of the IS_CONNECTOR property.
   *
   * @param node The node to check.
   * @return true if the node is a connector task, false otherwise.
   */
  public static boolean isConnectorTask(BaseNode node) {
    return node.getProperties().containsKey(IS_CONNECTOR);
  }
}
