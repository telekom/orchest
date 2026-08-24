package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.WorkerRegistry;
import java.util.List;
import java.util.Optional;

/** Repository for persisting and querying {@link WorkerRegistry} documents. */
public interface WorkerRegistryRepository {

  /**
   * Finds all registered workers for a given process definition.
   *
   * @param processDefinitionId the process definition identifier
   * @return list of matching worker registries
   */
  List<WorkerRegistry> findAllByProcessDefinitionId(String processDefinitionId);

  /**
   * Finds a worker registry entry by its namespace.
   *
   * @param nameSpace the worker namespace
   * @return an Optional containing the worker registry, or empty if not found
   */
  Optional<WorkerRegistry> findByNameSpace(String nameSpace);

  /**
   * Saves a worker registry entry.
   *
   * @param workerRegistry the worker registry to save
   * @return the saved worker registry
   */
  WorkerRegistry save(WorkerRegistry workerRegistry);
}
