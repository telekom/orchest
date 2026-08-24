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
 * Mongo persistence shape for one lifetime metric total.
 *
 * <p>The document {@code _id} is {@link io.telekom.orchest.telemetry.metrics.MetricKey#stableId()}
 * — a deterministic, human-readable string. Using the natural key as {@code _id} makes the upsert
 * filter free (no extra index needed for the write path) and lets ops grep collection contents
 * directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class MetricLifetimeTotalDocument {

  /** Stable natural key used as the document {@code _id}. */
  @Id private String id;

  /** The type of metric being tracked. */
  private MetricType metricType;

  /** The process or decision definition ID this metric relates to. */
  private String definitionId;

  /** The version of the definition (null for version-agnostic totals). */
  private Integer version;

  /** Cumulative count for this metric. */
  private long count;

  /** Timestamp when this metric was first observed. */
  private Instant firstObservedAt;

  /** Timestamp when this metric was last incremented. */
  private Instant lastUpdatedAt;
}
