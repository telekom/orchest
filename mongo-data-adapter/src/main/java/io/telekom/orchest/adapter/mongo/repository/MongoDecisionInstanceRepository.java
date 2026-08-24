package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.DecisionInstance;
import io.telekom.orchest.api.core.model.dmn.DIState;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link DecisionInstance} documents. Manages the storage and
 * retrieval of historical decision executions.
 */
@Repository
public interface MongoDecisionInstanceRepository extends MongoRepository<DecisionInstance, String> {

  /**
   * Finds a decision instance by its unique instance ID.
   *
   * @param decisionInstanceId the decision instance identifier
   * @return the matching instance, or empty if not found
   */
  Optional<DecisionInstance> findByDecisionInstanceId(String decisionInstanceId);

  /**
   * Finds all decision instances in a given state that were executed before the specified time.
   *
   * @param state the decision instance state to match
   * @param executedAt the cutoff time (exclusive upper bound)
   * @return list of matching decision instances
   */
  List<DecisionInstance> findAllByStateAndExecutedAtBefore(
      DIState state, OffsetDateTime executedAt);

  /**
   * Streams expired decision instances over a server-side cursor so callers can process them one at
   * a time without materializing the entire (potentially huge) result set on the heap. The returned
   * stream MUST be closed (use try-with-resources) to release the underlying cursor.
   */
  Stream<DecisionInstance> streamAllByStateAndExecutedAtBefore(
      DIState state, OffsetDateTime executedAt);

  /**
   * Deletes all decision instances in a given state that were executed before the specified time.
   *
   * @param state the decision instance state to match
   * @param executedAtAfter the cutoff time (exclusive upper bound)
   */
  void deleteAllByStateAndExecutedAtBefore(DIState state, OffsetDateTime executedAtAfter);
}
