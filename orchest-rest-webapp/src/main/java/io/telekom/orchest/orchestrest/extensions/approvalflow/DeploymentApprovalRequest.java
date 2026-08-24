package io.telekom.orchest.orchestrest.extensions.approvalflow;

import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request object for initiating a new deployment approval workflow. */
@Data
@NoArgsConstructor
public class DeploymentApprovalRequest {

  /** The underlying resource deployment request containing the definition XML. */
  private ResourceDeploymentRequest deploymentRequest;

  /** The type of resource being deployed (e.g., "BPMN", "DMN"). */
  private String type; // BPMN/DMN

  /** The identifier of the user requesting the deployment. */
  private String requestedBy;
}
