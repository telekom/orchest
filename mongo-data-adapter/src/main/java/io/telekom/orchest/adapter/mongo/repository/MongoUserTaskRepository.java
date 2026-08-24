package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.UserTaskInstance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link UserTaskInstance} documents. Handles storage and
 * retrieval of user task instances.
 */
@Repository
public interface MongoUserTaskRepository extends MongoRepository<UserTaskInstance, String> {

  /**
   * Finds a user task instance by its task ID.
   *
   * @param taskId the task identifier
   * @return the matching user task, or empty if not found
   */
  Optional<UserTaskInstance> findByTaskId(String taskId);

  /**
   * Finds user tasks for a process instance filtered by allowed states.
   *
   * @param processInstanceId the process instance identifier
   * @param states list of states to include
   * @return list of matching user task instances
   */
  List<UserTaskInstance> findByProcessInstanceIdAndStateIn(
      String processInstanceId, List<String> states);

  /**
   * Finds user tasks assigned to a specific user filtered by allowed states.
   *
   * @param assignee the assignee identifier
   * @param states list of states to include
   * @return list of matching user task instances
   */
  List<UserTaskInstance> findByAssigneeAndStateIn(String assignee, List<String> states);

  /**
   * Finds user tasks available to a user: tasks where the user is the assignee, is in
   * candidateUsers, or one of their groups is in candidateGroups. Only returns tasks in CREATED or
   * CLAIMED state.
   */
  @Query(
      "{ $and: [ "
          + "  { 'state': { $in: ['CREATED', 'CLAIMED'] } }, "
          + "  { $or: [ "
          + "    { 'assignee': ?0 }, "
          + "    { 'claimedBy': ?0 }, "
          + "    { 'candidateUsers': ?0 }, "
          + "    { 'candidateGroups': { $in: ?1 } }, "
          + "    { $and: [ "
          + "      { 'assignee': null }, "
          + "      { 'candidateUsers': { $size: 0 } }, "
          + "      { 'candidateGroups': { $size: 0 } } "
          + "    ] } "
          + "  ] } "
          + "] }")
  List<UserTaskInstance> findAvailableForUser(String userId, List<String> userGroups);

  /**
   * Finds user tasks for a process instance in a specific state.
   *
   * @param processInstanceId the process instance identifier
   * @param state the task state to filter by
   * @return list of matching user task instances
   */
  List<UserTaskInstance> findByProcessInstanceIdAndState(String processInstanceId, String state);
}
