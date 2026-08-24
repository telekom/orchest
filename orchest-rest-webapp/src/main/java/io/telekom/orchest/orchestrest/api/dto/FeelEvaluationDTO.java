package io.telekom.orchest.orchestrest.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a FEEL playground evaluation. On success, {@code success} is {@code true} and {@code
 * result} carries the evaluated value (which may be {@code null} for FEEL nulls). On failure,
 * {@code success} is {@code false} and {@code error} carries the engine's failure message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeelEvaluationDTO {

  private boolean success;
  private Object result;
  private String error;
}
