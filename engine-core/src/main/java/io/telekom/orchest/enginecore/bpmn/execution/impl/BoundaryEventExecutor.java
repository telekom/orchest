package io.telekom.orchest.enginecore.bpmn.execution.impl;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.EventType;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.EventNode;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Executor for Boundary Events. Handles different boundary event types: Timer, Message, Signal,
 * Error, Escalation, Conditional, Cancel. Boundary events interrupt or non-interrupt the attached
 * activity when triggered.
 */
@Slf4j
public class BoundaryEventExecutor implements NodeExecutor {

  /**
   * Executes a boundary event node by dispatching to the appropriate handler based on event type.
   *
   * @param instance the active process instance
   * @param node the boundary event node to execute
   * @param context the execution context
   * @throws IllegalArgumentException if the node is not an EventNode
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof EventNode eventNode)) {
      throw new IllegalArgumentException("BoundaryEventExecutor requires an EventNode");
    }

    EventType eventType = eventNode.getEventType();
    String attachedTo = eventNode.getAttachedToId();
    IODataMappingsUtils.setDataMappings(node, instance.getVariables());

    log.info(
        "Executing Boundary Event: {} (Type: {}) attached to: {}",
        node.getName(),
        eventType,
        attachedTo != null ? attachedTo : "unknown");

    // Check if this is an interrupting boundary event (default is true)
    boolean cancelActivity = eventNode.isCancelActivity();

    if (cancelActivity && attachedTo != null) {
      log.info("Boundary event {} is interrupting activity {}", node.getName(), attachedTo);
      // In a real implementation, this would cancel/interrupt the attached activity
    } else {
      log.info(
          "Boundary event {} is non-interrupting, activity {} continues",
          node.getName(),
          attachedTo != null ? attachedTo : "unknown");
    }

    switch (eventType) {
      case TIMER:
        handleTimerBoundaryEvent(instance, eventNode, context);
        break;
      case MESSAGE:
        handleMessageBoundaryEvent(instance, eventNode, context);
        break;
      case SIGNAL:
        handleSignalBoundaryEvent(instance, eventNode, context);
        break;
      case ERROR:
        handleErrorBoundaryEvent(instance, eventNode, context);
        break;
      case ESCALATION:
        handleEscalationBoundaryEvent(instance, eventNode, context);
        break;
      case CONDITIONAL:
        handleConditionalBoundaryEvent(instance, eventNode, context);
        break;
      case CANCEL:
        handleCancelBoundaryEvent(instance, eventNode, context);
        break;
      case NONE:
      default:
        // No specific event type - proceed along exception path
        log.debug("Boundary event {} has no specific event type, proceeding", node.getName());
        proceedToOutgoing(instance, eventNode, context);
        break;
    }
  }

  private void handleTimerBoundaryEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String timerDuration = eventNode.getTimerDuration();
    String timerDate = eventNode.getTimerDate();
    String timerCycle = eventNode.getTimerCycle();

    log.info(
        "Timer boundary event {} triggered - Duration: {}, Date: {}, Cycle: {}",
        eventNode.getName(),
        timerDuration,
        timerDate,
        timerCycle);
    proceedToOutgoing(instance, eventNode, context);
  }

  private void handleMessageBoundaryEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String messageRef = (String) eventNode.getProperties().get("messageRef");
    String messageId = (String) eventNode.getProperties().get("messageId");

    log.info(
        "Message boundary event {} triggered - MessageRef: {}, MessageId: {}",
        eventNode.getName(),
        messageRef,
        messageId);
    proceedToOutgoing(instance, eventNode, context);
  }

  private void handleSignalBoundaryEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String signalRef = eventNode.getSignalRef();
    String signalId = (String) eventNode.getProperties().get("signalId");

    log.info(
        "Signal boundary event {} triggered - SignalRef: {}, SignalId: {}",
        eventNode.getName(),
        signalRef,
        signalId);
    proceedToOutgoing(instance, eventNode, context);
  }

  private void handleErrorBoundaryEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String errorRef = eventNode.getErrorRef();
    String errorCode = eventNode.getErrorCode();

    log.info(
        "Error boundary event {} triggered - ErrorRef: {}, ErrorCode: {}",
        eventNode.getName(),
        errorRef,
        errorCode);
    proceedToOutgoing(instance, eventNode, context);
  }

  private void handleEscalationBoundaryEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String escalationRef = eventNode.getEscalationRef();
    String escalationCode = eventNode.getEscalationCode();

    log.warn(
        "Escalation boundary event {} triggered - EscalationRef: {}, EscalationCode: {}",
        eventNode.getName(),
        escalationRef,
        escalationCode);
    proceedToOutgoing(instance, eventNode, context);
  }

  private void handleConditionalBoundaryEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String condition = eventNode.getCondition();

    log.info(
        "Conditional boundary event {} triggered - Condition: {}", eventNode.getName(), condition);
    proceedToOutgoing(instance, eventNode, context);
  }

  private void handleCancelBoundaryEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    log.info("Cancel boundary event {} triggered - Canceling transaction", eventNode.getName());
    // Cancel events are used to cancel transactions
    proceedToOutgoing(instance, eventNode, context);
  }

  private void proceedToOutgoing(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    // Boundary events proceed along their exception/alternative path
    ProcessDefinition definition = instance.getProcessDefinition();
    for (BaseNode outgoing : definition.getOutgoingNodes(eventNode.getId())) {
      context.proceed(instance, outgoing, eventNode.getId());
    }
  }
}
