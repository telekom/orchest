package io.telekom.orchest.web.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A single (bucketStart, count) point on a metric time series. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesPointResponse {
  private Instant bucketStart;
  private long count;
}
