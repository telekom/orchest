package io.telekom.orchest.enginecore.bpmn.execution;

import static io.telekom.orchest.connectors.BpmnConnectorParser.IS_CONNECTOR;

import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.ServiceTaskHandlerAdapter;
import io.telekom.orchest.enginecore.bpmn.execution.impl.*;
import io.telekom.orchest.enginecore.feel.ConditionEvaluator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/** Registry that maps BPMN node types to their corresponding executor implementations. */
public class NodeExecutorRegistry {
  private final Map<NodeType, NodeExecutor> executors = new EnumMap<>(NodeType.class);
  private NodeExecutor connectorDispatchExecutor;

  public NodeExecutorRegistry(
      ConditionEvaluator conditionEvaluator,
      ServiceTaskHandlerAdapter serviceTaskHandlerAdapter,
      OrchestWorkflowEngine workflowEngine,
      Map<String, Boolean> commonWorker) {
    // Events
    executors.put(NodeType.START_EVENT, new StartEventExecutor());
    executors.put(NodeType.END_EVENT, new EndEventExecutor());
    executors.put(NodeType.BOUNDARY_EVENT, new BoundaryEventExecutor());
    executors.put(NodeType.INTERMEDIATE_CATCH_EVENT, new IntermediateCatchEventExecutor());
    executors.put(NodeType.INTERMEDIATE_THROW_EVENT, new IntermediateThrowEventExecutor());

    // Tasks
    executors.put(
        NodeType.SERVICE_TASK, new ServiceTaskExecutor(serviceTaskHandlerAdapter, commonWorker));
    executors.put(NodeType.USER_TASK, new UserTaskExecutor());
    executors.put(NodeType.RECEIVE_TASK, new ReceiveTaskExecutor());
    executors.put(NodeType.SEND_TASK, new SendTaskExecutor());
    executors.put(NodeType.MANUAL_TASK, new ManualTaskExecutor());
    executors.put(NodeType.SCRIPT_TASK, new ScriptTaskExecutor());
    executors.put(NodeType.TASK, new EmptyTaskExecutor());

    executors.put(NodeType.BUSINESS_RULE_TASK, new BusinessRuleTaskExecutor());

    // Subprocesses
    executors.put(NodeType.SUB_PROCESS, new SubProcessExecutor());
    if (workflowEngine != null) {
      executors.put(NodeType.CALL_ACTIVITY, new CallActivityExecutor(workflowEngine));
    }

    // Gateways
    executors.put(NodeType.EXCLUSIVE_GATEWAY, new ExclusiveGatewayExecutor(conditionEvaluator));
    executors.put(NodeType.PARALLEL_GATEWAY, new ParallelGatewayExecutor());
    executors.put(NodeType.INCLUSIVE_GATEWAY, new InclusiveGatewayExecutor(conditionEvaluator));
    executors.put(NodeType.EVENT_BASED_GATEWAY, new EventBasedGatewayExecutor());
  }

  /**
   * Registers a node executor for a specific BPMN node type.
   *
   * @param type the BPMN node type
   * @param executor the executor to handle nodes of this type
   */
  public void register(NodeType type, NodeExecutor executor) {
    executors.put(type, executor);
  }

  /**
   * Registers the executor used to dispatch all connector service tasks to the connector service.
   *
   * @param executor The connector dispatch executor.
   */
  public void registerConnectorDispatcher(NodeExecutor executor) {
    this.connectorDispatchExecutor = executor;
  }

  /**
   * Returns the executor for the given node type, routing connector service tasks to the connector
   * dispatch executor if registered.
   *
   * @param type the BPMN node type
   * @param node the node instance (inspected for connector properties)
   * @return the matching executor, or empty if none is registered
   */
  public Optional<NodeExecutor> getExecutor(NodeType type, BaseNode node) {
    Map<String, Object> nodeProperties = node.getProperties();
    // connector service tasks are dispatched to the connector service via a single dispatcher
    if (type == NodeType.SERVICE_TASK
        && nodeProperties != null
        && nodeProperties.getOrDefault(IS_CONNECTOR, false).equals(true)) {
      return Optional.ofNullable(connectorDispatchExecutor);
    }
    return Optional.ofNullable(executors.get(type));
  }
}
