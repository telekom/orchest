package io.telekom.orchest.api.core.request;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Encapsulates a set of process variables along with the action to perform (update or delete). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Variables {
  /** The variable key-value pairs. */
  private Map<String, Object> variables;

  /** The action to apply to these variables (defaults to UPDATE). */
  @Builder.Default private VariableAction action = VariableAction.UPDATE;

  /** Defines the operation to perform on process variables. */
  public enum VariableAction {
    /** Merge or overwrite the specified variables. */
    UPDATE,
    /** Remove the specified variables from the process instance. */
    DELETE
  }
}
