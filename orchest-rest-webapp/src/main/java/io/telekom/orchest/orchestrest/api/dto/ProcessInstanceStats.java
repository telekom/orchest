package io.telekom.orchest.orchestrest.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Aggregated counts of process instances by state for the statistics dashboard. */
@Data
@NoArgsConstructor
public class ProcessInstanceStats {

  private Integer total = 0;
  private Integer completed = 0;
  private Integer hold = 0;
  private Integer failed = 0;
  private Integer incidents = 0;
  private Integer active = 0;
  private Integer started = 0;
  private Integer terminated = 0;

  public Integer getTotal() {
    return completed + total + hold + failed + active + incidents + terminated;
  }
}
