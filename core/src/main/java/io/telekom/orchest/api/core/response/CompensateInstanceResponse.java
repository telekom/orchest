package io.telekom.orchest.api.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response payload returned after successfully triggering compensation on a process instance. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensateInstanceResponse {

  /** The unique identifier of the newly created compensation process instance. */
  private String processInstanceId;

  /** The identifier of the process definition used for compensation. */
  private String processDefinitionId;

  /** The version of the process definition used for compensation. */
  private Integer version;

  /** The identifier of the original process instance that was compensated. */
  private String compensatedInstanceId;
}
