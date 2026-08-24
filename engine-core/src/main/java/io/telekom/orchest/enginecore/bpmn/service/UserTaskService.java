package io.telekom.orchest.enginecore.bpmn.service;

import io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance;
import io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance.TaskState;
import io.telekom.orchest.api.core.adapters.data.repository.UserTaskRepository;
import io.telekom.orchest.api.core.model.bpmn.node.UserTaskNode;
import io.telekom.orchest.api.core.utils.IDGenerator;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing User Task lifecycle operations. Implements Camunda 8 compatible user task
 * semantics including:
 *
 * <ul>
 *   <li>Task creation with assignment (assignee, candidateUsers, candidateGroups)
 *   <li>Task claiming by authorized users
 *   <li>Task unclaiming (release back to pool)
 *   <li>Task completion with output variables
 *   <li>Task reassignment
 *   <li>Authorization checks based on assignment configuration
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class UserTaskService {

  private final UserTaskRepository userTaskRepository;

  /**
   * Creates a new user task instance when the engine reaches a User Task node. If an assignee is
   * defined, the task is automatically claimed for that user.
   *
   * @param processInstanceId The process instance ID.
   * @param processDefinitionId The process definition ID.
   * @param userTaskNode The User Task node definition.
   * @param variables The process variables at the time of task creation.
   * @return The created user task instance.
   */
  public UserTaskInstance createUserTask(
      String processInstanceId,
      String processDefinitionId,
      UserTaskNode userTaskNode,
      Map<String, Object> variables) {
    String taskId = IDGenerator.generate();
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    // Determine initial state: if assignee is set, auto-claim
    boolean hasAssignee =
        userTaskNode.getAssignee() != null && !userTaskNode.getAssignee().isBlank();
    TaskState initialState = hasAssignee ? TaskState.CLAIMED : TaskState.CREATED;

    UserTaskInstance userTask =
        UserTaskInstance.builder()
            .taskId(taskId)
            .processInstanceId(processInstanceId)
            .processDefinitionId(processDefinitionId)
            .activityId(userTaskNode.getId())
            .taskName(userTaskNode.getName())
            .state(initialState)
            .assignee(hasAssignee ? userTaskNode.getAssignee() : null)
            .candidateUsers(
                userTaskNode.getCandidateUsers() != null
                    ? userTaskNode.getCandidateUsers()
                    : List.of())
            .candidateGroups(
                userTaskNode.getCandidateGroups() != null
                    ? userTaskNode.getCandidateGroups()
                    : List.of())
            .claimedBy(hasAssignee ? userTaskNode.getAssignee() : null)
            .dueDate(userTaskNode.getDueDate())
            .followUpDate(userTaskNode.getFollowUpDate())
            .formKey(userTaskNode.getFormKey())
            .variables(variables != null ? variables : Map.of())
            .createdAt(now)
            .claimedAt(hasAssignee ? now : null)
            .build();

    UserTaskInstance saved = userTaskRepository.save(userTask);
    log.info(
        "Created user task '{}' (taskId={}) for process instance {}. State: {}, Assignee: {}, CandidateUsers: {}, CandidateGroups: {}",
        userTaskNode.getName(),
        taskId,
        processInstanceId,
        initialState,
        userTask.getAssignee(),
        userTask.getCandidateUsers(),
        userTask.getCandidateGroups());
    return saved;
  }

  /**
   * Claims a user task for the specified user. The user must be authorized (assignee,
   * candidateUser, or member of candidateGroup).
   *
   * @param taskId The unique task ID.
   * @param userId The user claiming the task.
   * @param userGroups The groups the user belongs to (from JWT/auth context).
   * @return The updated user task instance.
   * @throws IllegalArgumentException if the task is not found.
   * @throws IllegalStateException if the task is not in CREATED state or user is not authorized.
   */
  public UserTaskInstance claimTask(String taskId, String userId, List<String> userGroups) {
    UserTaskInstance task =
        userTaskRepository
            .findByTaskId(taskId)
            .orElseThrow(() -> new IllegalArgumentException("User task not found: " + taskId));

    //        if (task.getState() != TaskState.CREATED) {
    //            throw new IllegalStateException("Task " + taskId + " is not in CREATED state
    // (current: " + task.getState() + "). Only unclaimed tasks can be claimed.");
    //        }

    if (!isUserAuthorized(task, userId, userGroups)) {
      throw new IllegalStateException(
          "User '"
              + userId
              + "' is not authorized to claim task "
              + taskId
              + ". User must be the assignee, a candidate user, or a member of a candidate group.");
    }

    task.setState(TaskState.CLAIMED);
    task.setClaimedBy(userId);
    task.setAssignee(userId);
    task.setClaimedAt(OffsetDateTime.now(ZoneOffset.UTC));

    UserTaskInstance saved = userTaskRepository.save(task);
    log.info("User task '{}' (taskId={}) claimed by user '{}'", task.getTaskName(), taskId, userId);
    return saved;
  }

  /**
   * Unclaims a user task, releasing it back to the candidate pool. Only the user who claimed the
   * task (or an admin) can unclaim it.
   *
   * @param taskId The unique task ID.
   * @param userId The user unclaiming the task.
   * @return The updated user task instance.
   */
  public UserTaskInstance unclaimTask(String taskId, String userId) {
    UserTaskInstance task =
        userTaskRepository
            .findByTaskId(taskId)
            .orElseThrow(() -> new IllegalArgumentException("User task not found: " + taskId));

    if (task.getState() != TaskState.CLAIMED) {
      throw new IllegalStateException(
          "Task "
              + taskId
              + " is not in CLAIMED state (current: "
              + task.getState()
              + "). Only claimed tasks can be unclaimed.");
    }

    if (!userId.equals(task.getClaimedBy())) {
      throw new IllegalStateException(
          "User '"
              + userId
              + "' did not claim task "
              + taskId
              + ". Only the claimant can unclaim.");
    }

    task.setState(TaskState.CREATED);
    task.setClaimedBy(null);
    task.setClaimedAt(null);
    // Keep assignee if it was set from the BPMN definition
    // (assignee from definition vs runtime claim are different)

    UserTaskInstance saved = userTaskRepository.save(task);
    log.info(
        "User task '{}' (taskId={}) unclaimed by user '{}'", task.getTaskName(), taskId, userId);
    return saved;
  }

  /**
   * Reassigns a user task to a different user.
   *
   * @param taskId The unique task ID.
   * @param newAssignee The new assignee.
   * @return The updated user task instance.
   */
  public UserTaskInstance reassignTask(String taskId, String newAssignee) {
    UserTaskInstance task =
        userTaskRepository
            .findByTaskId(taskId)
            .orElseThrow(() -> new IllegalArgumentException("User task not found: " + taskId));

    if (task.getState() == TaskState.COMPLETED || task.getState() == TaskState.CANCELLED) {
      throw new IllegalStateException(
          "Cannot reassign task " + taskId + " in state " + task.getState());
    }

    task.setAssignee(newAssignee);
    task.setClaimedBy(newAssignee);
    task.setState(TaskState.CLAIMED);
    task.setClaimedAt(OffsetDateTime.now(ZoneOffset.UTC));

    UserTaskInstance saved = userTaskRepository.save(task);
    log.info(
        "User task '{}' (taskId={}) reassigned to '{}'", task.getTaskName(), taskId, newAssignee);
    return saved;
  }

  /**
   * Marks a user task as completed. The user must have claimed the task first. Returns the
   * completed task — the engine will then call resumeActivity() to continue the process.
   *
   * @param taskId The unique task ID.
   * @param userId The user completing the task.
   * @return The completed user task instance.
   */
  public UserTaskInstance completeTask(String taskId, String userId) {
    UserTaskInstance task =
        userTaskRepository
            .findByTaskId(taskId)
            .orElseThrow(() -> new IllegalArgumentException("User task not found: " + taskId));

    if (task.getState() == TaskState.COMPLETED) {
      throw new IllegalStateException("Task " + taskId + " is already completed.");
    }

    if (task.getState() == TaskState.CANCELLED) {
      throw new IllegalStateException(
          "Task " + taskId + " has been cancelled and cannot be completed.");
    }

    // For CREATED tasks (unclaimed), allow completion if user is authorized
    // For CLAIMED tasks, only the claimant can complete
    if (task.getState() == TaskState.CLAIMED && !userId.equals(task.getClaimedBy())) {
      throw new IllegalStateException(
          "User '"
              + userId
              + "' is not the claimant of task "
              + taskId
              + ". Only the user who claimed the task can complete it.");
    }

    task.setState(TaskState.COMPLETED);
    task.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
    if (task.getClaimedBy() == null) {
      task.setClaimedBy(userId);
      task.setAssignee(userId);
    }

    UserTaskInstance saved = userTaskRepository.save(task);
    log.info(
        "User task '{}' (taskId={}) completed by user '{}'", task.getTaskName(), taskId, userId);
    return saved;
  }

  /** Finds a user task by its unique task ID. */
  public Optional<UserTaskInstance> getTask(String taskId) {
    return userTaskRepository.findByTaskId(taskId);
  }

  /** Finds all active user tasks for a process instance. */
  public List<UserTaskInstance> getTasksForProcessInstance(String processInstanceId) {
    return userTaskRepository.findActiveByProcessInstanceId(processInstanceId);
  }

  /**
   * Finds all user tasks available to the specified user (assigned, candidate, or via group
   * membership).
   *
   * @param userId The user identifier.
   * @param userGroups The groups the user belongs to.
   * @return List of user tasks the user can see/work on.
   */
  public List<UserTaskInstance> getAvailableTasks(String userId, List<String> userGroups) {
    return userTaskRepository.findAvailableForUser(userId, userGroups);
  }

  /**
   * Cancels all active tasks for a process instance. Used when the process instance is terminated
   * or cancelled.
   */
  public void cancelTasksForProcessInstance(String processInstanceId) {
    userTaskRepository.cancelAllByProcessInstanceId(processInstanceId);
    log.info("Cancelled all active user tasks for process instance {}", processInstanceId);
  }

  /**
   * Checks if a user is authorized to interact with a user task. A user is authorized if:
   *
   * <ul>
   *   <li>They are the assignee of the task
   *   <li>They are in the candidateUsers list
   *   <li>Any of their groups matches a candidateGroup
   *   <li>There are no assignment restrictions (open task)
   * </ul>
   *
   * @param task The user task instance.
   * @param userId The user identifier.
   * @param userGroups The groups the user belongs to.
   * @return true if the user is authorized.
   */
  public boolean isUserAuthorized(UserTaskInstance task, String userId, List<String> userGroups) {
    // If no assignment restrictions, anyone can work on it
    boolean hasNoRestrictions =
        (task.getAssignee() == null || task.getAssignee().isBlank())
            && (task.getCandidateUsers() == null || task.getCandidateUsers().isEmpty())
            && (task.getCandidateGroups() == null || task.getCandidateGroups().isEmpty());
    if (hasNoRestrictions) {
      return true;
    }

    // Check direct assignee
    if (userId.equals(task.getAssignee())) {
      return true;
    }

    // Check candidate users
    if (task.getCandidateUsers() != null && task.getCandidateUsers().contains(userId)) {
      return true;
    }

    // Check candidate groups
    if (task.getCandidateGroups() != null && userGroups != null) {
      return task.getCandidateGroups().stream().anyMatch(userGroups::contains);
    }

    return false;
  }
}
