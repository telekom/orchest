package io.telekom.orchest.client.processor;

import static io.telekom.orchest.adapter.kafka.KafkaUtils.getServerWorkerEventTopic;
import static io.telekom.orchest.adapter.kafka.KafkaUtils.getTopicWithEnvSuffix;
import static io.telekom.orchest.adapter.kafka.client.TopicConstant.*;
import static io.telekom.orchest.adapter.kafka.client.TopicConstant.SERVER_UPDATE_VARIABLE_EVENT_TOPIC;

import io.telekom.orchest.adapter.kafka.client.KafkaClient;
import io.telekom.orchest.adapter.kafka.model.TypedKafkaEvent;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.api.core.utils.IDGenerator;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.telemetry.OrchestClientTelemetryService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event producer for the OrchesT client. Responsible for wrapping API requests into Kafka events
 * and sending them to the engine. Handles payload encryption and telemetry updates.
 */
@Slf4j
@RequiredArgsConstructor
public class EventProducer {

  private final KafkaClient kafkaClient;
  private final OrchestClientTelemetryService orchestClientTelemetryService;

  /**
   * Publishes a resource deployment event to Kafka.
   *
   * @param req the deployment request containing the BPMN/DMN XML
   */
  public void sendDeploymentEvent(ResourceDeploymentRequest req) {
    kafkaClient.sendMessage(TypedKafkaEvent.of(getTopicWithEnvSuffix(DEPLOYMENT_TOPIC), req));
  }

  /**
   * Publishes a process invocation event to start a new process instance.
   *
   * @param req the process invocation request
   */
  public void sendProcessInvocationEvent(ProcessInvocationRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            getTopicWithEnvSuffix(PROCESS_INVOCATION_EVENT_TOPIC),
            req,
            ProcessInvocationRequest::getProcessInstanceId,
            r ->
                Map.of(
                    "processDefinitionId", r.getProcessDefinitionId(),
                    "processInstanceId",
                        r.getProcessInstanceId() != null
                            ? r.getProcessInstanceId()
                            : IDGenerator.generate(),
                    "version", r.getVersion() == null ? "-1" : r.getVersion().toString())));
    orchestClientTelemetryService.incrementClientProcessInvocationCounter();
  }

  /**
   * Publishes a worker completion/failure event back to the engine.
   *
   * @param req the worker event with state and variables
   */
  public void sendServerWorkerEvent(WorkerEventRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            getServerWorkerEventTopic(req.getProcessDefinitionId()),
            req,
            r ->
                r.getParentProcessInstanceId() != null
                    ? r.getParentProcessInstanceId()
                    : r.getProcessInstanceId()));
    orchestClientTelemetryService.incrementWorkerCounter(
        req.getState().toString(), ((ServiceTaskNode) req.getNodeInformation()).getWorkerType());
    log.debug("Worker event sent to server: {}", JsonMapper.writeToJson(req.getNodeInformation()));
  }

  /**
   * Publishes a message throw event for correlation with waiting catch events.
   *
   * @param req the message event request
   */
  public void sendMessageEvent(MessageEventRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            getTopicWithEnvSuffix(SERVER_INTERMEDIATE_MESSAGE_THROW_EVENT_TOPIC), req));
    orchestClientTelemetryService.incrementMessageEventCounter(req.getMessageName());
  }

  /**
   * Publishes a signal broadcast event.
   *
   * @param req the signal event request
   */
  public void sendSignalEvent(SignalEventRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            getTopicWithEnvSuffix(SERVER_INTERMEDIATE_SIGNAL_THROW_EVENT_TOPIC), req));
    orchestClientTelemetryService.incrementSignalEventCounter(req.getSignalName());
  }

  /**
   * Publishes a pending-task event when no worker implementation is found.
   *
   * @param req the pending task request
   */
  public void sendPendingTaskEvent(PendingTaskRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(getTopicWithEnvSuffix(SERVER_PENDING_TASK_EVENT_TOPIC), req));
  }

  /**
   * Publishes a worker-registry update so the engine knows which workers are available.
   *
   * @param req the registry request containing worker metadata
   */
  public void sendWorkerRegistryUpdateEvent(WorkerRegistryRequest req) {
    kafkaClient.sendMessage(
        TypedKafkaEvent.of(
            getTopicWithEnvSuffix(CLIENT_WORKER_REGISTER_EVENT_TOPIC),
            req,
            WorkerRegistryRequest::getNamespace));
  }

  /**
   * Publishes a variable-update event for a running process instance.
   *
   * @param req the update variable request
   */
  public void sendUpdateVariableEvent(UpdateVariableRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(getTopicWithEnvSuffix(SERVER_UPDATE_VARIABLE_EVENT_TOPIC), req));
  }
}
