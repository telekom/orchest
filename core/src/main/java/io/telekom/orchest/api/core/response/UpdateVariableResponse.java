package io.telekom.orchest.api.core.response;

import io.telekom.orchest.api.core.request.Variables;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response payload returned after updating variables on a process instance. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVariableResponse {

  /** The unique identifier of the variable update operation. */
  private String id;

  /** The identifier of the process instance whose variables were updated. */
  private String processInstanceId;

  /** The updated variables. */
  private Variables variables;
}
