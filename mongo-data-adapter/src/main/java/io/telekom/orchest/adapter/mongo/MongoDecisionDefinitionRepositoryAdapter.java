package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.DecisionDefinitionMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoDecisionDefinitionRepository;
import io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition;
import io.telekom.orchest.api.core.adapters.data.repository.DecisionDefinitionRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * MongoDB implementation of the {@link DecisionDefinitionRepository}. Adapts the Spring Data
 * MongoDB repository to the domain-agnostic repository interface. Handles storage, retrieval, and
 * versioning of DMN decision definitions.
 */
@Component
@RequiredArgsConstructor
public class MongoDecisionDefinitionRepositoryAdapter implements DecisionDefinitionRepository {

  private final MongoDecisionDefinitionRepository repository;
  private final DecisionDefinitionMapper mapper;
  private final MongoTemplate mongoTemplate;

  /**
   * Retrieves all decision definitions from the database.
   *
   * @return list of all decision definitions
   */
  @Override
  public List<DecisionDefinition> getDecisionDefinition() {
    return repository.findAllDecisions().stream().map(mapper::toDomain).toList();
  }

  /**
   * Finds all decision definitions matching the given definition ID across all versions.
   *
   * @param definitionId the decision definition identifier
   * @return list of matching decision definitions
   */
  @Override
  public List<DecisionDefinition> getById(String definitionId) {
    Query query = getFindQuery(definitionId, null);
    List<io.telekom.orchest.adapter.mongo.model.DecisionDefinition> decisionDefinitions =
        mongoTemplate.find(query, io.telekom.orchest.adapter.mongo.model.DecisionDefinition.class);
    return decisionDefinitions.stream().map(mapper::toDomain).toList();
  }

  /**
   * Finds the latest version of a decision definition by decision ID.
   *
   * @param decisionId the decision identifier
   * @return the latest decision definition if found, or empty
   */
  @Override
  public Optional<DecisionDefinition> getLatestById(String decisionId) {
    Query query = getFindQuery(decisionId, null).with(Sort.by(Sort.Order.desc("version")));
    List<io.telekom.orchest.adapter.mongo.model.DecisionDefinition> decisionDefinitions =
        mongoTemplate.find(query, io.telekom.orchest.adapter.mongo.model.DecisionDefinition.class);
    return decisionDefinitions.stream().map(mapper::toDomain).findFirst();
  }

  /**
   * Finds a decision definition by decision ID and version.
   *
   * @param decisionId the decision identifier
   * @param version the version number
   * @return the matching decision definition if found, or empty
   */
  @Override
  public Optional<DecisionDefinition> getByDecisionIdAndVersion(
      String decisionId, Integer version) {
    Query query = getFindQuery(decisionId, version).with(Sort.by(Sort.Order.desc("version")));
    List<io.telekom.orchest.adapter.mongo.model.DecisionDefinition> decisionDefinitions =
        mongoTemplate.find(query, io.telekom.orchest.adapter.mongo.model.DecisionDefinition.class);
    return decisionDefinitions.stream().map(mapper::toDomain).findFirst();
  }

  /**
   * Finds the latest version of a decision definition by its definition ID.
   *
   * @param definitionId the definition identifier
   * @return the latest decision definition if found, or empty
   */
  @Override
  public Optional<DecisionDefinition> getLatestByDefinitionId(String definitionId) {
    return repository.findFirstByDefinitionIdOrderByVersionDesc(definitionId).map(mapper::toDomain);
  }

  /**
   * Finds a decision definition by definition ID and version using a query on decisionIds field.
   *
   * @param definitionId the definition identifier
   * @param version the version number
   * @return the matching decision definition if found, or empty
   */
  @Override
  public Optional<DecisionDefinition> getByIdAndVersion(String definitionId, Integer version) {
    Query query = getFindQuery(definitionId, version);
    List<io.telekom.orchest.adapter.mongo.model.DecisionDefinition> decisionDefinitions =
        mongoTemplate.find(query, io.telekom.orchest.adapter.mongo.model.DecisionDefinition.class);
    return decisionDefinitions.stream().map(mapper::toDomain).findFirst();
  }

  /**
   * Finds a decision definition by its definition ID and version using the Spring Data repository.
   *
   * @param definitionId the definition identifier
   * @param version the version number
   * @return the matching decision definition if found, or empty
   */
  @Override
  public Optional<DecisionDefinition> getByDefinitionIdAndVersion(
      String definitionId, Integer version) {
    return repository.findByDefinitionIdAndVersion(definitionId, version).map(mapper::toDomain);
  }

  /**
   * Persists a decision definition to the database.
   *
   * @param processDefinition the decision definition to save
   * @return the persisted decision definition
   */
  @Override
  public DecisionDefinition save(DecisionDefinition processDefinition) {
    return mapper.toDomain(repository.save(mapper.toDocument(processDefinition)));
  }

  /**
   * Retrieves all decision definitions with pagination, projecting only definitionId, version, and
   * decisionIds fields.
   *
   * @param page the zero-based page index
   * @param size the page size
   * @return list of decision definitions for the requested page
   */
  @Override
  public List<DecisionDefinition> findAllDefinitions(int page, int size) {
    Query query = new Query().with(PageRequest.of(page, size));
    query.fields().include("definitionId", "version", "decisionIds");
    return mongoTemplate.find(query, DecisionDefinition.class);
  }

  /**
   * Deletes a decision definition by its definition ID and version.
   *
   * @param definitionId the definition identifier
   * @param version the version number
   * @return true if a record was deleted, false otherwise
   */
  @Override
  public boolean deleteByDefinitionIdAndVersion(String definitionId, Integer version) {
    Long deletedCount = repository.deleteByDefinitionIdAndVersion(definitionId, version);
    return deletedCount > 0;
  }

  private Query getFindQuery(String definitionId, Integer version) {
    Query query = new Query();
    if (version != null) {
      query.addCriteria(Criteria.where("decisionIds").is(definitionId).and("version").is(version));
    } else {
      query.addCriteria(Criteria.where("decisionIds").is(definitionId));
    }
    return query;
  }
}
