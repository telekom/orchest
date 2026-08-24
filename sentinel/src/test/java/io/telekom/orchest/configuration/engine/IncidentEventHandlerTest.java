package io.telekom.orchest.configuration.engine;

import static org.mockito.Mockito.verify;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.api.core.adapters.data.dto.IncidentEventPayload;
import io.telekom.orchest.api.core.adapters.data.repository.IncidentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests for {@link IncidentEventHandler} verifying Kafka event delegation. */
@ExtendWith(MockitoExtension.class)
@DisplayName("IncidentEventHandler")
class IncidentEventHandlerTest {

  @Mock private KafkaClientEventProducer eventProducer;

  @Mock private IncidentRepository incidentRepository;

  @InjectMocks private IncidentEventHandler incidentEventHandler;

  @Test
  @DisplayName("handle() should delegate to KafkaClientEventProducer.sendIncidentEvent()")
  void handleShouldDelegateToProducer() {
    IncidentEventPayload event =
        IncidentEventPayload.builder()
            .processInstanceId("pi-500")
            .processDefinitionId("pd-def-1")
            .activityId("task-fail")
            .activityName("Failing Task")
            .incidentMessage("NullPointerException at line 42")
            .build();

    incidentEventHandler.handle(event);

    verify(eventProducer).sendIncidentEvent(event);
  }

  @Test
  @DisplayName("handle() should pass the exact event object to the producer")
  void handleShouldPassExactEventToProducer() {
    IncidentEventPayload event =
        IncidentEventPayload.builder()
            .processInstanceId("pi-600")
            .processDefinitionId("pd-def-2")
            .correlationId("corr-123")
            .incidentMessage("Service unavailable")
            .namespace("production")
            .activityId("external-call")
            .activityName("Call External API")
            .build();

    incidentEventHandler.handle(event);

    verify(eventProducer).sendIncidentEvent(event);
  }

  @Test
  @DisplayName("handle() should work with minimal event data")
  void handleShouldWorkWithMinimalEvent() {
    IncidentEventPayload event =
        IncidentEventPayload.builder().processInstanceId("pi-minimal").build();

    incidentEventHandler.handle(event);

    verify(eventProducer).sendIncidentEvent(event);
  }
}
