package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.TimedEvent;
import java.util.Optional;

/**
 * Repository interface for managing BPMN timer events. Provides methods to register, find, and
 * delete timer events used for time-based process triggers.
 */
public interface TimedEventRepository {
  /**
   * Registers a new timer event.
   *
   * @param timedEvent The timer event to register.
   * @return The registered timer event.
   */
  TimedEvent registerEvent(TimedEvent timedEvent);

  /**
   * Finds a timer event by its registry ID.
   *
   * @param registryId The timer event registry ID.
   * @return An Optional containing the timer event, or empty if not found.
   */
  Optional<TimedEvent> findByTimedEventRegistryId(String registryId);

  /**
   * Finds a timer event by its primary key.
   *
   * @param id The timer event id.
   * @return An Optional containing the timer event, or empty if not found.
   */
  Optional<TimedEvent> findById(String id);

  /**
   * Finds a timer start event by process definition ID.
   *
   * @param processDefinitionId The process definition ID.
   * @return An Optional containing the timer start event, or empty if not found.
   */
  Optional<TimedEvent> findStartTimedEvent(String processDefinitionId);

  /**
   * Checks whether a duplicate timer event already exists for the given process instance and
   * activity.
   *
   * @param processInstanceId the process instance identifier
   * @param activityId the activity identifier
   * @return an Optional containing the existing event if duplicate, or empty otherwise
   */
  Optional<TimedEvent> isDuplicateEvent(String processInstanceId, String activityId);

  /**
   * Deletes timer events by linked event ID.
   *
   * @param linkedEventId The linked event ID.
   */
  void deleteByLinkedEventId(String linkedEventId);

  /**
   * Removes a timer event.
   *
   * @param timedEvent The timer event to remove.
   */
  void remove(TimedEvent timedEvent);

  /**
   * Deletes start timer events by process definition ID.
   *
   * @param definitionId The process definition ID.
   */
  void deleteStartEventByDefinitionId(String definitionId);

  /**
   * Deletes all pending retry timer events by process instance ID.
   *
   * @param processInstanceId The process instance ID.
   */
  void deletePendingRetryTimersByProcessInstanceId(String processInstanceId);
}
