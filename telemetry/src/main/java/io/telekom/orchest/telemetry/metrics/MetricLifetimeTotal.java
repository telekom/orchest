package io.telekom.orchest.telemetry.metrics;

import java.time.Instant;

/**
 * Aggregated lifetime total for a single {@link MetricKey}.
 *
 * <p>This is the read-side projection produced by {@link MetricsRepository}.
 *
 * @param key the metric stream this total describes
 * @param count total count across all engine pods over the lifetime of the data store
 * @param firstObservedAt instant the first increment landed (set on document insert)
 * @param lastUpdatedAt instant of the most recent flush that updated the count
 */
public record MetricLifetimeTotal(
    MetricKey key, long count, Instant firstObservedAt, Instant lastUpdatedAt) {}
