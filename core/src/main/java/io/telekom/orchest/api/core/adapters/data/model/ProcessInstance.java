package io.telekom.orchest.api.core.adapters.data.model;

import io.telekom.orchest.api.core.model.bpmn.ExecutionLogEntry;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import io.telekom.orchest.api.core.request.StateChange;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Persistent model representing a running or completed BPMN process instance with its execution
 * state, variables, and history.
 */
@Data
@RequiredArgsConstructor
public class ProcessInstance {
  private String id;
  private final String processInstanceId;
  private final String processDefinitionId;
  private final Integer version;
  private Map<String, Object> variables = new HashMap<>();
  private Set<String> activeNodeIds = new HashSet<>();
  private Map<String, ExecutionLogEntry> executionHistory = new LinkedHashMap<>();
  // Generic execution state/metadata map for tracking various execution-related data
  // Examples: parallel gateway tokens, inclusive gateway tokens, loop counters, etc.
  private Map<String, Object> executionState = new HashMap<>();
  private boolean hasIncident = false;
  private String incidentMessage;
  private String incidentSourceInstanceId;
  private boolean completed = false;
  private PIState state;
  private List<String> correlationIds; // can be orderId, publicIdentifier or correlationId
  private transient ProcessDefinition processDefinition;

  // Multi-instance subprocess context (transient - not persisted)
  // Set when executing nodes inside an MI subprocess instance, cleared after each execution chain.
  private transient String currentMiSubProcessId;
  private transient int currentMiInstanceIndex = -1;

  private ParentProcessActivity parentProcesActivity;
  private boolean dynamicFlow;

  private OffsetDateTime createdAt;
  private OffsetDateTime completedAt;
  private OffsetDateTime lastModifiedAt;

  /**
   * Adds a node to the set of currently active nodes, respecting multi-instance context.
   *
   * @param nodeId the BPMN node ID to activate
   */
  public void addActiveNode(String nodeId) {
    if (hasMultiInstanceContext()) {
      activeNodeIds.add(
          buildMiActiveNodeKey(nodeId, currentMiInstanceIndex, currentMiSubProcessId));
    } else {
      activeNodeIds.add(nodeId);
    }
  }

  /**
   * Removes a node from the set of currently active nodes, respecting multi-instance context.
   *
   * @param nodeId the BPMN node ID to deactivate
   */
  public void removeActiveNode(String nodeId) {
    if (hasMultiInstanceContext()) {
      activeNodeIds.remove(
          buildMiActiveNodeKey(nodeId, currentMiInstanceIndex, currentMiSubProcessId));
    } else {
      activeNodeIds.remove(nodeId);
    }
  }

  // ========================================================================================
  // Multi-instance subprocess context management
  // ========================================================================================

  /**
   * Sets the transient multi-instance subprocess context for the current execution chain.
   *
   * @param subProcessId the MI subprocess node ID
   * @param instanceIndex the zero-based instance index within the MI expansion
   */
  public void setMultiInstanceContext(String subProcessId, int instanceIndex) {
    this.currentMiSubProcessId = subProcessId;
    this.currentMiInstanceIndex = instanceIndex;
  }

  /** Clears the transient multi-instance context after an execution chain completes. */
  public void clearMultiInstanceContext() {
    this.currentMiSubProcessId = null;
    this.currentMiInstanceIndex = -1;
  }

  /**
   * Returns whether a multi-instance context is currently active.
   *
   * @return true if executing inside an MI subprocess instance
   */
  public boolean hasMultiInstanceContext() {
    return currentMiSubProcessId != null && currentMiInstanceIndex >= 0;
  }

  /**
   * Returns the current MI instance index, or -1 if no MI context is set.
   *
   * @return zero-based instance index
   */
  public int getCurrentMiInstanceIndex() {
    return currentMiInstanceIndex;
  }

  /**
   * Returns the current MI subprocess node ID, or null if no MI context is set.
   *
   * @return subprocess node ID
   */
  public String getCurrentMiSubProcessId() {
    return currentMiSubProcessId;
  }

  /**
   * Builds a multi-instance indexed active node key. Format: {@code
   * nodeId::mi::instanceIndex::subProcessId}
   */
  public static String buildMiActiveNodeKey(String nodeId, int instanceIndex, String subProcessId) {
    return nodeId + "::mi::" + instanceIndex + "::" + subProcessId;
  }

  /** Checks if an active node key is a multi-instance indexed key. */
  public static boolean isMiActiveNodeKey(String activeNodeKey) {
    return activeNodeKey != null && activeNodeKey.contains("::mi::");
  }

  /** Extracts the base node ID from a potentially MI-indexed active node key. */
  public static String extractBaseNodeId(String activeNodeKey) {
    if (activeNodeKey == null) return null;
    int miIdx = activeNodeKey.indexOf("::mi::");
    return miIdx >= 0 ? activeNodeKey.substring(0, miIdx) : activeNodeKey;
  }

  /**
   * Extracts the MI instance index from an MI-indexed active node key. Returns -1 if the key is not
   * MI-indexed.
   */
  public static int extractMiInstanceIndex(String activeNodeKey) {
    if (activeNodeKey == null) return -1;
    int miIdx = activeNodeKey.indexOf("::mi::");
    if (miIdx < 0) return -1;
    String rest = activeNodeKey.substring(miIdx + 6);
    int sepIdx = rest.indexOf("::");
    if (sepIdx < 0) return -1;
    return Integer.parseInt(rest.substring(0, sepIdx));
  }

  /** Extracts the MI subprocess ID from an MI-indexed active node key. */
  public static String extractMiSubProcessId(String activeNodeKey) {
    if (activeNodeKey == null) return null;
    int miIdx = activeNodeKey.indexOf("::mi::");
    if (miIdx < 0) return null;
    String rest = activeNodeKey.substring(miIdx + 6);
    int sepIdx = rest.indexOf("::");
    return sepIdx >= 0 ? rest.substring(sepIdx + 2) : null;
  }

  /** Finds the first MI-indexed active node entry matching the given base node ID. */
  public Optional<String> findMiActiveNode(String baseNodeId) {
    return activeNodeIds.stream().filter(id -> id.startsWith(baseNodeId + "::mi::")).findFirst();
  }

  /** Checks if any MI-indexed children are active for a given subprocess. */
  public boolean hasMiActiveChildren(String subProcessId) {
    String suffix = "::" + subProcessId;
    return activeNodeIds.stream().anyMatch(id -> id.contains("::mi::") && id.endsWith(suffix));
  }

  /** Removes all MI-indexed active nodes for a given subprocess (used for scope termination). */
  public void removeAllMiActiveNodes(String subProcessId) {
    String suffix = "::" + subProcessId;
    activeNodeIds.removeIf(id -> id.contains("::mi::") && id.endsWith(suffix));
  }

  /**
   * Inserts or replaces an execution log entry keyed by node ID.
   *
   * @param entry the log entry to store
   */
  public void addLog(ExecutionLogEntry entry) {
    executionHistory.put(entry.getNodeId(), entry);
  }

  /**
   * Records an execution event for a node, creating or updating the log entry as needed.
   *
   * @param nodeId the BPMN node ID
   * @param nodeName human-readable node name
   * @param nodeType the type of BPMN element
   * @param sourceNodeId the upstream node that triggered this execution
   * @param sequenceFlowId the sequence flow traversed (may be null)
   * @param state the new node execution state
   * @param newMetaData additional metadata to merge into the log entry (may be null)
   */
  public void addExecutionLog(
      String nodeId,
      String nodeName,
      NodeType nodeType,
      String sourceNodeId,
      String sequenceFlowId,
      NodeState state,
      Map<String, Object> newMetaData) {
    // Find existing entry for this nodeId
    ExecutionLogEntry logEntry = executionHistory.get(nodeId);
    if (logEntry == null) {
      ExecutionLogEntry executionLogEntry =
          ExecutionLogEntry.builder()
              .nodeId(nodeId)
              .nodeName(nodeName)
              .nodeType(nodeType)
              .sourceNodeId(sourceNodeId)
              .stateChanges(new ArrayList<>(List.of(new StateChange(state))))
              .build();
      if (sequenceFlowId != null) {
        executionLogEntry.addSequenceFlowId(sequenceFlowId);
      }
      addLog(executionLogEntry);
    } else {
      logEntry.addStateChange(new StateChange(state));
      if (sequenceFlowId != null) {
        logEntry.addSequenceFlowId(sequenceFlowId);
      }

      Optional.ofNullable(newMetaData)
          .filter(m -> !m.isEmpty())
          .ifPresent(logEntry.getMetaData()::putAll);

      addLog(logEntry);
    }
  }

  // ========================================================================================
  // Type-safe execution state accessors (preferred — use ExecutionStateKey records)
  // ========================================================================================

  /**
   * Retrieves a typed execution state value.
   *
   * @param key the typed state key
   * @param <T> value type
   * @return the stored value, or null if absent
   */
  @SuppressWarnings("unchecked")
  public <T> T getState(ExecutionStateKey<T> key) {
    return (T) executionState.get(key.toStorageKey());
  }

  /**
   * Retrieves a typed execution state value with a fallback default.
   *
   * @param key the typed state key
   * @param defaultValue returned when no value is stored
   * @param <T> value type
   * @return the stored value, or defaultValue if absent
   */
  @SuppressWarnings("unchecked")
  public <T> T getState(ExecutionStateKey<T> key, T defaultValue) {
    Object value = executionState.get(key.toStorageKey());
    return value != null ? (T) value : defaultValue;
  }

  /**
   * Stores a typed execution state value.
   *
   * @param key the typed state key
   * @param value the value to store
   * @param <T> value type
   */
  public <T> void putState(ExecutionStateKey<T> key, T value) {
    executionState.put(key.toStorageKey(), value);
  }

  /**
   * Removes an execution state entry.
   *
   * @param key the typed state key to remove
   */
  public void removeState(ExecutionStateKey<?> key) {
    executionState.remove(key.toStorageKey());
  }

  /**
   * Checks whether an execution state entry exists.
   *
   * @param key the typed state key
   * @return true if a value is stored for this key
   */
  public boolean containsState(ExecutionStateKey<?> key) {
    return executionState.containsKey(key.toStorageKey());
  }

  // ========================================================================================
  // Legacy string-based accessor (kept for backward compatibility during migration)
  // ========================================================================================

  /**
   * Legacy string-keyed accessor for execution state. Prefer typed {@link #getState} methods.
   *
   * @param key the raw string key
   * @param defaultValue returned on null or class-cast failure
   * @param <T> expected value type
   * @return the stored value cast to T, or defaultValue
   */
  @SuppressWarnings("unchecked")
  public <T> T getExecutionState(String key, T defaultValue) {
    Object value = executionState.get(key);
    if (value == null) {
      return defaultValue;
    }
    try {
      return (T) value;
    } catch (ClassCastException e) {
      return defaultValue;
    }
  }

  // ========================================================================================
  // Parallel gateway token management (using typed keys)
  // ========================================================================================

  /**
   * Increments the token count for a parallel gateway and checks if all tokens have arrived.
   *
   * @param gatewayId the joining parallel gateway node ID
   * @param requiredTokens total tokens required to proceed
   * @return true if the gateway has received all required tokens
   */
  public boolean addParallelGatewayToken(String gatewayId, int requiredTokens) {
    var key = new ExecutionStateKey.ParallelGatewayToken(gatewayId);
    int currentTokens = getState(key, 0);
    int newTokenCount = currentTokens + 1;
    putState(key, newTokenCount);
    return newTokenCount >= requiredTokens;
  }

  /**
   * Returns the current token count for a parallel gateway.
   *
   * @param gatewayId the parallel gateway node ID
   * @return number of tokens received so far
   */
  public int getParallelGatewayTokenCount(String gatewayId) {
    return getState(new ExecutionStateKey.ParallelGatewayToken(gatewayId), 0);
  }

  /**
   * Resets the token count for a parallel gateway (e.g., after synchronization).
   *
   * @param gatewayId the parallel gateway node ID
   */
  public void resetParallelGatewayTokens(String gatewayId) {
    removeState(new ExecutionStateKey.ParallelGatewayToken(gatewayId));
  }

  @Override
  public String toString() {
    return "ProcessInstance{"
        + "processInstanceId='"
        + processInstanceId
        + '\''
        + ", processDefinitionId='"
        + processDefinitionId
        + '\''
        + ", completed="
        + completed
        + '}';
  }
}
