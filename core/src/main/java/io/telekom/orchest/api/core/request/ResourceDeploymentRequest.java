package io.telekom.orchest.api.core.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for deploying a BPMN or DMN resource to the engine. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDeploymentRequest {

  /** process topic partition count for concurrency */
  @Builder.Default private Integer partitionCount = 10;

  /** UTF8 XML of the definition */
  private String resourceUTF8XML;

  /** to bypass worker validation */
  @Builder.Default private Boolean bypassWorkerValidation = false;

  /**
   * approver emails who can approve the deployment for 1st deployment it will be the current user.
   */
  private List<String> approvers;

  /** Whether this deployment includes a compensation flow definition. */
  @Builder.Default private Boolean compensateFlow = false;
}
