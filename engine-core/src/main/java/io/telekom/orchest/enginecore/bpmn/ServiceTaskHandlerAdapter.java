package io.telekom.orchest.enginecore.bpmn;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;

/**
 * Interface for handling service task events. Implementations are responsible for delivering the
 * worker event to the appropriate destination (e.g., sending a Kafka message to a task worker).
 */
public interface ServiceTaskHandlerAdapter {

  /**
   * Handles the worker event request.
   *
   * @param event The worker event containing details about the service task execution.
   */
  void handle(WorkerEventRequest event);
}
