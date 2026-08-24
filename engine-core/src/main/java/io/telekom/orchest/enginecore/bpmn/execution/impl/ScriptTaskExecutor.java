package io.telekom.orchest.enginecore.bpmn.execution.impl;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.ScriptTaskNode;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils;
import io.telekom.orchest.enginecore.feel.FeelEvaluationEngine;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for Script Tasks. Evaluates a FEEL expression and stores the result in process
 * variables.
 */
public class ScriptTaskExecutor implements NodeExecutor {
  private static final Logger log = LoggerFactory.getLogger(ScriptTaskExecutor.class);

  /**
   * Executes the script task by evaluating its FEEL script against process variables.
   *
   * @param instance the active process instance
   * @param node the script task node to execute
   * @param context the execution context
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    log.info("Executing Script Task: {}", node.getName());
    IODataMappingsUtils.setDataMappings(node.getInputMappings(), instance.getVariables());

    if (node instanceof ScriptTaskNode scriptNode) {
      String script = scriptNode.getScript();
      if (script != null && !script.isBlank()) {
        Optional<Object> optionalResult =
            FeelEvaluationEngine.evaluateExpression(script, instance.getVariables());
        // update variable
        optionalResult.ifPresent(
            result -> {
              instance.getVariables().put(scriptNode.getResultVariable(), result);
            });
      }
    }
    IODataMappingsUtils.setDataMappings(node.getOutputMappings(), instance.getVariables());
    ProcessDefinition definition = instance.getProcessDefinition();
    for (BaseNode outgoing : definition.getOutgoingNodes(node.getId())) {
      context.proceed(instance, outgoing, node.getId());
    }
  }

  public static void main(String[] args) {
    String script =
        "=context merge(\n"
            + "  orderItem,\n"
            + "  {\n"
            + "    supportingService: [\n"
            + "      {\n"
            + "        id: internalOrderItem.service.id,\n"
            + "        \"@referredType\": internalOrderItem.service.`@type`,\n"
            + "        \"@baseType\": \"ServiceRef\",\n"
            + "        serviceType: internalOrderItem.service.serviceType\n"
            + "      }\n"
            + "    ]\n"
            + "  }\n"
            + ") ";

    FeelEvaluationEngine.evaluateExpression(
        script,
        Map.of(
            "orderItem",
            Map.of("id", "123"),
            "internalOrderItem",
            Map.of("service", Map.of("id", "123"))));
    System.out.println("lol");
  }
}
