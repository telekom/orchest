package io.telekom.orchest.api.core.adapters.data.dto;

import io.telekom.orchest.api.core.model.dmn.Rule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO containing the result of a DMN decision table evaluation. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DecisionEvaluationResponseDTO {

  /** Rules that matched during the decision evaluation. */
  @Builder.Default private List<Rule> matchedRules = new ArrayList<>();

  /** Input variables supplied to the decision evaluation. */
  @Builder.Default private Map<String, Object> inputVariables = new HashMap<>();

  /** Output variables produced by the matched rules. */
  @Builder.Default private Map<String, Object> outputVariables = new HashMap<>();
}
