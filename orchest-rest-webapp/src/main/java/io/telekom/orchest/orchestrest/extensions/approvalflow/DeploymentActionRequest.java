package io.telekom.orchest.orchestrest.extensions.approvalflow;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Request object for submitting an action (approve/reject) on a deployment approval. */
@Data
@NoArgsConstructor
public class DeploymentActionRequest {

  /** The unique identifier of the approval request. */
  private String deploymentRequestId;

  /** The identifier of the user performing the action (approver). */
  private String approver;

  /** The new state of the approval (e.g., ACCEPTED, REJECTED). */
  private DeploymentApprovalState state;
}
