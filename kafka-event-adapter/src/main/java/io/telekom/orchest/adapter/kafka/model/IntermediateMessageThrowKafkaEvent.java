package io.telekom.orchest.adapter.kafka.model;

import static io.telekom.orchest.adapter.kafka.client.TopicConstant.SERVER_INTERMEDIATE_MESSAGE_THROW_EVENT_TOPIC;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.request.MessageEventRequest;
import java.util.Map;
import lombok.Setter;

/** Kafka event for intermediate BPMN message throw events sent to the engine. */
@Setter
public class IntermediateMessageThrowKafkaEvent implements KafkaEvent<MessageEventRequest> {

  private MessageEventRequest messageEventRequest;

  @Override
  public String getTopicName() {
    return KafkaUtils.getTopicWithEnvSuffix(SERVER_INTERMEDIATE_MESSAGE_THROW_EVENT_TOPIC);
  }

  @Override
  public MessageEventRequest getValue() {
    return messageEventRequest;
  }

  @Override
  public Map<String, String> getHeaders() {
    return Map.of();
  }
}
