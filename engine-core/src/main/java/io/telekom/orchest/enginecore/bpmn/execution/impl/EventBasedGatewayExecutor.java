package io.telekom.orchest.enginecore.bpmn.execution.impl;

import static io.telekom.orchest.enginecore.parser.BPMNParser.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.EventType;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.EventNode;
import io.telekom.orchest.api.core.model.bpmn.node.GatewayNode;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import io.telekom.orchest.api.core.utils.ExecutionLogUtils;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.service.EventRegisterService;
import io.telekom.orchest.enginecore.feel.FeelEvaluationEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for Event-Based Gateways (BPMN 2.0).
 *
 * <p>An Event-Based Gateway is a branching point where execution waits for one of several competing
 * events (timer, message, signal, conditional) to occur. When the <b>first</b> event fires,
 * execution continues exclusively on that path and all other event subscriptions are cancelled
 * immediately.
 *
 * <h3>BPMN 2.0 Specification Compliance</h3>
 *
 * <ul>
 *   <li>Outgoing sequence flows must lead to Intermediate Catch Events or Receive Tasks
 *   <li>Exactly one path is taken — exclusive (race) semantics
 *   <li>All non-triggered event subscriptions are cancelled when one fires
 *   <li>Supported event types: Timer, Message, Signal, Conditional
 * </ul>
 *
 * <h3>Implementation Details</h3>
 *
 * <ol>
 *   <li>On execution, generates a unique {@code linkedEventId} for this gateway instance
 *   <li>Registers event subscriptions for all outgoing Intermediate Catch Events
 *   <li>Each subscription is tagged with the {@code linkedEventId} for coordinated cancellation
 *   <li>The gateway and all catch events are marked as active nodes
 *   <li>When an event fires, {@code OrchestWorkflowEngine.cleanupEventBasedGateway()} handles the
 *       cancellation of competing events and cleanup of active nodes
 * </ol>
 */
public class EventBasedGatewayExecutor implements NodeExecutor {
  private static final Logger log = LoggerFactory.getLogger(EventBasedGatewayExecutor.class);

  /** Creates a typed key for the linked event ID of an event-based gateway. */
  public static ExecutionStateKey.EventGatewayLinkedId gatewayLinkedIdKey(String gatewayId) {
    return new ExecutionStateKey.EventGatewayLinkedId(gatewayId);
  }

  /** Creates a typed key for the catch event node IDs of an event-based gateway. */
  public static ExecutionStateKey.EventGatewayCatchEvents gatewayCatchEventsKey(String gatewayId) {
    return new ExecutionStateKey.EventGatewayCatchEvents(gatewayId);
  }

  /**
   * Executes the event-based gateway by registering event subscriptions for all outgoing catch
   * events and entering a wait state.
   *
   * @param instance the active process instance
   * @param node the event-based gateway node to execute
   * @param context the execution context
   * @throws IllegalArgumentException if the node is not a GatewayNode
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    if (!(node instanceof GatewayNode gatewayNode)) {
      throw new IllegalArgumentException(
          "EventBasedGatewayExecutor requires a GatewayNode, got: "
              + node.getClass().getSimpleName());
    }

    String gatewayId = node.getId();
    ProcessDefinition definition = instance.getProcessDefinition();
    EventRegisterService eventRegisterService = context.getEventRegisterService();

    // Generate a unique linked event ID for this gateway execution.
    // This ID links all event subscriptions together so they can be cancelled as a group.
    String linkedEventId = "ebg:" + gatewayId + ":" + instance.getProcessInstanceId();

    log.info(
        "Executing Event-Based Gateway '{}' ({}). Registering outgoing event subscriptions with linkedEventId: {}",
        gatewayNode.getName(),
        gatewayId,
        linkedEventId);

    // Get all outgoing nodes (should be Intermediate Catch Events or Receive Tasks per BPMN spec)
    List<BaseNode> outgoingNodes = definition.getOutgoingNodes(gatewayId);
    List<String> catchEventNodeIds = new ArrayList<>();

    for (BaseNode outgoing : outgoingNodes) {
      if (outgoing instanceof EventNode eventNode
          && outgoing.getType() == NodeType.INTERMEDIATE_CATCH_EVENT) {
        // Register the event subscription and activate the catch event node
        catchEventNodeIds.add(eventNode.getId());
        registerCatchEventForGateway(instance, eventNode, linkedEventId, eventRegisterService);
        instance.addActiveNode(eventNode.getId());
        ExecutionLogUtils.addExecutionLog(instance, eventNode, gatewayId, NodeState.STARTED);

        log.info(
            "Registered {} catch event '{}' ({}) for Event-Based Gateway '{}'",
            eventNode.getEventType(),
            eventNode.getName(),
            eventNode.getId(),
            gatewayNode.getName());

      } else if (outgoing.getType() == NodeType.RECEIVE_TASK) {
        // Receive Tasks can also follow an Event-Based Gateway (per BPMN 2.0 spec)
        catchEventNodeIds.add(outgoing.getId());
        instance.addActiveNode(outgoing.getId());
        ExecutionLogUtils.addExecutionLog(instance, outgoing, gatewayId, NodeState.STARTED);

        log.info(
            "Activated Receive Task '{}' ({}) for Event-Based Gateway '{}'",
            outgoing.getName(),
            outgoing.getId(),
            gatewayNode.getName());

      } else {
        log.warn(
            "Event-Based Gateway '{}' has unsupported outgoing node type: {} ({}). "
                + "Only Intermediate Catch Events and Receive Tasks are valid per BPMN 2.0.",
            gatewayNode.getName(),
            outgoing.getType(),
            outgoing.getId());
      }
    }

    if (catchEventNodeIds.isEmpty()) {
      log.error(
          "Event-Based Gateway '{}' has no valid outgoing catch events. "
              + "This violates BPMN 2.0 specification.",
          gatewayNode.getName());
      return;
    }

    // Store gateway metadata in the execution state for cleanup during event resolution
    instance.putState(gatewayLinkedIdKey(gatewayId), linkedEventId);
    instance.putState(gatewayCatchEventsKey(gatewayId), catchEventNodeIds);

    log.info(
        "Event-Based Gateway '{}' is now waiting. {} event subscription(s) registered.",
        gatewayNode.getName(),
        catchEventNodeIds.size());
  }

  /**
   * Registers an event subscription for a catch event that is part of an event-based gateway. Each
   * subscription is tagged with a linkedEventId so all subscriptions from the same gateway can be
   * cancelled together when one of them fires.
   */
  private void registerCatchEventForGateway(
      ProcessInstance instance,
      EventNode eventNode,
      String linkedEventId,
      EventRegisterService eventRegisterService) {
    EventType eventType = eventNode.getEventType();
    switch (eventType) {
      case TIMER ->
          registerTimerForGateway(instance, eventNode, linkedEventId, eventRegisterService);
      case MESSAGE ->
          registerMessageForGateway(instance, eventNode, linkedEventId, eventRegisterService);
      case SIGNAL ->
          registerSignalForGateway(instance, eventNode, linkedEventId, eventRegisterService);
      case CONDITIONAL ->
          log.info(
              "Conditional catch event '{}' noted for Event-Based Gateway "
                  + "(polling-based evaluation, no persistent subscription created)",
              eventNode.getName());
      default ->
          log.warn(
              "Unsupported event type {} for Event-Based Gateway catch event '{}'",
              eventType,
              eventNode.getName());
    }
  }

  /**
   * Registers a timer event subscription for the gateway. Supports ISO-8601 duration, date, and
   * cycle expressions.
   */
  private void registerTimerForGateway(
      ProcessInstance instance,
      EventNode eventNode,
      String linkedEventId,
      EventRegisterService eventRegisterService) {
    String timerDuration = eventNode.getTimerDuration();
    String timerDate = eventNode.getTimerDate();
    String timerCycle = eventNode.getTimerCycle();

    // Use whichever timer expression is defined (priority: duration > date > cycle)
    String timerExpression = timerDuration;
    if (timerExpression == null) {
      timerExpression = timerDate;
    }
    if (timerExpression == null) {
      timerExpression = timerCycle;
    }

    if (timerExpression == null) {
      log.warn(
          "Timer catch event '{}' has no timer expression (duration/date/cycle) defined",
          eventNode.getName());
      return;
    }

    eventRegisterService.registerTimerEventForGateway(
        instance, timerExpression, eventNode, linkedEventId);
    log.debug(
        "Registered timer event for gateway: event='{}', expression='{}'",
        eventNode.getName(),
        timerExpression);
  }

  /**
   * Registers a message event subscription for the gateway. Evaluates the correlation key
   * expression against process variables.
   */
  private void registerMessageForGateway(
      ProcessInstance instance,
      EventNode eventNode,
      String linkedEventId,
      EventRegisterService eventRegisterService) {
    String messageName = eventNode.getMessageName();
    String correlationKey = eventNode.getMessageCorrelationKey();

    if (messageName == null) {
      log.warn("Message catch event '{}' has no message name defined", eventNode.getName());
      return;
    }

    // Evaluate correlation key expression against process variables
    String evaluatedCorrelationKey = null;
    if (correlationKey != null) {
      Optional<Object> evaluated =
          FeelEvaluationEngine.evaluateExpression(correlationKey, instance.getVariables());
      evaluatedCorrelationKey = evaluated.map(Object::toString).orElse(null);
    }

    eventRegisterService.registerMessageEventForGateway(
        messageName,
        evaluatedCorrelationKey,
        instance.getProcessInstanceId(),
        eventNode,
        linkedEventId);
    log.debug(
        "Registered message event for gateway: event='{}', messageName='{}', correlationKey='{}'",
        eventNode.getName(),
        messageName,
        evaluatedCorrelationKey);
  }

  /** Registers a signal event subscription for the gateway. */
  private void registerSignalForGateway(
      ProcessInstance instance,
      EventNode eventNode,
      String linkedEventId,
      EventRegisterService eventRegisterService) {
    String signalName = eventNode.getSignalRef();

    if (signalName == null) {
      log.warn("Signal catch event '{}' has no signal name defined", eventNode.getName());
      return;
    }

    eventRegisterService.registerSignalEventForGateway(
        signalName, instance.getProcessInstanceId(), eventNode, linkedEventId);
    log.debug(
        "Registered signal event for gateway: event='{}', signalName='{}'",
        eventNode.getName(),
        signalName);
  }
}
