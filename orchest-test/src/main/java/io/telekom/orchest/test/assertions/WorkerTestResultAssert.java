package io.telekom.orchest.test.assertions;

import io.telekom.orchest.test.engine.WorkerTestResult;
import java.util.Map;
import org.assertj.core.api.AbstractAssert;

/**
 * Fluent assertion class for {@link WorkerTestResult}. Provides expressive assertions for verifying
 * worker execution outcomes.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * assertThat(result)
 *     .isCompleted()
 *     .hasVariable("orderId", "ORD-123")
 *     .hasNoErrorEvent()
 *     .executedWithin(2000);
 * }</pre>
 */
public class WorkerTestResultAssert
    extends AbstractAssert<WorkerTestResultAssert, WorkerTestResult> {

  /**
   * Creates a new assertion for the given worker test result.
   *
   * @param actual the worker test result to assert on
   */
  public WorkerTestResultAssert(WorkerTestResult actual) {
    super(actual, WorkerTestResultAssert.class);
  }

  // ---- State assertions ----

  /** Asserts that the worker completed successfully (no exception thrown). */
  public WorkerTestResultAssert isCompleted() {
    isNotNull();
    if (!actual.isCompleted()) {
      failWithMessage(
          "Expected worker '%s' to complete successfully, but it threw: %s",
          actual.getWorkerType(),
          actual.getException() != null ? actual.getException().getMessage() : "unknown error");
    }
    return this;
  }

  /** Asserts that the worker failed with an exception. */
  public WorkerTestResultAssert hasFailed() {
    isNotNull();
    if (!actual.hasFailed()) {
      failWithMessage(
          "Expected worker '%s' to fail with an exception, but it completed successfully",
          actual.getWorkerType());
    }
    return this;
  }

  /** Asserts that the worker failed with an exception of the specified type. */
  public WorkerTestResultAssert hasFailedWith(Class<? extends Exception> exceptionType) {
    hasFailed();
    if (!exceptionType.isInstance(actual.getException())) {
      failWithMessage(
          "Expected worker '%s' to fail with %s, but got %s: %s",
          actual.getWorkerType(),
          exceptionType.getSimpleName(),
          actual.getException().getClass().getSimpleName(),
          actual.getException().getMessage());
    }
    return this;
  }

  // ---- Error event assertions ----

  /** Asserts that the worker returned an error event. */
  public WorkerTestResultAssert hasErrorEvent() {
    isNotNull();
    if (!actual.hasErrorEvent()) {
      failWithMessage(
          "Expected worker '%s' to produce an error event, but none was found",
          actual.getWorkerType());
    }
    return this;
  }

  /** Asserts that the worker returned an error event with the specified error code. */
  public WorkerTestResultAssert hasErrorCode(String expectedCode) {
    hasErrorEvent();
    if (!expectedCode.equals(actual.getErrorCode())) {
      failWithMessage("Expected error code '%s' but got '%s'", expectedCode, actual.getErrorCode());
    }
    return this;
  }

  /** Asserts that the worker did NOT return an error event. */
  public WorkerTestResultAssert hasNoErrorEvent() {
    isNotNull();
    if (actual.hasErrorEvent()) {
      failWithMessage(
          "Expected worker '%s' to not produce an error event, but found error: %s (%s)",
          actual.getWorkerType(), actual.getErrorName(), actual.getErrorCode());
    }
    return this;
  }

  // ---- Message event assertions ----

  /** Asserts that the worker returned a message event. */
  public WorkerTestResultAssert hasMessageEvent() {
    isNotNull();
    if (!actual.hasMessageEvent()) {
      failWithMessage(
          "Expected worker '%s' to produce a message event, but none was found",
          actual.getWorkerType());
    }
    return this;
  }

  // ---- Variable assertions ----

  /** Asserts that the worker produced output variables. */
  public WorkerTestResultAssert hasVariables() {
    isNotNull();
    if (!actual.hasVariables()) {
      failWithMessage(
          "Expected worker '%s' to produce output variables, but none were found",
          actual.getWorkerType());
    }
    return this;
  }

  /** Asserts that the worker produced a specific output variable with the expected value. */
  public WorkerTestResultAssert hasVariable(String key, Object expectedValue) {
    hasVariables();
    Object actualValue = actual.getOutputVariable(key);
    if (actualValue == null) {
      failWithMessage(
          "Expected output variable '%s' with value '%s', but variable was not present. Available: %s",
          key, expectedValue, actual.getOutputVariables().keySet());
    } else if (!expectedValue.equals(actualValue)) {
      failWithMessage(
          "Expected output variable '%s' to be '%s', but was '%s'",
          key, expectedValue, actualValue);
    }
    return this;
  }

  /** Asserts that the worker output contains all the specified variables. */
  public WorkerTestResultAssert hasVariables(Map<String, Object> expectedVariables) {
    hasVariables();
    expectedVariables.forEach(this::hasVariable);
    return this;
  }

  /** Asserts that the worker produced an output variable with the specified key (any value). */
  public WorkerTestResultAssert hasVariableKey(String key) {
    hasVariables();
    if (!actual.getOutputVariables().containsKey(key)) {
      failWithMessage(
          "Expected output variable key '%s' to be present, but available keys are: %s",
          key, actual.getOutputVariables().keySet());
    }
    return this;
  }

  // ---- Response type assertions ----

  /** Asserts that the worker returned a non-null {@code TaskResponse} (not auto-complete). */
  public WorkerTestResultAssert hasTaskResponse() {
    isNotNull();
    if (actual.getTaskResponse() == null) {
      failWithMessage(
          "Expected worker '%s' to return a TaskResponse, but it returned null (auto-complete)",
          actual.getWorkerType());
    }
    return this;
  }

  /** Asserts that the worker returned null (auto-complete behavior). */
  public WorkerTestResultAssert isAutoCompleted() {
    isNotNull();
    if (actual.getTaskResponse() != null) {
      failWithMessage(
          "Expected worker '%s' to auto-complete (return null), but it returned a TaskResponse",
          actual.getWorkerType());
    }
    return this;
  }

  // ---- Performance assertions ----

  /**
   * Asserts that the worker executed within the specified time limit.
   *
   * @param maxMillis Maximum execution time in milliseconds.
   */
  public WorkerTestResultAssert executedWithin(long maxMillis) {
    isNotNull();
    if (actual.getExecutionTimeMs() > maxMillis) {
      failWithMessage(
          "Expected worker '%s' to execute within %dms, but it took %dms",
          actual.getWorkerType(), maxMillis, actual.getExecutionTimeMs());
    }
    return this;
  }
}
