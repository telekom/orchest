package io.telekom.orchest.telemetry.metrics;

/**
 * Catalog of persistent platform metrics tracked by {@link MetricsRecorder}.
 *
 * <p>These complement the operational Micrometer counters exposed via {@code /actuator/prometheus}
 * by giving us <strong>lifetime totals</strong> persisted in MongoDB so consumers can answer "how
 * many BPMN process instances have ever started for definition X" without scraping a long
 * Prometheus retention window.
 *
 * <p>Add a new entry whenever a new business event needs lifetime accounting; the rest of the
 * pipeline (recorder, flush, repository, controller) is data-driven and requires no further
 * changes.
 */
public enum MetricType {

  /** A BPMN process instance was successfully started by the engine. */
  PROCESS_INSTANCE_STARTED,

  /** A BPMN process instance failed to start (e.g. unknown definition). */
  PROCESS_INSTANCE_FAILED,

  /** A DMN decision instance was evaluated by the engine. */
  DECISION_INSTANCE_EVALUATED;

  /** Lower-cased Micrometer-style suffix for the meter name (no dots/underscores). */
  public String meterSuffix() {
    return name().toLowerCase();
  }
}
