package io.telekom.orchest.api.core.model.bpmn.state;

import java.util.List;

/**
 * Sealed interface for type-safe execution state keys. Replaces magic string concatenation
 * scattered across the codebase with discoverable, compile-time-checked key types.
 *
 * <p>The underlying storage remains {@code Map<String, Object>} (for MongoDB compatibility), but
 * access through these keys provides type safety at call sites via {@code ProcessInstance} typed
 * accessors ({@code getState}, {@code putState}, {@code removeState}, {@code containsState}).
 *
 * @param <T> the type of value stored under this key
 */
public sealed interface ExecutionStateKey<T> {

  /** Returns the string key used in the underlying execution state map. */
  String toStorageKey();

  /** Tracks token count for parallel gateway joins. */
  record ParallelGatewayToken(String gatewayId) implements ExecutionStateKey<Integer> {
    @Override
    public String toStorageKey() {
      return "parallelGatewayToken:" + gatewayId;
    }
  }

  /** Tracks total loop size for multi-instance activities. Key: {@code mi:loop:{nodeId}} */
  record MultiInstanceLoopSize(String nodeId) implements ExecutionStateKey<Integer> {
    @Override
    public String toStorageKey() {
      return "mi:loop:" + nodeId;
    }
  }

  /**
   * Tracks individual loop iteration for multi-instance activities. Key: {@code
   * mi:loop:{counter}:{nodeId}}
   */
  record MultiInstanceLoopCounter(int counter, String nodeId)
      implements ExecutionStateKey<Integer> {
    @Override
    public String toStorageKey() {
      return "mi:loop:" + counter + ":" + nodeId;
    }
  }

  /**
   * Stores the linked event ID for an event-based gateway. Key: {@code eventGateway:{gatewayId}}
   */
  record EventGatewayLinkedId(String gatewayId) implements ExecutionStateKey<String> {
    @Override
    public String toStorageKey() {
      return "eventGateway:" + gatewayId;
    }
  }

  /** Stores the list of catch event node IDs for an event-based gateway. */
  record EventGatewayCatchEvents(String gatewayId) implements ExecutionStateKey<List<String>> {
    @Override
    public String toStorageKey() {
      return "eventGatewayCatchEvents:" + gatewayId;
    }
  }

  /**
   * Stores child process instance ID for a call activity. Key: {@code
   * callActivityChildInstanceId:{nodeId}}
   */
  record CallActivityChild(String nodeId) implements ExecutionStateKey<String> {
    @Override
    public String toStorageKey() {
      return "callActivityChildInstanceId:" + nodeId;
    }
  }

  /** Stores child process instance ID for multi-instance call activity iteration. */
  record CallActivityChildCounter(int counter) implements ExecutionStateKey<String> {
    @Override
    public String toStorageKey() {
      return "callActivityChildInstanceId:" + counter + ":";
    }
  }

  /**
   * Stores decision instance ID(s) for a business rule task. Value is String (single) or List
   * (multi).
   */
  record DecisionInstance(String nodeId) implements ExecutionStateKey<Object> {
    @Override
    public String toStorageKey() {
      return "decisionInstanceId:" + nodeId;
    }
  }

  /** Tracks the output collection being built for a multi-instance subprocess. */
  record MultiInstanceSubProcessOutput(String subProcessId)
      implements ExecutionStateKey<List<Object>> {
    @Override
    public String toStorageKey() {
      return "mi:sp:output:" + subProcessId;
    }
  }

  /** Tracks the number of completed instances for a multi-instance subprocess. */
  record MultiInstanceSubProcessCompleted(String subProcessId)
      implements ExecutionStateKey<Integer> {
    @Override
    public String toStorageKey() {
      return "mi:sp:completed:" + subProcessId;
    }
  }

  /**
   * Marks an activity retried after an incident, awaiting successful completion to emit a
   * resolution event.
   */
  record PendingIncidentResolution(String activityId) implements ExecutionStateKey<Boolean> {
    @Override
    public String toStorageKey() {
      return "pendingIncidentResolution:" + activityId;
    }
  }

  /** Stores the user task ID created for a user task node. */
  record UserTaskId(String nodeId) implements ExecutionStateKey<String> {
    @Override
    public String toStorageKey() {
      return "userTaskId:" + nodeId;
    }
  }
}
