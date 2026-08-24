package io.telekom.orchest.adapter.kafka.model;

import static io.telekom.orchest.adapter.kafka.client.TopicConstant.SERVER_RETRY_PROCESS_EVENT_TOPIC;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.request.RetryProcessEvent;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;

/** Kafka event for requesting a process instance retry via the retry topic. */
@Setter
public class RetryProcessInstanceKafkaEvent implements KafkaEvent<RetryProcessEvent> {

  private RetryProcessEvent retryProcessEvent;

  @Override
  public String getTopicName() {
    return KafkaUtils.getTopicWithEnvSuffix(SERVER_RETRY_PROCESS_EVENT_TOPIC);
  }

  @Override
  public RetryProcessEvent getValue() {
    return retryProcessEvent;
  }

  @Override
  public Map<String, String> getHeaders() {
    HashMap<String, String> headers = new HashMap<>();
    headers.put("processInstanceId", retryProcessEvent.getProcessInstanceId());
    return headers;
  }

  @Override
  public String getKey() {
    if (retryProcessEvent != null && retryProcessEvent.getProcessInstanceId() != null) {
      return retryProcessEvent.getProcessInstanceId();
    }
    return KafkaEvent.super.getKey();
  }
}
