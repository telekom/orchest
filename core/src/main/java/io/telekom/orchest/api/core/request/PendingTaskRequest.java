package io.telekom.orchest.api.core.request;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for submitting a task that is pending worker assignment. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingTaskRequest {

  /** The process definition this task belongs to. */
  private String processDefinitionId;

  /** The worker ID that should handle this task. */
  private String workerId;

  /** The worker event containing task completion or failure details. */
  private WorkerEventRequest workerEvent;
}
