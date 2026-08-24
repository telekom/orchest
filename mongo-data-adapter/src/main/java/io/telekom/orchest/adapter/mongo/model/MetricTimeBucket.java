package io.telekom.orchest.adapter.mongo.model;

import io.telekom.orchest.telemetry.metrics.MetricType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Mongo persistence shape for one minute-granularity time bucket.
 *
 * <p>{@code _id} is composed as {@code
 * <metricType>::<definitionId|_>::<version|_>::<bucketEpochMillis>} so each (key, minute) tuple
 * owns exactly one document and the upsert filter is free.
 *
 * <p>All indices for this collection (including the TTL on {@code bucketStart}) are declared
 * imperatively in {@code MongoIndicesConfiguration}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class MetricTimeBucket {

  /** Composite key encoding metric type, definition, version, and bucket epoch. */
  @Id private String id;

  /** The type of metric being tracked. */
  private MetricType metricType;

  /** The process or decision definition ID this bucket relates to. */
  private String definitionId;

  /** The version of the definition (null for version-agnostic buckets). */
  private Integer version;

  /** The start instant of this one-minute time bucket. */
  private Instant bucketStart;

  /** Event count accumulated in this bucket. */
  private long count;

  /** Timestamp when this bucket was last incremented. */
  private Instant lastUpdatedAt;
}
