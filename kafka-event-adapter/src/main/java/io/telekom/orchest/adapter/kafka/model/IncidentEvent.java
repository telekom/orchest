package io.telekom.orchest.adapter.kafka.model;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.adapters.data.dto.IncidentEventPayload;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;

/** Kafka event for publishing process incident notifications to the incident topic. */
@Setter
public class IncidentEvent implements KafkaEvent<IncidentEventPayload> {

  private IncidentEventPayload incidentEventPayload;

  @Override
  public String getTopicName() {
    return KafkaUtils.getIncidentEventTopic();
  }

  @Override
  public IncidentEventPayload getValue() {
    return incidentEventPayload;
  }

  @Override
  public Map<String, String> getHeaders() {
    HashMap<String, String> headers = new HashMap<>();
    headers.put("processDefinitionId", incidentEventPayload.getProcessDefinitionId());
    headers.put("processInstanceId", incidentEventPayload.getProcessInstanceId());
    return headers;
  }

  @Override
  public String getKey() { // TBD: have to think on this?
    return KafkaEvent.super.getKey();
  }
}
