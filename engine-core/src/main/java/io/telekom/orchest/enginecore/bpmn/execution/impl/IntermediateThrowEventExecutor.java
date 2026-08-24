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
 * Executor for Intermediate Throw Events. These events immediately throw/broadcast events (message,
 * signal, error, etc.) and proceed.
 */
@Slf4j
public class IntermediateThrowEventExecutor implements NodeExecutor {

  /**
   * Executes an intermediate throw event by broadcasting the event and proceeding to outgoing
   * nodes.
   *
   * @param instance the active process instance
   * @param node the intermediate throw event node to execute
   * @param context the execution context
   * @throws IllegalArgumentException if the node is not an EventNode
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof EventNode eventNode)) {
      throw new IllegalArgumentException("IntermediateThrowEventExecutor requires an EventNode");
    }

    EventType eventType = eventNode.getEventType();
    IODataMappingsUtils.setDataMappings(node, instance.getVariables());

    log.info("Executing Intermediate Throw Event: {} (Type: {})", node.getName(), eventType);

    switch (eventType) {
      case MESSAGE:
        handleMessageEvent(instance, eventNode, context);
        break;
      case SIGNAL:
        handleSignalEvent(instance, eventNode, context);
        break;
      case ERROR:
        handleErrorEvent(instance, eventNode, context);
        break;
      case ESCALATION:
        handleEscalationEvent(instance, eventNode, context);
        break;
      case LINK:
        handleLinkEvent(instance, eventNode, context);
        break;
      case COMPENSATION:
        handleCompensationEvent(instance, eventNode, context);
        break;
      case NONE:
      default:
        // No specific event type - proceed immediately
        log.debug(
            "Intermediate Throw Event {} has no specific event type, proceeding", node.getName());
        proceedToOutgoing(instance, eventNode, context);
        break;
    }
  }

  private void handleMessageEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String messageRef = (String) eventNode.getProperties().get("messageRef");
    String messageId = (String) eventNode.getProperties().get("messageId");

    log.info(
        "Throwing message event {} - MessageRef: {}, MessageId: {}",
        eventNode.getName(),
        messageRef,
        messageId);

    // In a real implementation, this would send a message to other processes/instances
    // For now, we just log and proceed
    log.info("Message '{}' thrown and broadcast", messageRef);
    proceedToOutgoing(instance, eventNode, context);
  }

  private void handleSignalEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String signalRef = eventNode.getSignalRef();
    String signalId = (String) eventNode.getProperties().get("signalId");

    log.info(
        "Throwing signal event {} - SignalRef: {}, SignalId: {}",
        eventNode.getName(),
        signalRef,
        signalId);

    // In a real implementation, this would broadcast a signal to all waiting catch events
    // For now, we just log and proceed
    log.info("Signal '{}' thrown and broadcast", signalRef);
    proceedToOutgoing(instance, eventNode, context);
  }

  private void handleErrorEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String errorRef = eventNode.getErrorRef();
    String errorCode = eventNode.getErrorCode();

    log.info(
        "Throwing error event {} - ErrorRef: {}, ErrorCode: {}",
        eventNode.getName(),
        errorRef,
        errorCode);

    // Error events typically trigger boundary error events or terminate the process
    // In a real implementation, this would search for matching error boundary events
    log.warn("Error '{}' thrown - this should trigger matching error boundary events", errorCode);
    proceedToOutgoing(instance, eventNode, context);
  }

  private void handleEscalationEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String escalationRef = eventNode.getEscalationRef();
    String escalationCode = eventNode.getEscalationCode();

    log.info(
        "Throwing escalation event {} - EscalationRef: {}, EscalationCode: {}",
        eventNode.getName(),
        escalationRef,
        escalationCode);

    // Escalation events typically trigger boundary escalation events
    // In a real implementation, this would search for matching escalation boundary events
    log.info(
        "Escalation '{}' thrown - this should trigger matching escalation boundary events",
        escalationCode);
    proceedToOutgoing(instance, eventNode, context);
  }

  private void handleLinkEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String linkName = eventNode.getLinkName();

    log.info("Throwing link event {} - LinkName: {}", eventNode.getName(), linkName);

    // Link events are used to create jumps within a process
    // In a real implementation, this would find the matching link catch event and jump to it
    log.info("Link '{}' thrown - jumping to matching link catch event", linkName);
    proceedToOutgoing(instance, eventNode, context);
  }

  private void handleCompensationEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String compensationRef = (String) eventNode.getProperties().get("compensationRef");

    log.info(
        "Throwing compensation event {} - CompensationRef: {}",
        eventNode.getName(),
        compensationRef);

    // Compensation events trigger compensation handlers
    // In a real implementation, this would find and execute compensation handlers
    log.info("Compensation '{}' thrown - triggering compensation handlers", compensationRef);
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
