package io.telekom.orchest.engine.handlers;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.api.core.adapters.data.dto.ConnectorTaskRequest;
import io.telekom.orchest.enginecore.bpmn.ConnectorDispatchAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Dispatches connector task events to the connector service via Kafka. */
@Slf4j
@RequiredArgsConstructor
public class ConnectorDispatchHandler implements ConnectorDispatchAdapter {

  private final KafkaClientEventProducer eventProducer;

  /**
   * Dispatches a connector task request to the connector service via Kafka.
   *
   * @param event the connector task request to dispatch
   */
  @Override
  public void dispatch(ConnectorTaskRequest event) {
    log.info(
        "dispatching connector task for instanceId: {} and activity: {}",
        event.getProcessInstanceId(),
        event.getActivityId());
    eventProducer.sendConnectorTaskEvent(event);
  }
}
