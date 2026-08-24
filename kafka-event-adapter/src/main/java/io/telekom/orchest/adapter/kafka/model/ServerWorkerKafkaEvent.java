package io.telekom.orchest.adapter.kafka.model;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import java.util.Map;
import lombok.Setter;

/**
 * Kafka event for sending worker task completions back to the engine via per-process server topics.
 */
@Setter
public class ServerWorkerKafkaEvent implements KafkaEvent<WorkerEventRequest> {

  private WorkerEventRequest workerEvent;

  @Override
  public String getTopicName() {
    return KafkaUtils.getServerWorkerEventTopic(workerEvent.getProcessDefinitionId());
  }

  @Override
  public WorkerEventRequest getValue() {
    return workerEvent;
  }

  @Override
  public Map<String, String> getHeaders() {
    return Map.of();
  }

  @Override
  public String getKey() { // prefer parent processId
    return workerEvent.getParentProcessInstanceId() != null
        ? workerEvent.getParentProcessInstanceId()
        : workerEvent.getProcessInstanceId();
  }
}
