package io.telekom.orchest.enginecore.bpmn.execution.impl;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.EventType;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.EventNode;
import io.telekom.orchest.api.core.utils.ExecutionLogUtils;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Executor for Start Events. Initiates process execution and proceeds to the first outgoing node.
 */
@Slf4j
public class StartEventExecutor implements NodeExecutor {

  /**
   * Executes the start event by dispatching to the appropriate handler based on event type and
   * proceeding to outgoing nodes.
   *
   * @param instance the active process instance
   * @param node the start event node to execute
   * @param context the execution context
   * @throws IllegalArgumentException if the node is not an EventNode
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof EventNode eventNode)) {
      throw new IllegalArgumentException("StartEventExecutor requires an EventNode");
    }

    EventType eventType = eventNode.getEventType();
    IODataMappingsUtils.setDataMappings(node, instance.getVariables());

    log.info("Executing Start Event: {} (Type: {})", node.getName(), eventType);

    switch (eventType) {
      case TIMER:
        handleTimerStartEvent(instance, eventNode, context);
        break;
      case MESSAGE:
        handleMessageStartEvent(instance, eventNode, context);
        break;
      case SIGNAL:
        handleSignalStartEvent(instance, eventNode, context);
        break;
      case CONDITIONAL:
        handleConditionalStartEvent(instance, eventNode, context);
        break;
      case ERROR:
      case ESCALATION:
        // Error and Escalation start events are typically for event subprocesses
        log.info(
            "Start event {} with type {} - typically used in event subprocesses",
            node.getName(),
            eventType);
        proceedToOutgoing(instance, eventNode, context);
        break;
      case NONE:
      default:
        // None start event - proceed immediately
        log.debug("Start event {} has no specific event type, proceeding", node.getName());
        proceedToOutgoing(instance, eventNode, context);
        break;
    }
  }

  private void handleTimerStartEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    ExecutionLogUtils.addExecutionLog(instance, eventNode, null, NodeState.COMPLETED);
    proceedToOutgoing(instance, eventNode, context);
    instance.removeActiveNode(eventNode.getId());
  }

  private void handleMessageStartEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    ExecutionLogUtils.addExecutionLog(instance, eventNode, null, NodeState.COMPLETED);
    proceedToOutgoing(instance, eventNode, context);
    instance.removeActiveNode(eventNode.getId());
  }

  private void handleSignalStartEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    ExecutionLogUtils.addExecutionLog(instance, eventNode, null, NodeState.COMPLETED);
    proceedToOutgoing(instance, eventNode, context);
    instance.removeActiveNode(eventNode.getId());
  }

  private void handleConditionalStartEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String condition = eventNode.getCondition();

    log.info("Conditional start event {} - Condition: {}", eventNode.getName(), condition);

    // Conditional start events are triggered when condition becomes true
    // For process start, we assume the condition has already been met
    log.info(
        "Conditional start event {} triggered, condition met: {}", eventNode.getName(), condition);
    proceedToOutgoing(instance, eventNode, context);
  }

  private void proceedToOutgoing(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    ProcessDefinition definition = instance.getProcessDefinition();
    for (BaseNode outgoing : definition.getOutgoingNodes(eventNode.getId())) {
      context.proceed(instance, outgoing, eventNode.getId());
    }
  }
}
