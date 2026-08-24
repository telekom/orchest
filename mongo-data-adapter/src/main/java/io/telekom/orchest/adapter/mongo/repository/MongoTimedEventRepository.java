package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.TimedEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link TimedEvent} documents. Stores detailed information
 * about registered timer events.
 */
@Repository
public interface MongoTimedEventRepository extends MongoRepository<TimedEvent, String> {

  /**
   * Finds a timed event by its registry ID.
   *
   * @param timedEventRegistryId the timed event registry identifier
   * @return the matching timed event, or empty if not found
   */
  Optional<TimedEvent> findByTimedEventRegistryId(String timedEventRegistryId);

  /**
   * Finds a single timed event by process definition ID and start event flag.
   *
   * @param processDefinitionId the process definition identifier
   * @param isStartEvent true for start events, false for intermediate events
   * @return the matching timed event, or empty if not found
   */
  Optional<TimedEvent> findByProcessDefinitionIdAndIsStartEvent(
      String processDefinitionId, boolean isStartEvent);

  /**
   * Returns all rows that match {@code processDefinitionId} and {@code isStartEvent} so the adapter
   * can cancel the corresponding scheduler tasks before the rows are removed. The {@code @Query}
   * annotation disambiguates this from the singleton {@code findBy...} method above (Spring Data
   * otherwise picks one or the other based on return type).
   */
  @Query("{ 'processDefinitionId': ?0, 'isStartEvent': ?1 }")
  List<TimedEvent> listByProcessDefinitionIdAndIsStartEvent(
      String processDefinitionId, boolean isStartEvent);

  /**
   * Finds all timed events linked to a specific event ID.
   *
   * @param linkedEventId the linked event identifier
   * @return list of matching timed events
   */
  List<TimedEvent> findByLinkedEventId(String linkedEventId);

  /**
   * Deletes all timed events linked to a specific event ID.
   *
   * @param linkedEventId the linked event identifier
   */
  void deleteByLinkedEventId(String linkedEventId);

  /**
   * Deletes timed events matching a process definition ID and start event flag.
   *
   * @param processDefinitionId the process definition identifier
   * @param isStartEvent true for start events, false for intermediate events
   */
  void deleteTimedEventsByProcessDefinitionIdAndIsStartEvent(
      String processDefinitionId, boolean isStartEvent);

  /**
   * Finds timed events by process instance ID and event type.
   *
   * @param processInstanceId the process instance identifier
   * @param type the timed event type
   * @return list of matching timed events
   */
  List<TimedEvent> findByProcessInstanceIdAndType(String processInstanceId, TimedEvent.Type type);

  /**
   * Deletes timed events by process instance ID and event type.
   *
   * @param processInstanceId the process instance identifier
   * @param type the timed event type
   */
  void deleteByProcessInstanceIdAndType(String processInstanceId, TimedEvent.Type type);

  /**
   * Checks whether a duplicate TASK_RETRY timed event already exists for a given process instance
   * and activity.
   *
   * @param processInstanceId the process instance identifier
   * @param activityId the activity identifier
   * @return the existing retry event if a duplicate exists, or empty otherwise
   */
  @Query(
      "{ 'eventRequest.processInstanceId': ?0, 'eventRequest.activityId': ?1, 'type': 'TASK_RETRY' }")
  Optional<TimedEvent> isDuplicateTaskRetryEvent(String processInstanceId, String activityId);
}
