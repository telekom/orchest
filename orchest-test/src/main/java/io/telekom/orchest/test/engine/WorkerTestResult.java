package io.telekom.orchest.test.engine;

import io.telekom.orchest.client.TaskResponse;
import io.telekom.orchest.client.annotations.ActivatedJob;
import java.util.Map;

/**
 * Encapsulates the result of executing a {@code @JobWorker} method during testing.
 *
 * <p>Provides access to the returned {@link TaskResponse}, any thrown exception, and the execution
 * metadata for building assertions.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * WorkerTestResult result = workerTester.execute("my-worker", Map.of("input", "value"));
 *
 * // Check execution succeeded
 * assertTrue(result.isCompleted());
 * assertNull(result.getException());
 *
 * // Check output variables
 * assertEquals("DONE", result.getOutputVariable("status"));
 *
 * // Check no error event was thrown
 * assertFalse(result.hasErrorEvent());
 * }</pre>
 */
public class WorkerTestResult {

  private final TaskResponse taskResponse;
  private final Exception exception;
  private final ActivatedJob activatedJob;
  private final String workerType;
  private final long executionTimeMs;

  /**
   * Constructs a worker test result.
   *
   * @param taskResponse the task response returned by the worker, or null
   * @param exception the exception thrown by the worker, or null
   * @param activatedJob the activated job passed to the worker
   * @param workerType the worker type that was invoked
   * @param executionTimeMs the execution time in milliseconds
   */
  public WorkerTestResult(
      TaskResponse taskResponse,
      Exception exception,
      ActivatedJob activatedJob,
      String workerType,
      long executionTimeMs) {
    this.taskResponse = taskResponse;
    this.exception = exception;
    this.activatedJob = activatedJob;
    this.workerType = workerType;
    this.executionTimeMs = executionTimeMs;
  }

  // ---- State checks ----

  /** Returns {@code true} if the worker executed successfully without throwing an exception. */
  public boolean isCompleted() {
    return exception == null;
  }

  /** Returns {@code true} if the worker threw an exception during execution. */
  public boolean hasFailed() {
    return exception != null;
  }

  /** Returns {@code true} if the worker returned a {@link TaskResponse} with an error event. */
  public boolean hasErrorEvent() {
    return taskResponse != null && taskResponse.getErrorEvent() != null;
  }

  /** Returns {@code true} if the worker returned a {@link TaskResponse} with a message event. */
  public boolean hasMessageEvent() {
    return taskResponse != null && taskResponse.getMessageEvent() != null;
  }

  /**
   * Returns {@code true} if the worker returned a {@link TaskResponse} with an incident message.
   */
  public boolean hasIncident() {
    return taskResponse != null && taskResponse.getIncidentMessage() != null;
  }

  /** Returns {@code true} if the worker returned output variables. */
  public boolean hasVariables() {
    return taskResponse != null
        && taskResponse.getVariables() != null
        && taskResponse.getVariables().getVariables() != null
        && !taskResponse.getVariables().getVariables().isEmpty();
  }

  // ---- Data access ----

  /** Returns the {@link TaskResponse} returned by the worker, or {@code null} if auto-complete. */
  public TaskResponse getTaskResponse() {
    return taskResponse;
  }

  /** Returns the exception thrown by the worker, or {@code null} if successful. */
  public Exception getException() {
    return exception;
  }

  /** Returns the {@link ActivatedJob} that was passed to the worker. */
  public ActivatedJob getActivatedJob() {
    return activatedJob;
  }

  /** Returns the worker type that was invoked. */
  public String getWorkerType() {
    return workerType;
  }

  /** Returns the execution time in milliseconds. */
  public long getExecutionTimeMs() {
    return executionTimeMs;
  }

  /** Returns the output variables map, or an empty map if none. */
  public Map<String, Object> getOutputVariables() {
    if (taskResponse == null || taskResponse.getVariables() == null) {
      return Map.of();
    }
    return taskResponse.getVariables().getVariables() != null
        ? taskResponse.getVariables().getVariables()
        : Map.of();
  }

  /**
   * Returns a specific output variable by key.
   *
   * @param key The variable key.
   * @return The variable value, or {@code null} if not present.
   */
  public Object getOutputVariable(String key) {
    return getOutputVariables().get(key);
  }

  /** Returns the error code from the error event, or {@code null}. */
  public String getErrorCode() {
    return hasErrorEvent() ? taskResponse.getErrorEvent().getErrorCode() : null;
  }

  /** Returns the error name from the error event, or {@code null}. */
  public String getErrorName() {
    return hasErrorEvent() ? taskResponse.getErrorEvent().getErrorName() : null;
  }

  @Override
  public String toString() {
    return "WorkerTestResult{"
        + "workerType='"
        + workerType
        + '\''
        + ", completed="
        + isCompleted()
        + ", hasErrorEvent="
        + hasErrorEvent()
        + ", executionTimeMs="
        + executionTimeMs
        + '}';
  }
}
