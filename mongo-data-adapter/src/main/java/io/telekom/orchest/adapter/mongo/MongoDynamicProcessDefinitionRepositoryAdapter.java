package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.DynamicProcessDefinitionMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoDynamicProcessDefinitionRepository;
import io.telekom.orchest.api.core.adapters.data.model.DynamicProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.repository.DynamicProcessDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * MongoDB implementation of the {@link DynamicProcessDefinitionRepository}. Adapts the Spring Data
 * MongoDB repository to the domain-agnostic repository interface.
 */
@Component
@RequiredArgsConstructor
public class MongoDynamicProcessDefinitionRepositoryAdapter
    implements DynamicProcessDefinitionRepository {

  private final MongoDynamicProcessDefinitionRepository repository;
  private final DynamicProcessDefinitionMapper mapper;

  /**
   * Persists a dynamic process definition to the database.
   *
   * @param processDefinition the dynamic process definition to save
   * @return the persisted dynamic process definition
   */
  @Override
  public DynamicProcessDefinition save(DynamicProcessDefinition processDefinition) {
    return mapper.toDomain(repository.save(mapper.toDocument(processDefinition)));
  }

  /**
   * Retrieves a dynamic process definition by its associated process instance ID.
   *
   * @param processInstanceId the process instance identifier
   * @return the matching dynamic process definition
   */
  @Override
  public DynamicProcessDefinition getByProcessInstanceId(String processInstanceId) {
    return mapper.toDomain(
        repository.getDynamicProcessDefinitionByProcessInstanceId(processInstanceId));
  }
}
