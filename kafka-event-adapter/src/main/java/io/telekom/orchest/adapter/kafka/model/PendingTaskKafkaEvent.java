package io.telekom.orchest.adapter.kafka.model;

import static io.telekom.orchest.adapter.kafka.client.TopicConstant.SERVER_PENDING_TASK_EVENT_TOPIC;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.request.PendingTaskRequest;
import java.util.Map;
import lombok.Setter;

/** Kafka event for publishing pending task requests to the server pending-task topic. */
@Setter
public class PendingTaskKafkaEvent implements KafkaEvent<PendingTaskRequest> {

  private PendingTaskRequest pendingTaskRequest;

  @Override
  public String getTopicName() {
    return KafkaUtils.getTopicWithEnvSuffix(SERVER_PENDING_TASK_EVENT_TOPIC);
  }

  @Override
  public PendingTaskRequest getValue() {
    return pendingTaskRequest;
  }

  @Override
  public Map<String, String> getHeaders() {
    return Map.of();
  }
}
