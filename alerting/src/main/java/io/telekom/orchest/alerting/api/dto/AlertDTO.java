package io.telekom.orchest.alerting.api.dto;

import io.telekom.orchest.api.core.adapters.data.model.AlertRecipients;
import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for carrying alert information. Contains details about the incident, process
 * context, and delivery options.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertDTO {

  /** The main body content of the alert message. */
  private String messageBody;

  /** Email / notification subject. When empty, channel implementations may use a default. */
  private String subject;

  /** The ID of the process instance where the incident occurred. */
  private String processInstanceId;

  /** The ID of the process definition. */
  private String processDefinitionId;

  /** The version of the process definition. */
  private Integer version;

  /** The number of occurrences of this incident (defaults to 1). */
  @Builder.Default private Integer count = 1;

  /** Whether to send the alert asynchronously. */
  private boolean sendAsync;

  /** Timestamp when the alert was created. */
  private Instant createdAt;

  /** email distribution list. */
  private AlertRecipients recipients;

  @Builder.Default private String fromAddress = "no-reply@telekom.com";

  /**
   * Lifecycle state used for email badge styling. When null, email rendering defaults to FIRING.
   */
  private AlertState state;
}
