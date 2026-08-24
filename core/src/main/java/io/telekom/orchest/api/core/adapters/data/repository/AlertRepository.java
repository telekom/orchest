package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.Alert;
import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Persistence SPI for alerts. Mongo implementation lives in {@code mongo-data-adapter}. */
public interface AlertRepository {

  /**
   * Saves an alert.
   *
   * @param alert the alert to save
   * @return the saved alert
   */
  Alert save(Alert alert);

  /**
   * Finds an alert by its unique ID.
   *
   * @param id the alert ID
   * @return an Optional containing the alert, or empty if not found
   */
  Optional<Alert> findById(String id);

  /**
   * Finds an alert by its fingerprint (deduplication key).
   *
   * @param fingerprint the alert fingerprint
   * @return an Optional containing the alert, or empty if not found
   */
  Optional<Alert> findByFingerprint(String fingerprint);

  /**
   * Retrieves all alerts.
   *
   * @return list of all alerts
   */
  List<Alert> findAll();

  /**
   * Finds all alerts in a given state.
   *
   * @param state the alert state to filter by
   * @return list of matching alerts
   */
  List<Alert> findByState(AlertState state);

  /**
   * Finds alerts matching any of the given states with pagination.
   *
   * @param states the collection of alert states to filter by
   * @param page the page number (0-indexed)
   * @param size the page size
   * @return a paginated result of matching alerts
   */
  AlertPage findByStates(Collection<AlertState> states, int page, int size);

  /**
   * Finds alerts with filtering, pagination, and sorting.
   *
   * @param state optional alert state filter
   * @param processDefinitionId optional process definition ID filter
   * @param createdFrom optional lower bound for creation time
   * @param createdTo optional upper bound for creation time
   * @param page the page number (0-indexed)
   * @param size the page size
   * @param sort the sort field and direction
   * @return a paginated result of matching alerts
   */
  AlertPage findAll(
      AlertState state,
      String processDefinitionId,
      Instant createdFrom,
      Instant createdTo,
      int page,
      int size,
      String sort);

  /**
   * Returns aggregated statistics for currently firing alerts grouped by process definition.
   *
   * @return list of alert statistics per process definition
   */
  List<AlertStats> firingStats();

  record AlertPage(List<Alert> content, long totalElements, int totalPages, int page, int size) {}

  record AlertStats(String processDefinitionId, String version, long totalCount) {}
}
