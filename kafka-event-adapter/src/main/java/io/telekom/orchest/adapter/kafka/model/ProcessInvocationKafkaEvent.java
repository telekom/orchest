package io.telekom.orchest.adapter.kafka.model;

import static io.telekom.orchest.adapter.kafka.client.TopicConstant.PROCESS_INVOCATION_EVENT_TOPIC;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.request.ProcessInvocationRequest;
import io.telekom.orchest.api.core.utils.IDGenerator;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;

/** Kafka event for triggering process invocations via the process invocation topic. */
@Setter
public class ProcessInvocationKafkaEvent implements KafkaEvent<ProcessInvocationRequest> {

  private ProcessInvocationRequest processInvocationRequest;

  @Override
  public String getTopicName() {
    return KafkaUtils.getTopicWithEnvSuffix(PROCESS_INVOCATION_EVENT_TOPIC);
  }

  @Override
  public ProcessInvocationRequest getValue() {
    return processInvocationRequest;
  }

  @Override
  public Map<String, String> getHeaders() {
    HashMap<String, String> headers = new HashMap<>();
    headers.put("processDefinitionId", processInvocationRequest.getProcessDefinitionId());
    headers.put(
        "processInstanceId",
        processInvocationRequest.getProcessInstanceId() != null
            ? processInvocationRequest.getProcessInstanceId()
            : IDGenerator.generate());
    headers.put(
        "version",
        processInvocationRequest.getVersion() == null
            ? "-1"
            : processInvocationRequest.getVersion().toString());
    return headers;
  }

  @Override
  public String getKey() {
    if (processInvocationRequest != null
        && processInvocationRequest.getProcessInstanceId() != null) {
      return processInvocationRequest.getProcessInstanceId();
    }
    return KafkaEvent.super.getKey();
  }
}
