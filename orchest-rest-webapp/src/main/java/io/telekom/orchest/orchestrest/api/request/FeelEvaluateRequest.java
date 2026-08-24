package io.telekom.orchest.orchestrest.api.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the FEEL playground evaluate endpoint. Carries the FEEL expression and the
 * variable bindings against which it is evaluated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeelEvaluateRequest {

  /** The FEEL expression to evaluate. May optionally start with '=' (which is stripped). */
  @NotBlank(message = "expression is required")
  private String expression;

  /** Variable bindings made available to the expression during evaluation. */
  private Map<String, Object> variables;
}
