package io.telekom.orchest.telemetry.metrics;

import java.time.Instant;

/**
 * Single delta emitted by the flush service to the {@link MetricsRepository}.
 *
 * @param key logical identity of the metric stream
 * @param delta non-negative count of increments accumulated since the previous flush
 * @param podId identifier of the emitting pod (typically the {@code HOSTNAME} env var); may be
 *     {@code null} when run outside of a Kubernetes pod
 * @param flushAt wall-clock instant at which the flush snapshot was taken
 */
public record MetricIncrement(MetricKey key, long delta, String podId, Instant flushAt) {

  public MetricIncrement {
    if (key == null) {
      throw new IllegalArgumentException("key must not be null");
    }
    if (delta < 0) {
      throw new IllegalArgumentException("delta must be non-negative");
    }
    if (flushAt == null) {
      throw new IllegalArgumentException("flushAt must not be null");
    }
  }
}
