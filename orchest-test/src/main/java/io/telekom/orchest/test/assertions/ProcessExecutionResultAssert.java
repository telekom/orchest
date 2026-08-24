package io.telekom.orchest.test.assertions;

import io.telekom.orchest.test.engine.ProcessExecutionResult;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.api.AbstractAssert;

/** AssertJ-style assertions for {@link ProcessExecutionResult}. */
public class ProcessExecutionResultAssert
    extends AbstractAssert<ProcessExecutionResultAssert, ProcessExecutionResult> {

  /**
   * Creates a new assertion for the given process execution result.
   *
   * @param actual the process execution result to assert on
   */
  public ProcessExecutionResultAssert(ProcessExecutionResult actual) {
    super(actual, ProcessExecutionResultAssert.class);
  }

  /**
   * Creates a new assertion for the given process execution result.
   *
   * @param actual the process execution result to assert on
   * @return a new {@link ProcessExecutionResultAssert} instance
   */
  public static ProcessExecutionResultAssert assertThat(ProcessExecutionResult actual) {
    return new ProcessExecutionResultAssert(actual);
  }

  /** Asserts that the process completed successfully. */
  public ProcessExecutionResultAssert isCompleted() {
    isNotNull();
    if (!actual.isCompleted()) {
      failWithMessage(
          "Expected process to be completed, but it failed. Failure: %s",
          actual.getFailureException() != null
              ? actual.getFailureException().getMessage()
              : "unknown");
    }
    return this;
  }

  /** Asserts that the process failed. */
  public ProcessExecutionResultAssert hasFailed() {
    isNotNull();
    if (!actual.hasFailed()) {
      failWithMessage("Expected process to have failed, but it completed successfully");
    }
    return this;
  }

  /** Asserts that the process has a specific number of steps. */
  public ProcessExecutionResultAssert hasStepCount(int expectedCount) {
    isNotNull();
    if (actual.getTotalSteps() != expectedCount) {
      failWithMessage("Expected %d steps, but found %d", expectedCount, actual.getTotalSteps());
    }
    return this;
  }

  /** Asserts that all steps completed successfully. */
  public ProcessExecutionResultAssert allStepsCompleted() {
    isNotNull();
    if (actual.getFailedSteps() > 0) {
      failWithMessage("Expected all steps to complete, but %d failed", actual.getFailedSteps());
    }
    return this;
  }

  /** Asserts that a specific final variable exists and has the expected value. */
  public ProcessExecutionResultAssert hasFinalVariable(String key, Object expectedValue) {
    isNotNull();
    Object actualValue = actual.getFinalVariable(key);
    if (!Objects.equals(actualValue, expectedValue)) {
      failWithMessage(
          "Expected final variable '%s' to be %s, but was %s", key, expectedValue, actualValue);
    }
    return this;
  }

  /** Asserts that a specific final variable exists. */
  public ProcessExecutionResultAssert hasFinalVariable(String key) {
    isNotNull();
    if (!actual.getFinalVariables().containsKey(key)) {
      failWithMessage("Expected final variable '%s' to exist, but it was not found", key);
    }
    return this;
  }

  /** Asserts that a step with the given worker type was executed. */
  public ProcessExecutionResultAssert hasStep(String workerType) {
    isNotNull();
    ProcessExecutionResult.StepExecutionResult stepResult = actual.getStepResult(workerType);
    if (stepResult == null) {
      failWithMessage(
          "Expected step with worker type '%s' to be executed, but it was not found", workerType);
    }
    return this;
  }

  /** Asserts that a step with the given worker type completed successfully. */
  public ProcessExecutionResultAssert stepCompleted(String workerType) {
    isNotNull();
    ProcessExecutionResult.StepExecutionResult stepResult = actual.getStepResult(workerType);
    if (stepResult == null) {
      failWithMessage(
          "Expected step with worker type '%s' to be executed, but it was not found", workerType);
    }
    if (!stepResult.isCompleted()) {
      failWithMessage("Expected step '%s' to be completed, but it failed", workerType);
    }
    return this;
  }

  /** Asserts that the process executed within the given time limit. */
  public ProcessExecutionResultAssert executedWithin(long maxTimeMs) {
    isNotNull();
    if (actual.getExecutionTimeMs() > maxTimeMs) {
      failWithMessage(
          "Expected process to execute within %d ms, but it took %d ms",
          maxTimeMs, actual.getExecutionTimeMs());
    }
    return this;
  }

  /** Asserts that the final variables match the expected map. */
  public ProcessExecutionResultAssert hasFinalVariables(Map<String, Object> expectedVariables) {
    isNotNull();
    Map<String, Object> actualVariables = actual.getFinalVariables();
    for (Map.Entry<String, Object> entry : expectedVariables.entrySet()) {
      if (!actualVariables.containsKey(entry.getKey())) {
        failWithMessage(
            "Expected final variable '%s' to exist, but it was not found", entry.getKey());
      }
      if (!Objects.equals(actualVariables.get(entry.getKey()), entry.getValue())) {
        failWithMessage(
            "Expected final variable '%s' to be %s, but was %s",
            entry.getKey(), entry.getValue(), actualVariables.get(entry.getKey()));
      }
    }
    return this;
  }
}
