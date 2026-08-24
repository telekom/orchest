package io.telekom.orchest.alerting.api;

import io.telekom.orchest.api.core.adapters.data.model.AlertRecipients;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Producer request to raise (or re-raise) a persistent alert via the telemetry SPI. */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RaiseAlertRequest {

  private String source;
  private String alertKey;
  private String subject;
  private String body;
  private AlertRecipients recipients;
  private String severity;

  @Builder.Default private Map<String, String> metadata = new HashMap<>();

  /** Optional per-alert resend override in milliseconds. */
  private Long resendIntervalMs;

  /**
   * Aggregates alerts per process definition + activity so repeated failures bump {@code count}
   * instead of creating one document per process instance.
   */
  public static String alertKey(String processDefinitionId, String activityId) {
    return "incident:" + processDefinitionId + ":" + activityId;
  }
}
