package io.telekom.orchest.telemetry.metrics;

import java.util.Collections;
import java.util.Map;

/**
 * Recorder that discards every increment. Wired into modules that don't participate in persistent
 * metrics collection (e.g. {@code orchest-rest}, {@code sentinel}) so engine code never has to
 * null-check its dependency.
 */
public final class NoOpMetricsRecorder implements MetricsRecorder {

  public static final NoOpMetricsRecorder INSTANCE = new NoOpMetricsRecorder();

  private NoOpMetricsRecorder() {}

  @Override
  public void increment(MetricKey key, long delta) {
    // intentional no-op
  }

  @Override
  public Map<MetricKey, Long> drainSnapshot() {
    return Collections.emptyMap();
  }

  @Override
  public Map<MetricKey, Long> peekSnapshot() {
    return Collections.emptyMap();
  }

  @Override
  public void restore(Map<MetricKey, Long> deltas) {
    // intentional no-op
  }
}
