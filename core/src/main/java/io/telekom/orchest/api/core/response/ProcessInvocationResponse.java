package io.telekom.orchest.api.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object returned after a process invocation request. Contains details about the initiated
 * process instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInvocationResponse {

  /** The unique identifier of the created process instance. */
  private String processInstanceId;

  /** The identifier of the process definition. */
  private String processId;

  /** The version of the process definition used. */
  private Integer version;
}
