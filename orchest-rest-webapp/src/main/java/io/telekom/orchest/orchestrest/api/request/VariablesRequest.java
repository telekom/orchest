package io.telekom.orchest.orchestrest.api.request;

import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for modifying variables on a running process instance. */
@Data
@NoArgsConstructor
public class VariablesRequest {

  /** The mutation action to perform on the variables. */
  private Action action;

  /** Target process instance ID. */
  private String processInstanceId;

  /** The variables to add, update, or delete (keys only for DELETE). */
  private Map<String, Object> variables;

  /** Supported variable mutation actions. */
  public enum Action {
    ADD,
    UPDATE,
    DELETE
  }
}
