package io.telekom.orchest.enginecore;

import io.telekom.orchest.api.core.adapters.data.dto.IncidentEventPayload;

/**
 * Interface for handling incident events that occur during process execution. Implementations are
 * responsible for processing incidents (errors, exceptions) that happen with process instances and
 * taking appropriate action (e.g., logging, alerting, recovery).
 */
public interface IncidentEventHandlerAdapter {

  /**
   * Handles the incident event.
   *
   * @param event The incident event containing details about the incident occured with process
   *     Instance.
   */
  void handle(IncidentEventPayload event);

  /**
   * Handles the incident resolution event when a process instance transitions from INCIDENT to
   * RUNNING.
   *
   * @param event The incident event payload containing details about the resolved incident.
   */
  void handleResolution(IncidentEventPayload event);
}
