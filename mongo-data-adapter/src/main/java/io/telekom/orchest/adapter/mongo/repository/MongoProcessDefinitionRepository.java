package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.ProcessDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link ProcessDefinition} documents. Provides custom query
 * methods for finding definitions by ID and version.
 */
@Repository
public interface MongoProcessDefinitionRepository
    extends MongoRepository<ProcessDefinition, String> {

  /**
   * Finds all versions of a process definition by its definition ID.
   *
   * @param definitionId the process definition identifier
   * @return list of all matching process definitions
   */
  List<ProcessDefinition> findAllByDefinitionId(String definitionId);

  /**
   * Finds the latest version of a process definition.
   *
   * @param definitionId the process definition identifier
   * @return the highest-versioned definition, or empty if none exists
   */
  Optional<ProcessDefinition> findFirstByDefinitionIdOrderByVersionDesc(String definitionId);

  /**
   * Finds a specific version of a process definition.
   *
   * @param definitionId the process definition identifier
   * @param version the version number
   * @return the matching definition, or empty if not found
   */
  Optional<ProcessDefinition> findByDefinitionIdAndVersion(String definitionId, Integer version);

  /**
   * Returns a list of distinct process definition IDs using an aggregation pipeline.
   *
   * @return list of process definitions containing only distinct definition IDs
   */
  @Aggregation(
      pipeline = {
        "{ '$group': { '_id': '$definitionId' }}",
        "{ '$project': { 'definitionId': '$_id' } }"
      })
  List<ProcessDefinition> findDistinctProcessIds();

  /**
   * Deletes a specific version of a process definition.
   *
   * @param definitionId the process definition identifier
   * @param version the version number to delete
   * @return the number of documents deleted
   */
  Long deleteByDefinitionIdAndVersion(String definitionId, Integer version);
}
