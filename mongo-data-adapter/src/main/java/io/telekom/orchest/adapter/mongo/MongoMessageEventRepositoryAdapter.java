package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.MessageEventMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoMessageEventRepository;
import io.telekom.orchest.api.core.adapters.data.model.MessageEventStore;
import io.telekom.orchest.api.core.adapters.data.repository.MessageEventRepository;
import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * MongoDB implementation of the {@link MessageEventRepository}. Adapts the Spring Data MongoDB
 * repository to the domain-agnostic repository interface. Manages message event subscriptions
 * (start events and intermediate catch events).
 */
@Repository
@RequiredArgsConstructor
public class MongoMessageEventRepositoryAdapter implements MessageEventRepository {

  private final MongoMessageEventRepository repository;
  private final MessageEventMapper mapper;

  /**
   * Persists a message event subscription.
   *
   * @param timedEvent the message event to register
   * @return the persisted message event
   */
  @Override
  public MessageEventStore registerEvent(MessageEventStore timedEvent) {
    return mapper.toDomain(repository.save(mapper.toDocument(timedEvent)));
  }

  /**
   * Finds all registered (not yet consumed) message events matching the given name and correlation
   * key.
   *
   * @param messageName the message name to match
   * @param correlationId the correlation key to match
   * @return list of matching registered message events
   */
  @Override
  public List<MessageEventStore> findRegisteredMessageEvent(
      String messageName, String correlationId) {
    return repository
        .findAllByMessageNameAndCorrelationKeyAndState(
            messageName, correlationId, IntermediateEventState.REGISTERED)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Finds a message start event subscription by message name.
   *
   * @param messageName the message name to match
   * @return the matching start event, or empty if none exists
   */
  @Override
  public Optional<MessageEventStore> findMessageStartEvent(String messageName) {
    return repository.findByMessageNameAndIsStartEvent(messageName, true).map(mapper::toDomain);
  }

  /**
   * Finds a message event by name and optional correlation key. Falls back to start event lookup
   * when correlation key is null.
   *
   * @param messageName the message name to match
   * @param correlationId the correlation key, or null for start events
   * @return the matching message event, or empty if none found
   */
  @Override
  public Optional<MessageEventStore> findMessageEvent(String messageName, String correlationId) {
    if (correlationId == null) {
      return findMessageStartEvent(messageName);
    }
    return findRegisteredMessageEvent(messageName, correlationId).stream().findFirst();
  }

  /**
   * Finds a message start event subscription by process definition ID.
   *
   * @param processDefinitionId the process definition ID to match
   * @return the matching start event, or empty if none exists
   */
  @Override
  public Optional<MessageEventStore> findStartMessageEvent(String processDefinitionId) {
    return repository
        .findMessageEventStoreByProcessDefinitionIdAndIsStartEvent(processDefinitionId, true)
        .map(mapper::toDomain);
  }

  /**
   * Deletes all message events linked to the given event ID.
   *
   * @param linkedEventId the linked event ID whose subscriptions should be removed
   */
  @Override
  public void deleteByLinkedEventId(String linkedEventId) {
    repository.deleteByLinkedEventId(linkedEventId);
  }

  /**
   * Removes a specific message event subscription.
   *
   * @param messageEventStore the message event to remove
   */
  @Override
  public void remove(MessageEventStore messageEventStore) {
    repository.delete(mapper.toDocument(messageEventStore));
  }

  /**
   * Deletes the message start event subscription for a given process definition.
   *
   * @param definitionId the process definition ID whose start event should be removed
   */
  @Override
  public void deleteStartEventByDefinitionId(String definitionId) {
    repository.deleteByProcessDefinitionIdAndIsStartEvent(definitionId, true);
  }

  /**
   * Deletes all message event subscriptions for a given process instance.
   *
   * @param processInstanceId the process instance ID whose events should be removed
   */
  @Override
  public void deleteByProcessInstanceId(String processInstanceId) {
    repository.deleteByProcessInstanceId(processInstanceId);
  }
}
