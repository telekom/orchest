package io.telekom.orchest.api.core.adapters.data.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persistent record of a runtime incident (unhandled error) that occurred during process execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

  /** Unique identifier for this incident record. */
  private String id;

  /** The process instance where the incident occurred. */
  private String processInstanceId;

  /** The process definition the instance was running. */
  private String processDefinitionId;

  /** Version of the process definition at the time of the incident. */
  private Integer version;

  /** BPMN element ID of the activity that failed. */
  private String activityId;

  /** Human-readable name of the activity that failed. */
  private String activityName;

  /** Full stack trace of the error that caused the incident. */
  private String stackTrace;

  /** Timestamp when the incident was created. */
  private OffsetDateTime createdAt;
}
