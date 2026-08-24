package io.telekom.orchest.test.engine;

import java.util.*;

/**
 * Result of executing an entire process flow.
 *
 * <p>Contains execution results for all steps, final variables, and execution metadata.
 */
public class ProcessExecutionResult {

  private final String processInstanceId;
  private final List<StepExecutionResult> stepResults;
  private final Map<String, Object> finalVariables;
  private final boolean completed;
  private final Exception failureException;
  private final long executionTimeMs;
  private final int totalSteps;
  private final int completedSteps;
  private final int failedSteps;

  /**
   * Constructs a process execution result.
   *
   * @param processInstanceId the process instance ID
   * @param stepResults the list of step execution results
   * @param finalVariables the final variable state after execution
   * @param completed whether the process completed successfully
   * @param failureException the exception that caused failure, or null
   * @param executionTimeMs the total execution time in milliseconds
   */
  public ProcessExecutionResult(
      String processInstanceId,
      List<StepExecutionResult> stepResults,
      Map<String, Object> finalVariables,
      boolean completed,
      Exception failureException,
      long executionTimeMs) {
    this.processInstanceId = processInstanceId;
    this.stepResults = new ArrayList<>(stepResults != null ? stepResults : List.of());
    this.finalVariables = new HashMap<>(finalVariables != null ? finalVariables : Map.of());
    this.completed = completed;
    this.failureException = failureException;
    this.executionTimeMs = executionTimeMs;
    this.totalSteps = this.stepResults.size();
    this.completedSteps =
        (int) this.stepResults.stream().filter(StepExecutionResult::isCompleted).count();
    this.failedSteps =
        (int) this.stepResults.stream().filter(StepExecutionResult::hasFailed).count();
  }

  /** Returns the process instance ID. */
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  /** Returns all step execution results in order. */
  public List<StepExecutionResult> getStepResults() {
    return Collections.unmodifiableList(stepResults);
  }

  /** Returns the final variables after process execution. */
  public Map<String, Object> getFinalVariables() {
    return Collections.unmodifiableMap(finalVariables);
  }

  /** Returns a specific final variable value. */
  public Object getFinalVariable(String key) {
    return finalVariables.get(key);
  }

  /** Returns {@code true} if the process completed successfully. */
  public boolean isCompleted() {
    return completed;
  }

  /** Returns {@code true} if the process failed. */
  public boolean hasFailed() {
    return failureException != null || failedSteps > 0;
  }

  /** Returns the exception that caused failure, if any. */
  public Exception getFailureException() {
    return failureException;
  }

  /** Returns the total execution time in milliseconds. */
  public long getExecutionTimeMs() {
    return executionTimeMs;
  }

  /** Returns the total number of steps executed. */
  public int getTotalSteps() {
    return totalSteps;
  }

  /** Returns the number of successfully completed steps. */
  public int getCompletedSteps() {
    return completedSteps;
  }

  /** Returns the number of failed steps. */
  public int getFailedSteps() {
    return failedSteps;
  }

  /** Returns the result for a specific step by index. */
  public StepExecutionResult getStepResult(int index) {
    if (index < 0 || index >= stepResults.size()) {
      return null;
    }
    return stepResults.get(index);
  }

  /** Returns the result for a specific worker type (first occurrence). */
  public StepExecutionResult getStepResult(String workerType) {
    return stepResults.stream()
        .filter(r -> workerType.equals(r.getWorkerType()))
        .findFirst()
        .orElse(null);
  }

  /** Returns all results for a specific worker type. */
  public List<StepExecutionResult> getStepResults(String workerType) {
    return stepResults.stream().filter(r -> workerType.equals(r.getWorkerType())).toList();
  }

  @Override
  public String toString() {
    return "ProcessExecutionResult{"
        + "processInstanceId='"
        + processInstanceId
        + '\''
        + ", completed="
        + completed
        + ", totalSteps="
        + totalSteps
        + ", completedSteps="
        + completedSteps
        + ", failedSteps="
        + failedSteps
        + ", executionTimeMs="
        + executionTimeMs
        + '}';
  }

  /** Result of executing a single step in the process flow. */
  public static class StepExecutionResult {
    private final String stepId;
    private final String workerType;
    private final WorkerTestResult workerResult;
    private final Map<String, Object> variablesBefore;
    private final Map<String, Object> variablesAfter;
    private final int stepIndex;

    /**
     * Constructs a step execution result.
     *
     * @param stepId the unique step identifier
     * @param workerType the worker type executed
     * @param workerResult the worker execution result
     * @param variablesBefore variables before execution
     * @param variablesAfter variables after execution
     * @param stepIndex the zero-based step position
     */
    public StepExecutionResult(
        String stepId,
        String workerType,
        WorkerTestResult workerResult,
        Map<String, Object> variablesBefore,
        Map<String, Object> variablesAfter,
        int stepIndex) {
      this.stepId = stepId;
      this.workerType = workerType;
      this.workerResult = workerResult;
      this.variablesBefore = new HashMap<>(variablesBefore != null ? variablesBefore : Map.of());
      this.variablesAfter = new HashMap<>(variablesAfter != null ? variablesAfter : Map.of());
      this.stepIndex = stepIndex;
    }

    /** Returns the unique step identifier. */
    public String getStepId() {
      return stepId;
    }

    /** Returns the worker type that was executed. */
    public String getWorkerType() {
      return workerType;
    }

    /** Returns the worker execution result. */
    public WorkerTestResult getWorkerResult() {
      return workerResult;
    }

    /** Returns the variables present before this step executed. */
    public Map<String, Object> getVariablesBefore() {
      return Collections.unmodifiableMap(variablesBefore);
    }

    /** Returns the variables present after this step executed. */
    public Map<String, Object> getVariablesAfter() {
      return Collections.unmodifiableMap(variablesAfter);
    }

    /** Returns the zero-based position of this step in the flow. */
    public int getStepIndex() {
      return stepIndex;
    }

    /** Returns {@code true} if this step completed successfully. */
    public boolean isCompleted() {
      return workerResult != null && workerResult.isCompleted();
    }

    /** Returns {@code true} if this step failed. */
    public boolean hasFailed() {
      return workerResult != null && workerResult.hasFailed();
    }
  }
}
