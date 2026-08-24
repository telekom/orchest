package io.telekom.orchest.enginecore.bpmn.execution.impl;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Executor for Receive Tasks. Enters a wait state until a matching message is correlated. */
public class ReceiveTaskExecutor implements NodeExecutor {
  private static final Logger log = LoggerFactory.getLogger(ReceiveTaskExecutor.class);

  /**
   * Executes the receive task by entering a wait state for an incoming message.
   *
   * @param instance the active process instance
   * @param node the receive task node to execute
   * @param context the execution context
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    log.info("Reached Receive Task: {}. Waiting for message...", node.getName());
  }
}
