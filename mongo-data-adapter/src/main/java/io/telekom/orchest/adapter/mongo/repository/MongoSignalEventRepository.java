package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.SignalEvent;
import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link SignalEvent} documents. Handles storage and retrieval
 * of signal event subscriptions.
 */
@Repository
public interface MongoSignalEventRepository extends MongoRepository<SignalEvent, String> {

  /**
   * Finds all signal events matching a signal name and state.
   *
   * @param signalEvent the signal name to match
   * @param state the intermediate event state to filter by
   * @return list of matching signal events
   */
  List<SignalEvent> findAllBySignalNameAndState(String signalEvent, IntermediateEventState state);

  /**
   * Finds a signal event by signal name and whether it is a start event.
   *
   * @param signalName the signal name
   * @param isStartEvent true to find start events, false for intermediate events
   * @return the matching signal event, or empty if not found
   */
  Optional<SignalEvent> findBySignalNameAndIsStartEvent(String signalName, boolean isStartEvent);

  /**
   * Finds a signal event by process definition ID and start event flag.
   *
   * @param processDefinitionId the process definition identifier
   * @param isStartEvent true for start events, false for intermediate events
   * @return the matching signal event, or empty if not found
   */
  Optional<SignalEvent> findSignalEventByProcessDefinitionIdAndIsStartEvent(
      String processDefinitionId, Boolean isStartEvent);

  /**
   * Deletes signal events by their linked event ID.
   *
   * @param linkedEventId the linked event identifier
   */
  void deleteByLinkedEventId(String linkedEventId);

  /**
   * Deletes signal events matching a process definition ID and start event flag.
   *
   * @param processDefinitionId the process definition identifier
   * @param isStartEvent true for start events, false for intermediate events
   */
  void deleteSignalEventByProcessDefinitionIdAndIsStartEvent(
      String processDefinitionId, Boolean isStartEvent);

  /**
   * Deletes all signal events belonging to a specific process instance.
   *
   * @param processInstanceId the process instance identifier
   */
  void deleteByProcessInstanceId(String processInstanceId);
}
