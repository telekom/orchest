package io.telekom.orchest.enginecore.feel;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;

/**
 * Interface for evaluating FEEL (Friendly Enough Expression Language) conditions. Used to evaluate
 * gateway conditions, boundary event conditions, and other conditional expressions in BPMN
 * processes.
 */
public interface ConditionEvaluator {
  /**
   * Evaluates a FEEL condition expression against a process instance's variables.
   *
   * @param condition The FEEL expression to evaluate.
   * @param instance The process instance containing variables for evaluation.
   * @return true if the condition evaluates to true, false otherwise.
   */
  boolean evaluate(String condition, ProcessInstance instance);
}
