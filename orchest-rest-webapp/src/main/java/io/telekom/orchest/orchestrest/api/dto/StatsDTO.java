package io.telekom.orchest.orchestrest.api.dto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data transfer object carrying aggregated process and decision instance statistics. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsDTO {

  private List<Stats> totalProcessStats;
  private List<Stats> totalDecisionStats;
  // <PROCESS_ID, <VERSION, [{state, count}]>>
  private Map<String, Map<String, List<Stats>>> processStats;
  private Map<String, Map<String, List<Stats>>> decisionStats;

  private OffsetDateTime lastUpdatedAt;

  public record Stats(String state, int count) {}

  public void copy(StatsDTO other) {
    this.totalProcessStats =
        other.totalProcessStats != null ? new ArrayList<>(other.totalProcessStats) : null;

    this.totalDecisionStats =
        other.totalDecisionStats != null ? new ArrayList<>(other.totalDecisionStats) : null;

    this.processStats = deepCopyStatsMap(other.processStats);
    this.decisionStats = deepCopyStatsMap(other.decisionStats);
    this.lastUpdatedAt = other.lastUpdatedAt;
  }

  private Map<String, Map<String, List<Stats>>> deepCopyStatsMap(
      Map<String, Map<String, List<Stats>>> source) {

    if (source == null) {
      return null;
    }
    return source.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                outerEntry ->
                    outerEntry.getValue().entrySet().stream()
                        .collect(
                            Collectors.toMap(
                                Map.Entry::getKey,
                                innerEntry -> new ArrayList<>(innerEntry.getValue())))));
  }
}
