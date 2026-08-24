package io.telekom.orchest.adapter.kafka.model;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;

/** Kafka event for dispatching worker tasks to per-process client worker event topics. */
@Setter
public class ClientWorkerKafkaEvent implements KafkaEvent<WorkerEventRequest> {

  private WorkerEventRequest workerEvent;

  @Override
  public String getTopicName() {
    return KafkaUtils.getClientWorkerEventTopic(workerEvent.getProcessDefinitionId());
  }

  @Override
  public WorkerEventRequest getValue() {
    return workerEvent;
  }

  @Override
  public Map<String, String> getHeaders() {
    HashMap<String, String> headers = new HashMap<>();
    headers.put("processDefinitionId", workerEvent.getProcessDefinitionId());
    headers.put("processInstanceId", workerEvent.getProcessInstanceId());
    headers.put(
        "version",
        workerEvent.getVersion() == null ? "latest" : workerEvent.getVersion().toString());
    return headers;
  }

  @Override
  public String getKey() { // TBD: have to think on this?
    //        if (workerEvent != null && workerEvent.getProcessInstanceId() != null) {
    //            return workerEvent.getProcessInstanceId();
    //        }
    return KafkaEvent.super.getKey();
  }
}
