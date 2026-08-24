package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.SignalEvent;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing BPMN signal events. Provides methods to register, find, and
 * delete signal events used for process communication.
 */
public interface SignalEventRepository {
  /**
   * Registers a new signal event.
   *
   * @param timedEvent The signal event to register.
   * @return The registered signal event.
   */
  SignalEvent registerEvent(SignalEvent timedEvent);

  /**
   * Finds a signal start event by process definition ID.
   *
   * @param processDefinitionId The process definition ID.
   * @return An Optional containing the signal start event, or empty if not found.
   */
  Optional<SignalEvent> findSignalStartEvent(String processDefinitionId);

  /**
   * Finds all registered signal events by signal name.
   *
   * @param signalName The name of the signal.
   * @return A list of signal events with the specified signal name.
   */
  List<SignalEvent> findAllRegisteredSignalsEvent(String signalName);

  /**
   * Deletes signal events by linked event ID.
   *
   * @param linkedEventId The linked event ID.
   */
  void deleteByLinkedEventId(String linkedEventId);

  /**
   * Removes a signal event.
   *
   * @param timedEvent The signal event to remove.
   */
  void remove(SignalEvent timedEvent);

  /**
   * Deletes start signal events by process definition ID.
   *
   * @param definitionId The process definition ID.
   */
  void deleteStartEventByDefinitionId(String definitionId);

  /**
   * Deletes all signal events registered for a specific process instance.
   *
   * @param processInstanceId The process instance ID.
   */
  void deleteByProcessInstanceId(String processInstanceId);
}
