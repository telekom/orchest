package io.telekom.orchest.telemetry.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Production {@link MetricsRecorder} that delegates the hot path to a Micrometer {@link Counter}.
 * Each {@link MetricKey} is materialised as exactly one {@code Counter} on the supplied {@link
 * MeterRegistry}, which means every increment is <strong>also</strong> visible to {@code
 * /actuator/prometheus} for free, in addition to being persisted to MongoDB by the {@link
 * MetricsFlushService}.
 *
 * <h2>Why this design</h2>
 *
 * <ul>
 *   <li>Micrometer's {@link Counter} is a {@code LongAdder} under the hood, so the increment
 *       hot-path is lock-free and contention-tolerant.
 *   <li>Counters are monotonic; deltas for the database flush are computed by keeping an {@link
 *       AtomicLong} per key with the count at the previous flush (the "high-water mark"). On a
 *       successful flush the mark is advanced to the current count; on a failed flush it is rewound
 *       by {@link #restore(Map)}.
 *   <li>The recorder owns its own per-key cache to avoid the cost of looking up the {@code Counter}
 *       by name+tags on every increment and to avoid having to parse {@code Meter.Id} back into a
 *       {@link MetricKey} during drain.
 * </ul>
 */
public final class MicrometerMetricsRecorder implements MetricsRecorder {

  private final MeterRegistry registry;
  private final ConcurrentHashMap<MetricKey, CounterState> counters = new ConcurrentHashMap<>();

  /**
   * Creates a new recorder backed by the given Micrometer registry.
   *
   * @param registry the Micrometer meter registry to register counters with
   * @throws IllegalArgumentException if registry is null
   */
  public MicrometerMetricsRecorder(MeterRegistry registry) {
    if (registry == null) {
      throw new IllegalArgumentException("MeterRegistry must not be null");
    }
    this.registry = registry;
  }

  @Override
  public void increment(MetricKey key, long delta) {
    if (key == null || delta <= 0) {
      return;
    }
    counters.computeIfAbsent(key, this::newCounterState).counter.increment(delta);
  }

  @Override
  public Map<MetricKey, Long> drainSnapshot() {
    Map<MetricKey, Long> out = new HashMap<>();
    counters.forEach(
        (key, state) -> {
          long current = (long) state.counter.count();
          long previous = state.lastPublished.getAndSet(current);
          long delta = current - previous;
          if (delta > 0) {
            out.put(key, delta);
          }
        });
    return out;
  }

  @Override
  public Map<MetricKey, Long> peekSnapshot() {
    Map<MetricKey, Long> out = new HashMap<>();
    counters.forEach(
        (key, state) -> {
          long delta = (long) state.counter.count() - state.lastPublished.get();
          if (delta > 0) {
            out.put(key, delta);
          }
        });
    return out;
  }

  @Override
  public void restore(Map<MetricKey, Long> deltas) {
    if (deltas == null || deltas.isEmpty()) {
      return;
    }
    deltas.forEach(
        (key, delta) -> {
          if (delta == null || delta <= 0) {
            return;
          }
          CounterState state = counters.get(key);
          if (state != null) {
            state.lastPublished.addAndGet(-delta);
          }
        });
  }

  private CounterState newCounterState(MetricKey key) {
    Counter counter =
        Counter.builder(key.meterName())
            .description("OrchesT lifetime counter for " + key.type().name())
            .tags(key.meterTags())
            .register(registry);
    return new CounterState(counter, new AtomicLong(0L));
  }

  private record CounterState(Counter counter, AtomicLong lastPublished) {}
}
