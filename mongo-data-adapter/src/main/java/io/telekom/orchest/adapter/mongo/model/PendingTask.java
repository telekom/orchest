package io.telekom.orchest.adapter.mongo.model;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a task that is pending execution. Stores information needed to
 * resume or retry a task.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class PendingTask {

  /** Unique identifier for the pending task. */
  private String id;

  /** ID of the process definition associated with the task. */
  private String processDefinitionId;

  /** ID of the process instance associated with the task. */
  private String processInstanceId;

  /** Identifier for the worker type responsible for this task. */
  private String workerId;

  /** The original worker event request that needs processing. */
  private WorkerEventRequest workerEventRequest;

  /** Timestamp when this record was created. */
  @CreatedDate private OffsetDateTime createdAt;

  /** Timestamp when this task was completed (if applicable). */
  private OffsetDateTime completedAt;
}
