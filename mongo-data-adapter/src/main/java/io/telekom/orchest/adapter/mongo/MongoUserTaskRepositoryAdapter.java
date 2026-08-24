package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.UserTaskInstanceMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoUserTaskRepository;
import io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance;
import io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance.TaskState;
import io.telekom.orchest.api.core.adapters.data.repository.UserTaskRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * MongoDB implementation of the {@link UserTaskRepository}. Adapts the Spring Data MongoDB
 * repository to the domain-agnostic repository interface. Manages the full lifecycle of user task
 * instances (create, claim, complete, cancel).
 */
@Repository
@RequiredArgsConstructor
public class MongoUserTaskRepositoryAdapter implements UserTaskRepository {

  private final MongoUserTaskRepository repository;
  private final UserTaskInstanceMapper mapper;

  private static final List<String> ACTIVE_STATES =
      List.of(TaskState.CREATED.name(), TaskState.CLAIMED.name());

  /**
   * Persists a user task instance and stamps the generated ID back onto the domain object.
   *
   * @param userTask the user task instance to save
   * @return the saved user task instance with its persisted ID
   */
  @Override
  public UserTaskInstance save(UserTaskInstance userTask) {
    io.telekom.orchest.adapter.mongo.model.UserTaskInstance saved =
        repository.save(mapper.toDocument(userTask));
    userTask.setId(saved.getId());
    return userTask;
  }

  /**
   * Finds a user task instance by its task ID.
   *
   * @param taskId the task ID
   * @return the matching user task, or empty if not found
   */
  @Override
  public Optional<UserTaskInstance> findByTaskId(String taskId) {
    return repository.findByTaskId(taskId).map(mapper::toDomain);
  }

  /**
   * Finds all active (CREATED or CLAIMED) user tasks for a given process instance.
   *
   * @param processInstanceId the process instance ID
   * @return list of active user tasks
   */
  @Override
  public List<UserTaskInstance> findActiveByProcessInstanceId(String processInstanceId) {
    return repository.findByProcessInstanceIdAndStateIn(processInstanceId, ACTIVE_STATES).stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Finds all active user tasks assigned to a specific user.
   *
   * @param userId the assignee user ID
   * @return list of active user tasks assigned to the user
   */
  @Override
  public List<UserTaskInstance> findByAssignee(String userId) {
    return repository.findByAssigneeAndStateIn(userId, ACTIVE_STATES).stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Finds all user tasks available to a user based on their ID and group memberships.
   *
   * @param userId the user ID
   * @param userGroups the user's group memberships, or null
   * @return list of user tasks available to the user
   */
  @Override
  public List<UserTaskInstance> findAvailableForUser(String userId, List<String> userGroups) {
    List<String> groups = userGroups != null ? userGroups : List.of();
    return repository.findAvailableForUser(userId, groups).stream().map(mapper::toDomain).toList();
  }

  /**
   * Finds an active user task by process instance and activity ID.
   *
   * @param processInstanceId the process instance ID
   * @param activityId the BPMN activity ID
   * @return the matching active user task, or empty if not found
   */
  @Override
  public Optional<UserTaskInstance> findByProcessInstanceIdAndActivityId(
      String processInstanceId, String activityId) {
    return repository.findByProcessInstanceIdAndStateIn(processInstanceId, ACTIVE_STATES).stream()
        .filter(task -> activityId.equals(task.getActivityId()))
        .findFirst()
        .map(mapper::toDomain);
  }

  /**
   * Cancels all active user tasks for a given process instance by setting their state to CANCELLED.
   *
   * @param processInstanceId the process instance ID whose tasks should be cancelled
   */
  @Override
  public void cancelAllByProcessInstanceId(String processInstanceId) {
    List<io.telekom.orchest.adapter.mongo.model.UserTaskInstance> activeTasks =
        repository.findByProcessInstanceIdAndStateIn(processInstanceId, ACTIVE_STATES);
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    activeTasks.forEach(
        task -> {
          task.setState(TaskState.CANCELLED.name());
          task.setCompletedAt(now);
          repository.save(task);
        });
  }

  /**
   * Removes a user task instance by its ID, if present.
   *
   * @param userTask the user task to remove
   */
  @Override
  public void remove(UserTaskInstance userTask) {
    if (userTask.getId() != null) {
      repository.deleteById(userTask.getId());
    }
  }
}
