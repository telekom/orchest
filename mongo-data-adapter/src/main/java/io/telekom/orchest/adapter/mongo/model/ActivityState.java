package io.telekom.orchest.adapter.mongo.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing the enabled/disabled state of an activity within a process
 * definition.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class ActivityState {
  /** Unique database identifier. */
  @Id private String id;

  /** The ID of the process definition this activity belongs to. */
  private String processDefinitionId;

  /** The version of the process definition. */
  private Integer version;

  /** The BPMN activity element ID. */
  private String activityId;

  /** Human-readable description of the activity state configuration. */
  private String description;

  /** Whether the activity is currently enabled for execution. */
  private boolean enabled;

  /** Timestamp when this record was created. */
  private OffsetDateTime createdAt;

  /** Timestamp when this record was last updated. */
  private OffsetDateTime updatedAt;
}
