package io.telekom.orchest.engine.handlers;

import static io.telekom.orchest.alerting.api.RaiseAlertRequest.alertKey;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.alerting.api.RaiseAlertRequest;
import io.telekom.orchest.alerting.service.AlertLifecycleService;
import io.telekom.orchest.api.core.adapters.data.dto.IncidentEventPayload;
import io.telekom.orchest.api.core.adapters.data.model.Incident;
import io.telekom.orchest.api.core.adapters.data.repository.IncidentRepository;
import io.telekom.orchest.enginecore.IncidentEventHandlerAdapter;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.StringUtils;

/**
 * Handles incident events by persisting them, publishing to Kafka, and optionally raising alerts.
 */
@Slf4j
@RequiredArgsConstructor
public class IncidentEventHandler implements IncidentEventHandlerAdapter {

  static final String ALERT_SOURCE = "orchest-engine";

  private final KafkaClientEventProducer eventProducer;
  private final IncidentRepository incidentRepository;
  private final ObjectProvider<AlertLifecycleService> alertLifecycleService;

  /**
   * Persists the incident, publishes it to Kafka, and raises an alert if alerting is available.
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
    raiseAlert(event);
  }

  /**
   * Publishes an incident resolution event to Kafka.
   *
   * @param event the incident resolution event payload
   */
  @Override
  public void handleResolution(IncidentEventPayload event) {
    eventProducer.sendIncidentResolutionEvent(event);
  }

  private void raiseAlert(IncidentEventPayload event) {
    AlertLifecycleService lifecycle = alertLifecycleService.getIfAvailable();
    if (lifecycle == null) {
      return;
    }
    try {
      lifecycle.raise(toRaiseRequest(event));
    } catch (Exception e) {
      log.error(
          "Failed to raise telemetry alert for processInstanceId={}",
          event.getProcessInstanceId(),
          e);
    }
  }

  static RaiseAlertRequest toRaiseRequest(IncidentEventPayload event) {
    String definitionId = blankTo(event.getProcessDefinitionId(), "unknown");
    String activityId = blankTo(event.getActivityId(), "unknown");
    Integer version = event.getVersion();
    String activityLabel =
        StringUtils.hasText(event.getActivityName()) ? event.getActivityName() : activityId;

    String subject =
        "OrchesT Incident on Process:" + definitionId + ":v" + version + " / " + activityLabel;
    String body = buildBody(event, definitionId, activityLabel);

    Map<String, String> metadata = new HashMap<>();
    putIfPresent(metadata, "processDefinitionId", event.getProcessDefinitionId());
    putIfPresent(metadata, "processInstanceId", event.getProcessInstanceId());
    putIfPresent(metadata, "activityId", event.getActivityId());
    putIfPresent(metadata, "activityName", event.getActivityName());
    putIfPresent(metadata, "correlationId", event.getCorrelationId());
    putIfPresent(metadata, "namespace", event.getNamespace());
    if (event.getVersion() != null) {
      metadata.put("version", String.valueOf(event.getVersion()));
    }

    return RaiseAlertRequest.builder()
        .source(ALERT_SOURCE)
        .alertKey(alertKey(definitionId, activityId))
        .subject(subject)
        .body(body)
        .severity("critical")
        .metadata(metadata)
        .build();
  }

  private static String buildBody(
      IncidentEventPayload event, String definitionId, String activityLabel) {
    StringBuilder body = new StringBuilder();
    body.append(
        StringUtils.hasText(event.getIncidentMessage())
            ? event.getIncidentMessage()
            : "An incident occurred during process execution.");
    body.append("\n\n");
    body.append("Process definition: ").append(definitionId).append('\n');
    body.append("Activity: ").append(activityLabel).append('\n');
    if (StringUtils.hasText(event.getProcessInstanceId())) {
      body.append("Process instance: ").append(event.getProcessInstanceId()).append('\n');
    }
    if (event.getVersion() != null) {
      body.append("Version: ").append(event.getVersion()).append('\n');
    }
    if (StringUtils.hasText(event.getCorrelationId())) {
      body.append("Correlation ID: ").append(event.getCorrelationId()).append('\n');
    }
    if (StringUtils.hasText(event.getNamespace())) {
      body.append("Namespace: ").append(event.getNamespace()).append('\n');
    }
    return body.toString();
  }

  private static void putIfPresent(Map<String, String> metadata, String key, String value) {
    if (StringUtils.hasText(value)) {
      metadata.put(key, value);
    }
  }

  private static String blankTo(String value, String fallback) {
    return StringUtils.hasText(value) ? value : fallback;
  }
}
