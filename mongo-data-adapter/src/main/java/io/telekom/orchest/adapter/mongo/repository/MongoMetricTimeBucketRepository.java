package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.MetricTimeBucket;
import io.telekom.orchest.telemetry.metrics.MetricType;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data Mongo handle for the {@code metric_time_buckets} collection. As with {@link
 * MongoMetricLifetimeTotalRepository}, only the read path goes through this interface; writes use
 * {@code BulkOperations}.
 */
public interface MongoMetricTimeBucketRepository extends MongoRepository<MetricTimeBucket, String> {

  /**
   * Finds time-bucketed metrics for a given type within a time range, ordered by bucket start
   * ascending.
   *
   * @param metricType the metric type to filter by
   * @param from the inclusive start of the time range
   * @param to the exclusive end of the time range
   * @return ordered list of matching time buckets
   */
  List<MetricTimeBucket>
      findAllByMetricTypeAndBucketStartGreaterThanEqualAndBucketStartLessThanOrderByBucketStartAsc(
          MetricType metricType, Instant from, Instant to);

  /**
   * Finds time-bucketed metrics for a given type and definition within a time range, ordered by
   * bucket start ascending.
   *
   * @param metricType the metric type to filter by
   * @param definitionId the process or decision definition identifier
   * @param from the inclusive start of the time range
   * @param to the exclusive end of the time range
   * @return ordered list of matching time buckets
   */
  List<MetricTimeBucket>
      findAllByMetricTypeAndDefinitionIdAndBucketStartGreaterThanEqualAndBucketStartLessThanOrderByBucketStartAsc(
          MetricType metricType, String definitionId, Instant from, Instant to);
}
