package io.telekom.orchest.api.core.adapters.data.model;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Persistent model representing a task dispatched to a worker that has not yet completed. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingTask {

  /** Unique document identifier. */
  private String id;

  /** The process definition this task belongs to. */
  private String processDefinitionId;

  /** The process instance that created this task. */
  private String processInstanceId;

  /** Identifier of the worker assigned to execute this task. */
  private String workerId;

  /** The event request payload sent to the worker. */
  private WorkerEventRequest workerEventRequest;

  /** Timestamp when this task was created/dispatched. */
  private OffsetDateTime createdAt;

  /** Timestamp when the worker reported task completion. */
  private OffsetDateTime completedAt;
}
