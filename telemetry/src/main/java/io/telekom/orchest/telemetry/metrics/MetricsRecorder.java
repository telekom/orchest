package io.telekom.orchest.telemetry.metrics;

import java.util.Map;

/**
 * Hot-path SPI for engine code to record metric increments.
 *
 * <p>Call sites only see this interface; the production implementation ({@code
 * MicrometerMetricsRecorder}) delegates to a Micrometer {@code MeterRegistry}. For modules that
 * don't need persistent metrics, {@link NoOpMetricsRecorder} is wired automatically and turns every
 * {@link #increment(MetricKey, long)} call into a no-op.
 *
 * <p>Implementations must be thread-safe and the {@code increment} hot-path must complete in O(1)
 * without taking any locks that contend with engine work.
 */
public interface MetricsRecorder {

  /** Records {@code delta} occurrences for {@code key}. */
  void increment(MetricKey key, long delta);

  /** Records exactly one occurrence for {@code key}. */
  default void increment(MetricKey key) {
    increment(key, 1L);
  }

  /**
   * Atomically returns the per-key delta accumulated since the previous {@code drainSnapshot} (or
   * since startup) and advances the high-water mark so subsequent calls report only newly
   * accumulated work. The underlying counters are never reset.
   *
   * <p>Implementations may return an empty map if there is no new work to flush.
   */
  Map<MetricKey, Long> drainSnapshot();

  /**
   * Returns the per-key delta currently accumulated since the previous flush
   * <strong>without</strong> advancing the high-water mark. Intended for diagnostic endpoints.
   */
  Map<MetricKey, Long> peekSnapshot();

  /**
   * Re-stages a previously drained snapshot back into the recorder. Used by the flush service to
   * recover from a transient persistence failure without losing counts.
   */
  void restore(Map<MetricKey, Long> deltas);
}
