package io.telekom.orchest.connectorservice.config.engine;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.enginecore.bpmn.ServiceTaskHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends service task worker events to clients via Kafka. Required because the connector service
 * runs an embedded workflow engine that may advance the process onto regular (non-connector)
 * service tasks after resuming a connector node.
 */
@Slf4j
@RequiredArgsConstructor
public class ServiceTaskHandler implements ServiceTaskHandlerAdapter {

  private final KafkaClientEventProducer eventProducer;

  @Override
  public void handle(WorkerEventRequest event) {
    log.info(
        "dispatching service task worker event for instanceId: {} and activity: {}",
        event.getProcessInstanceId(),
        event.getActivityId());
    eventProducer.sendClientWorkerEvent(event);
  }
}
