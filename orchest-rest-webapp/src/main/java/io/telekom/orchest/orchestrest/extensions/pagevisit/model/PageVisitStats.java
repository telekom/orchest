package io.telekom.orchest.orchestrest.extensions.pagevisit.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Aggregation result representing the unique user count for a URL on a given date. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVisitStats {
  private String url;
  private LocalDate date;
  private long uniqueUsers;
}
