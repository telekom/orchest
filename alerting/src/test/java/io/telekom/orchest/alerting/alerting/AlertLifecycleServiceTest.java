package io.telekom.orchest.alerting.alerting;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.alerting.api.RaiseAlertRequest;
import io.telekom.orchest.alerting.api.exception.AlertNotFoundException;
import io.telekom.orchest.alerting.api.exception.AlertTransitionException;
import io.telekom.orchest.alerting.service.AlertLifecycleService;
import io.telekom.orchest.api.core.adapters.data.model.Alert;
import io.telekom.orchest.api.core.adapters.data.model.AlertRecipients;
import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import io.telekom.orchest.api.core.adapters.data.repository.AlertRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link AlertLifecycleService} covering raise, state transitions, and due logic. */
class AlertLifecycleServiceTest {

  private static final Instant T0 = Instant.parse("2026-08-08T12:00:00Z");

  private InMemoryAlertRepository repository;
  private MutableClock clock;
  private AlertLifecycleService service;

  @BeforeEach
  void setUp() {
    repository = new InMemoryAlertRepository();
    clock = new MutableClock(T0);
    service = new AlertLifecycleService(repository, clock);
  }

  @Test
  @DisplayName("raise creates FIRING alert with count 1 and fingerprint")
  void raiseCreates() {
    Alert alert = service.raise(baseRequest());

    assertEquals("engine::disk-full", alert.getFingerprint());
    assertEquals(AlertState.FIRING, alert.getState());
    assertEquals(1, alert.getCount());
    assertEquals("Disk full", alert.getSubject());
    assertNull(alert.getLastTriggeredAt());
    assertTrue(alert.isDue(T0, 300_000));
  }

  @Test
  @DisplayName("re-raise upserts by fingerprint and increments count")
  void raiseIncrementsCount() {
    service.raise(baseRequest());
    Alert second = service.raise(baseRequest().toBuilder().body("Disk still full").build());

    assertEquals(1, repository.store.size());
    assertEquals(2, second.getCount());
    assertEquals("Disk still full", second.getBody());
    assertEquals(AlertState.FIRING, second.getState());
  }

  @Test
  @DisplayName("re-raise on RESOLVED reopens to FIRING and clears resolution")
  void raiseReopensResolved() {
    Alert created = service.raise(baseRequest());
    service.resolve(created.getId());

    Alert reopened = service.raise(baseRequest());

    assertEquals(AlertState.FIRING, reopened.getState());
    assertEquals(2, reopened.getCount());
    assertNull(reopened.getResolvedAt());
    assertNull(reopened.getLastTriggeredAt());
  }

  @Test
  @DisplayName("acknowledge and silence stop due notifications; unmute restores FIRING")
  void lifecycleTransitions() {
    Alert created = service.raise(baseRequest());
    assertFalse(service.acknowledge(created.getId(), "ops@example.com").isDue(T0, 300_000));

    Alert silenced = service.silence(created.getId(), "ops@example.com");
    assertEquals(AlertState.SILENCED, silenced.getState());

    Alert unmuted = service.unmute(created.getId());
    assertEquals(AlertState.FIRING, unmuted.getState());
    assertNull(unmuted.getSilencedAt());
  }

  @Test
  @DisplayName("illegal transitions throw AlertTransitionException")
  void illegalTransitions() {
    Alert created = service.raise(baseRequest());
    service.resolve(created.getId());

    assertThrows(AlertTransitionException.class, () -> service.resolve(created.getId()));
    assertThrows(AlertTransitionException.class, () -> service.acknowledge(created.getId(), "a"));
    assertThrows(AlertTransitionException.class, () -> service.silence(created.getId(), "a"));
    assertThrows(AlertTransitionException.class, () -> service.unmute(created.getId()));
    assertThrows(AlertNotFoundException.class, () -> service.getRequired("missing"));
  }

  @Test
  @DisplayName("isDue respects lastTriggeredAt and per-alert resend override")
  void dueLogic() {
    Alert alert =
        Alert.builder()
            .state(AlertState.FIRING)
            .lastTriggeredAt(T0)
            .resendIntervalMs(60_000L)
            .build();

    assertFalse(alert.isDue(T0.plusSeconds(30), 300_000));
    assertTrue(alert.isDue(T0.plusSeconds(60), 300_000));
  }

  private static RaiseAlertRequest baseRequest() {
    return RaiseAlertRequest.builder()
        .source("engine")
        .alertKey("disk-full")
        .subject("Disk full")
        .body("Volume /data is 95% full")
        .recipients(AlertRecipients.builder().to(List.of("oncall@example.com")).build())
        .severity("critical")
        .metadata(Map.of("host", "node-1"))
        .build();
  }

  private static final class MutableClock extends Clock {
    private Instant instant;

    MutableClock(Instant instant) {
      this.instant = instant;
    }

    @Override
    public ZoneOffset getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(java.time.ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return instant;
    }
  }

  private static final class InMemoryAlertRepository implements AlertRepository {
    private final Map<String, Alert> store = new ConcurrentHashMap<>();

    @Override
    public Alert save(Alert alert) {
      if (alert.getId() == null) {
        alert.setId(UUID.randomUUID().toString());
      }
      store.put(alert.getId(), cloneAlert(alert));
      return cloneAlert(alert);
    }

    @Override
    public Optional<Alert> findById(String id) {
      return Optional.ofNullable(store.get(id)).map(this::cloneAlert);
    }

    @Override
    public Optional<Alert> findByFingerprint(String fingerprint) {
      return store.values().stream()
          .filter(a -> fingerprint.equals(a.getFingerprint()))
          .findFirst()
          .map(this::cloneAlert);
    }

    @Override
    public List<Alert> findAll() {
      return store.values().stream()
          .map(this::cloneAlert)
          .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Alert> findByState(AlertState state) {
      return store.values().stream()
          .filter(a -> a.getState() == state)
          .map(this::cloneAlert)
          .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public AlertPage findByStates(Collection<AlertState> states, int page, int size) {
      return null;
    }

    @Override
    public AlertPage findAll(
        AlertState state,
        String processDefinitionId,
        Instant createdFrom,
        Instant createdTo,
        int page,
        int size,
        String sort) {
      var stream = store.values().stream();
      if (state != null) stream = stream.filter(a -> a.getState() == state);
      if (processDefinitionId != null)
        stream =
            stream.filter(
                a ->
                    a.getMetadata() != null
                        && processDefinitionId.equals(a.getMetadata().get("processDefinitionId")));
      if (createdFrom != null) stream = stream.filter(a -> !a.getCreatedAt().isBefore(createdFrom));
      if (createdTo != null) stream = stream.filter(a -> !a.getCreatedAt().isAfter(createdTo));
      var all =
          stream
              .map(this::cloneAlert)
              .sorted(Comparator.comparingLong(Alert::getCount).reversed())
              .toList();
      int total = all.size();
      int from = Math.min(page * size, total);
      int to = Math.min(from + size, total);
      int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
      return new AlertPage(all.subList(from, to), total, totalPages, page, size);
    }

    @Override
    public List<AlertStats> firingStats() {
      return store.values().stream()
          .filter(a -> a.getState() == AlertState.FIRING)
          .collect(
              Collectors.groupingBy(
                  a ->
                      (a.getMetadata() != null
                              ? a.getMetadata().getOrDefault("processDefinitionId", "")
                              : "")
                          + "|"
                          + (a.getMetadata() != null
                              ? a.getMetadata().getOrDefault("version", "")
                              : ""),
                  Collectors.summingLong(Alert::getCount)))
          .entrySet()
          .stream()
          .map(
              e -> {
                String[] parts = e.getKey().split("\\|", -1);
                return new AlertStats(parts[0], parts[1], e.getValue());
              })
          .toList();
    }

    private Alert cloneAlert(Alert alert) {
      Map<String, String> metadata =
          alert.getMetadata() == null ? new HashMap<>() : new HashMap<>(alert.getMetadata());
      AlertRecipients recipients = alert.getRecipients();
      AlertRecipients copiedRecipients =
          recipients == null
              ? null
              : AlertRecipients.builder()
                  .to(
                      recipients.getTo() == null
                          ? new ArrayList<>()
                          : new ArrayList<>(recipients.getTo()))
                  .cc(
                      recipients.getCc() == null
                          ? new ArrayList<>()
                          : new ArrayList<>(recipients.getCc()))
                  .bcc(
                      recipients.getBcc() == null
                          ? new ArrayList<>()
                          : new ArrayList<>(recipients.getBcc()))
                  .build();
      return Alert.builder()
          .id(alert.getId())
          .source(alert.getSource())
          .alertKey(alert.getAlertKey())
          .fingerprint(alert.getFingerprint())
          .state(alert.getState())
          .count(alert.getCount())
          .severity(alert.getSeverity())
          .metadata(metadata)
          .subject(alert.getSubject())
          .body(alert.getBody())
          .recipients(copiedRecipients)
          .resendIntervalMs(alert.getResendIntervalMs())
          .createdAt(alert.getCreatedAt())
          .updatedAt(alert.getUpdatedAt())
          .lastTriggeredAt(alert.getLastTriggeredAt())
          .resolvedAt(alert.getResolvedAt())
          .acknowledgedAt(alert.getAcknowledgedAt())
          .acknowledgedBy(alert.getAcknowledgedBy())
          .silencedAt(alert.getSilencedAt())
          .silencedBy(alert.getSilencedBy())
          .build();
    }
  }
}
