package io.telekom.orchest.test.assertions;

import io.telekom.orchest.api.core.model.bpmn.ExecutionLogEntry;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.api.AbstractAssert;

/**
 * AssertJ-style fluent assertions for execution history ({@code Map<String, ExecutionLogEntry>}).
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * import static io.telekom.orchest.test.assertions.OrchestAssertions.assertExecutionHistory;
 *
 * assertExecutionHistory(processInstance.getExecutionHistory())
 *     .hasSize(4)
 *     .containsNodeId("startEvent")
 *     .containsNodeId("serviceTask1")
 *     .hasOrderedNodeIds("startEvent", "serviceTask1", "gateway1", "endEvent")
 *     .nodeHasState("serviceTask1", NodeState.COMPLETED)
 *     .nodeHasMetaData("serviceTask1", "retryCount", 3);
 *
 * // Per-node deep assertions via forNode():
 * assertExecutionHistory(history)
 *     .forNode("serviceTask1")
 *         .hasState(NodeState.COMPLETED)
 *         .hasType(NodeType.SERVICE_TASK)
 *         .hasMetaDataKey("retryCount")
 *         .hasMetaData("retryCount", 3)
 *         .hasStateChange(NodeState.STARTED)
 *         .hasStateChangeCount(3)
 *     .and()
 *     .forNode("endEvent")
 *         .hasState(NodeState.COMPLETED);
 * }</pre>
 */
public class ExecutionHistoryAssert
    extends AbstractAssert<ExecutionHistoryAssert, Map<String, ExecutionLogEntry>> {

  /**
   * Creates a new assertion instance for the given execution history.
   *
   * @param actual the execution history map to assert on
   */
  public ExecutionHistoryAssert(Map<String, ExecutionLogEntry> actual) {
    super(actual, ExecutionHistoryAssert.class);
  }

  /**
   * Creates a new assertion instance for the given execution history.
   *
   * @param actual the execution history map to assert on
   * @return a new {@link ExecutionHistoryAssert} instance
   */
  public static ExecutionHistoryAssert assertThat(Map<String, ExecutionLogEntry> actual) {
    return new ExecutionHistoryAssert(actual);
  }

  // ========================================================================================
  // Size & emptiness
  // ========================================================================================

  /**
   * Asserts that the execution history contains exactly the expected number of entries.
   *
   * @param expectedSize the expected number of entries
   * @return this assertion for chaining
   */
  public ExecutionHistoryAssert hasSize(int expectedSize) {
    isNotNull();
    if (actual.size() != expectedSize) {
      failWithMessage(
          "Expected execution history to have %d entries, but found %d. Node IDs: %s",
          expectedSize, actual.size(), actual.keySet());
    }
    return this;
  }

  /**
   * Asserts that the execution history is empty.
   *
   * @return this assertion for chaining
   */
  public ExecutionHistoryAssert isEmpty() {
    isNotNull();
    if (!actual.isEmpty()) {
      failWithMessage(
          "Expected execution history to be empty, but found %d entries: %s",
          actual.size(), actual.keySet());
    }
    return this;
  }

  /**
   * Asserts that the execution history contains at least one entry.
   *
   * @return this assertion for chaining
   */
  public ExecutionHistoryAssert isNotEmpty() {
    isNotNull();
    if (actual.isEmpty()) {
      failWithMessage("Expected execution history to contain entries, but it was empty");
    }
    return this;
  }

  // ========================================================================================
  // Contains / does-not-contain
  // ========================================================================================

  /**
   * Asserts that the execution history contains an entry for the given node ID.
   *
   * @param nodeId the node ID expected to be present
   * @return this assertion for chaining
   */
  public ExecutionHistoryAssert containsNodeId(String nodeId) {
    isNotNull();
    if (!actual.containsKey(nodeId)) {
      failWithMessage(
          "Expected execution history to contain nodeId '%s', but available node IDs are: %s",
          nodeId, actual.keySet());
    }
    return this;
  }

  /**
   * Asserts that the execution history does not contain an entry for the given node ID.
   *
   * @param nodeId the node ID expected to be absent
   * @return this assertion for chaining
   */
  public ExecutionHistoryAssert doesNotContainNodeId(String nodeId) {
    isNotNull();
    if (actual.containsKey(nodeId)) {
      failWithMessage(
          "Expected execution history to NOT contain nodeId '%s', but it was present", nodeId);
    }
    return this;
  }

  /**
   * Asserts that the execution history contains entries for all the given node IDs.
   *
   * @param nodeIds the node IDs expected to be present
   * @return this assertion for chaining
   */
  public ExecutionHistoryAssert containsNodeIds(String... nodeIds) {
    isNotNull();
    List<String> missing = new ArrayList<>();
    for (String nodeId : nodeIds) {
      if (!actual.containsKey(nodeId)) {
        missing.add(nodeId);
      }
    }
    if (!missing.isEmpty()) {
      failWithMessage(
          "Expected execution history to contain node IDs %s, but missing: %s. Available: %s",
          List.of(nodeIds), missing, actual.keySet());
    }
    return this;
  }

  // ========================================================================================
  // Ordering assertions (leveraging LinkedHashMap insertion order)
  // ========================================================================================

  /**
   * Asserts that the execution history contains exactly these nodeIds in exactly this insertion
   * order.
   */
  public ExecutionHistoryAssert hasOrderedNodeIds(String... expectedNodeIds) {
    isNotNull();
    List<String> actualKeys = new ArrayList<>(actual.keySet());
    List<String> expectedKeys = List.of(expectedNodeIds);

    if (!actualKeys.equals(expectedKeys)) {
      failWithMessage("Expected execution history order %s, but was %s", expectedKeys, actualKeys);
    }
    return this;
  }

  /**
   * Asserts that the given nodeIds appear in the execution history in this relative order. Other
   * nodeIds may exist between them.
   */
  public ExecutionHistoryAssert hasNodeIdsInOrder(String... expectedNodeIds) {
    isNotNull();
    List<String> actualKeys = new ArrayList<>(actual.keySet());

    int lastIndex = -1;
    for (String expected : expectedNodeIds) {
      int index = actualKeys.indexOf(expected);
      if (index == -1) {
        failWithMessage(
            "Expected nodeId '%s' to be present in execution history, but available node IDs are: %s",
            expected, actualKeys);
        return this;
      }
      if (index <= lastIndex) {
        failWithMessage(
            "Expected nodeId '%s' to appear after '%s' in execution history, but order was: %s",
            expected, expectedNodeIds[indexOf(expectedNodeIds, expected) - 1], actualKeys);
        return this;
      }
      lastIndex = index;
    }
    return this;
  }

  /** Asserts that a nodeId is at a specific position (0-based) in the insertion order. */
  public ExecutionHistoryAssert nodeIsAtPosition(String nodeId, int expectedPosition) {
    isNotNull();
    containsNodeId(nodeId);
    List<String> actualKeys = new ArrayList<>(actual.keySet());
    int actualPosition = actualKeys.indexOf(nodeId);
    if (actualPosition != expectedPosition) {
      failWithMessage(
          "Expected nodeId '%s' at position %d, but it was at position %d. Order: %s",
          nodeId, expectedPosition, actualPosition, actualKeys);
    }
    return this;
  }

  /** Asserts that a nodeId appears before another nodeId in insertion order. */
  public ExecutionHistoryAssert nodeIsBefore(String nodeId, String otherNodeId) {
    isNotNull();
    containsNodeId(nodeId);
    containsNodeId(otherNodeId);
    List<String> actualKeys = new ArrayList<>(actual.keySet());
    if (actualKeys.indexOf(nodeId) >= actualKeys.indexOf(otherNodeId)) {
      failWithMessage(
          "Expected nodeId '%s' to appear before '%s', but order was: %s",
          nodeId, otherNodeId, actualKeys);
    }
    return this;
  }

  /** Asserts that a nodeId appears after another nodeId in insertion order. */
  public ExecutionHistoryAssert nodeIsAfter(String nodeId, String otherNodeId) {
    isNotNull();
    containsNodeId(nodeId);
    containsNodeId(otherNodeId);
    List<String> actualKeys = new ArrayList<>(actual.keySet());
    if (actualKeys.indexOf(nodeId) <= actualKeys.indexOf(otherNodeId)) {
      failWithMessage(
          "Expected nodeId '%s' to appear after '%s', but order was: %s",
          nodeId, otherNodeId, actualKeys);
    }
    return this;
  }

  /** Asserts that a nodeId is the first entry in the execution history. */
  public ExecutionHistoryAssert startsWithNodeId(String nodeId) {
    isNotNull();
    isNotEmpty();
    String firstKey = actual.keySet().iterator().next();
    if (!firstKey.equals(nodeId)) {
      failWithMessage(
          "Expected execution history to start with nodeId '%s', but first was '%s'. Order: %s",
          nodeId, firstKey, new ArrayList<>(actual.keySet()));
    }
    return this;
  }

  /** Asserts that a nodeId is the last entry in the execution history. */
  public ExecutionHistoryAssert endsWithNodeId(String nodeId) {
    isNotNull();
    isNotEmpty();
    List<String> keys = new ArrayList<>(actual.keySet());
    String lastKey = keys.get(keys.size() - 1);
    if (!lastKey.equals(nodeId)) {
      failWithMessage(
          "Expected execution history to end with nodeId '%s', but last was '%s'. Order: %s",
          nodeId, lastKey, keys);
    }
    return this;
  }

  // ========================================================================================
  // Per-node state assertions (shorthand — no need for forNode())
  // ========================================================================================

  /** Asserts that the current (latest) state for a nodeId matches the expected state. */
  public ExecutionHistoryAssert nodeHasState(String nodeId, NodeState expectedState) {
    isNotNull();
    ExecutionLogEntry entry = requireEntry(nodeId);
    NodeState currentState = entry.getState();
    if (currentState != expectedState) {
      failWithMessage(
          "Expected nodeId '%s' to have current state %s, but was %s",
          nodeId, expectedState, currentState);
    }
    return this;
  }

  /** Asserts that the node type for a nodeId matches the expected type. */
  public ExecutionHistoryAssert nodeHasType(String nodeId, NodeType expectedType) {
    isNotNull();
    ExecutionLogEntry entry = requireEntry(nodeId);
    if (entry.getNodeType() != expectedType) {
      failWithMessage(
          "Expected nodeId '%s' to have type %s, but was %s",
          nodeId, expectedType, entry.getNodeType());
    }
    return this;
  }

  /** Asserts that the metaData for a nodeId contains a specific key-value pair. */
  public ExecutionHistoryAssert nodeHasMetaData(String nodeId, String key, Object expectedValue) {
    isNotNull();
    ExecutionLogEntry entry = requireEntry(nodeId);
    Object actualValue = entry.getMetaData().get(key);
    if (!Objects.equals(actualValue, expectedValue)) {
      failWithMessage(
          "Expected nodeId '%s' metaData key '%s' to be '%s', but was '%s'. MetaData: %s",
          nodeId, key, expectedValue, actualValue, entry.getMetaData());
    }
    return this;
  }

  /** Asserts that the metaData for a nodeId contains a specific key (any value). */
  public ExecutionHistoryAssert nodeHasMetaDataKey(String nodeId, String key) {
    isNotNull();
    ExecutionLogEntry entry = requireEntry(nodeId);
    if (!entry.getMetaData().containsKey(key)) {
      failWithMessage(
          "Expected nodeId '%s' metaData to contain key '%s', but available keys are: %s",
          nodeId, key, entry.getMetaData().keySet());
    }
    return this;
  }

  /** Asserts that the node's source node ID matches the expected value. */
  public ExecutionHistoryAssert nodeHasSourceNodeId(String nodeId, String expectedSourceNodeId) {
    isNotNull();
    ExecutionLogEntry entry = requireEntry(nodeId);
    if (!Objects.equals(entry.getSourceNodeId(), expectedSourceNodeId)) {
      failWithMessage(
          "Expected nodeId '%s' to have sourceNodeId '%s', but was '%s'",
          nodeId, expectedSourceNodeId, entry.getSourceNodeId());
    }
    return this;
  }

  /** Asserts that the node's sequence flow IDs contain the expected ID. */
  public ExecutionHistoryAssert nodeHasSequenceFlowId(String nodeId, String sequenceFlowId) {
    isNotNull();
    ExecutionLogEntry entry = requireEntry(nodeId);
    if (!entry.getSequenceFlowIds().contains(sequenceFlowId)) {
      failWithMessage(
          "Expected nodeId '%s' to have sequenceFlowId '%s', but sequence flow IDs are: %s",
          nodeId, sequenceFlowId, entry.getSequenceFlowIds());
    }
    return this;
  }

  // ========================================================================================
  // Nested per-node assertion (deep fluent API)
  // ========================================================================================

  /**
   * Returns a {@link ExecutionLogEntryAssert} scoped to a specific nodeId, allowing deep fluent
   * assertions on that entry. Call {@code .and()} to return back to this history-level assertion.
   */
  public ExecutionLogEntryAssert forNode(String nodeId) {
    isNotNull();
    ExecutionLogEntry entry = requireEntry(nodeId);
    return new ExecutionLogEntryAssert(entry, this);
  }

  // ========================================================================================
  // Internals
  // ========================================================================================

  private ExecutionLogEntry requireEntry(String nodeId) {
    containsNodeId(nodeId);
    return actual.get(nodeId);
  }

  private static int indexOf(String[] array, String value) {
    for (int i = 0; i < array.length; i++) {
      if (array[i].equals(value)) return i;
    }
    return -1;
  }
}
