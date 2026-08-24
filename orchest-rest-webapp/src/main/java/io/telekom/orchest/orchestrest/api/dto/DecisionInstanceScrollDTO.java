package io.telekom.orchest.orchestrest.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of scrolling decision instances by offset range {@code [from, to)} over the filtered,
 * sorted sequence.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionInstanceScrollDTO {

  private List<DecisionInstanceDTO> content;

  /** Inclusive start offset of this slice in the filtered result sequence. */
  private int from;

  /** Exclusive end offset of this slice (same semantics as {@link List#subList(int, int)}). */
  private int to;

  /**
   * True when at least one more matching row exists beyond the last row in {@code content}
   * (determined via a single extra fetched document, not a count query).
   */
  private boolean hasNext;
}
