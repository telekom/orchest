package io.telekom.orchest.api.core.adapters.data.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an active user task instance in the workflow engine. Persisted to the database when a
 * process instance reaches a User Task node.
 *
 * <p>Implements Camunda 8 compatible user task lifecycle:
 *
 * <ul>
 *   <li><b>CREATED</b> - Task has been created and is available for assignment
 *   <li><b>CLAIMED</b> - Task has been claimed by a user who will work on it
 *   <li><b>COMPLETED</b> - Task has been completed by the assigned user
 *   <li><b>CANCELLED</b> - Task was cancelled (e.g., process was terminated)
 * </ul>
 *
 * <p>Authorization model (Camunda 8 compatible):
 *
 * <ul>
 *   <li>{@code assignee} - Direct assignee, task is auto-claimed for this user
 *   <li>{@code candidateUsers} - List of users who may claim the task
 *   <li>{@code candidateGroups} - List of groups whose members may claim the task
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTaskInstance {

  private String id;

  /** Unique task identifier */
  private String taskId;

  /** The process instance this task belongs to */
  private String processInstanceId;

  /** The process definition ID */
  private String processDefinitionId;

  /** The BPMN activity/node ID of this user task */
  private String activityId;

  /** Human-readable name of the task */
  private String taskName;

  /** Current state of the task: CREATED, CLAIMED, COMPLETED, CANCELLED */
  private TaskState state;

  // ---- Assignment ----

  /** The user directly assigned to this task (from BPMN definition or runtime assignment) */
  private String assignee;

  /** List of candidate users who may claim this task */
  @Builder.Default private List<String> candidateUsers = new ArrayList<>();

  /** List of candidate groups whose members may claim this task */
  @Builder.Default private List<String> candidateGroups = new ArrayList<>();

  /** The user who claimed this task (may differ from assignee if reassigned) */
  private String claimedBy;

  // ---- Scheduling ----

  /** The due date for this task (ISO 8601) */
  private String dueDate;

  /** The follow-up date for this task (ISO 8601) */
  private String followUpDate;

  // ---- Form ----

  /** The form key for rendering the task form */
  private String formKey;

  // ---- Variables ----

  /** Task-level variables (input mappings from the process) */
  @Builder.Default private Map<String, Object> variables = new HashMap<>();

  // ---- Timestamps ----

  private OffsetDateTime createdAt;
  private OffsetDateTime claimedAt;
  private OffsetDateTime completedAt;

  /** Represents the lifecycle states of a user task. */
  public enum TaskState {
    /** Task created, available for claiming */
    CREATED,
    /** Task claimed by a user */
    CLAIMED,
    /** Task completed by the assigned user */
    COMPLETED,
    /** Task cancelled (process terminated, boundary event, etc.) */
    CANCELLED
  }
}
