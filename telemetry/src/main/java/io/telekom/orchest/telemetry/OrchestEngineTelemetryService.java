package io.telekom.orchest.telemetry;

import static io.telekom.orchest.telemetry.OrchestTelemetryService.*;

import io.micrometer.core.instrument.Tag;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Service for reporting engine-side metrics. Tracks resource deployments, process invocations, and
 * worker interactions from the engine's perspective.
 */
@Component
@RequiredArgsConstructor
public class OrchestEngineTelemetryService {

  private final OrchestTelemetryService orchestTelemetryService;

  private static final String ENGINE_SUFFIX = ".engine";

  /**
   * Records a resource deployment metric.
   *
   * @param processDefinitionId The ID of the process definition.
   * @param version The version deployed.
   * @param type The type (e.g., BPMN).
   */
  public void incrementResourceDeploymentMetrics(
      String processDefinitionId, int version, String type, String state) {
    orchestTelemetryService.incrementMetrics(
        DEPLOYMENT_METRIC_NAME + ENGINE_SUFFIX, processDefinitionId, version, type, state);
  }

  /**
   * Records a process invocation metric.
   *
   * @param processDefinitionId The ID of the process definition.
   * @param version The version invoked.
   */
  public void incrementProcessInvocationCounter(
      String processDefinitionId, int version, String state) {
    orchestTelemetryService.incrementMetrics(
        PROCESS_INVOCATION_METRIC_NAME + ENGINE_SUFFIX, processDefinitionId, version, BPMN, state);
  }

  /**
   * Records a metric for workers sent by the engine.
   *
   * @param state The state.
   * @param workerId The worker ID.
   * @param processDefinitionId The process definition ID.
   * @param version The version.
   */
  public void incrementSentWorkerCounter(
      String state, String workerId, String processDefinitionId, Integer version) {
    createWithDefaultTags(state, workerId, processDefinitionId, version, SENT_WORKER_METRIC_NAME);
  }

  /**
   * Records a metric for workers received by the engine.
   *
   * @param state The state.
   * @param workerId The worker ID.
   * @param processDefinitionId The process definition ID.
   * @param version The version.
   */
  public void incrementReceivedWorkerCounter(
      String state, String workerId, String processDefinitionId, Integer version) {
    createWithDefaultTags(
        state, workerId, processDefinitionId, version, RECEIVED_WORKER_METRIC_NAME);
  }

  private void createWithDefaultTags(
      String state,
      String workerId,
      String processDefinitionId,
      Integer version,
      String receivedWorkerMetricName) {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("state", state));
    if (workerId != null) {
      tags.add(Tag.of("workerId", workerId));
    }
    tags.add(Tag.of("processDefinitionId", processDefinitionId));
    tags.add(Tag.of("version", String.valueOf(version)));
    orchestTelemetryService.incrementMetric(receivedWorkerMetricName + ENGINE_SUFFIX, tags);
  }

  /**
   * Records a signal event metric.
   *
   * @param signalName The signal name.
   */
  public void incrementSignalEventCounter(String signalName, String state) {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("signalName", signalName));
    tags.add(Tag.of("state", state));
    orchestTelemetryService.incrementMetric(SIGNAL_EVENT_METRIC_NAME + ENGINE_SUFFIX, tags);
  }

  /**
   * Records a message event metric.
   *
   * @param messageName The message name.
   */
  public void incrementMessageEventCounter(String messageName, String state) {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("messageName", messageName));
    tags.add(Tag.of("state", state));
    orchestTelemetryService.incrementMetric(MESSAGE_EVENT_METRIC_NAME + ENGINE_SUFFIX, tags);
  }

  /** Records a timer event metric. */
  public void incrementTimerEventCounter() {
    orchestTelemetryService.incrementMetric(TIMER_EVENT_METRIC_NAME + ENGINE_SUFFIX);
  }

  /**
   * Records an incident event metric.
   *
   * @param activityName the name of the activity where the incident occurred
   * @param processDefinitionId the process definition ID
   * @param version the process version
   */
  public void incrementIncidentEvent(
      String activityName, String processDefinitionId, Integer version) {
    createWithDefaultTags(
        "incident", activityName, processDefinitionId, version, PROCESS_INCIDENT_METRIC_NAME);
  }
}
