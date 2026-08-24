package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.MessageEventStore;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing BPMN message events. Provides methods to register, find, and
 * delete message events used for process communication.
 */
public interface MessageEventRepository {
  /**
   * Registers a new message event.
   *
   * @param timedEvent The message event to register.
   * @return The registered message event.
   */
  MessageEventStore registerEvent(MessageEventStore timedEvent);

  /**
   * Finds List of registered message event by message name and correlation ID.
   *
   * @param messageName The name of the message.
   * @param correlationId The correlation ID used to correlate the message.
   * @return An Optional containing the message event, or empty if not found.
   */
  List<MessageEventStore> findRegisteredMessageEvent(String messageName, String correlationId);

  /**
   * Finds a message start event by message name.
   *
   * @param messageName The name of the message.
   * @return An Optional containing the message start event, or empty if not found.
   */
  Optional<MessageEventStore> findMessageStartEvent(String messageName);

  /**
   * Finds a message event by message name and correlation ID.
   *
   * @param messageName The name of the message.
   * @param correlationId The correlation ID.
   * @return An Optional containing the message event, or empty if not found.
   */
  Optional<MessageEventStore> findMessageEvent(String messageName, String correlationId);

  /**
   * Finds a start message event by process definition ID.
   *
   * @param processDefinitionId The process definition ID.
   * @return An Optional containing the start message event, or empty if not found.
   */
  Optional<MessageEventStore> findStartMessageEvent(String processDefinitionId);

  /**
   * Deletes message events by linked event ID.
   *
   * @param linkedEventId The linked event ID.
   */
  void deleteByLinkedEventId(String linkedEventId);

  /**
   * Removes a message event.
   *
   * @param messageEventStore The message event to remove.
   */
  void remove(MessageEventStore messageEventStore);

  /**
   * Deletes start message events by process definition ID.
   *
   * @param definitionId The process definition ID.
   */
  void deleteStartEventByDefinitionId(String definitionId);

  /**
   * Deletes all message events registered for a specific process instance.
   *
   * @param processInstanceId The process instance ID.
   */
  void deleteByProcessInstanceId(String processInstanceId);
}
