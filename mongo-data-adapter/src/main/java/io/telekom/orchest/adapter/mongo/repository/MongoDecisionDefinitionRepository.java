package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.DecisionDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link DecisionDefinition} documents. Provides custom query
 * methods for finding DMN definitions by ID and version.
 */
@Repository
public interface MongoDecisionDefinitionRepository
    extends MongoRepository<DecisionDefinition, String> {

  /**
   * Finds all versions of a decision definition.
   *
   * @param definitionId the decision definition identifier
   * @return all versions of the given definition
   */
  List<DecisionDefinition> findAllByDefinitionId(String definitionId);

  /**
   * Finds the latest (highest version) of a decision definition.
   *
   * @param definitionId the decision definition identifier
   * @return the latest version, or empty if none exists
   */
  Optional<DecisionDefinition> findFirstByDefinitionIdOrderByVersionDesc(String definitionId);

  /**
   * Finds a specific version of a decision definition.
   *
   * @param definitionId the decision definition identifier
   * @param version the version number
   * @return the matching definition, or empty if not found
   */
  Optional<DecisionDefinition> findByDefinitionIdAndVersion(String definitionId, Integer version);

  /**
   * Returns a lightweight projection of all decision definitions containing only definitionId,
   * version, and decisionIds fields.
   *
   * @return all decisions with minimal fields
   */
  @Query(value = "{}", fields = "{definitionId: 1, version: 1, decisionIds:  1}")
  List<DecisionDefinition> findAllDecisions();

  /**
   * Returns distinct decision definition IDs using an aggregation pipeline.
   *
   * @return list of definitions with only the definitionId field populated
   */
  @Aggregation(
      pipeline = {
        "{ '$group': { '_id': '$definitionId' }}",
        "{ '$project': { 'definitionId': '$_id' } }"
      })
  List<DecisionDefinition> findDistinctDecisionIds();

  /**
   * Deletes a specific version of a decision definition.
   *
   * @param definitionId the decision definition identifier
   * @param version the version number to delete
   * @return the number of documents deleted
   */
  Long deleteByDefinitionIdAndVersion(String definitionId, Integer version);
}
