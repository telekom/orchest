package io.telekom.orchest.api.core.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for updating or deleting variables on a running process instance. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVariableRequest {
  /** The process instance whose variables should be modified. */
  private String processInstanceId;

  /** The variable payload containing the values and the action (UPDATE or DELETE). */
  private Variables variables;
}
