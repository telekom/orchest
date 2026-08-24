package io.telekom.orchest.enginecore.bpmn.execution;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;

/**
 * Interface for executing BPMN nodes during process instance execution. Implementations handle the
 * specific logic for different node types (e.g., tasks, gateways, events). Each node type has its
 * own executor that implements this interface.
 */
public interface NodeExecutor {
  /**
   * Executes a BPMN node within a process instance.
   *
   * @param instance The process instance containing execution state and variables.
   * @param node The node to execute.
   * @param context The execution context providing access to engine services and operations.
   */
  void execute(ProcessInstance instance, BaseNode node, ExecutionContext context);
}
