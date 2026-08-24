package io.telekom.orchest.api.core.adapters.connector;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;

/** Adapter interface for executing connector-based worker tasks. */
public interface ConnectorExecutor {

  /**
   * Executes a worker event through this connector.
   *
   * @param workerEventRequest the event request to execute
   */
  void execute(WorkerEventRequest workerEventRequest);
}
