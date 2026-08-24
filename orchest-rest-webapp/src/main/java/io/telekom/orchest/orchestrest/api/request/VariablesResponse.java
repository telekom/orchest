package io.telekom.orchest.orchestrest.api.request;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response payload containing the current variables of a process instance. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariablesResponse {

  /** The process instance variables. */
  private Map<String, Object> variables;
}
