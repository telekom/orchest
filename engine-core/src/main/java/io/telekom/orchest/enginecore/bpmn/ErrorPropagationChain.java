package io.telekom.orchest.enginecore.bpmn;

import static io.telekom.orchest.enginecore.bpmn.utils.EngineExecutionUtils.*;

import io.telekom.orchest.api.core.adapters.data.dto.IncidentEventPayload;
import io.telekom.orchest.api.core.adapters.data.model.ParentProcessActivity;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.*;
import io.telekom.orchest.api.core.model.bpmn.node.*;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.enginecore.IncidentEventHandlerAdapter;
import io.telekom.orchest.enginecore.bpmn.service.EventRegisterService;
import io.telekom.orchest.enginecore.bpmn.service.ProcessInstanceService;
import io.telekom.orchest.enginecore.bpmn.utils.VariablesUtils;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

/**
 * Handles error propagation through the BPMN scope hierarchy using Chain of Responsibility.
 * Searches for matching error boundary events starting from the activity's scope and traversing up.
 * If no matching boundary event is found, raises an incident.
 */
@Slf4j
@RequiredArgsConstructor
public class ErrorPropagationChain {

  /**
   * Callback to execute a node in the engine's execution loop. Used to trigger boundary event flows
   * after finding a matching error handler.
   */
  @FunctionalInterface
  public interface NodeExecutionCallback {
    void executeNode(ProcessInstance instance, BaseNode node, String sourceNodeId);
  }

  private final ProcessInstanceService processInstanceService;
  private final IncidentEventHandlerAdapter incidentEventHandlerAdapter;
  private final EventRegisterService eventRegisterService;
  private final NodeExecutionCallback nodeExecutionCallback;

  /**
   * Public API: handles error for a process instance by ID. Loads the instance, merges variables,
   * and delegates to the core error handler.
   */
  public boolean handleError(
      String processInstanceId, String activityId, String errorCode, Variables variables) {
    log.info(
        "Handling error for activity {} in instance {} - ErrorCode: {}",
        activityId,
        processInstanceId,
        errorCode);

    ProcessInstance instance =
        processInstanceService
            .getInstanceById(processInstanceId)
            .orElseThrow(
                () -> new IllegalArgumentException("Instance not found: " + processInstanceId));

    Map<String, Object> mergedVariables =
        VariablesUtils.getMergedVariables(variables, instance.getVariables());
    if (MapUtils.isNotEmpty(mergedVariables)) {
      instance.setVariables(mergedVariables);
      log.debug(
          "Updated variables for instance {}: {}", instance.getProcessInstanceId(), variables);
    }

    return handleError(instance, activityId, errorCode);
  }

  /**
   * Core error handling: searches for matching error boundary events and propagates up scopes.
   * Implements the ExecutionContext.handleError contract.
   */
  public boolean handleError(ProcessInstance instance, String nodeId, String errorCode) {
    ProcessDefinition definition = instance.getProcessDefinition();

    BaseNode node =
        definition
            .getNode(nodeId)
            .orElseThrow(() -> new IllegalArgumentException("Node not found: " + nodeId));

    try {
      EventNode errorBoundaryEvent = findErrorBoundaryEvent(definition, node, errorCode);

      if (errorBoundaryEvent != null) {
        log.info(
            "Found error boundary event {} for error code {} on node {}",
            errorBoundaryEvent.getId(),
            errorCode,
            nodeId);

        // MI-aware removal: remove this node (handles both regular and MI-indexed keys)
        removeActiveNodeMiAware(instance, nodeId);

        instance.addExecutionLog(
            nodeId,
            node.getName(),
            node.getType(),
            null,
            getSequenceFlowId(instance.getProcessDefinition(), null, nodeId),
            NodeState.FAILED,
            buildMetadata(instance, node));

        // Handle cleanup BEFORE executing the boundary event flow
        String attachedToRef = errorBoundaryEvent.getAttachedToId();
        if (attachedToRef != null) {
          boolean cancelActivity = errorBoundaryEvent.isCancelActivity();
          if (cancelActivity) {
            BaseNode attachedNode = definition.getNode(attachedToRef).orElse(null);
            if (attachedNode instanceof SubProcessNode) {
              log.info(
                  "Error boundary event interrupts SubProcess {}. Terminating scope.",
                  attachedToRef);
              Set<String> nodesToRemove = new HashSet<>();
              for (String activeNodeId : instance.getActiveNodeIds()) {
                String baseId = ProcessInstance.extractBaseNodeId(activeNodeId);
                if (baseId.equals(nodeId)) continue;
                instance
                    .getProcessDefinition()
                    .getNode(baseId)
                    .ifPresent(
                        n -> {
                          if (isChildOfScope(n, attachedToRef, definition)) {
                            nodesToRemove.add(activeNodeId);
                          }
                        });
              }
              nodesToRemove.forEach(id -> instance.getActiveNodeIds().remove(id));
              // Also remove any MI-indexed children of this scope
              instance.removeAllMiActiveNodes(attachedToRef);
            }
            instance.removeActiveNode(attachedToRef);
          }
        }

        // Execute the error boundary event flow
        nodeExecutionCallback.executeNode(instance, errorBoundaryEvent, nodeId);

        processInstanceService.save(instance);
        return true;

      } else {
        // Not found in current process instance. Check parent process (Call Activity propagation).
        if (instance.getParentProcesActivity() != null) {
          ParentProcessActivity ppa = instance.getParentProcesActivity();
          log.info(
              "Propagating error {} from instance {} to parent instance {} (Call Activity {})",
              errorCode,
              instance.getProcessInstanceId(),
              ppa.getProcessInstanceId(),
              ppa.getLinkedNode().getId());

          boolean parentHandled =
              handleError(
                  ppa.getProcessInstanceId(),
                  ppa.getLinkedNode().getId(),
                  errorCode,
                  Variables.builder().variables(instance.getVariables()).build());

          if (parentHandled) {
            log.info(
                "Parent handled propagated error. Terminating child instance {}",
                instance.getProcessInstanceId());
            instance.setCompleted(true);
            instance.setState(PIState.TERMINATED);
            instance.removeActiveNode(nodeId);
            instance.addExecutionLog(
                nodeId,
                node.getName(),
                node.getType(),
                null,
                getSequenceFlowId(instance.getProcessDefinition(), null, nodeId),
                NodeState.FAILED,
                buildMetadata(instance, node));
            processInstanceService.save(instance);
            return true;
          } else {
            log.warn(
                "Parent failed to handle propagated error. Raising incident on child instance {}",
                instance.getProcessInstanceId());
            raiseIncident(instance, nodeId, errorCode, "Parent failed to handle propagated error");
            processInstanceService.save(instance);
            return false;
          }
        }

        // No error boundary event found and no parent to propagate to - raise incident
        log.error(
            "No error boundary event found for error code {} on node {} in instance {}. Stopping execution and raising incident.",
            errorCode,
            nodeId,
            instance.getProcessInstanceId());
        raiseIncident(
            instance, nodeId, errorCode, "No matching error boundary event found in any scope");

        processInstanceService.save(instance);
        return false;
      }

    } catch (Exception e) {
      log.error("{}", e.getMessage(), e);
      return false;
    }
  }

  /**
   * Public API: handles incident on a process instance. Sets the instance to INCIDENT state, sends
   * an incident event, and propagates the incident up to all parent processes in the call activity
   * hierarchy.
   */
  public boolean handleIncident(
      String processInstanceId, String activityId, String incidentMessage) {
    log.info("Handling incident for activity {} in instance {}", activityId, processInstanceId);

    Optional<ProcessInstance> instanceOpt =
        processInstanceService.getInstanceById(processInstanceId);
    if (instanceOpt.isEmpty()) {
      log.error("Instance not found for incident handling: {}", processInstanceId);
      return false;
    }

    ProcessInstance instance = instanceOpt.get();
    if (instance.isHasIncident() || PIState.INCIDENT.equals(instance.getState())) {
      log.warn(
          "Instance {} already has an incident. Skipping duplicate incident handling for activity {}.",
          processInstanceId,
          activityId);
      return true;
    }

    instance.setHasIncident(true);
    instance.setState(PIState.INCIDENT);
    instance.setIncidentMessage(incidentMessage);
    ProcessInstance saved = processInstanceService.save(instance);

    eventRegisterService.cleanupPendingRetryTimers(processInstanceId);

    propagateIncidentToParents(instance, processInstanceId, incidentMessage);

    IncidentEventPayload incidentEventPayload =
        IncidentEventPayload.builder()
            .processInstanceId(processInstanceId)
            .correlationId(
                saved.getCorrelationIds() != null
                    ? String.join(",", saved.getCorrelationIds())
                    : "")
            .incidentMessage(incidentMessage)
            .processDefinitionId(saved.getProcessDefinitionId())
            .version(saved.getVersion())
            .activityId(activityId)
            .build();
    incidentEventHandlerAdapter.handle(incidentEventPayload);
    return true;
  }

  /**
   * Propagates an incident up the parent process chain. Each parent is marked with INCIDENT state
   * and the incidentSourceInstanceId pointing to the child that originally caused the incident.
   */
  void propagateIncidentToParents(
      ProcessInstance childInstance,
      String incidentSourceInstanceId,
      String originalIncidentMessage) {
    if (childInstance.getParentProcesActivity() == null) {
      log.info(
          "Instance {} has no parent process activity. No incident propagation needed.",
          incidentSourceInstanceId);
      return;
    }

    log.info(
        "Starting incident propagation from child {} to parent chain", incidentSourceInstanceId);
    ProcessInstance current = childInstance;
    while (current.getParentProcesActivity() != null) {
      String parentInstanceId = current.getParentProcesActivity().getProcessInstanceId();
      log.info("Propagating incident to parent instance {}", parentInstanceId);
      Optional<ProcessInstance> parentOpt =
          processInstanceService.getInstanceById(parentInstanceId);
      if (parentOpt.isEmpty()) {
        log.warn("Parent instance {} not found during incident propagation", parentInstanceId);
        break;
      }

      ProcessInstance parent = parentOpt.get();
      if (parent.isHasIncident() || PIState.INCIDENT.equals(parent.getState())) {
        log.info(
            "Parent instance {} already has an incident. Stopping propagation.", parentInstanceId);
        break;
      }

      String propagatedMessage =
          String.format(
              "Child process instance %s has incident: %s",
              incidentSourceInstanceId, originalIncidentMessage);

      parent.setHasIncident(true);
      parent.setState(PIState.INCIDENT);
      parent.setIncidentMessage(propagatedMessage);
      parent.setIncidentSourceInstanceId(incidentSourceInstanceId);

      ParentProcessActivity parentActivity = current.getParentProcesActivity();
      if (parentActivity.getLinkedNode() != null) {
        BaseNode callActivityNode = parentActivity.getLinkedNode();
        parent.addExecutionLog(
            callActivityNode.getId(),
            callActivityNode.getName(),
            callActivityNode.getType(),
            null,
            null,
            NodeState.INCIDENT,
            null);
      }

      processInstanceService.save(parent);

      log.info(
          "Propagated incident from child {} to parent instance {} successfully",
          incidentSourceInstanceId,
          parentInstanceId);

      current = parent;
    }
  }

  /**
   * Removes an active node, handling both regular and MI-indexed active node keys. For MI-indexed
   * nodes, finds the first matching entry for the base node ID.
   */
  private void removeActiveNodeMiAware(ProcessInstance instance, String nodeId) {
    // Try direct removal first
    if (instance.getActiveNodeIds().contains(nodeId)) {
      instance.getActiveNodeIds().remove(nodeId);
      return;
    }
    // Try MI-indexed removal
    instance.findMiActiveNode(nodeId).ifPresent(miKey -> instance.getActiveNodeIds().remove(miKey));
  }

  /**
   * Searches for a matching error boundary event for the given error code. Starts from the node's
   * direct boundary events, then traverses up to parent scopes.
   */
  private EventNode findErrorBoundaryEvent(
      ProcessDefinition definition, BaseNode node, String errorCode) {
    // First, check boundary events directly attached to this activity
    if (node instanceof ActivityNode activityNode) {
      EventNode boundaryEvent =
          findErrorBoundaryEventOnActivity(definition, activityNode, errorCode);
      if (boundaryEvent != null) {
        return boundaryEvent;
      }
    }

    // Traverse up scopes (subprocess -> process)
    String currentScopeId = node.getScopeId();
    ScopeType currentScopeType = node.getScopeType();

    while (currentScopeId != null && currentScopeType != ScopeType.PROCESS) {
      BaseNode scopeNode = definition.getNode(currentScopeId).orElse(null);
      if (scopeNode instanceof SubProcessNode subProcess) {
        EventNode boundaryEvent =
            findErrorBoundaryEventOnActivity(definition, subProcess, errorCode);
        if (boundaryEvent != null) {
          return boundaryEvent;
        }
        currentScopeId = subProcess.getScopeId();
        currentScopeType = subProcess.getScopeType();
      } else {
        break;
      }
    }

    return null;
  }

  /**
   * Finds a matching error boundary event on a specific activity. Matches by error code, or catches
   * all if no error code is specified on the boundary event.
   */
  private EventNode findErrorBoundaryEventOnActivity(
      ProcessDefinition definition, ActivityNode activity, String errorCode) {
    Map<String, BaseNode> definitionNodes = definition.getNodes();
    return activity.getBoundaryEventIds().stream()
        .map(definitionNodes::get)
        .filter(node -> node instanceof EventNode)
        .map(node -> (EventNode) node)
        .filter(
            boundaryEvent -> {
              if (boundaryEvent.getEventType() == EventType.ERROR) {
                String boundaryErrorCode = boundaryEvent.getErrorCode();
                return boundaryErrorCode == null
                    || boundaryErrorCode.isEmpty()
                    || boundaryErrorCode.equals(errorCode);
              }
              return false;
            })
        .findFirst()
        .orElse(null);
  }

  /**
   * Raises an incident when no error boundary event is found. Sets the instance to INCIDENT state
   * and sends an incident event payload.
   */
  void raiseIncident(ProcessInstance instance, String activityId, String errorCode, String reason) {
    String incidentMessage =
        String.format(
            "No error boundary event found for activity %s. ErrorCode=%s, Reason=%s",
            activityId, errorCode, reason);

    instance.setHasIncident(true);
    instance.setIncidentMessage(incidentMessage);
    instance.setState(PIState.INCIDENT);

    removeActiveNodeMiAware(instance, activityId);

    String activityName = "";
    NodeType activityType = NodeType.SERVICE_TASK;

    BaseNode activityNode = instance.getProcessDefinition().getNode(activityId).orElse(null);
    Map<String, Object> incidentMetadata =
        activityNode != null ? buildMetadata(instance, activityNode) : null;
    instance.addExecutionLog(
        activityId,
        activityName,
        activityType,
        null,
        getSequenceFlowId(instance.getProcessDefinition(), null, activityId),
        NodeState.INCIDENT,
        incidentMetadata);
    log.error(
        "INCIDENT raised for instance {} - Execution STOPPED: {}",
        instance.getProcessInstanceId(),
        incidentMessage);
    log.error(
        "Process instance {} is now in INCIDENT state and will not proceed further until incident is resolved",
        instance.getProcessInstanceId());

    propagateIncidentToParents(instance, instance.getProcessInstanceId(), incidentMessage);

    IncidentEventPayload incidentEventPayload =
        IncidentEventPayload.builder()
            .processInstanceId(instance.getProcessInstanceId())
            .correlationId(
                instance.getCorrelationIds() != null
                    ? String.join(",", instance.getCorrelationIds())
                    : "")
            .incidentMessage(incidentMessage)
            .processDefinitionId(instance.getProcessDefinitionId())
            .version(instance.getVersion())
            .activityId(activityId)
            .build();
    incidentEventHandlerAdapter.handle(incidentEventPayload);
  }
}
