package io.telekom.orchest.telemetry.metrics;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Time-bucket granularity for metric storage and querying.
 *
 * <p>The flush pipeline always persists at the finest granularity ({@link #MINUTE}); coarser
 * buckets ({@link #HOUR}, {@link #DAY}) are produced at query time by aggregating minute buckets in
 * the application layer. This avoids amplifying flush writes by 3x and keeps a single source of
 * truth in storage.
 */
public enum MetricGranularity {
  MINUTE(ChronoUnit.MINUTES, Duration.ofMinutes(1)),
  HOUR(ChronoUnit.HOURS, Duration.ofHours(1)),
  DAY(ChronoUnit.DAYS, Duration.ofDays(1));

  private final ChronoUnit unit;
  private final Duration step;

  MetricGranularity(ChronoUnit unit, Duration step) {
    this.unit = unit;
    this.step = step;
  }

  /** Returns the bucket-start instant for {@code at}, truncated to this granularity. */
  public Instant bucketStart(Instant at) {
    return at.truncatedTo(unit);
  }

  /** Step size between two consecutive buckets. */
  public Duration step() {
    return step;
  }
}
