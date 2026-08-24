package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.ProcessEnvVariables;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link ProcessEnvVariables} documents. Manages environment
 * variable configurations associated with process definitions.
 */
@Repository
public interface MongoProcessEnvVariablesRepository
    extends MongoRepository<ProcessEnvVariables, String> {

  /**
   * Finds all environment variable sets for a given process definition.
   *
   * @param processDefinitionId the process definition identifier
   * @return list of environment variable documents for the definition
   */
  List<ProcessEnvVariables> findAllByProcessDefinitionId(String processDefinitionId);
}
