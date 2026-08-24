package io.telekom.orchest.api.core.utils;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.*;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.EventNode;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import java.util.*;

/**
 * Utility class for managing execution logs in BPMN process instances. Provides methods to add
 * execution log entries and determine special start event types.
 */
public class ExecutionLogUtils {

  /**
   * List of event types that are considered special start events. These include timer, signal, and
   * message start events.
   */
  public static final List<EventType> SPECIAL_START_EVENTS =
      Arrays.asList(EventType.TIMER, EventType.SIGNAL, EventType.MESSAGE);

  /**
   * Adds an execution log entry for a node execution. Records the node state transition, sequence
   * flow, and metadata (e.g., child process instance ID for call activities).
   *
   * @param instance The process instance to add the log entry to.
   * @param node The node that was executed.
   * @param sourceNodeId The ID of the source node that triggered this execution.
   * @param nodeState The state of the node (e.g., TRIGGERED, STARTED, COMPLETED).
   */
  public static void addExecutionLog(
      ProcessInstance instance, BaseNode node, String sourceNodeId, NodeState nodeState) {
    String sequenceFlowId =
        getSequenceFlowId(instance.getProcessDefinition(), sourceNodeId, node.getId());
    instance.addExecutionLog(
        node.getId(),
        node.getName(),
        node.getType(),
        sourceNodeId,
        sequenceFlowId,
        nodeState,
        buildMetadata(instance, node));
  }

  private static String getSequenceFlowId(
      ProcessDefinition definition, String sourceNodeId, String targetNodeId) {
    if (sourceNodeId == null || targetNodeId == null || definition == null) {
      return null;
    }

    BaseNode sourceNode = definition.getNode(sourceNodeId).orElse(null);
    if (sourceNode == null) {
      return null;
    }

    // Find the sequence flow ID where the target matches targetNodeId
    return sourceNode.getOutgoingSequenceFlowIds().entrySet().stream()
        .filter(entry -> targetNodeId.equals(entry.getValue()))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(null);
  }

  /**
   * Builds metadata map for ExecutionLogEntry. For CallActivity nodes, includes the child process
   * instance ID(s). For BusinessRuleTask nodes, includes the decision instance ID.
   */
  private static Map<String, Object> buildMetadata(ProcessInstance instance, BaseNode node) {
    Map<String, Object> metadata = new HashMap<>();

    if (node.getType() == NodeType.CALL_ACTIVITY) {
      // Check for multi-instance: collect all child process instance IDs from counter keys
      int loopSize = getLoopSizeFromState(instance, node.getId());
      if (loopSize > 0) {
        List<String> childIds = new ArrayList<>();
        for (int i = 0; i < loopSize; i++) {
          var counterKey = new ExecutionStateKey.CallActivityChildCounter(i);
          String childId = instance.getState(counterKey);
          if (childId != null) {
            childIds.add(childId);
          }
        }
        if (!childIds.isEmpty()) {
          metadata.put("childProcessInstanceIds", childIds);
        }
      } else {
        // Single call activity
        var key = new ExecutionStateKey.CallActivityChild(node.getId());
        String childProcessInstanceId = instance.getState(key);
        if (childProcessInstanceId != null) {
          metadata.put("childProcessInstanceId", childProcessInstanceId);
        }
      }
    }

    if (node.getType() == NodeType.BUSINESS_RULE_TASK) {
      var key = new ExecutionStateKey.DecisionInstance(node.getId());
      Object decisionInstanceId = instance.getState(key);
      if (decisionInstanceId != null) {
        metadata.put("decisionInstanceId", decisionInstanceId);
        instance.removeState(key);
      }
    }

    if (node.getType() == NodeType.USER_TASK) {
      var key = new ExecutionStateKey.UserTaskId(node.getId());
      String userTaskId = instance.getState(key);
      if (userTaskId != null) {
        metadata.put("userTaskId", userTaskId);
      }
    }

    return metadata.isEmpty() ? null : metadata;
  }

  /**
   * Safely retrieves the multi-instance loop size from execution state. Handles MongoDB
   * deserialization where Integer may be stored as Long or Number.
   */
  private static int getLoopSizeFromState(ProcessInstance instance, String nodeId) {
    Object raw =
        instance
            .getExecutionState()
            .get(new ExecutionStateKey.MultiInstanceLoopSize(nodeId).toStorageKey());
    if (raw instanceof Number n) {
      return n.intValue();
    }
    return 0;
  }

  /**
   * Checks if a node is a special start event (timer, signal, or message start event).
   *
   * @param node The node to check.
   * @return true if the node is a special start event, false otherwise.
   */
  public static boolean isNodeASpecialStartEvent(BaseNode node) {
    return node instanceof EventNode startEvent
        && startEvent.getType().equals(NodeType.START_EVENT)
        && SPECIAL_START_EVENTS.contains(startEvent.getEventType());
  }

  //    public void addExecutionLog(String nodeId, String nodeName, NodeType nodeType,
  //                                String sourceNodeId, String sequenceFlowId,
  //                                NodeState state,
  //                                Map<String, Object> newMetaData) {
  //        // Find existing entry for this nodeId
  //        ExecutionLogEntry logEntry = executionHistory.get(nodeId);
  //        if(logEntry == null){
  //            ExecutionLogEntry executionLogEntry = ExecutionLogEntry.builder()
  //
  // .nodeId(nodeId).nodeName(nodeName).nodeType(nodeType).sourceNodeId(sourceNodeId)
  //                    .stateChanges(new ArrayList<>(List.of(new StateChange(state))))
  //                    .build();
  //            if(sequenceFlowId != null){
  //                executionLogEntry.addSequenceFlowId(sequenceFlowId);
  //            }
  //            addLog(executionLogEntry);
  //        }else {
  //            logEntry.addStateChange(new StateChange(state));
  //            if(sequenceFlowId != null){
  //                logEntry.addSequenceFlowId(sequenceFlowId);
  //            }
  //
  //            Optional.ofNullable(newMetaData)
  //                    .filter(m -> !m.isEmpty())
  //                    .ifPresent(logEntry.getMetaData()::putAll);
  //
  //            addLog(logEntry);
  //        }
  //    }
  //
  //    public void addLog(ExecutionLogEntry entry) {
  //        executionHistory.put(entry.getNodeId(), entry);
  //    }
}
