package io.telekom.orchest.orchestrest.extensions.approvalflow;

/** Enumeration representing the possible states of a deployment approval request. */
public enum DeploymentApprovalState {
  /** The deployment has been requested and is pending approval. */
  REQUESTED,
  /** The deployment has been approved by an authorized reviewer. */
  ACCEPTED,
  /** The deployment request has been rejected. */
  REJECTED
}
