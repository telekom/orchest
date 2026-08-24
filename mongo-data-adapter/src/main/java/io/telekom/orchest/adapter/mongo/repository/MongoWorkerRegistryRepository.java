package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.WorkerRegistry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link WorkerRegistry} documents. Tracks registered worker
 * namespaces and their capabilities.
 */
@Repository
public interface MongoWorkerRegistryRepository extends MongoRepository<WorkerRegistry, String> {

  /**
   * Finds all worker registrations for a given process definition.
   *
   * @param processDefinitionId the process definition identifier
   * @return list of worker registrations associated with the definition
   */
  List<WorkerRegistry> findAllByProcessDefinitionId(String processDefinitionId);

  /**
   * Finds a worker registration by its namespace.
   *
   * @param nameSpace the worker namespace identifier
   * @return the matching worker registry entry, or empty if not found
   */
  Optional<WorkerRegistry> findByNameSpace(String nameSpace);
}
