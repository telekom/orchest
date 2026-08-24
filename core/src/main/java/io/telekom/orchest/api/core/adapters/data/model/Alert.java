package io.telekom.orchest.api.core.adapters.data.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Persistent telemetry alert with Grafana-style lifecycle metadata and email payload. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

  /** Unique identifier for this alert record. */
  private String id;

  /** Origin system or component that raised the alert. */
  private String source;

  /** Logical key identifying the alert type within its source. */
  private String alertKey;

  /** Composite fingerprint derived from source and alertKey for deduplication. */
  private String fingerprint;

  /** Current lifecycle state of this alert. */
  private AlertState state;

  /** Number of times this alert has been triggered. */
  @Builder.Default private long count = 1;

  /** Severity level (e.g. critical, warning, info). */
  private String severity;

  /** Arbitrary key-value metadata attached to the alert. */
  @Builder.Default private Map<String, String> metadata = new HashMap<>();

  /** Email subject line for notifications. */
  private String subject;

  /** Email body content for notifications. */
  private String body;

  /** Distribution list for alert email notifications. */
  private AlertRecipients recipients;

  /** Optional per-alert resend override in milliseconds. */
  private Long resendIntervalMs;

  /** Timestamp when this alert was first created. */
  private Instant createdAt;

  /** Timestamp when this alert was last modified. */
  private Instant updatedAt;

  /** Timestamp when the alert was last sent/triggered. */
  private Instant lastTriggeredAt;

  /** Timestamp when the alert was resolved. */
  private Instant resolvedAt;

  /** Timestamp when the alert was acknowledged. */
  private Instant acknowledgedAt;

  /** Identity of the user who acknowledged the alert. */
  private String acknowledgedBy;

  /** Timestamp when the alert was silenced. */
  private Instant silencedAt;

  /** Identity of the user who silenced the alert. */
  private String silencedBy;

  /**
   * Computes a deterministic fingerprint from source and alert key.
   *
   * @param source the origin system
   * @param alertKey the logical alert key
   * @return composite fingerprint string
   */
  public static String fingerprintOf(String source, String alertKey) {
    return source + "::" + alertKey;
  }

  /**
   * Returns the effective resend interval, preferring the per-alert override if set.
   *
   * @param defaultResendIntervalMs fallback interval when no per-alert override exists
   * @return effective resend interval in milliseconds
   */
  public long effectiveResendIntervalMs(long defaultResendIntervalMs) {
    if (resendIntervalMs != null && resendIntervalMs > 0) {
      return resendIntervalMs;
    }
    return defaultResendIntervalMs;
  }

  /**
   * Determines whether this alert is due for resending based on the current time and interval.
   *
   * @param now the current instant
   * @param defaultResendIntervalMs fallback resend interval in milliseconds
   * @return true if the alert is firing and enough time has elapsed since last trigger
   */
  public boolean isDue(Instant now, long defaultResendIntervalMs) {
    if (state != AlertState.FIRING) {
      return false;
    }
    if (lastTriggeredAt == null) {
      return true;
    }
    long intervalMs = effectiveResendIntervalMs(defaultResendIntervalMs);
    return !lastTriggeredAt.plusMillis(intervalMs).isAfter(now);
  }
}
