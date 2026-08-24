package io.telekom.orchest.test.assertions;

import io.telekom.orchest.api.core.model.bpmn.ExecutionLogEntry;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.request.StateChange;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.api.AbstractAssert;

/**
 * AssertJ-style fluent assertions for a single {@link ExecutionLogEntry}.
 *
 * <p>Typically obtained via {@link ExecutionHistoryAssert#forNode(String)}. Call {@link #and()} to
 * return to the parent {@link ExecutionHistoryAssert}.
 *
 * <h3>Usage</h3>
 *
 * <pre>{@code
 * assertExecutionHistory(history)
 *     .forNode("serviceTask1")
 *         .hasState(NodeState.COMPLETED)
 *         .hasType(NodeType.SERVICE_TASK)
 *         .hasName("Process Order")
 *         .hasSourceNodeId("startEvent")
 *         .hasStateChange(NodeState.STARTED)
 *         .hasStateChangeCount(3)
 *         .hasMetaData("retryCount", 3)
 *         .hasMetaDataKey("correlationId")
 *     .and()
 *     .containsNodeId("endEvent");
 * }</pre>
 */
public class ExecutionLogEntryAssert
    extends AbstractAssert<ExecutionLogEntryAssert, ExecutionLogEntry> {

  private final ExecutionHistoryAssert parent;

  /**
   * Creates an assertion scoped to a parent history assert, enabling {@link #and()} navigation.
   *
   * @param actual the log entry to assert on
   * @param parent the parent history assertion
   */
  ExecutionLogEntryAssert(ExecutionLogEntry actual, ExecutionHistoryAssert parent) {
    super(actual, ExecutionLogEntryAssert.class);
    this.parent = parent;
  }

  /**
   * Creates a standalone assertion for the given execution log entry.
   *
   * @param actual the log entry to assert on
   */
  public ExecutionLogEntryAssert(ExecutionLogEntry actual) {
    super(actual, ExecutionLogEntryAssert.class);
    this.parent = null;
  }

  /**
   * Creates a new standalone assertion for the given execution log entry.
   *
   * @param actual the log entry to assert on
   * @return a new {@link ExecutionLogEntryAssert} instance
   */
  public static ExecutionLogEntryAssert assertThat(ExecutionLogEntry actual) {
    return new ExecutionLogEntryAssert(actual);
  }

  /**
   * Returns to the parent {@link ExecutionHistoryAssert} for continued chaining.
   *
   * @throws IllegalStateException if this assert was not created via {@code forNode()}.
   */
  public ExecutionHistoryAssert and() {
    if (parent == null) {
      throw new IllegalStateException(
          "and() can only be called when this assert was created via ExecutionHistoryAssert.forNode()");
    }
    return parent;
  }

  // ========================================================================================
  // Identity & type
  // ========================================================================================

  /**
   * Asserts that the entry has the expected node ID.
   *
   * @param expectedNodeId the expected node ID
   * @return this assertion for chaining
   */
  public ExecutionLogEntryAssert hasNodeId(String expectedNodeId) {
    isNotNull();
    if (!Objects.equals(actual.getNodeId(), expectedNodeId)) {
      failWithMessage("Expected nodeId '%s', but was '%s'", expectedNodeId, actual.getNodeId());
    }
    return this;
  }

  /**
   * Asserts that the entry has the expected node name.
   *
   * @param expectedName the expected node name
   * @return this assertion for chaining
   */
  public ExecutionLogEntryAssert hasName(String expectedName) {
    isNotNull();
    if (!Objects.equals(actual.getNodeName(), expectedName)) {
      failWithMessage(
          "Expected node '%s' to have name '%s', but was '%s'",
          actual.getNodeId(), expectedName, actual.getNodeName());
    }
    return this;
  }

  /**
   * Asserts that the entry has the expected node type.
   *
   * @param expectedType the expected node type
   * @return this assertion for chaining
   */
  public ExecutionLogEntryAssert hasType(NodeType expectedType) {
    isNotNull();
    if (actual.getNodeType() != expectedType) {
      failWithMessage(
          "Expected node '%s' to have type %s, but was %s",
          actual.getNodeId(), expectedType, actual.getNodeType());
    }
    return this;
  }

  /**
   * Asserts that the entry has the expected source node ID.
   *
   * @param expectedSourceNodeId the expected source node ID
   * @return this assertion for chaining
   */
  public ExecutionLogEntryAssert hasSourceNodeId(String expectedSourceNodeId) {
    isNotNull();
    if (!Objects.equals(actual.getSourceNodeId(), expectedSourceNodeId)) {
      failWithMessage(
          "Expected node '%s' to have sourceNodeId '%s', but was '%s'",
          actual.getNodeId(), expectedSourceNodeId, actual.getSourceNodeId());
    }
    return this;
  }

  // ========================================================================================
  // Current state (latest state change)
  // ========================================================================================

  /** Asserts that the current (latest) state matches the expected state. */
  public ExecutionLogEntryAssert hasState(NodeState expectedState) {
    isNotNull();
    NodeState currentState = actual.getState();
    if (currentState != expectedState) {
      failWithMessage(
          "Expected node '%s' to have current state %s, but was %s",
          actual.getNodeId(), expectedState, currentState);
    }
    return this;
  }

  // ========================================================================================
  // State change history
  // ========================================================================================

  /** Asserts that at least one state change with the given state exists in the history. */
  public ExecutionLogEntryAssert hasStateChange(NodeState expectedState) {
    isNotNull();
    boolean found =
        actual.getStateChanges().stream().anyMatch(sc -> sc.getState() == expectedState);
    if (!found) {
      List<NodeState> states =
          actual.getStateChanges().stream().map(StateChange::getState).toList();
      failWithMessage(
          "Expected node '%s' to have a state change to %s, but state changes are: %s",
          actual.getNodeId(), expectedState, states);
    }
    return this;
  }

  /** Asserts that the state changes contain exactly these states in order. */
  public ExecutionLogEntryAssert hasStateChangesInOrder(NodeState... expectedStates) {
    isNotNull();
    List<NodeState> actualStates =
        actual.getStateChanges().stream().map(StateChange::getState).toList();
    List<NodeState> expectedList = List.of(expectedStates);
    if (!actualStates.equals(expectedList)) {
      failWithMessage(
          "Expected node '%s' state changes to be %s, but were %s",
          actual.getNodeId(), expectedList, actualStates);
    }
    return this;
  }

  /** Asserts the total number of state changes recorded for this node. */
  public ExecutionLogEntryAssert hasStateChangeCount(int expectedCount) {
    isNotNull();
    int actualCount = actual.getStateChanges().size();
    if (actualCount != expectedCount) {
      failWithMessage(
          "Expected node '%s' to have %d state changes, but had %d",
          actual.getNodeId(), expectedCount, actualCount);
    }
    return this;
  }

  // ========================================================================================
  // Sequence flows
  // ========================================================================================

  /**
   * Asserts that the entry contains the specified sequence flow ID.
   *
   * @param sequenceFlowId the sequence flow ID expected to be present
   * @return this assertion for chaining
   */
  public ExecutionLogEntryAssert hasSequenceFlowId(String sequenceFlowId) {
    isNotNull();
    if (!actual.getSequenceFlowIds().contains(sequenceFlowId)) {
      failWithMessage(
          "Expected node '%s' to have sequenceFlowId '%s', but sequence flow IDs are: %s",
          actual.getNodeId(), sequenceFlowId, actual.getSequenceFlowIds());
    }
    return this;
  }

  /**
   * Asserts that the entry has the expected number of sequence flow IDs.
   *
   * @param expectedCount the expected sequence flow count
   * @return this assertion for chaining
   */
  public ExecutionLogEntryAssert hasSequenceFlowCount(int expectedCount) {
    isNotNull();
    int actualCount = actual.getSequenceFlowIds().size();
    if (actualCount != expectedCount) {
      failWithMessage(
          "Expected node '%s' to have %d sequence flow IDs, but had %d: %s",
          actual.getNodeId(), expectedCount, actualCount, actual.getSequenceFlowIds());
    }
    return this;
  }

  // ========================================================================================
  // MetaData assertions
  // ========================================================================================

  /** Asserts that metaData contains a specific key-value pair. */
  public ExecutionLogEntryAssert hasMetaData(String key, Object expectedValue) {
    isNotNull();
    Object actualValue = actual.getMetaData().get(key);
    if (!Objects.equals(actualValue, expectedValue)) {
      failWithMessage(
          "Expected node '%s' metaData key '%s' to be '%s', but was '%s'. MetaData: %s",
          actual.getNodeId(), key, expectedValue, actualValue, actual.getMetaData());
    }
    return this;
  }

  /** Asserts that metaData contains a specific key (any value). */
  public ExecutionLogEntryAssert hasMetaDataKey(String key) {
    isNotNull();
    if (!actual.getMetaData().containsKey(key)) {
      failWithMessage(
          "Expected node '%s' metaData to contain key '%s', but available keys are: %s",
          actual.getNodeId(), key, actual.getMetaData().keySet());
    }
    return this;
  }

  /** Asserts that metaData does NOT contain a specific key. */
  public ExecutionLogEntryAssert doesNotHaveMetaDataKey(String key) {
    isNotNull();
    if (actual.getMetaData().containsKey(key)) {
      failWithMessage(
          "Expected node '%s' metaData to NOT contain key '%s', but it was present with value '%s'",
          actual.getNodeId(), key, actual.getMetaData().get(key));
    }
    return this;
  }

  /** Asserts that metaData contains all entries from the given map. */
  public ExecutionLogEntryAssert hasMetaDataEntries(Map<String, Object> expectedEntries) {
    isNotNull();
    for (Map.Entry<String, Object> entry : expectedEntries.entrySet()) {
      hasMetaData(entry.getKey(), entry.getValue());
    }
    return this;
  }

  /** Asserts that metaData has the expected size. */
  public ExecutionLogEntryAssert hasMetaDataSize(int expectedSize) {
    isNotNull();
    int actualSize = actual.getMetaData().size();
    if (actualSize != expectedSize) {
      failWithMessage(
          "Expected node '%s' metaData to have %d entries, but had %d: %s",
          actual.getNodeId(), expectedSize, actualSize, actual.getMetaData());
    }
    return this;
  }

  /** Asserts that the entry's metaData is empty. */
  public ExecutionLogEntryAssert hasEmptyMetaData() {
    isNotNull();
    if (!actual.getMetaData().isEmpty()) {
      failWithMessage(
          "Expected node '%s' metaData to be empty, but had %d entries: %s",
          actual.getNodeId(), actual.getMetaData().size(), actual.getMetaData());
    }
    return this;
  }
}
