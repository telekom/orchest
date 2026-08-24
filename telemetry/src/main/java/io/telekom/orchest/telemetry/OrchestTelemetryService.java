package io.telekom.orchest.telemetry;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service for reporting telemetry data and metrics. Uses Micrometer to publish metrics to
 * configured backends (e.g., Prometheus). Defines common metric names and helper methods for
 * incrementing counters.
 */
@Component
@RequiredArgsConstructor
public class OrchestTelemetryService {

  @Autowired(required = false)
  private MeterRegistry meterRegistry;

  private final ModuleProperties moduleProperties;
  public static final String ORCHEST_PREFIX = "orchest.";
  public static final String BPMN = "BPMN";
  public static final String DMN = "DMN";
  public static final String DEPLOYMENT_METRIC_NAME =
      ORCHEST_PREFIX + "bpmn.deployment.received.event";
  public static final String PROCESS_INVOCATION_METRIC_NAME =
      ORCHEST_PREFIX + "process.invocations.received.event";

  public static final String SENT_WORKER_METRIC_NAME = ORCHEST_PREFIX + "client.worker.sent.event";
  public static final String RECEIVED_WORKER_METRIC_NAME =
      ORCHEST_PREFIX + "client.worker.received.event";
  public static final String MESSAGE_EVENT_METRIC_NAME = ORCHEST_PREFIX + "message.received.event";
  public static final String SIGNAL_EVENT_METRIC_NAME = ORCHEST_PREFIX + "signal.received.event";
  public static final String TIMER_EVENT_METRIC_NAME = ORCHEST_PREFIX + "timer.triggred.event";
  public static final String PROCESS_INCIDENT_METRIC_NAME = ORCHEST_PREFIX + "incident.event";

  /**
   * Increments a counter metric with specific tags.
   *
   * @param counterName The name of the metric.
   * @param tags The tags associated with the metric.
   */
  public void incrementMetric(String counterName, Iterable<Tag> tags) {
    if (metricsExportOff()) return;
    meterRegistry.counter(counterName, tags).increment();
  }

  /**
   * Increments a counter metric.
   *
   * @param counterName The name of the metric.
   */
  public void incrementMetric(String counterName) {
    if (metricsExportOff()) return;
    meterRegistry.counter(counterName).increment();
  }

  /**
   * Helper to increment resource deployment metrics.
   *
   * @param metricName The name of the metric.
   * @param definitionId The ID of the deployed resource.
   * @param version The version of the resource.
   * @param type The type of resource (e.g., BPMN, DMN).
   */
  public void incrementResourceDeploymentMetrics(
      String metricName, String definitionId, Integer version, String type) {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("definitionId", definitionId));
    tags.add(Tag.of("version", String.valueOf(version)));
    tags.add(Tag.of("type", type));
    incrementMetric(metricName, tags);
  }

  /**
   * Generic helper to increment metrics with standard tags (definitionId, version, type).
   *
   * @param metricName The name of the metric.
   * @param definitionId The ID of the resource.
   * @param version The version of the resource.
   * @param type The type context.
   */
  public void incrementMetrics(
      String metricName, String definitionId, Integer version, String type, String state) {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("definitionId", definitionId));
    tags.add(Tag.of("version", String.valueOf(version)));
    tags.add(Tag.of("type", type));
    tags.add(Tag.of("state", state));
    incrementMetric(metricName, tags);
  }

  /**
   * Checks whether metrics export is disabled.
   *
   * @return true if telemetry is disabled, false otherwise
   */
  public boolean metricsExportOff() {
    return !moduleProperties.isEnabled();
  }
}
