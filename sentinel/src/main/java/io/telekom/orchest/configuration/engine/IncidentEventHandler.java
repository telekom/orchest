package io.telekom.orchest.configuration.engine;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.api.core.adapters.data.dto.IncidentEventPayload;
import io.telekom.orchest.api.core.adapters.data.model.Incident;
import io.telekom.orchest.api.core.adapters.data.repository.IncidentRepository;
import io.telekom.orchest.enginecore.IncidentEventHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Persists incidents to MongoDB and publishes incident events via Kafka. */
@Slf4j
@RequiredArgsConstructor
public class IncidentEventHandler implements IncidentEventHandlerAdapter {

  private final KafkaClientEventProducer eventProducer;
  private final IncidentRepository incidentRepository;

  /**
   * Persists the incident and publishes a Kafka event.
   *
   * @param event the incident event payload
   */
  @Override
  public void handle(IncidentEventPayload event) {
    incidentRepository.save(
        Incident.builder()
            .processInstanceId(event.getProcessInstanceId())
            .processDefinitionId(event.getProcessDefinitionId())
            .version(event.getVersion())
            .activityId(event.getActivityId())
            .activityName(event.getActivityName())
            .stackTrace(event.getIncidentMessage())
            .build());
    eventProducer.sendIncidentEvent(event);
  }

  /**
   * Publishes an incident resolution event via Kafka.
   *
   * @param event the incident resolution event payload
   */
  @Override
  public void handleResolution(IncidentEventPayload event) {
    eventProducer.sendIncidentResolutionEvent(event);
  }
}
