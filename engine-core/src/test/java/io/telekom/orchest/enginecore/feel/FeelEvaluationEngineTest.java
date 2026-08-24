package io.telekom.orchest.enginecore.feel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.camunda.feel.api.EvaluationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests FeelEvaluationEngine: expression evaluation, unary tests, and extraction utilities. */
class FeelEvaluationEngineTest {

  @Test
  @DisplayName("unary tests: ? < 0 is true for negative input")
  void unaryTests_lessThan_negative() {
    EvaluationResult r = FeelEvaluationEngine.evaluateFeelExpression("? < 0", -5, Map.of());
    assertNotNull(r);
    assertTrue(r.isSuccess());
    assertEquals(true, r.result());
  }

  @Test
  @DisplayName("unary tests: ? < 0 is false for positive input")
  void unaryTests_lessThan_positive() {
    EvaluationResult r = FeelEvaluationEngine.evaluateFeelExpression("? < 0", 5, Map.of());
    assertNotNull(r);
    assertTrue(r.isSuccess());
    assertEquals(false, r.result());
  }

  @Test
  @DisplayName("unary tests: leading '=' is stripped like other expressions")
  void unaryTests_withEqualsPrefix() {
    EvaluationResult r = FeelEvaluationEngine.evaluateFeelExpression("=? < 0", -1, Map.of());
    assertNotNull(r);
    assertTrue(r.isSuccess());
    assertEquals(true, r.result());
  }

  @Test
  @DisplayName("unary tests: ?.path on map input (nested property access)")
  void unaryTests_optionalPath_notNull() {
    Map<String, Object> row = Map.of("specificationId", Map.of("name", "n1"));
    EvaluationResult r =
        FeelEvaluationEngine.evaluateFeelExpression(
            "?.specificationId.name != null", row, Map.of());
    assertNotNull(r);
    assertTrue(r.isSuccess(), () -> r.isFailure() ? r.failure().message() : "expected success");
    assertEquals(true, r.result());
  }

  @Test
  @DisplayName("null expression yields null result")
  void nullExpression() {
    assertNull(FeelEvaluationEngine.evaluateFeelExpression(null, 0, Map.of()));
  }
}
