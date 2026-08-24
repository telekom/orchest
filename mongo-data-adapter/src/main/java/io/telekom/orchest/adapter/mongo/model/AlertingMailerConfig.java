package io.telekom.orchest.adapter.mongo.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document storing per-process alerting mail configuration. Defines the default recipients
 * for alert emails sent on behalf of a process.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class AlertingMailerConfig {

  /** Unique database identifier. */
  @Id private String id;

  /** The process definition ID this configuration applies to. */
  private String processId;

  /** Default email recipients for alerts from this process. */
  private Alert.Recipients alertingRecipient;

  /** Timestamp when this configuration was created. */
  private OffsetDateTime createdAt;

  /** Timestamp when this configuration was last modified. */
  private OffsetDateTime lastModifiedAt;
}
