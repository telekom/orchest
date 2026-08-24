package io.telekom.orchest.adapter.kafka.model;

import static io.telekom.orchest.adapter.kafka.client.TopicConstant.DEPLOYMENT_TOPIC;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import java.util.Map;
import lombok.Setter;

/** Kafka event for publishing process definition deployment requests to the deployment topic. */
@Setter
public class DeploymentKafkaEvent implements KafkaEvent<ResourceDeploymentRequest> {

  private ResourceDeploymentRequest deploymentEvent;

  @Override
  public String getTopicName() {
    return KafkaUtils.getTopicWithEnvSuffix(DEPLOYMENT_TOPIC);
  }

  @Override
  public ResourceDeploymentRequest getValue() {
    return deploymentEvent;
  }

  @Override
  public Map<String, String> getHeaders() {
    return Map.of();
  }
}
