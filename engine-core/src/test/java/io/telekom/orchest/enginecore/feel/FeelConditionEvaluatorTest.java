package io.telekom.orchest.enginecore.feel;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests FEEL condition evaluator with various expression types and edge cases. */
class FeelConditionEvaluatorTest {

  private final FeelConditionEvaluator evaluator = new FeelConditionEvaluator();

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  ", "\n"})
  @DisplayName("null or blank condition short-circuits to true")
  void blankCondition_true(String condition) {
    ProcessInstance pi = new ProcessInstance("i", "d", 1);
    assertTrue(evaluator.evaluate(condition, pi));
  }

  @Test
  @DisplayName("boolean FEEL expression evaluates against variables")
  void feelBooleanExpression() {
    ProcessInstance pi = new ProcessInstance("i", "d", 1);
    Map<String, Object> vars = new HashMap<>();
    vars.put("x", 2);
    pi.setVariables(vars);
    assertTrue(evaluator.evaluate("x > 1", pi));
    assertFalse(evaluator.evaluate("x < 1", pi));
  }

  @Test
  @DisplayName("non-boolean successful result yields false and does not throw")
  void nonBooleanResult_false() {
    ProcessInstance pi = new ProcessInstance("i", "d", 1);
    pi.getVariables().put("n", 5);
    assertFalse(evaluator.evaluate("n", pi));
  }

  @Test
  @DisplayName("invalid FEEL expression throws RuntimeException")
  void invalidExpression_throws() {
    ProcessInstance pi = new ProcessInstance("i", "d", 1);
    assertThrows(RuntimeException.class, () -> evaluator.evaluate("@@@invalid@@@", pi));
  }
}
