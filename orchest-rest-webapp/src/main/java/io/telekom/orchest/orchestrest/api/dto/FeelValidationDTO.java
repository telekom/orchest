package io.telekom.orchest.orchestrest.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a FEEL playground syntax validation. {@code valid} is {@code true} when the expression
 * parses successfully; otherwise {@code error} carries the parse failure message reported by the
 * FEEL engine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeelValidationDTO {

  private boolean valid;
  private String error;
}
