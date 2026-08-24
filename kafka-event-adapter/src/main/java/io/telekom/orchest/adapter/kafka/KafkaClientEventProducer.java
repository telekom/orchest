package io.telekom.orchest.adapter.kafka;

import io.telekom.orchest.adapter.kafka.client.KafkaClient;
import io.telekom.orchest.adapter.kafka.model.TypedKafkaEvent;
import io.telekom.orchest.api.core.adapters.data.dto.ConnectorTaskRequest;
import io.telekom.orchest.api.core.adapters.data.dto.IncidentEventPayload;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.adapters.event.ClientEventProducer;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event producer implementation for sending events to client workers via Kafka. Encapsulates the
 * logic for sending encrypted messages to specific topics.
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaClientEventProducer implements ClientEventProducer<WorkerEventRequest> {

  private static final String HEADER_PROCESS_DEFINITION_ID = "processDefinitionId";
  private static final String HEADER_PROCESS_INSTANCE_ID = "processInstanceId";

  private final KafkaClient kafkaClient;
  private final OrchestEngineTelemetryService telemetryService;

  /**
   * Sends a worker event to the appropriate client worker topic via Kafka.
   *
   * @param event the worker event request containing task details and routing information
   */
  @Override
  public void sendClientWorkerEvent(WorkerEventRequest event) {
    BaseNode node = event.getNodeInformation();
    log.info(
        "going to send client worker event for instanceId: {}, for activityId: {}",
        event.getProcessInstanceId(),
        node.getId());
    boolean commonWorker = event.isCommonWorker();
    String workerEventTopic;
    if (commonWorker) {
      workerEventTopic = KafkaUtils.getClientCommonWorkerEventTopic();
    } else {
      workerEventTopic = KafkaUtils.getClientWorkerEventTopic(event.getProcessDefinitionId());
    }
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            workerEventTopic,
            event,
            WorkerEventRequest::getProcessInstanceId,
            e ->
                Map.of(
                    HEADER_PROCESS_DEFINITION_ID,
                    e.getProcessDefinitionId(),
                    HEADER_PROCESS_INSTANCE_ID,
                    e.getProcessInstanceId(),
                    "version",
                    e.getVersion() == null ? "latest" : e.getVersion().toString(),
                    "isCommonWorker",
                    String.valueOf(commonWorker))));
    telemetryService.incrementSentWorkerCounter(
        event.getState().name(),
        event.getType(),
        event.getProcessDefinitionId(),
        event.getVersion());
  }

  /**
   * Publishes an incident event to the incident topic for downstream alerting.
   *
   * @param event the incident event payload
   */
  public void sendIncidentEvent(IncidentEventPayload event) {
    log.info("going to send incident event for the instanceId: {}", event.getProcessInstanceId());
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            KafkaUtils.getIncidentEventTopic(),
            event,
            e -> null,
            e ->
                Map.of(
                    HEADER_PROCESS_DEFINITION_ID, e.getProcessDefinitionId(),
                    HEADER_PROCESS_INSTANCE_ID, e.getProcessInstanceId())));
    telemetryService.incrementIncidentEvent(
        event.getActivityName(), event.getProcessDefinitionId(), event.getVersion());
  }

  /**
   * Publishes an incident resolution event indicating an incident has been resolved.
   *
   * @param event the incident resolution payload
   */
  public void sendIncidentResolutionEvent(IncidentEventPayload event) {
    log.info(
        "going to send incident resolution event for the instanceId: {}",
        event.getProcessInstanceId());
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            KafkaUtils.getIncidentResolutionEventTopic(),
            event,
            e -> null,
            e ->
                Map.of(
                    HEADER_PROCESS_DEFINITION_ID, e.getProcessDefinitionId(),
                    HEADER_PROCESS_INSTANCE_ID, e.getProcessInstanceId())));
  }

  /**
   * Dispatches a connector task to the connector service via the shared connector topic.
   *
   * @param event The connector task request describing the connector node to execute.
   */
  public void sendConnectorTaskEvent(ConnectorTaskRequest event) {
    log.info(
        "going to send connector task event for instanceId: {}, activityId: {}, connectorType: {}",
        event.getProcessInstanceId(),
        event.getActivityId(),
        event.getConnectorType());
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            KafkaUtils.getConnectorTaskTopic(),
            event,
            ConnectorTaskRequest::getProcessInstanceId,
            e ->
                Map.of(
                    HEADER_PROCESS_DEFINITION_ID,
                    e.getProcessDefinitionId(),
                    HEADER_PROCESS_INSTANCE_ID,
                    e.getProcessInstanceId(),
                    "connectorType",
                    e.getConnectorType())));
  }
}
