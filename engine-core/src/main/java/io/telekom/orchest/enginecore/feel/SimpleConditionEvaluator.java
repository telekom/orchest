package io.telekom.orchest.enginecore.feel;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;

/** Lightweight condition evaluator that resolves conditions by direct variable lookup (no FEEL). */
public class SimpleConditionEvaluator implements ConditionEvaluator {
  @Override
  public boolean evaluate(String condition, ProcessInstance instance) {
    if (condition == null || condition.isBlank() || "true".equalsIgnoreCase(condition)) {
      return true;
    }
    Object val = instance.getVariables().get(condition);
    return Boolean.TRUE.equals(val);
  }
}
