package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.ProcessInstanceMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoProcessInstanceRepository;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessInstanceRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * MongoDB implementation of the {@link ProcessInstanceRepository}. Adapts the Spring Data MongoDB
 * repository to the domain-agnostic repository interface. Handles storage and retrieval of process
 * instances, including hydration of linked process definitions.
 */
@Repository
@RequiredArgsConstructor
public class MongoProcessInstanceRepositoryAdapter implements ProcessInstanceRepository {

  private final MongoProcessInstanceRepository processInstanceRepository;
  private final MongoProcessDefinitionRepositoryAdapter mongoProcessDefinitionRepositoryAdapter;
  private final MongoDynamicProcessDefinitionRepositoryAdapter
      mongoDynamicProcessDefinitionRepositoryAdapter;
  private final MongoTemplate mongoTemplate;

  private final ProcessInstanceMapper processInstanceMapper;

  /**
   * Returns all process instances.
   *
   * @return list of all process instances
   */
  @Override
  public List<ProcessInstance> getProcessInstances() {
    return processInstanceRepository.findAll().stream()
        .map(processInstanceMapper::toDomain)
        .toList();
  }

  /**
   * Returns all process instances with a custom field projection.
   *
   * @param fields the fields to include in the projection
   * @return list of process instances with projected fields
   */
  @Override
  public List<ProcessInstance> getProcessInstances(List<String> fields) {
    Query query = new Query();
    if (fields != null) {
      fields.forEach(field -> query.fields().include(field));
    }
    return mongoTemplate.find(query, ProcessInstance.class);
  }

  /**
   * Returns a process instance by ID, hydrating its linked process definition.
   *
   * @param processInstanceId the process instance ID
   * @return the process instance with its definition attached, or empty if not found
   */
  @Override
  public Optional<ProcessInstance> getById(String processInstanceId) {
    return processInstanceRepository
        .findByProcessInstanceId(processInstanceId)
        .map(processInstanceMapper::toDomain)
        .map(
            processInstance -> {
              if (processInstance.isDynamicFlow()) {
                processInstance.setProcessDefinition(
                    mongoDynamicProcessDefinitionRepositoryAdapter.getByProcessInstanceId(
                        processInstanceId));
                return processInstance;
              } else {
                mongoProcessDefinitionRepositoryAdapter
                    .getByIdAndVersion(
                        processInstance.getProcessDefinitionId(), processInstance.getVersion())
                    .map(
                        processDefinition -> {
                          processInstance.setProcessDefinition(processDefinition);
                          return processInstance;
                        });
              }

              return processInstance;
            });
  }

  /**
   * Returns a process instance by ID with a custom field projection.
   *
   * @param processInstanceId the process instance ID
   * @param fields the fields to include in the projection
   * @return the matching process instance, or empty if not found
   */
  @Override
  public Optional<ProcessInstance> getById(String processInstanceId, List<String> fields) {
    Query query = new Query(Criteria.where("processInstanceId").is(processInstanceId));
    if (fields != null) {
      fields.forEach(field -> query.fields().include(field));
    }
    return Optional.ofNullable(mongoTemplate.findOne(query, ProcessInstance.class));
  }

  /**
   * Persists a process instance.
   *
   * @param processInstance the process instance to save
   * @return the saved process instance
   */
  @Override
  public ProcessInstance save(ProcessInstance processInstance) {
    io.telekom.orchest.adapter.mongo.model.ProcessInstance saved =
        processInstanceRepository.save(processInstanceMapper.toDocument(processInstance));
    return processInstanceMapper.toDomain(saved);
  }

  /**
   * Checks whether any active child process instances exist for the given parent.
   *
   * @param parentProcessInstanceId the parent process instance ID
   * @return true if at least one active child instance exists
   */
  @Override
  public boolean hasActiveChildInstances(String parentProcessInstanceId) {
    return !processInstanceRepository.hasActiveChildInstances(parentProcessInstanceId).isEmpty();
  }
}
