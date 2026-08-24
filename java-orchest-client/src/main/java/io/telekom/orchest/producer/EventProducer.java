package io.telekom.orchest.producer;

import static io.telekom.orchest.adapter.kafka.model.TopicConstant.*;

import io.telekom.orchest.adapter.kafka.model.TypedKafkaEvent;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.config.OrchestClientProperties;
import io.telekom.orchest.kafka.KafkaClient;
import io.telekom.orchest.kafka.KafkaProducerManager;
import io.telekom.orchest.kafka.kms.IEncryptionClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Event producer for the pure Java OrchesT client. Responsible for wrapping API requests into Kafka
 * events and sending them to the engine.
 */
@Slf4j
public class EventProducer {

  private final KafkaClient kafkaClient;
  private final String kafkaSuffix;

  /**
   * Creates a new event producer backed by a Kafka client.
   *
   * @param properties client configuration
   * @param encryptionClient encryption client for payload encryption
   */
  public EventProducer(OrchestClientProperties properties, IEncryptionClient encryptionClient) {
    kafkaClient = new KafkaClient(new KafkaProducerManager(properties), encryptionClient);
    kafkaSuffix = properties.getKafkaSuffix();
  }

  /**
   * Sends a process definition deployment event.
   *
   * @param req the deployment request
   */
  public void sendDeploymentEvent(ResourceDeploymentRequest req) {
    kafkaClient.sendMessage(TypedKafkaEvent.of(getWithSuffix(DEPLOYMENT_TOPIC), req));
  }

  /**
   * Sends a process invocation event (encrypted).
   *
   * @param req the process invocation request
   */
  public void sendProcessInvocationEvent(ProcessInvocationRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            getWithSuffix(PROCESS_INVOCATION_EVENT_TOPIC),
            req,
            ProcessInvocationRequest::getProcessInstanceId));
  }

  /**
   * Sends a worker event result back to the server (encrypted).
   *
   * @param req the worker event request containing execution results
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
    log.debug("Worker event sent to server: {}", JsonMapper.writeToJson(req.getNodeInformation()));
  }

  /**
   * Sends an intermediate message throw event (encrypted).
   *
   * @param req the message event request
   */
  public void sendMessageEvent(MessageEventRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(getWithSuffix(SERVER_INTERMEDIATE_MESSAGE_THROW_EVENT_TOPIC), req));
  }

  /**
   * Sends an intermediate signal throw event (encrypted).
   *
   * @param req the signal event request
   */
  public void sendSignalEvent(SignalEventRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(getWithSuffix(SERVER_INTERMEDIATE_SIGNAL_THROW_EVENT_TOPIC), req));
  }

  /**
   * Sends a pending task event when no worker is registered for a task type.
   *
   * @param req the pending task request
   */
  public void sendPendingTaskEvent(PendingTaskRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(getWithSuffix(SERVER_PENDING_TASK_EVENT_TOPIC), req));
  }

  /**
   * Sends a worker registry update to inform the engine about available workers.
   *
   * @param req the worker registry request
   */
  public void sendWorkerRegistryUpdateEvent(WorkerRegistryRequest req) {
    kafkaClient.sendMessage(
        TypedKafkaEvent.of(
            getWithSuffix(CLIENT_WORKER_REGISTER_EVENT_TOPIC),
            req,
            WorkerRegistryRequest::getNamespace));
  }

  /**
   * Sends a variable update event for a running process instance (encrypted).
   *
   * @param req the update variable request
   */
  public void sendUpdateVariableEvent(UpdateVariableRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(getWithSuffix(SERVER_UPDATE_VARIABLE_EVENT_TOPIC), req));
  }

  private String getServerWorkerEventTopic(String processDefinitionId) {
    return getWithSuffix(
        SERVER_WORKER_EVENT_TOPIC_PREFIX
            + StringUtils.upperCase(processDefinitionId.replace("-", "_")));
  }

  private String getWithSuffix(String topic) {
    return topic + this.kafkaSuffix;
  }
}
