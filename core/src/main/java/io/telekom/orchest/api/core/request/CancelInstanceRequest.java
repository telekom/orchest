package io.telekom.orchest.api.core.request;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request object for canceling one or more process instances. */
@Data
@NoArgsConstructor
public class CancelInstanceRequest {
  /** List of process instances to cancel. */
  private List<Request> instances;

  /**
   * Record representing a single process instance cancellation request.
   *
   * @param instanceId The ID of the process instance to cancel.
   * @param processDefinitionId The ID of the process definition.
   */
  public record Request(String instanceId, String processDefinitionId) {}
}
