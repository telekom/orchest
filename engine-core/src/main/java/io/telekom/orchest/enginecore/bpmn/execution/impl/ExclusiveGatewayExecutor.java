package io.telekom.orchest.enginecore.bpmn.execution.impl;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.GatewayNode;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils;
import io.telekom.orchest.enginecore.feel.ConditionEvaluator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for Exclusive Gateways (XOR). Evaluates outgoing conditions and proceeds along the first
 * matching path. If no condition matches, proceeds along the default path if defined, otherwise
 * throws an exception.
 */
public class ExclusiveGatewayExecutor implements NodeExecutor {
  private static final Logger log = LoggerFactory.getLogger(ExclusiveGatewayExecutor.class);

  private final ConditionEvaluator conditionEvaluator;

  /**
   * Creates a new ExclusiveGatewayExecutor.
   *
   * @param conditionEvaluator the evaluator used to assess sequence flow conditions
   */
  public ExclusiveGatewayExecutor(ConditionEvaluator conditionEvaluator) {
    this.conditionEvaluator = conditionEvaluator;
  }

  /**
   * Executes the exclusive gateway by evaluating outgoing conditions and proceeding along the first
   * matching path or the default path.
   *
   * @param instance the active process instance
   * @param node the gateway node to execute
   * @param context the execution context
   * @throws IllegalArgumentException if the node is not a GatewayNode or no matching path is found
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof GatewayNode gateway)) {
      throw new IllegalArgumentException("ExclusiveGatewayExecutor requires a GatewayNode");
    }
    IODataMappingsUtils.setDataMappings(gateway, instance.getVariables());
    // Tokens leave the gateway immediately; remove from active before nested proceed() so
    // completion checks (e.g. subprocess scope) do not see this gateway as a still-active child.
    instance.removeActiveNode(gateway.getId());
    ProcessDefinition definition = instance.getProcessDefinition();
    List<BaseNode> outgoingNodes = definition.getOutgoingNodes(node.getId());
    for (BaseNode target : outgoingNodes) {
      String condition = gateway.getCondition(target.getId());
      if (condition != null && conditionEvaluator.evaluate(condition, instance)) {
        log.info("Condition matched: {} -> {}", condition, target.getName());
        context.proceed(instance, target, node.getId());
        return; // Exclusive: only one path
      }
    }

    // converging node or merge gateway case
    if (outgoingNodes.size() == 1) {
      context.proceed(instance, outgoingNodes.getFirst(), node.getId());
    } else {
      // run default path
      String defaultNodeTargetId =
          definition.getSequenceFlows().get(gateway.getDefaultNode()).getTargetId();
      outgoingNodes.stream()
          .filter(outgoingNode -> outgoingNode.getId().equals(defaultNodeTargetId))
          .findFirst()
          .ifPresentOrElse(
              defaultNode -> {
                context.proceed(instance, defaultNode, node.getId());
              },
              () -> {
                throw new IllegalArgumentException(
                    "ExclusiveGatewayExecutor requires a default GatewayNode");
              });
    }
  }
}
