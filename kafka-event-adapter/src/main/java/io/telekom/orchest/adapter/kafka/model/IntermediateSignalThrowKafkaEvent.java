package io.telekom.orchest.adapter.kafka.model;

import static io.telekom.orchest.adapter.kafka.client.TopicConstant.SERVER_INTERMEDIATE_SIGNAL_THROW_EVENT_TOPIC;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.request.SignalEventRequest;
import java.util.Map;
import lombok.Setter;

/** Kafka event for intermediate BPMN signal throw events sent to the engine. */
@Setter
public class IntermediateSignalThrowKafkaEvent implements KafkaEvent<SignalEventRequest> {

  private SignalEventRequest signalEventRequest;

  @Override
  public String getTopicName() {
    return KafkaUtils.getTopicWithEnvSuffix(SERVER_INTERMEDIATE_SIGNAL_THROW_EVENT_TOPIC);
  }

  @Override
  public SignalEventRequest getValue() {
    return signalEventRequest;
  }

  @Override
  public Map<String, String> getHeaders() {
    return Map.of();
  }
}
