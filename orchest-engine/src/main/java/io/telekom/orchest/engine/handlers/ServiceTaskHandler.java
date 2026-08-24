package io.telekom.orchest.engine.handlers;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.enginecore.bpmn.ServiceTaskHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Adapter handler implementation that delegates Service Task worker events to Kafka. */
@Slf4j
@RequiredArgsConstructor
public class ServiceTaskHandler implements ServiceTaskHandlerAdapter {

  private final KafkaClientEventProducer eventProducer;

  /**
   * Publishes a service task worker event to Kafka for downstream processing.
   *
   * @param event the worker event request containing task details
   */
  @Override
  public void handle(WorkerEventRequest event) {
    log.info(
        "insideServiceTaskHandler serviceTask handler for instanceId: {} and activity: {}",
        event.getProcessInstanceId(),
        event.getActivityId());
    eventProducer.sendClientWorkerEvent(event);
  }
}
