package io.telekom.orchest.api.core.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Event payload for retrying a failed activity within a process instance. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryProcessEvent {

  /** The process instance containing the failed activity. */
  private String processInstanceId;

  /** The activity ID to retry execution from. */
  private String activityId;

  /** The previous activity ID (same as activityId for retry events). */
  private String previousActivityId; // activityId and previousActivityId is same for retry event
}
