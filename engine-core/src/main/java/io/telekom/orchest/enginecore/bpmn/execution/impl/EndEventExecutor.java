package io.telekom.orchest.enginecore.bpmn.execution.impl;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.EventType;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.EventNode;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Executor for End Events. Handles various end event types including terminate, message, signal,
 * error, escalation, and compensation to finalize process execution.
 */
@Slf4j
public class EndEventExecutor implements NodeExecutor {

  /**
   * Executes an end event node by dispatching to the appropriate handler based on event type.
   *
   * @param instance the active process instance
   * @param node the end event node to execute
   * @param context the execution context
   * @throws IllegalArgumentException if the node is not an EventNode
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof EventNode eventNode)) {
      throw new IllegalArgumentException("EndEventExecutor requires an EventNode");
    }

    IODataMappingsUtils.setDataMappings(eventNode, instance.getVariables());
    EventType eventType = eventNode.getEventType();

    log.info("Executing End Event: {} (Type: {})", node.getName(), eventType);

    switch (eventType) {
      case TERMINATE:
        handleTerminateEndEvent(instance, eventNode, context);
        break;
      case MESSAGE:
        handleMessageEndEvent(instance, eventNode, context);
        break;
      case SIGNAL:
        handleSignalEndEvent(instance, eventNode, context);
        break;
      case ERROR:
        handleErrorEndEvent(instance, eventNode, context);
        break;
      case ESCALATION:
        handleEscalationEndEvent(instance, eventNode, context);
        break;
      case COMPENSATION:
        handleCompensationEndEvent(instance, eventNode, context);
        break;
      case NONE:
      default:
        // None end event - normal completion
        log.info("Process completed at EndEvent: {}", node.getName());
        break;
    }
  }

  private void handleTerminateEndEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    log.warn(
        "Terminate End Event {} - Terminating scope {}",
        eventNode.getName(),
        eventNode.getScopeId());
    // Terminate events end the current scope (process or subprocess), canceling all active
    // activities in that scope
    context.terminate(instance, eventNode.getScopeId());
  }

  private void handleMessageEndEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String messageRef = (String) eventNode.getProperties().get("messageRef");
    String messageId = (String) eventNode.getProperties().get("messageId");

    log.info(
        "Message End Event {} - Sending message: MessageRef: {}, MessageId: {}",
        eventNode.getName(),
        messageRef,
        messageId);

    // In a real implementation, this would send a message to other processes/instances
    log.info("Message '{}' sent, process completed", messageRef);
  }

  private void handleSignalEndEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String signalRef = eventNode.getSignalRef();
    String signalId = (String) eventNode.getProperties().get("signalId");

    log.info(
        "Signal End Event {} - Broadcasting signal: SignalRef: {}, SignalId: {}",
        eventNode.getName(),
        signalRef,
        signalId);

    // In a real implementation, this would broadcast a signal to all waiting catch events
    log.info("Signal '{}' broadcast, process completed", signalRef);
  }

  private void handleErrorEndEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String errorRef = eventNode.getErrorRef();
    String errorCode = eventNode.getErrorCode();

    log.error(
        "Error End Event {} - Throwing error: ErrorRef: {}, ErrorCode: {}",
        eventNode.getName(),
        errorRef,
        errorCode);

    // Error end events throw an error that can be caught by parent processes or boundary events
    log.error("Error '{}' thrown, process ended with error", errorCode);

    context.handleError(instance, eventNode.getId(), errorCode);
  }

  private void handleEscalationEndEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String escalationRef = eventNode.getEscalationRef();
    String escalationCode = eventNode.getEscalationCode();

    log.warn(
        "Escalation End Event {} - Throwing escalation: EscalationRef: {}, EscalationCode: {}",
        eventNode.getName(),
        escalationRef,
        escalationCode);

    // Escalation end events throw an escalation that can be caught by parent processes
    log.warn("Escalation '{}' thrown, process ended with escalation", escalationCode);
  }

  private void handleCompensationEndEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String compensationRef = (String) eventNode.getProperties().get("compensationRef");

    log.info(
        "Compensation End Event {} - Triggering compensation: CompensationRef: {}",
        eventNode.getName(),
        compensationRef);

    // Compensation end events trigger compensation handlers
    // In a real implementation, this would find and execute compensation handlers
    log.info("Compensation '{}' triggered, process completed", compensationRef);
  }
}
