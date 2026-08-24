package io.telekom.orchest.configuration.engine;

import static org.mockito.Mockito.verify;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.node.TaskNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests for {@link ServiceTaskHandler} verifying Kafka worker event delegation. */
@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceTaskHandler")
class ServiceTaskHandlerTest {

  @Mock private KafkaClientEventProducer eventProducer;

  @InjectMocks private ServiceTaskHandler serviceTaskHandler;

  @Test
  @DisplayName("handle() should delegate to KafkaClientEventProducer.sendClientWorkerEvent()")
  void handleShouldDelegateToProducer() {
    TaskNode node = new TaskNode("task-1", "My Service Task");

    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-100")
            .activityId("task-1")
            .nodeInformation(node)
            .build();

    serviceTaskHandler.handle(event);

    verify(eventProducer).sendClientWorkerEvent(event);
  }

  @Test
  @DisplayName("handle() should pass the exact event object to the producer")
  void handleShouldPassExactEventToProducer() {
    TaskNode node = new TaskNode("activity-xyz", "Process Payment");

    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-200")
            .processDefinitionId("pd-abc")
            .activityId("activity-xyz")
            .activityName("Process Payment")
            .nodeInformation(node)
            .build();

    serviceTaskHandler.handle(event);

    // Verifies that the exact same event reference is passed through
    verify(eventProducer).sendClientWorkerEvent(event);
  }

  @Test
  @DisplayName("handle() should work with minimal event data")
  void handleShouldWorkWithMinimalEvent() {
    TaskNode node = new TaskNode("minimal-task", "Minimal");

    WorkerEventRequest event = WorkerEventRequest.builder().nodeInformation(node).build();

    serviceTaskHandler.handle(event);

    verify(eventProducer).sendClientWorkerEvent(event);
  }
}
