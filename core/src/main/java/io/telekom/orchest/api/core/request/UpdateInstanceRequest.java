package io.telekom.orchest.api.core.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for modifying a running process instance by moving its execution token. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInstanceRequest {

  /** The process instance to modify. */
  private String processInstanceId;

  /** The node ID to move the execution token from. */
  private String fromNodeId;

  /** The node ID to move the execution token to. */
  private String toNodeId;
}
