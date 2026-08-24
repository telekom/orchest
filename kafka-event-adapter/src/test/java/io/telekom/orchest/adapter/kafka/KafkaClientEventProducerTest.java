package io.telekom.orchest.adapter.kafka;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.adapter.kafka.client.KafkaClient;
import io.telekom.orchest.adapter.kafka.model.TypedKafkaEvent;
import io.telekom.orchest.api.core.adapters.data.dto.ConnectorTaskRequest;
import io.telekom.orchest.api.core.adapters.data.dto.IncidentEventPayload;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests event production to Kafka topics for worker, incident, and connector task events. */
@ExtendWith(MockitoExtension.class)
class KafkaClientEventProducerTest {

  @Mock private KafkaClient kafkaClient;

  @Mock private OrchestEngineTelemetryService telemetryService;

  private KafkaClientEventProducer producer;

  @BeforeEach
  void setUp() {
    // Initialize static fields so KafkaUtils works
    KafkaUtils.ENVIRONMENT = "LOCAL";
    producer = new KafkaClientEventProducer(kafkaClient, telemetryService);
  }

  // ============================================================
  // sendClientWorkerEvent
  // ============================================================

  @Test
  @DisplayName("sendClientWorkerEvent sends encrypted message with processInstanceId as key")
  void sendClientWorkerEvent_sendsEncryptedMessageWithProcessInstanceIdKey() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Task");
    serviceTask.setWorkerType("myWorker");

    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-100")
            .processDefinitionId("my-process")
            .version(1)
            .state(NodeState.STARTED)
            .type("myWorker")
            .nodeInformation(serviceTask)
            .variables(new Variables(new HashMap<>(), Variables.VariableAction.UPDATE))
            .build();

    producer.sendClientWorkerEvent(event);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<TypedKafkaEvent<WorkerEventRequest>> captor =
        ArgumentCaptor.forClass(TypedKafkaEvent.class);
    verify(kafkaClient).sendEncryptedMessage(captor.capture());

    TypedKafkaEvent<WorkerEventRequest> capturedEvent = captor.getValue();
    assertEquals("pi-100", capturedEvent.getKey());
    assertNotNull(capturedEvent.getTopicName());
    assertTrue(
        capturedEvent.getTopicName().contains("ORCHEST_CLIENT_WORKER_EVENT_TOPIC_MY_PROCESS"));
    assertEquals(event, capturedEvent.getValue());

    // Verify headers
    Map<String, String> headers = capturedEvent.getHeaders();
    assertEquals("my-process", headers.get("processDefinitionId"));
    assertEquals("pi-100", headers.get("processInstanceId"));
    assertEquals("1", headers.get("version"));
  }

  @Test
  @DisplayName("sendClientWorkerEvent uses 'latest' for null version in headers")
  void sendClientWorkerEvent_nullVersion_usesLatestInHeaders() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Task");
    serviceTask.setWorkerType("myWorker");

    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-101")
            .processDefinitionId("my-process")
            .version(null)
            .state(NodeState.STARTED)
            .type("myWorker")
            .nodeInformation(serviceTask)
            .variables(new Variables(new HashMap<>(), Variables.VariableAction.UPDATE))
            .build();

    producer.sendClientWorkerEvent(event);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<TypedKafkaEvent<WorkerEventRequest>> captor =
        ArgumentCaptor.forClass(TypedKafkaEvent.class);
    verify(kafkaClient).sendEncryptedMessage(captor.capture());

    Map<String, String> headers = captor.getValue().getHeaders();
    assertEquals("latest", headers.get("version"));
  }

  // ============================================================
  // sendIncidentEvent
  // ============================================================

  @Test
  @DisplayName("sendIncidentEvent sends encrypted message with correct topic and headers")
  void sendIncidentEvent_sendsEncryptedMessage() {
    IncidentEventPayload incident =
        IncidentEventPayload.builder()
            .processDefinitionId("pd-500")
            .processInstanceId("pi-500")
            .correlationId("corr-500")
            .incidentMessage("Something failed")
            .build();

    producer.sendIncidentEvent(incident);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<TypedKafkaEvent<IncidentEventPayload>> captor =
        ArgumentCaptor.forClass(TypedKafkaEvent.class);
    verify(kafkaClient).sendEncryptedMessage(captor.capture());

    TypedKafkaEvent<IncidentEventPayload> capturedEvent = captor.getValue();
    assertTrue(capturedEvent.getTopicName().contains("ORCHEST_SERVER_INCIDENT_EVENT_TOPIC"));
    assertEquals(incident, capturedEvent.getValue());

    // Key extractor returns null for incidents, so default UUID key is used
    assertNotNull(capturedEvent.getKey());

    Map<String, String> headers = capturedEvent.getHeaders();
    assertEquals("pd-500", headers.get("processDefinitionId"));
    assertEquals("pi-500", headers.get("processInstanceId"));
  }

  // ============================================================
  // sendConnectorTaskEvent
  // ============================================================

  @Test
  @DisplayName(
      "sendConnectorTaskEvent sends encrypted message to the connector topic with processInstanceId key")
  void sendConnectorTaskEvent_sendsEncryptedMessage() {
    ConnectorTaskRequest event =
        ConnectorTaskRequest.builder()
            .eventId("evt-1")
            .processInstanceId("pi-900")
            .processDefinitionId("pd-900")
            .activityId("connector-node")
            .connectorType("io.orchest:http-json:1")
            .version(1)
            .build();

    producer.sendConnectorTaskEvent(event);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<TypedKafkaEvent<ConnectorTaskRequest>> captor =
        ArgumentCaptor.forClass(TypedKafkaEvent.class);
    verify(kafkaClient).sendEncryptedMessage(captor.capture());

    TypedKafkaEvent<ConnectorTaskRequest> capturedEvent = captor.getValue();
    assertEquals("pi-900", capturedEvent.getKey());
    assertTrue(capturedEvent.getTopicName().contains("ORCHEST_CONNECTOR_TASK_EVENT"));
    assertEquals(event, capturedEvent.getValue());

    Map<String, String> headers = capturedEvent.getHeaders();
    assertEquals("pd-900", headers.get("processDefinitionId"));
    assertEquals("pi-900", headers.get("processInstanceId"));
    assertEquals("io.orchest:http-json:1", headers.get("connectorType"));
  }
}
