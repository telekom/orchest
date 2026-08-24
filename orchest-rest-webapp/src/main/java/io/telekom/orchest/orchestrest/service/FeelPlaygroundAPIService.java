package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.enginecore.feel.FeelEvaluationEngine;
import io.telekom.orchest.orchestrest.api.dto.FeelEvaluationDTO;
import io.telekom.orchest.orchestrest.api.dto.FeelValidationDTO;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.camunda.feel.api.EvaluationResult;
import org.camunda.feel.api.ParseResult;
import org.springframework.stereotype.Component;

/**
 * Service backing the FEEL playground REST endpoints. Delegates expression evaluation and syntax
 * validation to {@link FeelEvaluationEngine} and translates the engine's results into transport
 * DTOs.
 */
@Slf4j
@Component
public class FeelPlaygroundAPIService {

  public FeelEvaluationDTO evaluate(String expression, Map<String, Object> variables) {
    Map<String, Object> safeVariables = variables == null ? Collections.emptyMap() : variables;
    EvaluationResult result =
        FeelEvaluationEngine.evaluateFeelExpression(expression, safeVariables);

    if (result == null) {
      return FeelEvaluationDTO.builder()
          .success(false)
          .error("Expression must not be blank")
          .build();
    }

    if (result.isFailure()) {
      return FeelEvaluationDTO.builder().success(false).error(result.failure().message()).build();
    }

    return FeelEvaluationDTO.builder().success(true).result(result.result()).build();
  }

  public FeelValidationDTO validate(String expression) {
    ParseResult parseResult = FeelEvaluationEngine.validateExpression(expression);

    if (parseResult == null) {
      return FeelValidationDTO.builder().valid(false).error("Expression must not be blank").build();
    }

    if (parseResult.isFailure()) {
      return FeelValidationDTO.builder()
          .valid(false)
          .error(parseResult.failure().message())
          .build();
    }

    return FeelValidationDTO.builder().valid(true).build();
  }
}
