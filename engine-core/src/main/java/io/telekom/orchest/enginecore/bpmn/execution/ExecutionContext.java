package io.telekom.orchest.enginecore.bpmn.execution;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.enginecore.bpmn.service.DecisionDefinitionService;
import io.telekom.orchest.enginecore.bpmn.service.DecisionInstanceService;
import io.telekom.orchest.enginecore.bpmn.service.EventRegisterService;
import io.telekom.orchest.enginecore.bpmn.service.UserTaskService;
import java.util.Map;

/**
 * Interface providing execution context for BPMN process execution. Provides methods to control
 * process flow (proceed, terminate, error handling) and access to engine services (decision
 * definitions, decision instances, event registration).
 */
public interface ExecutionContext {
  /**
   * Proceeds execution to the next node in the process flow.
   *
   * @param instance The process instance.
   * @param nextNode The next node to execute.
   * @param sourceNodeId The ID of the node that triggered this transition.
   */
  void proceed(ProcessInstance instance, BaseNode nextNode, String sourceNodeId);

  /**
   * Terminates a process instance or subprocess scope.
   *
   * @param instance The process instance.
   * @param scopeId The ID of the scope (process or subprocess) to terminate.
   */
  void terminate(ProcessInstance instance, String scopeId);

  /**
   * Handles an error that occurred during node execution.
   *
   * @param instance The process instance.
   * @param nodeId The ID of the node where the error occurred.
   * @param errorCode The error code identifying the type of error.
   * @return true if the error was handled successfully, false otherwise.
   */
  boolean handleError(ProcessInstance instance, String nodeId, String errorCode);

  void sendMessageEvent(String messageName, String correlationId, Map<String, Object> variables);

  void sendSignalEvent(String signalName, Map<String, Object> variables);

  /**
   * Gets the decision definition service for evaluating DMN decision tables.
   *
   * @return The DecisionDefinitionService instance.
   */
  DecisionDefinitionService getDecisionDefinitionService();

  /**
   * Gets the decision instance service for managing decision execution instances.
   *
   * @return The DecisionInstanceService instance.
   */
  DecisionInstanceService getDecisionInstanceService();

  /**
   * Gets the event register service for managing BPMN events (timers, messages, signals).
   *
   * @return The EventRegisterService instance.
   */
  EventRegisterService getEventRegisterService();

  /**
   * Gets the user task service for managing user task lifecycle (create, claim, complete).
   *
   * @return The UserTaskService instance, or null if not configured.
   */
  UserTaskService getUserTaskService();
}
