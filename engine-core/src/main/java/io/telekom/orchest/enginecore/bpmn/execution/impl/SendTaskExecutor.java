package io.telekom.orchest.enginecore.bpmn.execution.impl;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for Send Tasks. Applies data mappings and proceeds, representing outbound message
 * dispatch.
 */
public class SendTaskExecutor implements NodeExecutor {
  private static final Logger log = LoggerFactory.getLogger(SendTaskExecutor.class);

  /**
   * Executes the send task by applying data mappings and proceeding to outgoing nodes.
   *
   * @param instance the active process instance
   * @param node the send task node to execute
   * @param context the execution context
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    log.info("Executing Send Task: {}. (Sending message/signal)", node.getName());
    ProcessDefinition definition = instance.getProcessDefinition();
    IODataMappingsUtils.setDataMappings(node, instance.getVariables());

    for (BaseNode outgoing : definition.getOutgoingNodes(node.getId())) {
      context.proceed(instance, outgoing, node.getId());
    }
  }
}
