package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing User Task instances. Provides methods to create, query, update,
 * and delete user tasks throughout their lifecycle (CREATED → CLAIMED → COMPLETED).
 */
public interface UserTaskRepository {

  /**
   * Saves a user task instance (create or update).
   *
   * @param userTask The user task instance to save.
   * @return The saved user task instance.
   */
  UserTaskInstance save(UserTaskInstance userTask);

  /**
   * Finds a user task by its unique task ID.
   *
   * @param taskId The unique task ID.
   * @return An Optional containing the user task, or empty if not found.
   */
  Optional<UserTaskInstance> findByTaskId(String taskId);

  /**
   * Finds all active (non-completed, non-cancelled) user tasks for a process instance.
   *
   * @param processInstanceId The process instance ID.
   * @return List of active user tasks for the process instance.
   */
  List<UserTaskInstance> findActiveByProcessInstanceId(String processInstanceId);

  /**
   * Finds all user tasks assigned to a specific user (as assignee or claimedBy).
   *
   * @param userId The user identifier.
   * @return List of user tasks assigned to the user.
   */
  List<UserTaskInstance> findByAssignee(String userId);

  /**
   * Finds all active user tasks where the user is a candidate (directly or via groups). Returns
   * tasks in CREATED state where: - The user is in candidateUsers, OR - Any of the user's groups
   * matches candidateGroups, OR - The user is the assignee
   *
   * @param userId The user identifier.
   * @param userGroups The groups the user belongs to.
   * @return List of user tasks available to the user.
   */
  List<UserTaskInstance> findAvailableForUser(String userId, List<String> userGroups);

  /**
   * Finds a user task by process instance ID and activity ID. Used internally when completing a
   * user task via the engine.
   *
   * @param processInstanceId The process instance ID.
   * @param activityId The BPMN activity/node ID.
   * @return An Optional containing the user task, or empty if not found.
   */
  Optional<UserTaskInstance> findByProcessInstanceIdAndActivityId(
      String processInstanceId, String activityId);

  /**
   * Cancels all active user tasks for a process instance. Used when a process instance is
   * terminated or cancelled.
   *
   * @param processInstanceId The process instance ID.
   */
  void cancelAllByProcessInstanceId(String processInstanceId);

  /**
   * Removes a user task instance.
   *
   * @param userTask The user task instance to remove.
   */
  void remove(UserTaskInstance userTask);
}
