package io.telekom.orchest.adapter.kafka.model;

import static io.telekom.orchest.adapter.kafka.client.TopicConstant.CLIENT_WORKER_REGISTER_EVENT_TOPIC;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.request.WorkerRegistryRequest;
import java.util.Map;
import lombok.Setter;

/**
 * Kafka event payload for registering workers. Used to announce worker capabilities to the engine
 * via Kafka.
 */
@Setter
public class WorkerRegistryKafkaEvent implements KafkaEvent<WorkerRegistryRequest> {

  private WorkerRegistryRequest workerRegistryRequest;

  @Override
  public String getTopicName() {
    return KafkaUtils.getTopicWithEnvSuffix(CLIENT_WORKER_REGISTER_EVENT_TOPIC);
  }

  @Override
  public WorkerRegistryRequest getValue() {
    return workerRegistryRequest;
  }

  @Override
  public Map<String, String> getHeaders() {
    return Map.of();
  }

  @Override
  public String getKey() {
    return workerRegistryRequest.getNamespace();
  }
}
