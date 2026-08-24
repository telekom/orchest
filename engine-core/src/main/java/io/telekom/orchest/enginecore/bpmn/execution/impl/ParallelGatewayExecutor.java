package io.telekom.orchest.enginecore.bpmn.execution.impl;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Executor for Parallel Gateways (AND). Handles both fork (splitting into parallel paths) and join
 * (synchronizing multiple incoming tokens) semantics.
 */
@Slf4j
public class ParallelGatewayExecutor implements NodeExecutor {

  /**
   * Executes the parallel gateway by forking into multiple parallel paths or joining after all
   * incoming tokens are received.
   *
   * @param instance the active process instance
   * @param node the parallel gateway node to execute
   * @param context the execution context
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    ProcessDefinition definition = instance.getProcessDefinition();
    int incomingCount = definition.getIncomingNodes(node.getId()).size();
    int outgoingCount = definition.getOutgoingNodes(node.getId()).size();
    IODataMappingsUtils.setDataMappings(node, instance.getVariables());
    // Same as exclusive gateway: proceed() runs synchronously; clear active token first.
    instance.removeActiveNode(node.getId());

    // Determine if this is a fork (multiple outgoing) or join (multiple incoming)
    if (incomingCount > 1 && outgoingCount == 1) {
      // Join: Multiple incoming, single outgoing
      // All tokens have been received (checked before execution), so proceed
      log.info(
          "Parallel Gateway Join: {} - All incoming flows completed, proceeding", node.getName());
      for (BaseNode outgoing : definition.getOutgoingNodes(node.getId())) {
        context.proceed(instance, outgoing, node.getId());
      }
    } else if (incomingCount == 1 && outgoingCount > 1) {
      // Fork: Single incoming, multiple outgoing - split into parallel paths
      log.info(
          "Parallel Gateway Fork: {} - Splitting into {} parallel paths",
          node.getName(),
          outgoingCount);
      for (BaseNode outgoing : definition.getOutgoingNodes(node.getId())) {
        context.proceed(instance, outgoing, node.getId());
      }
    } else {
      // Mixed or unexpected configuration
      log.warn(
          "Parallel Gateway {} has {} incoming and {} outgoing flows. Treating as fork.",
          node.getName(),
          incomingCount,
          outgoingCount);
      for (BaseNode outgoing : definition.getOutgoingNodes(node.getId())) {
        context.proceed(instance, outgoing, node.getId());
      }
    }
  }

  /**
   * Checks if a node is a parallel gateway join and handles token tracking. Returns true if the
   * join gateway should be executed (all tokens received), false if waiting. Returns null if the
   * node is not a parallel gateway join.
   */
  public static Boolean handleJoinToken(
      ProcessInstance instance, BaseNode node, String sourceNodeId) {
    if (node.getType() != NodeType.PARALLEL_GATEWAY) {
      return null; // Not a parallel gateway
    }

    int incomingCount = instance.getProcessDefinition().getIncomingNodes(node.getId()).size();
    if (incomingCount <= 1) {
      return null; // Not a join (fork or single path)
    }

    // This is a parallel gateway join - track tokens
    int requiredTokens = incomingCount;
    boolean allTokensReceived = instance.addParallelGatewayToken(node.getId(), requiredTokens);

    log.debug(
        "Parallel gateway join {} received token from {}. Tokens: {}/{}",
        node.getId(),
        sourceNodeId,
        instance.getParallelGatewayTokenCount(node.getId()),
        requiredTokens);

    if (allTokensReceived) {
      log.info("All tokens received for parallel gateway join {}. Ready to proceed.", node.getId());
      instance.resetParallelGatewayTokens(node.getId());
      return true; // Ready to execute
    } else {
      log.debug(
          "Waiting for more tokens at parallel gateway join {}. Current: {}/{}",
          node.getId(),
          instance.getParallelGatewayTokenCount(node.getId()),
          requiredTokens);
      return false; // Still waiting
    }
  }
}
