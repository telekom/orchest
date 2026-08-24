package io.telekom.orchest.enginecore.bpmn;

import io.telekom.orchest.api.core.adapters.data.dto.ConnectorTaskRequest;

/**
 * Interface for dispatching connector task events. Implementations are responsible for delivering
 * the connector task event to the connector service (e.g., by publishing a Kafka message). The
 * connector service executes the connector implementation and resumes the workflow once it
 * completes.
 */
public interface ConnectorDispatchAdapter {

  /**
   * Dispatches the connector task event for asynchronous execution.
   *
   * @param event The connector task request describing the connector node to execute.
   */
  void dispatch(ConnectorTaskRequest event);
}
