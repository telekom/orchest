package io.telekom.orchest.enginecore.feel;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import java.util.HashMap;
import java.util.Map;
import org.camunda.feel.FeelEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.util.Either;

/**
 * FEEL-based condition evaluator using the Camunda FEEL engine for gateway condition resolution.
 */
public class FeelConditionEvaluator implements ConditionEvaluator {
  private static final Logger log = LoggerFactory.getLogger(FeelConditionEvaluator.class);

  private final FeelEngine feelEngine;

  public FeelConditionEvaluator() {
    // Initialize FEEL engine with default configuration
    // In 1.18.0, custom value mapper might be different or default handles it.
    // Trying default builder first.
    this.feelEngine = new FeelEngine.Builder().build();
  }

  @Override
  public boolean evaluate(String condition, ProcessInstance instance) {
    if (condition == null || condition.isBlank()) {
      return true;
    }

    try {
      Map<String, Object> variables = new HashMap<>(instance.getVariables());

      Either<FeelEngine.Failure, Object> result = feelEngine.evalExpression(condition, variables);

      if (result.isRight()) {
        Object value = result.right().get();
        if (value instanceof Boolean) {
          return (Boolean) value;
        } else {
          log.warn("Condition '{}' did not evaluate to a boolean. Result: {}", condition, value);
          return false;
        }
      } else {
        FeelEngine.Failure failure = result.left().get();
        log.error("Failed to evaluate FEEL condition '{}': {}", condition, failure.message());
        throw new RuntimeException("FEEL evaluation failed: " + failure.message());
      }
    } catch (Exception e) {
      log.error("Error evaluating condition '{}'", condition, e);
      throw new RuntimeException("Condition evaluation failed", e);
    }
  }
}
