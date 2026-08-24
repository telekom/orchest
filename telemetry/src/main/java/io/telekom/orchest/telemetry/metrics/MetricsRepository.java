package io.telekom.orchest.telemetry.metrics;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Persistence SPI for the metrics pipeline.
 *
 * <p>Implementations must be safe to call concurrently from multiple pods. The lifetime-totals
 * write path is expected to use atomic per-document upserts (e.g. MongoDB's {@code $inc}) so that
 * concurrent flushes from N pods aggregate correctly without coordination.
 *
 * <p>The time-bucket write path is bucketed at the storage granularity returned by {@link
 * #storageGranularity()}; coarser granularities are produced by summation in the controller layer.
 */
public interface MetricsRepository {

  /** Storage-side granularity used for time-bucket persistence (typically MINUTE). */
  default MetricGranularity storageGranularity() {
    return MetricGranularity.MINUTE;
  }

  /**
   * Atomically applies all increments to both the lifetime-totals and the time-bucket collections.
   * Implementations should use a single batched / bulk operation per collection to minimise
   * database round-trips.
   */
  void applyIncrements(Collection<MetricIncrement> increments);

  /**
   * Returns lifetime totals for streams matching the optional filters. {@code null} means "any" for
   * that filter, e.g. {@code findLifetimeTotals(null, null)} returns every total.
   */
  List<MetricLifetimeTotal> findLifetimeTotals(MetricType type, String definitionId);

  /** Returns the lifetime total for an exact {@link MetricKey} if it exists. */
  Optional<MetricLifetimeTotal> findLifetimeTotal(MetricKey key);

  /**
   * Returns minute-granularity time buckets matching {@code type} (required) and {@code
   * definitionId} (optional, {@code null} = "any") with {@code from <= bucketStart < to}.
   */
  List<MetricTimeBucket> findTimeBuckets(
      MetricType type, String definitionId, Instant from, Instant to);
}
