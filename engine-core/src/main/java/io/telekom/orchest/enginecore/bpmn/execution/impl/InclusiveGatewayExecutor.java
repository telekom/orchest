package io.telekom.orchest.enginecore.bpmn.execution.impl;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.GatewayNode;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils;
import io.telekom.orchest.enginecore.feel.ConditionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for Inclusive Gateways (OR). Evaluates all outgoing conditions and proceeds along every
 * path whose condition evaluates to true, enabling parallel execution of multiple branches.
 */
public class InclusiveGatewayExecutor implements NodeExecutor {
  private static final Logger log = LoggerFactory.getLogger(InclusiveGatewayExecutor.class);

  private final ConditionEvaluator conditionEvaluator;

  /**
   * Creates a new InclusiveGatewayExecutor.
   *
   * @param conditionEvaluator the evaluator used to assess sequence flow conditions
   */
  public InclusiveGatewayExecutor(ConditionEvaluator conditionEvaluator) {
    this.conditionEvaluator = conditionEvaluator;
  }

  /**
   * Executes the inclusive gateway by evaluating all outgoing conditions and proceeding along every
   * matching path.
   *
   * @param instance the active process instance
   * @param node the gateway node to execute
   * @param context the execution context
   * @throws IllegalArgumentException if the node is not a GatewayNode
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof GatewayNode gateway)) {
      throw new IllegalArgumentException("InclusiveGatewayExecutor requires a GatewayNode");
    }
    boolean matchedAny = false;
    IODataMappingsUtils.setDataMappings(node, instance.getVariables());
    instance.removeActiveNode(gateway.getId());

    log.info("Evaluating Inclusive Gateway: {}", node.getName());

    ProcessDefinition definition = instance.getProcessDefinition();
    for (BaseNode target : definition.getOutgoingNodes(node.getId())) {
      String condition = gateway.getCondition(target.getId());
      if (conditionEvaluator.evaluate(condition, instance)) {
        log.info("  -> Condition '{}' matched. Taking path to {}", condition, target.getName());
        context.proceed(instance, target, node.getId());
        matchedAny = true;
      }
    }

    if (!matchedAny) {
      log.warn("No condition matched for Inclusive Gateway: {}", node.getName());
    }
  }
}
