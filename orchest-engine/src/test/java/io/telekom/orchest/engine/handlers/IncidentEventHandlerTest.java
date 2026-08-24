package io.telekom.orchest.engine.handlers;

import static io.telekom.orchest.alerting.api.RaiseAlertRequest.alertKey;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.alerting.api.RaiseAlertRequest;
import io.telekom.orchest.alerting.service.AlertLifecycleService;
import io.telekom.orchest.api.core.adapters.data.dto.IncidentEventPayload;
import io.telekom.orchest.api.core.adapters.data.repository.IncidentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Tests for {@link IncidentEventHandler} verifying incident persistence, Kafka event publishing,
 * and alert raising.
 */
@ExtendWith(MockitoExtension.class)
class IncidentEventHandlerTest {

  @Mock private KafkaClientEventProducer eventProducer;

  @Mock private IncidentRepository incidentRepository;

  @Mock private ObjectProvider<AlertLifecycleService> alertLifecycleServiceProvider;

  @Mock private AlertLifecycleService alertLifecycleService;

  @InjectMocks private IncidentEventHandler incidentEventHandler;

  @Test
  @DisplayName("handle() persists incident, publishes Kafka event, and raises telemetry alert")
  void handle_shouldPersistPublishAndRaiseAlert() {
    when(alertLifecycleServiceProvider.getIfAvailable()).thenReturn(alertLifecycleService);

    IncidentEventPayload event =
        IncidentEventPayload.builder()
            .processInstanceId("pi-123")
            .processDefinitionId("pd-456")
            .version(2)
            .activityId("act-1")
            .activityName("Call Worker")
            .incidentMessage("Something went wrong")
            .correlationId("corr-1")
            .namespace("prod")
            .build();

    incidentEventHandler.handle(event);

    verify(incidentRepository).save(any());
    verify(eventProducer).sendIncidentEvent(event);

    ArgumentCaptor<RaiseAlertRequest> captor = ArgumentCaptor.forClass(RaiseAlertRequest.class);
    verify(alertLifecycleService).raise(captor.capture());
    RaiseAlertRequest request = captor.getValue();
    assertEquals(IncidentEventHandler.ALERT_SOURCE, request.getSource());
    assertEquals("incident:pd-456:act-1", request.getAlertKey());
    assertTrue(request.getSubject().contains("pd-456"));
    assertTrue(request.getBody().contains("Something went wrong"));
    assertEquals("pi-123", request.getMetadata().get("processInstanceId"));
    assertEquals("pd-456", request.getMetadata().get("processDefinitionId"));
    assertEquals("2", request.getMetadata().get("version"));
  }

  @Test
  @DisplayName("handle() skips raise when AlertLifecycleService is not available")
  void handle_shouldSkipRaiseWhenAlertingUnavailable() {
    when(alertLifecycleServiceProvider.getIfAvailable()).thenReturn(null);

    IncidentEventPayload event =
        IncidentEventPayload.builder()
            .processInstanceId("pi-123")
            .processDefinitionId("pd-456")
            .build();

    incidentEventHandler.handle(event);

    verify(eventProducer).sendIncidentEvent(event);
    verify(alertLifecycleService, never()).raise(any());
  }

  @Test
  @DisplayName("handle() still publishes Kafka when raise fails")
  void handle_shouldNotFailWhenRaiseThrows() {
    when(alertLifecycleServiceProvider.getIfAvailable()).thenReturn(alertLifecycleService);
    when(alertLifecycleService.raise(any())).thenThrow(new RuntimeException("mongo down"));

    IncidentEventPayload event =
        IncidentEventPayload.builder()
            .processInstanceId("pi-100")
            .processDefinitionId("pd-100")
            .activityId("task-1")
            .build();

    assertDoesNotThrow(() -> incidentEventHandler.handle(event));
    verify(eventProducer).sendIncidentEvent(event);
  }

  @Test
  @DisplayName("handleResolution() should delegate to Kafka resolution event")
  void handleResolution_shouldDelegate() {
    IncidentEventPayload event =
        IncidentEventPayload.builder()
            .processInstanceId("pi-1")
            .processDefinitionId("pd-1")
            .build();

    incidentEventHandler.handleResolution(event);

    verify(eventProducer).sendIncidentResolutionEvent(event);
    verify(alertLifecycleService, never()).raise(any());
  }

  @Test
  @DisplayName("toRaiseRequest builds stable alertKey from definition and activity")
  void toRaiseRequest_alertKey() {
    RaiseAlertRequest request =
        IncidentEventHandler.toRaiseRequest(
            IncidentEventPayload.builder().processDefinitionId("order").activityId("ship").build());

    assertEquals("incident:order:ship", request.getAlertKey());
    assertEquals("incident:unknown:unknown", alertKey("unknown", "unknown"));
  }
}
