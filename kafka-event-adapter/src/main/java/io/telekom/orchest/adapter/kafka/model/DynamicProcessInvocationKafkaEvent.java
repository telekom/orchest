package io.telekom.orchest.adapter.kafka.model;

import static io.telekom.orchest.adapter.kafka.client.TopicConstant.DYNAMIC_PROCESS_INVOCATION_EVENT_TOPIC;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.request.DynamicProcessInvocationRequest;
import io.telekom.orchest.api.core.utils.IDGenerator;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;

/** Kafka event for triggering dynamic (ad-hoc) process invocations via Kafka. */
@Setter
public class DynamicProcessInvocationKafkaEvent
    implements KafkaEvent<DynamicProcessInvocationRequest> {

  private DynamicProcessInvocationRequest processInvocationRequest;

  @Override
  public String getTopicName() {
    return KafkaUtils.getTopicWithEnvSuffix(DYNAMIC_PROCESS_INVOCATION_EVENT_TOPIC);
  }

  @Override
  public DynamicProcessInvocationRequest getValue() {
    return processInvocationRequest;
  }

  @Override
  public Map<String, String> getHeaders() {
    HashMap<String, String> headers = new HashMap<>();
    headers.put("processDefinitionId", "dynamic-execution");
    headers.put(
        "processInstanceId",
        processInvocationRequest.getProcessInstanceId() != null
            ? processInvocationRequest.getProcessInstanceId()
            : IDGenerator.generate());
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
