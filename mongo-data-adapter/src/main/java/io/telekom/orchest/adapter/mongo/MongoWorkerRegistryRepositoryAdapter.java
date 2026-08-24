package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.WorkerRegistryMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoWorkerRegistryRepository;
import io.telekom.orchest.api.core.adapters.data.model.WorkerRegistry;
import io.telekom.orchest.api.core.adapters.data.repository.WorkerRegistryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * MongoDB implementation of the {@link WorkerRegistryRepository}. Adapts the Spring Data MongoDB
 * repository to the domain-agnostic repository interface. Manages storage of registered workers.
 */
@Repository
@RequiredArgsConstructor
public class MongoWorkerRegistryRepositoryAdapter implements WorkerRegistryRepository {

  private final MongoWorkerRegistryRepository repository;
  private final WorkerRegistryMapper mapper;

  /**
   * Finds all registered workers for a given process definition.
   *
   * @param processDefinitionId the process definition ID
   * @return list of matching worker registrations
   */
  @Override
  public List<WorkerRegistry> findAllByProcessDefinitionId(String processDefinitionId) {
    return repository.findAllByProcessDefinitionId(processDefinitionId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Finds a worker registration by its namespace.
   *
   * @param namespace the worker namespace
   * @return the matching worker registration, or empty if not found
   */
  @Override
  public Optional<WorkerRegistry> findByNameSpace(String namespace) {
    return repository.findByNameSpace(namespace).map(mapper::toDomain);
  }

  /**
   * Persists a worker registration.
   *
   * @param workerRegistry the worker registration to save
   * @return the saved worker registration
   */
  @Override
  public WorkerRegistry save(WorkerRegistry workerRegistry) {
    return mapper.toDomain(repository.save(mapper.toDocument(workerRegistry)));
  }
}
