package io.telekom.orchest.telemetry.metrics;

import java.time.Instant;

/**
 * One time bucket of metric activity used for time-series queries.
 *
 * <p>Storage granularity is always {@link MetricGranularity#MINUTE}; the controller folds buckets
 * up to coarser granularities at query time so writes stay cheap.
 *
 * @param key the metric stream this bucket belongs to
 * @param bucketStart inclusive minute the bucket represents (truncated to minute)
 * @param count number of increments aggregated into this bucket
 * @param lastUpdatedAt instant of the most recent flush that updated this bucket
 */
public record MetricTimeBucket(
    MetricKey key, Instant bucketStart, long count, Instant lastUpdatedAt) {}
