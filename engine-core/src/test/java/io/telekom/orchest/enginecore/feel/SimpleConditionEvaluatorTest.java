package io.telekom.orchest.enginecore.feel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests SimpleConditionEvaluator: null/blank short-circuits and direct variable boolean lookup. */
class SimpleConditionEvaluatorTest {

  private final SimpleConditionEvaluator evaluator = new SimpleConditionEvaluator();

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t"})
  @DisplayName("null or blank condition is true")
  void nullOrBlank_true(String condition) {
    ProcessInstance pi = new ProcessInstance("i", "d", 1);
    assertTrue(evaluator.evaluate(condition, pi));
  }

  @ParameterizedTest
  @ValueSource(strings = {"true", "TRUE", "True"})
  @DisplayName("literal true condition is true")
  void literalTrue_true(String condition) {
    ProcessInstance pi = new ProcessInstance("i", "d", 1);
    assertTrue(evaluator.evaluate(condition, pi));
  }

  @Test
  @DisplayName("variable name resolves to Boolean.TRUE in instance variables")
  void variableName_true() {
    ProcessInstance pi = new ProcessInstance("i", "d", 1);
    pi.getVariables().put("flag", true);
    assertTrue(evaluator.evaluate("flag", pi));
  }

  @Test
  @DisplayName("variable name resolves to Boolean.FALSE")
  void variableName_false() {
    ProcessInstance pi = new ProcessInstance("i", "d", 1);
    pi.getVariables().put("flag", false);
    assertFalse(evaluator.evaluate("flag", pi));
  }

  @Test
  @DisplayName("missing variable is false")
  void missingVariable_false() {
    ProcessInstance pi = new ProcessInstance("i", "d", 1);
    assertFalse(evaluator.evaluate("missing", pi));
  }

  @Test
  @DisplayName("non-boolean variable value is false")
  void nonBoolean_false() {
    ProcessInstance pi = new ProcessInstance("i", "d", 1);
    pi.getVariables().put("x", "string");
    assertFalse(evaluator.evaluate("x", pi));
  }
}
