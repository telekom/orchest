package io.telekom.orchest.alerting;

import io.telekom.orchest.alerting.api.dto.AlertDTO;
import io.telekom.orchest.alerting.service.AlertLifecycleService;
import io.telekom.orchest.api.core.adapters.data.model.Alert;
import io.telekom.orchest.api.core.adapters.data.model.AlertRecipients;
import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import io.telekom.orchest.api.core.adapters.data.model.AlertingMailerConfig;
import io.telekom.orchest.api.core.adapters.data.repository.AlertRepository;
import io.telekom.orchest.api.core.adapters.data.repository.AlertingMailerConfigRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Opt-in poller that re-notifies due {@link AlertState#FIRING} alerts via configured channels.
 * Enable with {@code telemetry.alerting.scheduler.enabled=true} on a single deployment.
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "alerting.scheduler.enabled", havingValue = "true")
@RequiredArgsConstructor
public class AlertNotificationScheduler {

  private final AlertRepository alertRepository;
  private final AlertingMailerConfigRepository mailerConfigRepository;
  private final AlertLifecycleService alertLifecycleService;
  private final AlertingInterface alertingInterface;
  private final ModuleProperties moduleProperties;
  private final Clock clock;

  /**
   * Polls all FIRING alerts and sends notifications for those that are due based on the configured
   * resend interval. Advances {@code lastTriggeredAt} even on send failure to back off.
   */
  @Scheduled(fixedDelayString = "${alerting.scheduler.poll-interval-ms:30_000}")
  public void notifyDueAlerts() {
    long defaultResend = moduleProperties.getDefaultResendIntervalMs();
    Instant now = clock.instant();
    List<Alert> firing = alertRepository.findByState(AlertState.FIRING);
    for (Alert alert : firing) {
      if (!alert.isDue(now, defaultResend)) {
        continue;
      }
      try {
        Optional<AlertingMailerConfig> alertingMailerConfig =
            mailerConfigRepository.findByProcessId(alert.getMetadata().get("processDefinitionId"));
        AlertDTO alertDto = toAlertDto(alert);
        alertingMailerConfig.ifPresent(
            mailerConfig -> alertDto.setRecipients(mailerConfig.getAlertingRecipient()));

        alertingInterface.sendAlert(alertDto);
        alertLifecycleService.markTriggered(alert);
      } catch (Exception e) {
        log.error("Failed to notify alert fingerprint={}", alert.getFingerprint(), e);
        // Still advance lastTriggeredAt so we back off to the resend interval.
        try {
          alertLifecycleService.markTriggered(alert);
        } catch (Exception persistError) {
          log.error(
              "Failed to update lastTriggeredAt for fingerprint={}",
              alert.getFingerprint(),
              persistError);
        }
      }
    }
  }

  /**
   * Converts a persistent {@link Alert} entity to an {@link AlertDTO} for notification dispatch.
   *
   * @param alert the persisted alert entity
   * @return a populated DTO ready for channel delivery
   */
  public static AlertDTO toAlertDto(Alert alert) {
    AlertRecipients recipients = alert.getRecipients();
    Map<String, String> metadata = alert.getMetadata();
    Integer version = null;
    if (metadata != null && metadata.get("version") != null) {
      try {
        version = Integer.valueOf(metadata.get("version"));
      } catch (NumberFormatException ignored) {
        // leave null
      }
    }
    return AlertDTO.builder()
        .messageBody(alert.getBody())
        .subject(alert.getSubject())
        .count((int) Math.min(alert.getCount(), Integer.MAX_VALUE))
        .createdAt(alert.getCreatedAt())
        .state(alert.getState())
        .processDefinitionId(metadata != null ? metadata.get("processDefinitionId") : null)
        .processInstanceId(metadata != null ? metadata.get("processInstanceId") : null)
        .version(version)
        .recipients(
            recipients == null
                ? null
                : AlertRecipients.builder()
                    .to(recipients.getTo())
                    .cc(recipients.getCc())
                    .bcc(recipients.getBcc())
                    .build())
        .sendAsync(false)
        .build();
  }
}
