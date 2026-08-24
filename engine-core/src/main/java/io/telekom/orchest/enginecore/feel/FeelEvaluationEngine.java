package io.telekom.orchest.enginecore.feel;

import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.feel.api.EvaluationResult;
import org.camunda.feel.api.FeelEngineApi;
import org.camunda.feel.api.FeelEngineBuilder;
import org.camunda.feel.api.ParseResult;

/** Static utility that wraps the Camunda FEEL engine API for expression parsing and evaluation. */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeelEvaluationEngine {

  private static final FeelEngineApi feelEngine;

  static {
    feelEngine = FeelEngineBuilder.forJava().build();
  }

  /**
   * Validates the syntax of a FEEL expression without evaluating it. Returns a {@link ParseResult}
   * whose {@code isSuccess()} indicates whether the expression parses, and whose {@code
   * failure().message()} carries the parse error when it does not. A {@code null} or blank
   * expression is reported as a parse failure.
   */
  public static ParseResult validateExpression(String expression) {
    Optional<String> feelExpression = extractExpression(expression);
    return feelExpression.map(feelEngine::parseExpression).orElse(null);
  }

  /**
   * Evaluates a FEEL <em>value</em> expression (e.g. DMN input expression) with the given
   * variables.
   */
  public static EvaluationResult evaluateFeelExpression(
      String expression, Map<String, Object> inputVariables) {
    Optional<String> feelExpression = extractExpression(expression);
    return feelExpression.map(s -> feelEngine.evaluateExpression(s, inputVariables)).orElse(null);
  }

  /**
   * Evaluates FEEL <em>unary tests</em> (e.g. {@code ? &lt; 0}, {@code ?.x != null}) against {@code
   * input}. The value {@code input} is bound to {@code ?}; {@code context} supplies named
   * variables.
   */
  public static EvaluationResult evaluateFeelExpression(
      String expression, Object input, Map<String, Object> context) {
    Optional<String> feelExpression = extractExpression(expression);
    return feelExpression.map(s -> feelEngine.evaluateUnaryTests(s, input, context)).orElse(null);
  }

  /**
   * Evaluates a FEEL expression and returns the result, or empty on failure.
   *
   * @param expression the FEEL expression (optionally prefixed with '=')
   * @param inputVariables the variable context
   * @return the evaluation result, or empty if expression is null or evaluation fails
   */
  public static Optional<Object> evaluateExpression(
      String expression, Map<String, Object> inputVariables) {
    Optional<String> feelExpression = extractExpression(expression);
    if (feelExpression.isEmpty()) {
      return Optional.empty();
    }
    EvaluationResult result = feelEngine.evaluateExpression(feelExpression.get(), inputVariables);

    if (result.isFailure()) {
      log.warn(
          "Failed to evaluate input expression '{}' {}", expression, result.failure().message());
      return Optional.empty();
    }

    if (result.suppressedFailures() != null && !result.suppressedFailures().isEmpty()) {
      throw new RuntimeException(result.suppressedFailures().iterator().next().failureMessage());
    }

    return Optional.of(result.result());
  }

  /**
   * Strips the leading '=' prefix from a FEEL expression if present.
   *
   * @param fx the raw expression string
   * @return the cleaned expression, or empty if null
   */
  public static Optional<String> extractExpression(String fx) {
    if (fx == null) return Optional.empty();
    return fx.startsWith("=") ? fx.substring(1).describeConstable() : fx.describeConstable();
  }

  /**
   * Evaluates a value that may be a literal or a FEEL expression (prefixed with '=').
   *
   * @param fx the value or FEEL expression
   * @param inputVariables the variable context
   * @return the resolved value, or the literal string if not an expression
   */
  public static Optional<Object> evaluateSimpleVariable(
      String fx, Map<String, Object> inputVariables) {
    if (fx == null || !fx.startsWith("=")) {
      return Optional.ofNullable(fx);
    }
    return evaluateExpression(fx, inputVariables);
  }
}
