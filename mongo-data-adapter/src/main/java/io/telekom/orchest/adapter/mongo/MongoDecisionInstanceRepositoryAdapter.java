package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.DecisionInstanceMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoDecisionInstanceRepository;
import io.telekom.orchest.api.core.adapters.data.model.DecisionInstance;
import io.telekom.orchest.api.core.adapters.data.repository.DecisionInstanceRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * MongoDB implementation of the {@link DecisionInstanceRepository}. Adapts the Spring Data MongoDB
 * repository to the domain-agnostic repository interface. Handles storage and retrieval of DMN
 * decision instances.
 */
@Component
@RequiredArgsConstructor
public class MongoDecisionInstanceRepositoryAdapter implements DecisionInstanceRepository {

  private final MongoDecisionInstanceRepository decisionInstanceRepository;
  private final DecisionInstanceMapper mapper;
  private final MongoTemplate mongoTemplate;

  /**
   * Retrieves all decision instances from the database.
   *
   * @return list of all decision instances
   */
  @Override
  public List<DecisionInstance> getDecisionInstances() {
    return decisionInstanceRepository.findAll().stream().map(mapper::toDomain).toList();
  }

  /**
   * Retrieves all decision instances, projecting only the specified fields.
   *
   * @param fields the list of field names to include in the projection
   * @return list of decision instances with only the requested fields populated
   */
  @Override
  public List<DecisionInstance> getDecisionInstances(List<String> fields) {
    Query query = new Query();
    if (fields != null) {
      fields.forEach(field -> query.fields().include(field));
    }
    return mongoTemplate.find(query, DecisionInstance.class);
  }

  /**
   * Finds a decision instance by its unique identifier.
   *
   * @param decisionInstanceId the decision instance identifier
   * @return the decision instance if found, or empty
   */
  @Override
  public Optional<DecisionInstance> getById(String decisionInstanceId) {
    return decisionInstanceRepository
        .findByDecisionInstanceId(decisionInstanceId)
        .map(mapper::toDomain);
  }

  /**
   * Persists a decision instance to the database.
   *
   * @param decisionInstance the decision instance to save
   * @return the persisted decision instance
   */
  @Override
  public DecisionInstance save(DecisionInstance decisionInstance) {
    return mapper.toDomain(decisionInstanceRepository.save(mapper.toDocument(decisionInstance)));
  }
}
