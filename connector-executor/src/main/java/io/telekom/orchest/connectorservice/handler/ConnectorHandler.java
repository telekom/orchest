package io.telekom.orchest.connectorservice.handler;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import java.util.Map;

/**
 * SPI for connector implementations executed by the connector service.
 *
 * <p>Implementations read connector configuration from the node's input mappings (evaluating FEEL
 * expressions against the process instance variables), execute the integration and return the
 * connector output as a variables map. The map is merged into the process instance and the activity
 * is resumed by {@link io.telekom.orchest.connectorservice.service.ConnectorExecutionService}.
 *
 * <p>On failure, implementations throw an exception (typically {@link
 * io.telekom.orchest.api.core.model.bpmn.ConnectorException}); the execution service then routes
 * the failure through the engine's error/incident handling using {@link #errorCode()}.
 */
public interface ConnectorHandler {

  /**
   * @return The connector task-definition type this handler serves (e.g. {@code
   *     io.orchest.http-json:1}).
   */
  String connectorType();

  /**
   * Executes the connector and returns the output variables to merge into the process instance.
   *
   * @param instance The process instance (with current variables).
   * @param node The connector node carrying input/output mappings.
   * @return The output variables produced by the connector (never {@code null}).
   */
  Map<String, Object> execute(ProcessInstance instance, BaseNode node);

  /**
   * @return The BPMN error code raised when this connector fails, used for error boundary matching.
   */
  default String errorCode() {
    return "CONNECTOR_ERROR";
  }
}
