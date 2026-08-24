package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.SignalEventMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoSignalEventRepository;
import io.telekom.orchest.api.core.adapters.data.model.SignalEvent;
import io.telekom.orchest.api.core.adapters.data.repository.SignalEventRepository;
import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * MongoDB implementation of the {@link SignalEventRepository}. Adapts the Spring Data MongoDB
 * repository to the domain-agnostic repository interface. Manages registration and retrieval of
 * signal event subscriptions.
 */
@Repository
@RequiredArgsConstructor
public class MongoSignalEventRepositoryAdapter implements SignalEventRepository {

  private final MongoSignalEventRepository repository;
  private final SignalEventMapper mapper;

  /**
   * Persists a signal event subscription and stamps the generated ID back onto the domain object.
   *
   * @param signalEvent the signal event to register
   * @return the registered signal event with its persisted ID
   */
  @Override
  public SignalEvent registerEvent(SignalEvent signalEvent) {
    io.telekom.orchest.adapter.mongo.model.SignalEvent save =
        repository.save(mapper.toDocument(signalEvent));
    signalEvent.setId(save.getId());
    return signalEvent;
  }

  /**
   * Finds the signal start event subscription for a given process definition.
   *
   * @param processDefinitionId the process definition ID
   * @return the signal start event, or empty if none exists
   */
  @Override
  public Optional<SignalEvent> findSignalStartEvent(String processDefinitionId) {
    return repository
        .findSignalEventByProcessDefinitionIdAndIsStartEvent(processDefinitionId, true)
        .map(mapper::toDomain);
  }

  /**
   * Finds all registered (not yet consumed) signal event subscriptions by signal name.
   *
   * @param signalName the signal name to match
   * @return list of matching registered signal events
   */
  @Override
  public List<SignalEvent> findAllRegisteredSignalsEvent(String signalName) {
    return repository
        .findAllBySignalNameAndState(signalName, IntermediateEventState.REGISTERED)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Deletes all signal events linked to the given event ID.
   *
   * @param linkedEventId the linked event ID whose subscriptions should be removed
   */
  @Override
  public void deleteByLinkedEventId(String linkedEventId) {
    repository.deleteByLinkedEventId(linkedEventId);
  }

  /**
   * Removes a specific signal event subscription.
   *
   * @param timedEvent the signal event to remove
   */
  @Override
  public void remove(SignalEvent timedEvent) {
    repository.deleteById(timedEvent.getId());
  }

  /**
   * Deletes the signal start event subscription for a given process definition.
   *
   * @param definitionId the process definition ID whose start event should be removed
   */
  @Override
  public void deleteStartEventByDefinitionId(String definitionId) {
    repository.deleteSignalEventByProcessDefinitionIdAndIsStartEvent(definitionId, true);
  }

  /**
   * Deletes all signal event subscriptions for a given process instance.
   *
   * @param processInstanceId the process instance ID whose events should be removed
   */
  @Override
  public void deleteByProcessInstanceId(String processInstanceId) {
    repository.deleteByProcessInstanceId(processInstanceId);
  }
}
