package io.telekom.orchest.alerting.alerting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import io.telekom.orchest.alerting.AlertNotificationScheduler;
import io.telekom.orchest.alerting.AlertingInterface;
import io.telekom.orchest.alerting.ModuleProperties;
import io.telekom.orchest.alerting.api.dto.AlertDTO;
import io.telekom.orchest.alerting.service.AlertLifecycleService;
import io.telekom.orchest.api.core.adapters.data.model.Alert;
import io.telekom.orchest.api.core.adapters.data.model.AlertRecipients;
import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import io.telekom.orchest.api.core.adapters.data.repository.AlertRepository;
import io.telekom.orchest.api.core.adapters.data.repository.AlertingMailerConfigRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Tests for {@link AlertNotificationScheduler} polling and DTO mapping logic. */
class AlertNotificationSchedulerTest {

  @Test
  @DisplayName("poller sends only due FIRING alerts and updates lastTriggeredAt")
  void notifiesDueOnly() {
    Instant now = Instant.parse("2026-08-08T12:00:00Z");
    AlertRepository repository = mock(AlertRepository.class);
    AlertingMailerConfigRepository alertingMailerConfig =
        mock(AlertingMailerConfigRepository.class);
    AlertLifecycleService lifecycle = mock(AlertLifecycleService.class);
    AlertingInterface alerting = mock(AlertingInterface.class);
    ModuleProperties props = new ModuleProperties();
    props.setEnabled(true);
    props.setDefaultResendIntervalMs(300_000L);

    Alert due =
        Alert.builder()
            .id("1")
            .fingerprint("a::b")
            .state(AlertState.FIRING)
            .body("body")
            .subject("subj")
            .count(3)
            .createdAt(now.minusSeconds(60))
            .build();
    Alert notDue =
        Alert.builder()
            .id("2")
            .fingerprint("a::c")
            .state(AlertState.FIRING)
            .body("body2")
            .subject("subj2")
            .lastTriggeredAt(now.minusSeconds(10))
            .build();

    when(repository.findByState(AlertState.FIRING)).thenReturn(List.of(due, notDue));

    AlertNotificationScheduler scheduler =
        new AlertNotificationScheduler(
            repository,
            alertingMailerConfig,
            lifecycle,
            alerting,
            props,
            Clock.fixed(now, ZoneOffset.UTC));
    scheduler.notifyDueAlerts();

    ArgumentCaptor<AlertDTO> captor = ArgumentCaptor.forClass(AlertDTO.class);
    verify(alerting, times(1)).sendAlert(captor.capture());
    assertEquals("body", captor.getValue().getMessageBody());
    assertEquals("subj", captor.getValue().getSubject());
    assertEquals(3, captor.getValue().getCount());
    verify(lifecycle).markTriggered(due);
    verify(lifecycle, never()).markTriggered(notDue);
  }

  @Test
  @DisplayName("toAlertDto maps recipients and metadata process ids")
  void mapsDto() {
    Alert alert =
        Alert.builder()
            .body("b")
            .subject("s")
            .count(2)
            .state(AlertState.RESOLVED)
            .metadata(Map.of("processDefinitionId", "pd", "processInstanceId", "pi"))
            .recipients(AlertRecipients.builder().to(List.of("a@b.c")).cc(List.of("c@d.e")).build())
            .build();

    AlertDTO dto = AlertNotificationScheduler.toAlertDto(alert);
    assertEquals("pd", dto.getProcessDefinitionId());
    assertEquals("pi", dto.getProcessInstanceId());
    assertEquals(List.of("a@b.c"), dto.getRecipients().getTo());
    assertEquals(List.of("c@d.e"), dto.getRecipients().getCc());
    assertEquals(AlertState.RESOLVED, dto.getState());
  }
}
