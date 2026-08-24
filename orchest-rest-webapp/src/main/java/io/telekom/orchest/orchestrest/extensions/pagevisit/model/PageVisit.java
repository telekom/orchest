package io.telekom.orchest.orchestrest.extensions.pagevisit.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** MongoDB document representing a single page visit record (one per user per URL per day). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pageVisits")
public class PageVisit {

  @Id private String id;
  private String url;
  private String userId;
  private LocalDate date;
}
