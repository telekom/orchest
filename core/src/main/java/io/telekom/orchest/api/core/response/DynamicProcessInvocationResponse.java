package io.telekom.orchest.api.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object returned after a dynamic process invocation request. Contains details about the
 * initiated process instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicProcessInvocationResponse {

  /** The unique identifier of the created dynamic process instance. */
  private String processInstanceId;
}
