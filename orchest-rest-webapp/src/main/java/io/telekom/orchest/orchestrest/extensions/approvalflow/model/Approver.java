package io.telekom.orchest.orchestrest.extensions.approvalflow.model;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing the configuration of approvers for a specific process. Defines who
 * is authorized to approve deployments for a given process definition ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Approver {

  /** Unique identifier for the approver configuration. */
  @Id private String id;

  /** The process definition ID this configuration applies to. Must be unique. */
  @Indexed(unique = true)
  private String processId;

  /** List of user identifiers authorized to approve deployments. */
  private List<String> approvers;

  /** Timestamp when the configuration was created. */
  @CreatedDate private OffsetDateTime createdAt;

  /** Timestamp when the configuration was last modified. */
  @LastModifiedDate private OffsetDateTime lastModifiedAt;
}
