package io.telekom.orchest.alerting.api;

import io.swagger.v3.oas.annotations.media.Schema;
import io.telekom.orchest.api.core.adapters.data.model.Alert;
import io.telekom.orchest.api.core.adapters.data.model.AlertRecipients;
import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** REST response representation of a persistent telemetry alert with full lifecycle timestamps. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representation of a persistent telemetry alert")
public class AlertResponse {

  @Schema(description = "Unique alert identifier", example = "665f1a2b3c4d5e6f7a8b9c0d")
  private String id;

  @Schema(description = "System or service that raised the alert", example = "orchest-engine")
  private String source;

  @Schema(
      description = "Deduplication key scoped to the source",
      example = "incident:order-flow:task-abc")
  private String alertKey;

  @Schema(description = "Computed fingerprint (source::alertKey) used for deduplication")
  private String fingerprint;

  @Schema(description = "Current lifecycle state of the alert")
  private AlertState state;

  @Schema(description = "Number of times this alert has been raised (re-fired)", example = "3")
  private long count;

  @Schema(description = "Alert severity level", example = "critical")
  private String severity;

  @Schema(
      description =
          "Arbitrary key-value metadata attached to the alert (e.g. processDefinitionId, processInstanceId)")
  private Map<String, String> metadata;

  @Schema(description = "Email/notification subject line")
  private String subject;

  @Schema(description = "Email/notification body content")
  private String body;

  @Schema(description = "Notification recipients (to, cc, bcc)")
  private AlertRecipients recipients;

  @Schema(description = "Per-alert resend interval override in milliseconds", example = "300000")
  private Long resendIntervalMs;

  @Schema(description = "Timestamp when the alert was first created")
  private Instant createdAt;

  @Schema(description = "Timestamp of the most recent update to this alert")
  private Instant updatedAt;

  @Schema(description = "Timestamp when a notification was last sent for this alert")
  private Instant lastTriggeredAt;

  @Schema(description = "Timestamp when the alert was resolved (null if not resolved)")
  private Instant resolvedAt;

  @Schema(description = "Timestamp when the alert was acknowledged")
  private Instant acknowledgedAt;

  @Schema(description = "Identity of the actor who acknowledged the alert")
  private String acknowledgedBy;

  @Schema(description = "Timestamp when the alert was silenced")
  private Instant silencedAt;

  @Schema(description = "Identity of the actor who silenced the alert")
  private String silencedBy;

  /**
   * Maps a persistent {@link Alert} entity to its REST response representation.
   *
   * @param alert the alert entity
   * @return the response DTO
   */
  public static AlertResponse from(Alert alert) {
    return AlertResponse.builder()
        .id(alert.getId())
        .source(alert.getSource())
        .alertKey(alert.getAlertKey())
        .fingerprint(alert.getFingerprint())
        .state(alert.getState())
        .count(alert.getCount())
        .severity(alert.getSeverity())
        .metadata(alert.getMetadata())
        .subject(alert.getSubject())
        .body(alert.getBody())
        .recipients(alert.getRecipients())
        .resendIntervalMs(alert.getResendIntervalMs())
        .createdAt(alert.getCreatedAt())
        .updatedAt(alert.getUpdatedAt())
        .lastTriggeredAt(alert.getLastTriggeredAt())
        .resolvedAt(alert.getResolvedAt())
        .acknowledgedAt(alert.getAcknowledgedAt())
        .acknowledgedBy(alert.getAcknowledgedBy())
        .silencedAt(alert.getSilencedAt())
        .silencedBy(alert.getSilencedBy())
        .build();
  }
}
