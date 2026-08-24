package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.DynamicProcessDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link DynamicProcessDefinition} documents. Provides custom
 * query methods for finding definitions by ID and version.
 */
@Repository
public interface MongoDynamicProcessDefinitionRepository
    extends MongoRepository<DynamicProcessDefinition, String> {

  /**
   * Retrieves the dynamic process definition associated with a running process instance.
   *
   * @param processInstanceId the process instance identifier
   * @return the matching dynamic definition, or {@code null} if not found
   */
  DynamicProcessDefinition getDynamicProcessDefinitionByProcessInstanceId(String processInstanceId);
}
