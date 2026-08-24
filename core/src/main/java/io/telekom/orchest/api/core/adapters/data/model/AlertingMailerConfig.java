package io.telekom.orchest.api.core.adapters.data.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Per-process alerting mailer configuration binding a process to its email recipients. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertingMailerConfig {

  /** Unique identifier for this configuration record. */
  private String id;

  /** The process definition ID this mailer config applies to. */
  private String processId;

  /** Email recipients for alerts raised by the associated process. */
  private AlertRecipients alertingRecipient;

  /** Timestamp when this configuration was created. */
  private OffsetDateTime createdAt;

  /** Timestamp when this configuration was last modified. */
  private OffsetDateTime lastModifiedAt;
}
