package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.DecisionInstance;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing DMN decision instance executions. Provides methods to retrieve
 * and save decision instances.
 */
public interface DecisionInstanceRepository {

  /**
   * Retrieves all decision instances.
   *
   * @return A list of all decision instances.
   */
  List<DecisionInstance> getDecisionInstances();

  /**
   * Retrieves all decision instances.
   *
   * @param fields projection Ids.
   * @return A list of all decision instances.
   */
  List<DecisionInstance> getDecisionInstances(List<String> fields);

  /**
   * Retrieves a decision instance by its ID.
   *
   * @param decisionInstanceId The decision instance ID.
   * @return An Optional containing the decision instance, or empty if not found.
   */
  Optional<DecisionInstance> getById(String decisionInstanceId);

  /**
   * Saves a decision instance.
   *
   * @param decisionInstance The decision instance to save.
   * @return The saved decision instance.
   */
  DecisionInstance save(DecisionInstance decisionInstance);
}
