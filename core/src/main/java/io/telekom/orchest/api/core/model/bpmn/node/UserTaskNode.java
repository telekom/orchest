package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.NodeType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a User Task node in a BPMN process. User Tasks model work that needs to be done by a
 * human actor.
 *
 * <p>Supports Camunda 8 / BPMN 2.0 assignment semantics:
 *
 * <ul>
 *   <li>{@code assignee} - A single user directly assigned to the task
 *   <li>{@code candidateUsers} - A list of users who may claim and work on the task
 *   <li>{@code candidateGroups} - A list of groups whose members may claim and work on the task
 * </ul>
 *
 * <p>Assignment values can be static strings or FEEL expressions (evaluated at activation time).
 */
@Getter
@Setter
@NoArgsConstructor
public class UserTaskNode extends ActivityNode {

  /**
   * The user directly assigned to this task. When set, the task is automatically claimed for this
   * user. Can be a static value (e.g. "john@example.com") or a FEEL expression (e.g. "= manager").
   */
  private String assignee;

  /**
   * List of users who are candidates to work on this task. These users can claim the task before
   * completing it.
   */
  private List<String> candidateUsers = new ArrayList<>();

  /**
   * List of groups whose members are candidates to work on this task. Members of these groups can
   * claim the task before completing it.
   */
  private List<String> candidateGroups = new ArrayList<>();

  /** The due date for this task (ISO 8601 format). Indicates when the task should be completed. */
  private String dueDate;

  /**
   * The follow-up date for this task (ISO 8601 format). Indicates when a user should start working
   * on the task.
   */
  private String followUpDate;

  /** The form key associated with this user task. Used to reference a form for task completion. */
  private String formKey;

  public UserTaskNode(String id, String name) {
    super(id, name, NodeType.USER_TASK);
  }
}
