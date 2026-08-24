package io.telekom.orchest.alerting.api.impl;

import io.telekom.orchest.alerting.IAlertingService;
import io.telekom.orchest.alerting.ModuleProperties;
import io.telekom.orchest.alerting.api.dto.AlertDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Alerting service implementation for sending notifications to Microsoft Teams via Webhook. Enabled
 * only when 'telemetry.alerting.enabled' property is true.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "alerting.enabled", havingValue = "true")
public class MSTeamsAlertingService implements IAlertingService {

  private final OkHttpClient client = new OkHttpClient();
  private final ModuleProperties msTeamsAlertingConfig;

  @Value("${spring.profiles.active:local}")
  private String activeProfiles;

  @Override
  public boolean sendAlert(AlertDTO alert) {
    return sendNotification(alert);
  }

  @Async
  @Override
  public void sendAlertAsync(AlertDTO alert) {
    sendNotification(alert);
  }

  @Override
  public boolean isActive() {
    return msTeamsAlertingConfig != null && msTeamsAlertingConfig.isEnabled();
  }

  /**
   * Posts an alert notification as an MS Teams MessageCard to the configured webhook URL.
   *
   * @param alertDTO the alert data to format and send
   * @return true if the webhook responded successfully
   */
  public boolean sendNotification(AlertDTO alertDTO) {
    MediaType mediaType = MediaType.get("application/json; charset=utf-8");
    RequestBody body = RequestBody.create(prepareMessageBody(alertDTO), mediaType);
    ModuleProperties.AlertingConfig alertingConfig =
        msTeamsAlertingConfig.getConfigs().get(ModuleProperties.AlertType.MS_TEAMS);
    Request request = new Request.Builder().url(alertingConfig.getHost()).post(body).build();

    try (Response response = client.newCall(request).execute()) {
      return response.isSuccessful();
    } catch (Exception e) {
      log.error("failed to send Teams alert to: {}", alertingConfig.getHost(), e);
      return false;
    }
  }

  private String prepareMessageBody(AlertDTO alertDTO) {
    String title =
        StringUtils.hasText(alertDTO.getSubject())
            ? escapeJson(alertDTO.getSubject())
            : "Orchest incident alerts";
    String text =
        StringUtils.hasText(alertDTO.getMessageBody())
            ? escapeJson(alertDTO.getMessageBody())
            : "Incident occurred at Engine while processing the instance";
    String definitionId = nullToEmpty(alertDTO.getProcessDefinitionId());
    String version = alertDTO.getVersion() == null ? "" : String.valueOf(alertDTO.getVersion());
    String instanceId = nullToEmpty(alertDTO.getProcessInstanceId());
    String count = alertDTO.getCount() == null ? "1" : String.valueOf(alertDTO.getCount());

    String messageRequest =
        """
                {
                   "@type": "MessageCard",
                   "@context": "http://schema.org/extensions/project-tandem/1.0",
                   "themeColor": "E20074",
                   "summary": "%s",
                   "sections": [
                     {
                       "activityTitle": "%s",
                       "facts": [
                         {
                           "name": "Environment:",
                           "value": "%s"
                         },
                         {
                           "name": "Subject:",
                           "value": "%s"
                         },
                         {
                           "name": "Count:",
                           "value": "%s"
                         },
                         {
                           "name": "Process DefinitionId:",
                           "value": "%s"
                         },
                         {
                           "name": "Version:",
                           "value": "%s"
                         },
                         {
                           "name": "Instance Link:",
                           "value": "[%s](%s/processes/%s)"
                         }
                       ],
                       "markdown": true,
                       "text": "%s"
                     }
                   ]
                 }
                """;

    return String.format(
        messageRequest,
        title,
        title,
        escapeJson(activeProfiles),
        title,
        count,
        escapeJson(definitionId),
        version,
        escapeJson(instanceId),
        "",
        escapeJson(instanceId),
        text);
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private static String escapeJson(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
  }
}
