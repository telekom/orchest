package io.telekom.orchest.orchestrest.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the FEEL playground validate endpoint. Carries the FEEL expression whose syntax
 * should be checked.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeelValidateRequest {

  /** The FEEL expression to validate. May optionally start with '=' (which is stripped). */
  @NotBlank(message = "expression is required")
  private String expression;
}
