package io.telekom.orchest.orchestrest.api.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data Transfer Object for User Task instances. Returned by the User Task REST API endpoints. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTaskDTO {

  /** Unique task identifier */
  private String taskId;

  /** The process instance this task belongs to */
  private String processInstanceId;

  /** The process definition ID */
  private String processDefinitionId;

  /** The BPMN activity/node ID */
  private String activityId;

  /** Human-readable task name */
  private String taskName;

  /** Current state: CREATED, CLAIMED, COMPLETED, CANCELLED */
  private String state;

  /** The user assigned to this task */
  private String assignee;

  /** Candidate users who can claim this task */
  private List<String> candidateUsers;

  /** Candidate groups whose members can claim this task */
  private List<String> candidateGroups;

  /** The user who claimed this task */
  private String claimedBy;

  /** Due date (ISO 8601) */
  private String dueDate;

  /** Follow-up date (ISO 8601) */
  private String followUpDate;

  /** Form key for rendering the task form */
  private String formKey;

  /** Task variables */
  private Map<String, Object> variables;

  /** Timestamps */
  private String createdAt;

  private String claimedAt;
  private String completedAt;
}
