package io.telekom.orchest.enginecore.bpmn.execution.impl;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.EventType;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.EventNode;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils;
import io.telekom.orchest.enginecore.feel.FeelEvaluationEngine;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for Intermediate Catch Events. These events wait for external triggers (timer, message,
 * signal, etc.) before proceeding.
 */
public class IntermediateCatchEventExecutor implements NodeExecutor {
  private static final Logger log = LoggerFactory.getLogger(IntermediateCatchEventExecutor.class);

  /**
   * Executes an intermediate catch event by registering the appropriate subscription and entering a
   * wait state until the event is triggered.
   *
   * @param instance the active process instance
   * @param node the intermediate catch event node to execute
   * @param context the execution context
   * @throws IllegalArgumentException if the node is not an EventNode
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof EventNode eventNode)) {
      throw new IllegalArgumentException("IntermediateCatchEventExecutor requires an EventNode");
    }

    EventType eventType = eventNode.getEventType();
    IODataMappingsUtils.setDataMappings(node, instance.getVariables());

    log.info("Executing Intermediate Catch Event: {} (Type: {})", node.getName(), eventType);

    switch (eventType) {
      case TIMER:
        handleTimerEvent(instance, eventNode, context);
        break;
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
      case CONDITIONAL:
        handleConditionalEvent(instance, eventNode, context);
        break;
      case NONE:
      default:
        // No specific event type - proceed immediately
        log.debug(
            "Intermediate Catch Event {} has no specific event type, proceeding", node.getName());
        proceedToOutgoing(instance, eventNode, context);
        break;
    }
  }

  private void handleTimerEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    Map<String, Object> variables = instance.getVariables();
    String timerDuration = null;
    String timerDate = null;
    String timerCycle = null;
    Optional<Object> evaluatedTimerDuration =
        FeelEvaluationEngine.evaluateSimpleVariable(eventNode.getTimerDuration(), variables);
    if (evaluatedTimerDuration.isPresent()) {
      timerDuration = (String) evaluatedTimerDuration.get();
    } else {
      Optional<Object> evaluatedTimerDate =
          FeelEvaluationEngine.evaluateSimpleVariable(eventNode.getTimerDate(), variables);
      if (evaluatedTimerDate.isPresent()) {
        timerDate = (String) evaluatedTimerDate.get();
      } else {
        Optional<Object> evaluatedTimerCycle =
            FeelEvaluationEngine.evaluateSimpleVariable(eventNode.getTimerCycle(), variables);
        if (evaluatedTimerCycle.isPresent()) {
          timerCycle = (String) evaluatedTimerCycle.get();
        }
      }
    }

    log.info(
        "Timer event {} - Duration: {}, Date: {}, Cycle: {}",
        eventNode.getName(),
        timerDuration,
        timerDate,
        timerCycle);
    // register the timer event
    context.getEventRegisterService().registerTimerEvent(instance, timerDuration, eventNode);

    log.info("Timer event {} is waiting for timer trigger", eventNode.getName());
  }

  private void handleMessageEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String messageName = eventNode.getMessageName();
    String correlationKey = eventNode.getMessageCorrelationKey();

    // evaluate key value
    Optional<Object> optionalEvaluateExpression =
        FeelEvaluationEngine.evaluateExpression(correlationKey, instance.getVariables());
    String evaluateExpression = optionalEvaluateExpression.map(Object::toString).orElse(null);

    log.info(
        "Message event {} - messageName: {}, correlationKey: {}",
        eventNode.getName(),
        messageName,
        correlationKey);
    context
        .getEventRegisterService()
        .registerMessageEvent(
            messageName, evaluateExpression, instance.getProcessInstanceId(), eventNode);
    // Message events are wait states - they wait for a message to arrive
    log.info("Message event {} is waiting for message: {}", eventNode.getName(), messageName);
    // The node remains active until signaled via signalEvent()
  }

  private void handleSignalEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String signalRef = eventNode.getSignalRef();
    String signalId = (String) eventNode.getProperties().get("signalId");

    log.info(
        "Signal event {} - SignalRef: {}, SignalId: {}", eventNode.getName(), signalRef, signalId);

    context
        .getEventRegisterService()
        .registerSignalEvent(
            signalRef,
            instance.getProcessInstanceId(),
            eventNode,
            instance.getProcessDefinitionId(),
            false);
    // Signal events are wait states - they wait for a signal to be broadcast
    log.info("Signal event {} is waiting for signal: {}", eventNode.getName(), signalRef);
  }

  private void handleErrorEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String errorRef = eventNode.getErrorRef();
    String errorCode = eventNode.getErrorCode();

    log.info(
        "Error event {} - ErrorRef: {}, ErrorCode: {}", eventNode.getName(), errorRef, errorCode);

    // Error events are wait states - they wait for an error to occur
    log.info("Error event {} is waiting for error: {}", eventNode.getName(), errorCode);
    // The node remains active until signaled via signalEvent()
  }

  private void handleEscalationEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String escalationRef = eventNode.getEscalationRef();
    String escalationCode = eventNode.getEscalationCode();

    log.info(
        "Escalation event {} - EscalationRef: {}, EscalationCode: {}",
        eventNode.getName(),
        escalationRef,
        escalationCode);

    // Escalation events are wait states - they wait for an escalation to occur
    log.info(
        "Escalation event {} is waiting for escalation: {}", eventNode.getName(), escalationCode);
    // The node remains active until signaled via signalEvent()
  }

  private void handleConditionalEvent(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    String condition = eventNode.getCondition();

    log.info("Conditional event {} - Condition: {}", eventNode.getName(), condition);

    // Conditional events wait for a condition to become true
    // In a real implementation, this would evaluate the condition periodically
    log.info(
        "Conditional event {} is waiting for condition to become true: {}",
        eventNode.getName(),
        condition);
    // The node remains active until signaled via signalEvent()
  }

  private void proceedToOutgoing(
      ProcessInstance instance, EventNode eventNode, ExecutionContext context) {
    ProcessDefinition definition = instance.getProcessDefinition();
    for (BaseNode outgoing : definition.getOutgoingNodes(eventNode.getId())) {
      context.proceed(instance, outgoing, eventNode.getId());
    }
  }
}
