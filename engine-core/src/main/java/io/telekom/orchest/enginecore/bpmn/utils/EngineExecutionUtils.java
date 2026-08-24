package io.telekom.orchest.enginecore.bpmn.utils;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Shared utility methods used across engine sub-components for execution logging, sequence flow
 * resolution, and scope hierarchy traversal.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EngineExecutionUtils {

  /** Finds the sequence flow ID between a source node and a target node. */
  public static String getSequenceFlowId(
      ProcessDefinition definition, String sourceNodeId, String targetNodeId) {
    if (sourceNodeId == null || targetNodeId == null || definition == null) {
      return null;
    }

    BaseNode sourceNode = definition.getNode(sourceNodeId).orElse(null);
    if (sourceNode == null) {
      return null;
    }

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
  public static Map<String, Object> buildMetadata(ProcessInstance instance, BaseNode node) {
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
        instance.removeState(key);
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
   * Checks if a node belongs to the given scope (or any nested child scope). Traverses the scope
   * hierarchy upward from the node's scope.
   */
  public static boolean isChildOfScope(
      BaseNode node, String scopeId, ProcessDefinition definition) {
    String currentScope = node.getScopeId();
    while (currentScope != null) {
      if (currentScope.equals(scopeId)) return true;

      String scopeToFind = currentScope;
      BaseNode scopeNode = definition.getNode(scopeToFind).orElse(null);

      if (scopeNode == null) break;
      currentScope = scopeNode.getScopeId();
    }
    return false;
  }
}
