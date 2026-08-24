package io.telekom.orchest.web.dto;

import io.telekom.orchest.telemetry.metrics.MetricGranularity;
import io.telekom.orchest.telemetry.metrics.MetricType;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Time series response for a single metric type, optionally filtered by definitionId. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesResponse {
  private MetricType metricType;
  private String definitionId;
  private MetricGranularity granularity;
  private Instant from;
  private Instant to;
  private long total;
  private List<TimeSeriesPointResponse> points;
}
