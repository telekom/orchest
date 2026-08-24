package io.telekom.orchest.adapter.mongo.model;

import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing an alert raised by the orchestration engine. Tracks lifecycle from
 * firing through acknowledgement, silencing, and resolution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Alert {

  /** Unique database identifier. */
  @Id private String id;

  /** The system or component that raised the alert. */
  private String source;

  /** Logical key used to group or deduplicate alerts. */
  private String alertKey;

  /** Unique fingerprint for deduplication of identical alerts. */
  private String fingerprint;

  /** Current lifecycle state of the alert. */
  private AlertState state;

  /** Number of times this alert has been triggered. */
  private long count;

  /** Severity level (e.g., critical, warning, info). */
  private String severity;

  /** Arbitrary metadata attached to the alert. */
  @Builder.Default private Map<String, String> metadata = new HashMap<>();

  /** Email subject line for alert notifications. */
  private String subject;

  /** Email body content for alert notifications. */
  private String body;

  /** The recipients to notify when the alert fires. */
  private Recipients recipients;

  /** Minimum interval in milliseconds between resending the alert notification. */
  private Long resendIntervalMs;

  /** Timestamp when the alert was first created. */
  private Instant createdAt;

  /** Timestamp when the alert was last updated. */
  private Instant updatedAt;

  /** Timestamp when the alert was last triggered. */
  private Instant lastTriggeredAt;

  /** Timestamp when the alert was resolved. */
  private Instant resolvedAt;

  /** Timestamp when the alert was acknowledged. */
  private Instant acknowledgedAt;

  /** The user who acknowledged the alert. */
  private String acknowledgedBy;

  /** Timestamp when the alert was silenced. */
  private Instant silencedAt;

  /** The user who silenced the alert. */
  private String silencedBy;

  /** Email recipient lists for alert notifications. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Recipients {
    /** Primary recipients. */
    @Builder.Default private List<String> to = new ArrayList<>();

    /** Carbon-copy recipients. */
    @Builder.Default private List<String> cc = new ArrayList<>();

    /** Blind carbon-copy recipients. */
    @Builder.Default private List<String> bcc = new ArrayList<>();
  }
}
