package io.telekom.orchest.api.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object returned after a resource (e.g., process definition) deployment. Contains details
 * about the deployed artifact and its status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDeploymentResponse {

  /** The unique identifier of the deployed process definition. */
  private String processId;

  /** The version assigned to the deployed process definition. */
  private int version;

  /** The status of the deployment (e.g., "DEPLOYED", "FAILED"). */
  private String status;
}
