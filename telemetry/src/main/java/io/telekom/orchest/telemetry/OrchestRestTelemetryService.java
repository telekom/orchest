package io.telekom.orchest.telemetry;

import static io.telekom.orchest.telemetry.OrchestTelemetryService.*;

import io.micrometer.core.instrument.Tag;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Service for reporting metrics from the REST API layer. Tracks operations performed via REST
 * endpoints, such as process starts, retries, and variable updates.
 */
@Component
@RequiredArgsConstructor
public class OrchestRestTelemetryService {

  private final OrchestTelemetryService orchestTelemetryService;

  private static final String REST_SUFFIX = ".rest";
  private static final String REST_RETRY_EVENT_TOTAL_METRIC_NAME = ORCHEST_PREFIX + "retry.event";
  private static final String REST_CANCEL_EVENT_TOTAL_METRIC_NAME = ORCHEST_PREFIX + "cancel.event";
  private static final String REST_VARIABLES_UPDATE_TOTAL_METRIC_NAME =
      ORCHEST_PREFIX + "variables.update.event";
  private static final String REST_MESSAGE_EVENT_TOTAL_METRIC_NAME =
      ORCHEST_PREFIX + "message.event";
  private static final String REST_SIGNAL_EVENT_TOTAL_METRIC_NAME = ORCHEST_PREFIX + "signal.event";

  /**
   * Increments the process invocation counter for REST-initiated starts.
   *
   * @param processDefinitionId The process definition ID.
   * @param version The version.
   */
  public void incrementRestProcessInvocationCounter(String processDefinitionId, Integer version) {
    orchestTelemetryService.incrementMetrics(
        PROCESS_INVOCATION_METRIC_NAME + REST_SUFFIX,
        processDefinitionId,
        version,
        BPMN,
        "success");
  }

  /**
   * Increments the resource deployment counter for REST-initiated deployments.
   *
   * @param processDefinitionId The process definition ID.
   * @param version The version.
   * @param type The resource type.
   */
  public void incrementResourceDeploymentMetrics(
      String processDefinitionId, int version, String type) {
    orchestTelemetryService.incrementResourceDeploymentMetrics(
        DEPLOYMENT_METRIC_NAME + REST_SUFFIX, processDefinitionId, version, type);
  }

  /** Increments the retry event counter. */
  public void incrementRestRetryEventCounter() {
    orchestTelemetryService.incrementMetric(REST_RETRY_EVENT_TOTAL_METRIC_NAME + REST_SUFFIX);
  }

  /**
   * Increments the cancel instance counter.
   *
   * @param processDefinitionId The process definition ID of the cancelled instance.
   */
  public void incrementCancelInstanceCounter(String processDefinitionId) {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("processDefinitionId", processDefinitionId));
    orchestTelemetryService.incrementMetric(
        REST_CANCEL_EVENT_TOTAL_METRIC_NAME + REST_SUFFIX, tags);
  }

  /** Increments the variable update counter. */
  public void incrementVariablesUpdateCounter() {
    orchestTelemetryService.incrementMetric(REST_VARIABLES_UPDATE_TOTAL_METRIC_NAME + REST_SUFFIX);
  }

  /**
   * Increments the counter for message events published via REST.
   *
   * @param messageName The BPMN message name.
   */
  public void incrementRestMessageEventCounter(String messageName) {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("messageName", messageName));
    orchestTelemetryService.incrementMetric(
        REST_MESSAGE_EVENT_TOTAL_METRIC_NAME + REST_SUFFIX, tags);
  }

  /**
   * Increments the counter for signal events published via REST.
   *
   * @param signalName The BPMN signal name.
   */
  public void incrementRestSignalEventCounter(String signalName) {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("signalName", signalName));
    orchestTelemetryService.incrementMetric(
        REST_SIGNAL_EVENT_TOTAL_METRIC_NAME + REST_SUFFIX, tags);
  }
}
