package io.telekom.orchest.client.clients;

import io.telekom.orchest.client.notification.IncidentNotification;
import io.telekom.orchest.client.notification.IncidentNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @deprecated Use {@link IncidentNotifier} directly instead.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Deprecated
public class TeamsService {
  private final IncidentNotifier incidentNotifier;

  /**
   * Sends an incident alert message asynchronously.
   *
   * @param processInstanceId the process instance that raised the incident
   * @param message the incident message or stack trace
   */
  @Async
  public void sendIncidentMessage(String processInstanceId, String message) {
    incidentNotifier.notify(
        new IncidentNotification(processInstanceId, processInstanceId, null, message));
  }

  /**
   * Prepares the message body for a Teams notification.
   *
   * @param processInstanceId the process instance ID
   * @param correlationId the correlation ID
   * @param env the environment name
   * @param exceptionStacktrace the exception stack trace
   * @return the formatted message body
   */
  public String prepareMessageBody(
      String processInstanceId, String correlationId, String env, String exceptionStacktrace) {
    return exceptionStacktrace;
  }
}
