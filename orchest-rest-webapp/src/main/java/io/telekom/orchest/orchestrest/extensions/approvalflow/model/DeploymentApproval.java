package io.telekom.orchest.orchestrest.extensions.approvalflow.model;

import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.orchestrest.extensions.approvalflow.DeploymentApprovalState;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a deployment approval request. Stores details about the requested
 * deployment, its current state, and the audit log.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
@CompoundIndex(name = "reviewers_-1_state_-1", def = "{'reviewers': -1, 'state': -1}")
public class DeploymentApproval {
  /** Unique identifier for the deployment approval request. */
  @Id private String id;

  /** List of reviewers assigned to this request. */
  @Indexed private List<String> reviewers;

  /** The user who requested the deployment. */
  private String requestedBy;

  /** The current state of the approval request. */
  private DeploymentApprovalState state;

  /** The type of resource being deployed (BPMN or DMN). */
  private String resourceType; // BPMN/DMN

  /** The original deployment request data. */
  private ResourceDeploymentRequest resourceDeploymentRequest;

  /** The ID of the definition being deployed. */
  private String definitionId;

  /** The version that will be assigned if deployed. */
  private String nextVersion;

  /** Audit log of the approval action. */
  private DeploymentAuditLog auditLog;

  /** Timestamp when the request was created. */
  @Indexed @CreatedDate private Instant createdAt;

  /** Timestamp when the approval action was taken. */
  private Instant approvalTime;

  /** Inner class representing the audit log for a deployment action. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DeploymentAuditLog {
    /** The state resulting from the action (ACCEPTED/REJECTED). */
    private DeploymentApprovalState approvalState;

    /** The user who performed the action. */
    private String approver;

    /** The time the action was performed. */
    private Instant createTime;
  }
}
