package io.telekom.orchest.engine.handlers;

import static org.mockito.Mockito.*;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link ServiceTaskHandler} verifying delegation of worker events to the Kafka producer.
 */
@ExtendWith(MockitoExtension.class)
class ServiceTaskHandlerTest {

  @Mock private KafkaClientEventProducer eventProducer;

  @InjectMocks private ServiceTaskHandler serviceTaskHandler;

  @Test
  @DisplayName("handle() should delegate to KafkaClientEventProducer.sendClientWorkerEvent()")
  void handle_shouldDelegateToEventProducer() {
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-123")
            .activityId("activity-1")
            .processDefinitionId("pd-456")
            .build();

    serviceTaskHandler.handle(event);

    verify(eventProducer, times(1)).sendClientWorkerEvent(event);
  }

  @Test
  @DisplayName("handle() should forward the exact event object to the producer")
  void handle_shouldForwardExactEventObject() {
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-999")
            .activityId("act-abc")
            .processDefinitionId("pd-def")
            .type("SERVICE_TASK")
            .version(2)
            .retriesLeft(5)
            .build();

    serviceTaskHandler.handle(event);

    verify(eventProducer).sendClientWorkerEvent(same(event));
  }

  @Test
  @DisplayName("handle() should not call sendIncidentEvent")
  void handle_shouldNotCallSendIncidentEvent() {
    WorkerEventRequest event =
        WorkerEventRequest.builder().processInstanceId("pi-100").activityId("act-100").build();

    serviceTaskHandler.handle(event);

    verify(eventProducer, never()).sendIncidentEvent(any());
    verify(eventProducer, times(1)).sendClientWorkerEvent(event);
  }

  @Test
  @DisplayName("handle() should work with minimal event fields populated")
  void handle_shouldWorkWithMinimalEvent() {
    WorkerEventRequest event = new WorkerEventRequest();
    event.setProcessInstanceId("pi-min");
    event.setActivityId("act-min");

    serviceTaskHandler.handle(event);

    verify(eventProducer).sendClientWorkerEvent(event);
  }
}
