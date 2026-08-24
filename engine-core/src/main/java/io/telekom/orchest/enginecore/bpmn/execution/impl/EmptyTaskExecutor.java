package io.telekom.orchest.enginecore.bpmn.execution.impl;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.TaskNode;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Executor for Empty Tasks. */
@Slf4j
@RequiredArgsConstructor
public class EmptyTaskExecutor implements NodeExecutor {

  /**
   * Executes the Empty task node.
   *
   * @param instance The process instance.
   * @param node The service task node to execute.
   * @param context The execution context.
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof TaskNode taskNode)) {
      throw new IllegalStateException("failed, invalid node in executor: " + node.getType());
    }
    log.info("Empty task executor has been executed for node: {}", node.getType());
    ProcessDefinition processDefinition = instance.getProcessDefinition();
    for (BaseNode outgoing : processDefinition.getOutgoingNodes(node.getId())) {
      context.proceed(instance, outgoing, node.getId());
    }
  }
}
