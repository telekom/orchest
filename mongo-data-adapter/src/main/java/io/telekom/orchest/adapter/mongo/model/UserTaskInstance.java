package io.telekom.orchest.adapter.mongo.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document for storing User Task instances. Tracks the lifecycle of user tasks including
 * assignment, claiming, and completion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class UserTaskInstance {

  /** Unique database identifier. */
  @Id private String id;

  /** The logical task ID assigned during process execution. */
  private String taskId;

  /** The process instance this task belongs to. */
  private String processInstanceId;

  /** The process definition this task belongs to. */
  private String processDefinitionId;

  /** The BPMN activity element ID of this user task. */
  private String activityId;

  /** Human-readable name of the task. */
  private String taskName;

  /** Current lifecycle state of the task (e.g., CREATED, CLAIMED, COMPLETED). */
  private String state;

  /** The user currently assigned to this task. */
  private String assignee;

  /** Users eligible to claim this task. */
  @Builder.Default private List<String> candidateUsers = new ArrayList<>();

  /** Groups eligible to claim this task. */
  @Builder.Default private List<String> candidateGroups = new ArrayList<>();

  /** The user who claimed this task. */
  private String claimedBy;

  /** The due date for task completion. */
  private String dueDate;

  /** The follow-up date for task review. */
  private String followUpDate;

  /** The form key used to render the task form in the UI. */
  private String formKey;

  /** Task-local variables (form data, contextual information). */
  @Builder.Default private Map<String, Object> variables = new HashMap<>();

  /** Timestamp when the task was created. */
  @CreatedDate private OffsetDateTime createdAt;

  /** Timestamp when the task was claimed. */
  private OffsetDateTime claimedAt;

  /** Timestamp when the task was completed. */
  private OffsetDateTime completedAt;
}
