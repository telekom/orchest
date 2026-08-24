package io.telekom.orchest.test.assertions;

import io.telekom.orchest.api.core.model.bpmn.ExecutionLogEntry;
import io.telekom.orchest.test.engine.ProcessExecutionResult;
import io.telekom.orchest.test.engine.WorkerTestResult;
import java.util.Map;

/**
 * Entry point for Orchest test assertions. Provides static factory methods for creating fluent
 * assertion objects.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * import static io.telekom.orchest.test.assertions.OrchestAssertions.assertThat;
 * import static io.telekom.orchest.test.assertions.OrchestAssertions.assertExecutionHistory;
 *
 * WorkerTestResult result = workerTester.execute("my-worker", Map.of("orderId", "123"));
 *
 * assertThat(result)
 *     .isCompleted()
 *     .hasVariable("status", "PROCESSED")
 *     .hasNoErrorEvent()
 *     .executedWithin(5000);
 *
 * assertExecutionHistory(processInstance.getExecutionHistory())
 *     .hasOrderedNodeIds("startEvent", "serviceTask1", "endEvent")
 *     .nodeHasState("serviceTask1", NodeState.COMPLETED)
 *     .forNode("serviceTask1")
 *         .hasType(NodeType.SERVICE_TASK)
 *         .hasMetaData("retryCount", 3)
 *     .and()
 *     .endsWithNodeId("endEvent");
 * }</pre>
 */
public final class OrchestAssertions {

  private OrchestAssertions() {}

  /**
   * Creates a fluent assertion object for a {@link WorkerTestResult}.
   *
   * @param result The worker test result to assert on.
   * @return A new {@link WorkerTestResultAssert} instance.
   */
  public static WorkerTestResultAssert assertThat(WorkerTestResult result) {
    return new WorkerTestResultAssert(result);
  }

  /**
   * Creates a fluent assertion object for a {@link ProcessExecutionResult}.
   *
   * @param result The process execution result to assert on.
   * @return A new {@link ProcessExecutionResultAssert} instance.
   */
  public static ProcessExecutionResultAssert assertThat(ProcessExecutionResult result) {
    return new ProcessExecutionResultAssert(result);
  }

  /**
   * Creates a fluent assertion object for an execution history map.
   *
   * @param executionHistory The execution history map to assert on.
   * @return A new {@link ExecutionHistoryAssert} instance.
   */
  public static ExecutionHistoryAssert assertExecutionHistory(
      Map<String, ExecutionLogEntry> executionHistory) {
    return new ExecutionHistoryAssert(executionHistory);
  }

  /**
   * Creates a fluent assertion object for a single {@link ExecutionLogEntry}.
   *
   * @param entry The execution log entry to assert on.
   * @return A new {@link ExecutionLogEntryAssert} instance.
   */
  public static ExecutionLogEntryAssert assertThat(ExecutionLogEntry entry) {
    return new ExecutionLogEntryAssert(entry);
  }
}
