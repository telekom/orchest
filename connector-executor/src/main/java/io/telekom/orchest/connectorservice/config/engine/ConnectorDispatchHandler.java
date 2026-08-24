package io.telekom.orchest.connectorservice.config.engine;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.api.core.adapters.data.dto.ConnectorTaskRequest;
import io.telekom.orchest.enginecore.bpmn.ConnectorDispatchAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Re-dispatches connector tasks to the connector topic. Used when the embedded engine advances the
 * process onto another connector node after resuming.
 */
@Slf4j
@RequiredArgsConstructor
public class ConnectorDispatchHandler implements ConnectorDispatchAdapter {

  private final KafkaClientEventProducer eventProducer;

  @Override
  public void dispatch(ConnectorTaskRequest event) {
    log.info(
        "re-dispatching connector task for instanceId: {} and activity: {}",
        event.getProcessInstanceId(),
        event.getActivityId());
    eventProducer.sendConnectorTaskEvent(event);
  }
}
