package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.MessageEventStore;
import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link MessageEventStore} documents. Handles querying message
 * subscriptions by name, correlation key, and state.
 */
@Repository
public interface MongoMessageEventRepository extends MongoRepository<MessageEventStore, String> {

  /**
   * Finds all message event subscriptions matching a name, correlation key, and state.
   *
   * @param messageName the message name
   * @param correlationKey the correlation key
   * @param state the subscription state
   * @return list of matching subscriptions
   */
  List<MessageEventStore> findAllByMessageNameAndCorrelationKeyAndState(
      String messageName, String correlationKey, IntermediateEventState state);

  /**
   * Finds a single message event subscription matching a name, correlation key, and state.
   *
   * @param messageName the message name
   * @param correlationKey the correlation key
   * @param state the subscription state
   * @return the matching subscription, or empty if not found
   */
  Optional<MessageEventStore> findByMessageNameAndCorrelationKeyAndState(
      String messageName, String correlationKey, IntermediateEventState state);

  /**
   * Finds a message event subscription by name and start-event flag.
   *
   * @param messageName the message name
   * @param isStartEvent whether the subscription is for a start event
   * @return the matching subscription, or empty if not found
   */
  Optional<MessageEventStore> findByMessageNameAndIsStartEvent(
      String messageName, Boolean isStartEvent);

  /**
   * Finds a start-event message subscription for a given process definition.
   *
   * @param processDefinitionId the process definition identifier
   * @param isStartEvent whether the subscription is for a start event
   * @return the matching subscription, or empty if not found
   */
  Optional<MessageEventStore> findMessageEventStoreByProcessDefinitionIdAndIsStartEvent(
      String processDefinitionId, Boolean isStartEvent);

  /**
   * Deletes a message event subscription by its linked event ID.
   *
   * @param linkedEventId the linked event identifier
   */
  void deleteByLinkedEventId(String linkedEventId);

  /**
   * Deletes start-event message subscriptions for a given process definition.
   *
   * @param processDefinitionId the process definition identifier
   * @param isStartEvent whether the subscription is for a start event
   */
  void deleteByProcessDefinitionIdAndIsStartEvent(String processDefinitionId, Boolean isStartEvent);

  /**
   * Deletes all message event subscriptions belonging to a process instance.
   *
   * @param processInstanceId the process instance identifier
   */
  void deleteByProcessInstanceId(String processInstanceId);
}
