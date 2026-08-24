package io.telekom.orchest.orchestrest.event;

import static io.telekom.orchest.adapter.kafka.KafkaUtils.getTopicWithEnvSuffix;
import static io.telekom.orchest.adapter.kafka.client.TopicConstant.*;

import io.telekom.orchest.adapter.kafka.client.KafkaClient;
import io.telekom.orchest.adapter.kafka.model.TypedKafkaEvent;
import io.telekom.orchest.api.core.adapters.data.dto.ResumeActivityEventRequest;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.request.DynamicProcessInvocationRequest;
import io.telekom.orchest.api.core.request.MessageEventRequest;
import io.telekom.orchest.api.core.request.ProcessInvocationRequest;
import io.telekom.orchest.api.core.request.RetryProcessEvent;
import io.telekom.orchest.api.core.request.SignalEventRequest;
import io.telekom.orchest.api.core.utils.IDGenerator;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.telemetry.OrchestRestTelemetryService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes orchestration events (process invocations, retries, messages, signals) to Kafka topics.
 */
@Slf4j
@RequiredArgsConstructor
public class EventProducer {

  private final KafkaClient kafkaClient;
  private final OrchestRestTelemetryService metricService;

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
    metricService.incrementRestProcessInvocationCounter(
        req.getProcessDefinitionId(), req.getVersion());
  }

  public void sendDynamicProcessInvocationEvent(DynamicProcessInvocationRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            getTopicWithEnvSuffix(DYNAMIC_PROCESS_INVOCATION_EVENT_TOPIC),
            req,
            r -> r.getProcessInstanceId(),
            r ->
                Map.of(
                    "processDefinitionId",
                    "dynamic-execution",
                    "processInstanceId",
                    r.getProcessInstanceId() != null
                        ? r.getProcessInstanceId()
                        : IDGenerator.generate())));
    metricService.incrementRestProcessInvocationCounter("dynamic-execution", 1);
  }

  public void sendRetryEvent(RetryProcessEvent req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            getTopicWithEnvSuffix(SERVER_RETRY_PROCESS_EVENT_TOPIC),
            req,
            RetryProcessEvent::getProcessInstanceId,
            r -> Map.of("processInstanceId", r.getProcessInstanceId())));
    metricService.incrementRestRetryEventCounter();
  }

  public void sendResumeEvent(ResumeActivityEventRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            getTopicWithEnvSuffix(PROCESS_RESUME_EVENT_TOPIC),
            req,
            ResumeActivityEventRequest::getProcessInstanceId,
            r -> Map.of("processInstanceId", r.getProcessInstanceId())));
    metricService.incrementRestRetryEventCounter();
  }

  public void sendServerWorkerEvent(WorkerEventRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            getTopicWithEnvSuffix(SERVER_WORKER_EVENT_TOPIC),
            req,
            r ->
                r.getParentProcessInstanceId() != null
                    ? r.getParentProcessInstanceId()
                    : r.getProcessInstanceId()));
    log.info("Worker event sent to server: {}", JsonMapper.writeToJson(req.getNodeInformation()));
  }

  public void sendMessageEvent(MessageEventRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            getTopicWithEnvSuffix(SERVER_INTERMEDIATE_MESSAGE_THROW_EVENT_TOPIC), req));
    metricService.incrementRestMessageEventCounter(req.getMessageName());
  }

  public void sendSignalEvent(SignalEventRequest req) {
    kafkaClient.sendEncryptedMessage(
        TypedKafkaEvent.of(
            getTopicWithEnvSuffix(SERVER_INTERMEDIATE_SIGNAL_THROW_EVENT_TOPIC), req));
    metricService.incrementRestSignalEventCounter(req.getSignalName());
  }
}
