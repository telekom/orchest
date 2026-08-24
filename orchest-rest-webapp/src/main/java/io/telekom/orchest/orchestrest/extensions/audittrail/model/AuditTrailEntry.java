package io.telekom.orchest.orchestrest.extensions.audittrail.model;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** MongoDB document representing a single audit trail entry for a mutating API request. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "auditTrails")
public class AuditTrailEntry {

  @Id private String id;

  @Indexed private String userEmail;

  private List<String> roles;

  private boolean admin;

  private String httpMethod;

  @Indexed private String path;

  private int responseStatus;

  private String correlationId;

  @CreatedDate @Indexed private OffsetDateTime createdAt;
}
