package io.telekom.orchest.api.core.adapters.data.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persistent model representing the enabled/disabled state of a BPMN activity within a process
 * definition.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityState {
  /** Unique identifier for this activity state record. */
  private String id;

  /** The process definition this activity belongs to. */
  private String processDefinitionId;

  /** Version of the process definition. */
  private Integer version;

  // workerType is workerId(serviceTaskId)
  /** Identifier of the activity (e.g. service task ID) within the process. */
  private String activityId;

  /** Human-readable description of the activity. */
  private String description;

  /** Whether this activity is currently enabled for execution. */
  private boolean enabled;

  /** Timestamp when this record was created. */
  private OffsetDateTime createdAt;

  /** Timestamp when this record was last updated. */
  private OffsetDateTime updatedAt;
}
