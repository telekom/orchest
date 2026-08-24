package io.telekom.orchest.configuration.engine;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.enginecore.bpmn.ServiceTaskHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Adapter handler which send the Service task worker event to client via Kafka Events */
@Slf4j
@RequiredArgsConstructor
public class ServiceTaskHandler implements ServiceTaskHandlerAdapter {

  private final KafkaClientEventProducer eventProducer;

  /**
   * Dispatches a service task worker event via Kafka.
   *
   * @param event the worker event request
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
