package io.telekom.orchest.alerting.service;

import io.telekom.orchest.alerting.api.RaiseAlertRequest;
import io.telekom.orchest.alerting.api.exception.AlertNotFoundException;
import io.telekom.orchest.alerting.api.exception.AlertTransitionException;
import io.telekom.orchest.api.core.adapters.data.model.Alert;
import io.telekom.orchest.api.core.adapters.data.model.AlertRecipients;
import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import io.telekom.orchest.api.core.adapters.data.repository.AlertRepository;
import io.telekom.orchest.api.core.adapters.data.repository.AlertRepository.AlertPage;
import io.telekom.orchest.api.core.adapters.data.repository.AlertRepository.AlertStats;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * Owns raise (SPI) and lifecycle transitions (REST). Does not send notifications — that is the
 * scheduler's job while state is {@link AlertState#FIRING}.
 */
@RequiredArgsConstructor
public class AlertLifecycleService {

  private final AlertRepository alertRepository;
  private final Clock clock;

  /**
   * Raises a new alert or re-raises an existing one by fingerprint (upsert). Re-raising increments
   * count and reopens RESOLVED alerts to FIRING.
   *
   * @param request the raise request containing source, alertKey, subject, body, and metadata
   * @return the created or updated alert entity
   * @throws IllegalArgumentException if required fields are missing
   */
  public Alert raise(RaiseAlertRequest request) {
    Objects.requireNonNull(request, "request");
    if (!StringUtils.hasText(request.getSource()) || !StringUtils.hasText(request.getAlertKey())) {
      throw new IllegalArgumentException("source and alertKey are required");
    }
    if (!StringUtils.hasText(request.getSubject()) || !StringUtils.hasText(request.getBody())) {
      throw new IllegalArgumentException("subject and body are required");
    }

    Instant now = clock.instant();
    String fingerprint = Alert.fingerprintOf(request.getSource(), request.getAlertKey());

    Alert alert =
        alertRepository
            .findByFingerprint(fingerprint)
            .map(existing -> applyRaise(existing, request, now))
            .orElseGet(() -> newAlert(request, fingerprint, now));

    return alertRepository.save(alert);
  }

  /**
   * Fetches an alert by ID or throws if not found.
   *
   * @param id the alert identifier
   * @return the alert entity
   * @throws AlertNotFoundException if no alert exists with the given ID
   */
  public Alert getRequired(String id) {
    return alertRepository.findById(id).orElseThrow(() -> new AlertNotFoundException(id));
  }

  /**
   * Lists alerts with optional filtering and pagination.
   *
   * @param state optional state filter
   * @param processDefinitionId optional process definition filter
   * @param createdFrom optional inclusive lower bound for creation time
   * @param createdTo optional inclusive upper bound for creation time
   * @param page zero-based page index
   * @param size page size
   * @param sort sort expression (e.g. "-count")
   * @return paginated alert results
   */
  public AlertPage list(
      AlertState state,
      String processDefinitionId,
      Instant createdFrom,
      Instant createdTo,
      int page,
      int size,
      String sort) {
    return alertRepository.findAll(
        state, processDefinitionId, createdFrom, createdTo, page, size, sort);
  }

  /**
   * Lists alerts matching any of the given states with pagination.
   *
   * @param states the set of states to include
   * @param page zero-based page index
   * @param size page size
   * @return paginated alert results
   */
  public AlertPage listByStates(Collection<AlertState> states, int page, int size) {
    return alertRepository.findByStates(states, page, size);
  }

  /**
   * Returns aggregated statistics for all FIRING alerts grouped by process definition and version.
   *
   * @return list of firing alert statistics
   */
  public List<AlertStats> firingStats() {
    return alertRepository.firingStats();
  }

  /**
   * Transitions an alert to ACKNOWLEDGED state. Idempotent if already acknowledged.
   *
   * @param id the alert identifier
   * @param actor identity of the acknowledging user
   * @return the updated alert
   * @throws AlertNotFoundException if the alert does not exist
   * @throws AlertTransitionException if the alert is RESOLVED
   */
  public Alert acknowledge(String id, String actor) {
    Alert alert = getRequired(id);
    requireNotResolved(alert, "acknowledge");
    if (alert.getState() == AlertState.ACKNOWLEDGED) {
      return alert;
    }
    Instant now = clock.instant();
    alert.setState(AlertState.ACKNOWLEDGED);
    alert.setAcknowledgedAt(now);
    alert.setAcknowledgedBy(actor);
    alert.setUpdatedAt(now);
    return alertRepository.save(alert);
  }

  /**
   * Transitions an alert to SILENCED state, suppressing further notifications. Idempotent if
   * already silenced.
   *
   * @param id the alert identifier
   * @param actor identity of the silencing user
   * @return the updated alert
   * @throws AlertNotFoundException if the alert does not exist
   * @throws AlertTransitionException if the alert is RESOLVED
   */
  public Alert silence(String id, String actor) {
    Alert alert = getRequired(id);
    requireNotResolved(alert, "silence");
    if (alert.getState() == AlertState.SILENCED) {
      return alert;
    }
    Instant now = clock.instant();
    alert.setState(AlertState.SILENCED);
    alert.setSilencedAt(now);
    alert.setSilencedBy(actor);
    alert.setUpdatedAt(now);
    return alertRepository.save(alert);
  }

  /**
   * Returns a SILENCED alert back to FIRING state, resuming notifications.
   *
   * @param id the alert identifier
   * @return the updated alert
   * @throws AlertNotFoundException if the alert does not exist
   * @throws AlertTransitionException if the alert is not in SILENCED state
   */
  public Alert unmute(String id) {
    Alert alert = getRequired(id);
    if (alert.getState() != AlertState.SILENCED) {
      throw new AlertTransitionException(
          "Cannot unmute alert in state " + alert.getState() + "; expected SILENCED");
    }
    Instant now = clock.instant();
    alert.setState(AlertState.FIRING);
    alert.setSilencedAt(null);
    alert.setSilencedBy(null);
    alert.setUpdatedAt(now);
    return alertRepository.save(alert);
  }

  /**
   * Marks an alert as RESOLVED. No further notifications will be sent.
   *
   * @param id the alert identifier
   * @return the updated alert
   * @throws AlertNotFoundException if the alert does not exist
   * @throws AlertTransitionException if the alert is already RESOLVED
   */
  public Alert resolve(String id) {
    Alert alert = getRequired(id);
    if (alert.getState() == AlertState.RESOLVED) {
      throw new AlertTransitionException("Alert is already RESOLVED");
    }
    Instant now = clock.instant();
    alert.setState(AlertState.RESOLVED);
    alert.setResolvedAt(now);
    alert.setUpdatedAt(now);
    return alertRepository.save(alert);
  }

  /**
   * Updates {@code lastTriggeredAt} to the current time after a notification is sent.
   *
   * @param alert the alert to update
   * @return the persisted alert with updated timestamp
   */
  public Alert markTriggered(Alert alert) {
    Instant now = clock.instant();
    alert.setLastTriggeredAt(now);
    alert.setUpdatedAt(now);
    return alertRepository.save(alert);
  }

  private static void requireNotResolved(Alert alert, String action) {
    if (alert.getState() == AlertState.RESOLVED) {
      throw new AlertTransitionException("Cannot " + action + " a RESOLVED alert");
    }
  }

  private static Alert newAlert(RaiseAlertRequest request, String fingerprint, Instant now) {
    return Alert.builder()
        .source(request.getSource())
        .alertKey(request.getAlertKey())
        .fingerprint(fingerprint)
        .state(AlertState.FIRING)
        .count(1)
        .severity(request.getSeverity())
        .metadata(copyMetadata(request))
        .subject(request.getSubject())
        .body(request.getBody())
        .recipients(
            request.getRecipients() != null ? request.getRecipients() : new AlertRecipients())
        .resendIntervalMs(request.getResendIntervalMs())
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  private static Alert applyRaise(Alert existing, RaiseAlertRequest request, Instant now) {
    existing.setCount(existing.getCount() + 1);
    existing.setSubject(request.getSubject());
    existing.setBody(request.getBody());
    if (request.getRecipients() != null) {
      existing.setRecipients(request.getRecipients());
    }
    if (request.getSeverity() != null) {
      existing.setSeverity(request.getSeverity());
    }
    if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
      if (existing.getMetadata() == null) {
        existing.setMetadata(new HashMap<>());
      }
      existing.getMetadata().putAll(request.getMetadata());
    }
    if (request.getResendIntervalMs() != null) {
      existing.setResendIntervalMs(request.getResendIntervalMs());
    }
    if (existing.getState() == AlertState.RESOLVED) {
      existing.setState(AlertState.FIRING);
      existing.setResolvedAt(null);
      existing.setAcknowledgedAt(null);
      existing.setAcknowledgedBy(null);
      existing.setSilencedAt(null);
      existing.setSilencedBy(null);
      existing.setLastTriggeredAt(null);
    }
    existing.setUpdatedAt(now);
    return existing;
  }

  private static HashMap<String, String> copyMetadata(RaiseAlertRequest request) {
    return request.getMetadata() != null ? new HashMap<>(request.getMetadata()) : new HashMap<>();
  }
}
