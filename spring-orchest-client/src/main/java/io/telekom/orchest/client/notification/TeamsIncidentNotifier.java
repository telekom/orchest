package io.telekom.orchest.client.notification;

import io.telekom.orchest.client.OrchestProperties;
import io.telekom.orchest.client.configuration.msteams.TeamsWebhookClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

/**
 * {@link IncidentNotifier} implementation that posts MessageCard payloads to an MS Teams webhook.
 */
@Slf4j
@RequiredArgsConstructor
public class TeamsIncidentNotifier implements IncidentNotifier {
  private final TeamsWebhookClient teamsWebhookClient;
  private final OrchestProperties orchestProperties;

  @Async
  @Override
  public void notify(IncidentNotification notification) {
    try {
      String messageBody =
          prepareMessageBody(
              notification.processInstanceId(),
              notification.correlationId(),
              notification.environment(),
              notification.message());
      teamsWebhookClient.sendMessageToChannel(messageBody);
      log.debug(
          "Incident message sent for processInstanceId: {}", notification.processInstanceId());
    } catch (Exception e) {
      log.error(
          "failed to send ms-teams notification for incident on processInstanceId: {}",
          notification.processInstanceId(),
          e);
    }
  }

  @Override
  public boolean isEnabled() {
    return orchestProperties.getIncidentAlert().isEnabled();
  }

  private String prepareMessageBody(
      String processInstanceId, String correlationId, String env, String exceptionStacktrace) {
    String messageRequest =
        """
                {
                   "@type": "MessageCard",
                   "@context": "http://schema.org/extensions/project-tandem/1.0",
                   "themeColor": "E20074",
                   "summary": "OrchesT Client Incident Alerts",
                   "sections": [
                     {
                       "activityTitle": "OrchesT Client Incident Alerts",
                       "facts": [
                         {
                           "name": "Id:",
                           "value": "%s"
                         },
                         {
                           "name": "Environment:",
                           "value": "%s"
                         },
                          {
                           "name": "InstanceId:",
                           "value": "%s"
                         },
                         {
                           "name": "OrchesT URL:",
                           "value": "[%s](https://orchest.%s.your-domain.example.com/processes/%s)"
                         },
                         {
                           "name": "StackTrace: ",
                           "value": "``` %s```"
                         }
                       ]
                      }
                     ]
                    }
                """;
    return String.format(
        messageRequest,
        correlationId,
        env,
        processInstanceId,
        processInstanceId,
        env,
        processInstanceId,
        exceptionStacktrace);
  }
}
