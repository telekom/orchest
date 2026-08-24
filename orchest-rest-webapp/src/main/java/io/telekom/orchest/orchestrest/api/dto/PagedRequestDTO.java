package io.telekom.orchest.orchestrest.api.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal request object carrying pagination, filtering, and sorting parameters for list queries.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedRequestDTO {

  /** Filter by process/decision definition ID. */
  private String definitionId;

  /** Filter by definition version. */
  private Integer version;

  /** Free-text search across instance data. */
  private String searchText;

  /** Filter by instance state (e.g. ACTIVE, COMPLETED, INCIDENT). */
  private String state;

  /** Inclusive lower bound on createdAt (UTC). */
  private LocalDateTime from;

  /** Exclusive upper bound on createdAt (UTC). */
  private LocalDateTime to;

  /** Zero-based page index. */
  private int page;

  /** Page size. */
  private int size;

  /** Sort field with direction prefix (+ASC / -DESC). */
  private String sort;
}
