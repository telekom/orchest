package io.telekom.orchest.enginecore.bpmn;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import io.telekom.orchest.enginecore.bpmn.service.EventRegisterService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles cleanup of Event-Based Gateways when one of their competing catch events fires. Per BPMN
 * 2.0 specification, when one event fires in an event-based gateway, all other competing event
 * subscriptions must be cancelled (exclusive/race semantics).
 */
@Slf4j
@RequiredArgsConstructor
public class EventGatewayCoordinator {

  private final EventRegisterService eventRegisterService;

  /**
   * Cleans up an Event-Based Gateway when one of its catch events is triggered. Cancels all other
   * event subscriptions and removes competing catch event nodes from active nodes.
   *
   * @param instance The process instance.
   * @param triggeredNode The catch event node that was triggered.
   */
  public void cleanupEventBasedGateway(ProcessInstance instance, BaseNode triggeredNode) {
    ProcessDefinition definition = instance.getProcessDefinition();

    String gatewayId = findEventBasedGatewayId(definition, triggeredNode);
    if (gatewayId == null) {
      return; // Not part of an event-based gateway
    }

    var linkedIdKey = new ExecutionStateKey.EventGatewayLinkedId(gatewayId);
    var catchEventsKey = new ExecutionStateKey.EventGatewayCatchEvents(gatewayId);

    String linkedEventId = instance.getState(linkedIdKey);
    List<String> catchEventNodeIds = instance.getState(catchEventsKey);

    if (linkedEventId == null) {
      log.warn(
          "Event-Based Gateway {} has no linked event ID in execution state. "
              + "Gateway may have already been cleaned up.",
          gatewayId);
      return;
    }

    log.info(
        "Cleaning up Event-Based Gateway '{}'. Triggered event: '{}' ({}). "
            + "Cancelling all competing event subscriptions.",
        gatewayId,
        triggeredNode.getName(),
        triggeredNode.getId());

    // Cancel ALL linked event subscriptions from all repositories
    eventRegisterService.cancelLinkedEvents(linkedEventId);

    // Remove all OTHER catch event nodes from active nodes and log their cancellation
    if (catchEventNodeIds != null) {
      for (String catchEventId : catchEventNodeIds) {
        if (!catchEventId.equals(triggeredNode.getId())) {
          instance.removeActiveNode(catchEventId);
          definition
              .getNode(catchEventId)
              .ifPresent(
                  node ->
                      instance.addExecutionLog(
                          catchEventId,
                          node.getName(),
                          node.getType(),
                          gatewayId,
                          null,
                          NodeState.CANCELLED,
                          null));
          log.info(
              "Cancelled competing catch event '{}' from Event-Based Gateway '{}'",
              catchEventId,
              gatewayId);
        }
      }
    }

    // Remove the gateway itself from active nodes and log its completion
    instance.removeActiveNode(gatewayId);
    definition
        .getNode(gatewayId)
        .ifPresent(
            gwNode ->
                instance.addExecutionLog(
                    gatewayId,
                    gwNode.getName(),
                    gwNode.getType(),
                    null,
                    null,
                    NodeState.COMPLETED,
                    null));

    // Clean up execution state entries
    instance.removeState(linkedIdKey);
    instance.removeState(catchEventsKey);

    log.info(
        "Event-Based Gateway '{}' cleanup complete. Continuing on path of triggered event '{}'",
        gatewayId,
        triggeredNode.getId());
  }

  /**
   * Finds the Event-Based Gateway ID that is the direct incoming node of the given catch event
   * node.
   *
   * @param definition The process definition.
   * @param node The node to check for an incoming event-based gateway.
   * @return The gateway ID, or null if not found.
   */
  public String findEventBasedGatewayId(ProcessDefinition definition, BaseNode node) {
    List<BaseNode> incomingNodes = definition.getIncomingNodes(node.getId());
    for (BaseNode incoming : incomingNodes) {
      if (incoming.getType() == NodeType.EVENT_BASED_GATEWAY) {
        return incoming.getId();
      }
    }
    return null;
  }
}
