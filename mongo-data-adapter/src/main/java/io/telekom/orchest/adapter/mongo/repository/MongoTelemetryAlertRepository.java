package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.Alert;
import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link Alert} documents. Manages telemetry alert records and
 * their lifecycle states.
 */
@Repository
public interface MongoTelemetryAlertRepository extends MongoRepository<Alert, String> {

  /**
   * Finds an alert by its unique fingerprint for deduplication.
   *
   * @param fingerprint the alert fingerprint hash
   * @return the matching alert, or empty if not found
   */
  Optional<Alert> findByFingerprint(String fingerprint);

  /**
   * Finds all alerts in a given state.
   *
   * @param state the alert state to filter by
   * @return list of alerts matching the state
   */
  List<Alert> findAllByState(AlertState state);
}
