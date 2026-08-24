package io.telekom.orchest.adapter.mongo.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing an incident (error) that occurred during process execution. Records
 * the failing activity and its stack trace for debugging and retry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Incident implements Persistable<String> {

  /** Unique database identifier. */
  @Id private String id;

  /** The process instance where the incident occurred. */
  @Indexed private String processInstanceId;

  /** The process definition the instance belongs to. */
  @Indexed private String processDefinitionId;

  /** The version of the process definition. */
  private Integer version;

  /** The BPMN element ID of the activity that failed. */
  private String activityId;

  /** Human-readable name of the activity that failed. */
  private String activityName;

  /** The exception stack trace captured at the time of failure. */
  private String stackTrace;

  /** Timestamp when the incident was created. */
  @CreatedDate private OffsetDateTime createdAt;

  @Override
  public boolean isNew() {
    return id == null;
  }
}
