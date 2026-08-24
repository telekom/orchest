package io.telekom.orchest.web.dto;

import io.telekom.orchest.telemetry.metrics.MetricLifetimeTotal;
import io.telekom.orchest.telemetry.metrics.MetricType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** REST projection of {@link MetricLifetimeTotal}. Flat for human-friendly JSON. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LifetimeMetricResponse {
  private MetricType metricType;
  private String definitionId;
  private Integer version;
  private long count;
  private Instant firstObservedAt;
  private Instant lastUpdatedAt;

  /**
   * Converts a domain {@link MetricLifetimeTotal} into a REST-friendly response DTO.
   *
   * @param total the lifetime total to convert
   * @return a new response DTO populated from the given total
   */
  public static LifetimeMetricResponse from(MetricLifetimeTotal total) {
    return LifetimeMetricResponse.builder()
        .metricType(total.key().type())
        .definitionId(total.key().definitionId())
        .version(total.key().version())
        .count(total.count())
        .firstObservedAt(total.firstObservedAt())
        .lastUpdatedAt(total.lastUpdatedAt())
        .build();
  }
}
