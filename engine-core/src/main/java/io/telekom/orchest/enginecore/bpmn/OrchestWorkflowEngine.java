package io.telekom.orchest.enginecore.bpmn;

import static io.telekom.orchest.api.core.model.bpmn.NodeType.SUB_PROCESS;
import static io.telekom.orchest.enginecore.bpmn.utils.EngineExecutionUtils.*;
import static io.telekom.orchest.enginecore.bpmn.utils.MultiInstanceUtils.*;
import static io.telekom.orchest.enginecore.bpmn.utils.MultiInstanceUtils.isSequentialAndPending;
import static io.telekom.orchest.enginecore.bpmn.utils.ProcessInstanceUtils.checkIfInstanceHasIncident;

import io.telekom.orchest.api.core.adapters.data.dto.IncidentEventPayload;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.adapters.data.model.*;
import io.telekom.orchest.api.core.model.bpmn.*;
import io.telekom.orchest.api.core.model.bpmn.node.*;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.api.core.utils.ExecutionLogUtils;
import io.telekom.orchest.api.core.utils.StateChangeUtils;
import io.telekom.orchest.cache.core.Cache;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import io.telekom.orchest.enginecore.bpmn.execution.impl.ParallelGatewayExecutor;
import io.telekom.orchest.enginecore.bpmn.service.*;
import io.telekom.orchest.enginecore.bpmn.utils.*;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

/**
 * Core workflow engine implementation. Delegates error handling to {@link ErrorPropagationChain},
 * event gateway cleanup to {@link EventGatewayCoordinator}, and process lifecycle to {@link
 * ProcessLifecycleManager}.
 */
@Slf4j
@Getter
public class OrchestWorkflowEngine implements WorkflowEngine, ExecutionContext {

  private final OWEDependencies oweDependencies;
  private final ErrorPropagationChain errorPropagationChain;
  private final EventGatewayCoordinator eventGatewayCoordinator;
  private final ProcessLifecycleManager processLifecycleManager;
  private final OrchestEngineTelemetryService telemetryService;
  private final Cache<String, ActivityState> activityStateCache;

  public OrchestWorkflowEngine(
      OWEDependencies oweDependencies,
      OrchestEngineTelemetryService telemetryService,
      Cache<String, ActivityState> activityStateCache) {
    this.oweDependencies = oweDependencies;
    this.activityStateCache = activityStateCache;
    this.errorPropagationChain =
        new ErrorPropagationChain(
            oweDependencies.processInstanceService(),
            oweDependencies.incidentEventHandlerAdapter(),
            oweDependencies.eventRegisterService(),
            this::executeNode);
    this.eventGatewayCoordinator =
        new EventGatewayCoordinator(oweDependencies.eventRegisterService());
    this.processLifecycleManager =
        new ProcessLifecycleManager(
            oweDependencies.processDefinitionService(),
            oweDependencies.processInstanceService(),
            this::executeNode,
            telemetryService,
            oweDependencies.metricsRecorder());
    this.telemetryService = telemetryService;
  }

  // ========================================================================================
  // WorkflowEngine: Deployment
  // ========================================================================================

  @Override
  public ProcessDefinition deployProcessDefinition(String xml, boolean isCompensateFlow) {
    if (isCompensateFlow) {
      return oweDependencies.processDefinitionService().deployCompensateProcessDefinition(xml);
    } else {
      return oweDependencies.processDefinitionService().deployProcessDefinition(xml);
    }
  }

  @Override
  public DecisionDefinition deployDecisionDefinition(String xml) {
    return oweDependencies.decisionDefinitionService().deployDecisionDefinition(xml);
  }

  // ========================================================================================
  // WorkflowEngine: Process Start (delegated to ProcessLifecycleManager)
  // ========================================================================================

  @Override
  public ProcessInstance startProcess(
      String processDefinitionId, String processInstanceId, Map<String, Object> variables) {
    return processLifecycleManager.startProcess(
        processDefinitionId, null, processInstanceId, variables, null);
  }

  @Override
  public ProcessInstance startDynamicProcess(
      String utf8BpmnXML, String processInstanceId, Map<String, Object> variables) {
    return processLifecycleManager.startDynamicProcess(utf8BpmnXML, processInstanceId, variables);
  }

  @Override
  public ProcessInstance startProcess(
      String processDefinitionId,
      String processInstanceId,
      Map<String, Object> variables,
      ParentProcessActivity parentProcessActivity) {
    return processLifecycleManager.startProcess(
        processDefinitionId, null, processInstanceId, variables, parentProcessActivity);
  }

  @Override
  public ProcessInstance startProcess(
      String definitionId,
      Integer version,
      String processInstanceId,
      Map<String, Object> variables) {
    return processLifecycleManager.startProcess(
        definitionId, version, processInstanceId, variables, null);
  }

  // ========================================================================================
  // WorkflowEngine: Event Handling
  // ========================================================================================

  @Override
  public void handleSignalEvent(String eventName, Variables variables) {
    log.info("received signalEvent request with messageName: {}", eventName);
    List<SignalEvent> signalEvents =
        oweDependencies.signalEventRepository().findAllRegisteredSignalsEvent(eventName);
    if (signalEvents.isEmpty()) {
      log.warn("No registered signalEvent for signal {}", eventName);
      telemetryService.incrementSignalEventCounter(eventName, "notFound");
      return;
    }

    signalEvents.forEach(
        signals -> {
          try {
            if (signals.getIsStartEvent()) {
              processLifecycleManager.startProcess(
                  signals.getProcessDefinitionId(), null, null, variables.getVariables(), null);
            } else {
              resumeActivity(
                  signals.getProcessInstanceId(), signals.getNodeInformation().getId(), variables);
              oweDependencies.signalEventRepository().remove(signals);
            }
            telemetryService.incrementSignalEventCounter(eventName, "success");
          } catch (Exception e) {
            log.error("failed to handle signalEvent for message {}", signals.getSignalName(), e);
            telemetryService.incrementSignalEventCounter(eventName, "failed");
          }
        });
  }

  @Override
  public void handleMessageEvent(String messageName, String correlationId, Variables variables) {
    log.info(
        "received message event request with messageName: {} and correlationId: {}",
        messageName,
        correlationId);
    Optional<MessageEventStore> messageEventStore =
        oweDependencies.messageEventRepository().findMessageEvent(messageName, correlationId);
    if (messageEventStore.isEmpty()) {
      log.warn(
          "No registered message event for message {} with correlationKey: {}",
          messageName,
          correlationId);
      telemetryService.incrementMessageEventCounter(messageName, "notFound");
      return;
    }

    messageEventStore.ifPresent(
        messageEvent -> {
          try {
            if (messageEvent.getIsStartEvent()) {
              processLifecycleManager.startProcess(
                  messageEvent.getProcessDefinitionId(),
                  null,
                  null,
                  variables.getVariables(),
                  null);
            } else {
              resumeActivity(
                  messageEvent.getProcessInstanceId(),
                  messageEvent.getNodeInformation().getId(),
                  variables);
            }
            telemetryService.incrementMessageEventCounter(messageName, "success");
          } catch (IllegalArgumentException ex) {
            if (ex.getMessage().contains("Instance not found:")) {
              log.warn(
                  "{} for message event for message {} with correlationKey: {}",
                  ex.getMessage(),
                  messageName,
                  correlationId);
              // clear the instance if instance is not found
              oweDependencies.messageEventRepository().remove(messageEvent);
            }
            log.error(
                "failed to handle message event for message {} with correlationKey: {}",
                messageName,
                correlationId,
                ex);
          } catch (Exception e) {
            telemetryService.incrementMessageEventCounter(messageName, "failed");
            log.error(
                "failed to handle message event for message {} with correlationKey: {}",
                messageName,
                correlationId,
                e);
          }
        });
  }

  @Override
  public void handleTimerEvent(TimedEvent timedEvent) {
    BaseNode nodeInformation = timedEvent.getNodeInformation();
    if (timedEvent.getType().equals(TimedEvent.Type.TIMER)) {
      try {
        resumeActivity(timedEvent.getProcessInstanceId(), nodeInformation.getId(), null);
      } finally {
        oweDependencies.eventRegisterService().clearEvent(timedEvent);
      }
    } else if (timedEvent.getType().equals(TimedEvent.Type.START_EVENT_TIMER)) {
      try {
        processLifecycleManager.startProcess(
            timedEvent.getProcessDefinitionId(), null, null, new HashMap<>(), null);
      } finally {
        oweDependencies.eventRegisterService().registerCycledTimedEvent(timedEvent);
      }
    }
  }

  @Override
  public void handleProgressiveRetry(TimedEvent timedEvent) {
    try {
      WorkerEventRequest retryWorkerEvent = timedEvent.getEventRequest();
      if (retryWorkerEvent == null) {
        log.warn(
            "Progressive retry timed event {} has no event request; skipping",
            timedEvent.getTimedEventRegistryId());
        return;
      }
      String processInstanceId = retryWorkerEvent.getProcessInstanceId();
      String activityId = retryWorkerEvent.getActivityId();
      log.info(
          "received progressive retry event trigger for activityId: {} and instanceId: {}",
          activityId,
          processInstanceId);
      ProcessInstance instance =
          oweDependencies
              .processInstanceService()
              .getInstanceById(processInstanceId)
              .orElseThrow(
                  () -> new IllegalArgumentException("Instance not found: " + processInstanceId));

      if (instance.isHasIncident()
          || List.of(PIState.CANCELLED, PIState.COMPLETED, PIState.TERMINATED, PIState.INCIDENT)
              .contains(instance.getState())) {
        log.warn(
            "Skipping progressive retry for activity {} in instance {} - process is in state {} (hasIncident={})",
            activityId,
            processInstanceId,
            instance.getState(),
            instance.isHasIncident());
        return;
      }

      if (retryWorkerEvent.getRetriesLeft() <= 0) {
        log.warn(
            "Skipping progressive retry for activity {} in instance {} - no retries left (retriesLeft={}). Raising incident.",
            activityId,
            processInstanceId,
            retryWorkerEvent.getRetriesLeft());
        errorPropagationChain.handleIncident(
            processInstanceId, activityId, "Retry exhausted during progressive retry");
        return;
      }

      ProcessDefinition definition = instance.getProcessDefinition();
      BaseNode executedNode =
          definition
              .getNode(activityId)
              .orElseThrow(() -> new IllegalArgumentException("Node not found: " + activityId));
      instance.addExecutionLog(
          activityId,
          executedNode.getName(),
          executedNode.getType(),
          null,
          getSequenceFlowId(instance.getProcessDefinition(), null, activityId),
          NodeState.TRIGGERED,
          buildMetadata(instance, executedNode));
      retryWorkerEvent
          .getStateChanges()
          .add(StateChangeUtils.buildStateChanges(NodeState.TRIGGERED));
      retryWorkerEvent.setState(NodeState.TRIGGERED);

      oweDependencies.processInstanceService().save(instance);

      retryWorkerEvent.setEventId(java.util.UUID.randomUUID().toString());
      oweDependencies.serviceTaskHandlerAdapter().handle(retryWorkerEvent);
    } finally {
      oweDependencies.eventRegisterService().clearEvent(timedEvent);
    }
  }

  // ========================================================================================
  // WorkflowEngine: Activity Resumption
  // ========================================================================================

  @Override
  public void resumeActivity(String processInstanceId, String activityId, Variables variables) {
    log.info("Completing Service Task activity {} for instance {}", activityId, processInstanceId);
    String traceId = UUID.randomUUID().toString();
    log.info(
        "{}===================================================================================",
        traceId);
    ProcessInstance instance =
        oweDependencies
            .processInstanceService()
            .getInstanceById(processInstanceId)
            .orElseThrow(
                () -> new IllegalArgumentException("Instance not found: " + processInstanceId));

    if (instance.isHasIncident()
        || List.of(PIState.CANCELLED, PIState.COMPLETED, PIState.TERMINATED)
            .contains(instance.getState())) {
      log.warn(
          "Process instance {} has an incident or is not running. Cannot complete activity {}. Incident: {}",
          processInstanceId,
          activityId,
          instance.getIncidentMessage());
      return;
    }

    ProcessDefinition definition = instance.getProcessDefinition();

    Map<String, Object> mergedVariables =
        VariablesUtils.getMergedVariables(variables, instance.getVariables());
    if (MapUtils.isNotEmpty(mergedVariables)) {
      instance.setVariables(mergedVariables);
      log.debug("Updated variables for instance {}: {}", processInstanceId, variables);
    }

    BaseNode executedNode =
        definition
            .getNode(activityId)
            .orElseThrow(() -> new IllegalArgumentException("Node not found: " + activityId));

    // Map output data mapping after node completion
    IODataMappingsUtils.setDataMappings(executedNode.getOutputMappings(), instance.getVariables());

    // Check if this activity is inside a multi-instance subprocess - detect MI context from active
    // nodes
    Optional<String> miActiveEntry = instance.findMiActiveNode(activityId);
    if (miActiveEntry.isPresent()) {
      int miIndex = ProcessInstance.extractMiInstanceIndex(miActiveEntry.get());
      String miSubProcessId = ProcessInstance.extractMiSubProcessId(miActiveEntry.get());
      log.info(
          "Activity {} is inside MI SubProcess {} instance {}",
          activityId,
          miSubProcessId,
          miIndex);
      instance.setMultiInstanceContext(miSubProcessId, miIndex);
    }

    try {
      if (!instance.getActiveNodeIds().contains(activityId)
          && miActiveEntry.isEmpty()
          && !isMultiInstance(executedNode)) {
        log.warn(
            "Node {} is not active in instance {}. It may have already been completed or never started.",
            activityId,
            processInstanceId);
        return;
      }

      // handle multiInstance (for MI tasks like ServiceTask with MI, not for MI subprocess
      // children)
      if (isMultiInstance(executedNode)) {
        if (isParallelInstance(executedNode)) {
          clearMultiInstance(instance, executedNode);
        }
        boolean multiInstancePending = isMultiInstancePending(instance, executedNode);
        log.info("multiInstancePending: {}: Node:{}", multiInstancePending, executedNode);
        oweDependencies.processInstanceService().save(instance);
        boolean isSequentialAndPending = isSequentialAndPending(instance, executedNode);
        if (isSequentialAndPending) {
          instance.addExecutionLog(
              activityId,
              executedNode.getName(),
              executedNode.getType(),
              null,
              getSequenceFlowId(instance.getProcessDefinition(), null, activityId),
              NodeState.COMPLETED,
              buildMetadata(instance, executedNode));
          executedNode.getIncomingSequenceFlowIds().keySet().stream()
              .findFirst()
              .ifPresent(
                  flowNode -> {
                    Optional<BaseNode> previousNode =
                        instance
                            .getProcessDefinition()
                            .getNode(executedNode.getIncomingSequenceFlowIds().get(flowNode));
                    previousNode.ifPresent(
                        node -> {
                          processOutgoing(instance, node, node.getId());
                          clearMultiInstance(instance, executedNode);
                        });
                  });
          oweDependencies.processInstanceService().save(instance);
          return;
        }
        if (multiInstancePending) {
          return;
        }
      }

      instance.removeActiveNode(activityId);

      // Handle Event-Based Gateway cleanup
      eventGatewayCoordinator.cleanupEventBasedGateway(instance, executedNode);

      instance.addExecutionLog(
          activityId,
          executedNode.getName(),
          executedNode.getType(),
          null,
          getSequenceFlowId(instance.getProcessDefinition(), null, activityId),
          NodeState.COMPLETED,
          buildMetadata(instance, executedNode));

      log.info(
          "Service Task {} completed by client, continuing workflow execution",
          executedNode.getName());

      // The retried activity has now actually passed; emit the resolution event (if it was
      // awaiting one) before running downstream nodes so it is unaffected by later incidents.
      sendIncidentResolutionIfPending(instance, executedNode);

      processOutgoing(instance, executedNode, activityId);

      checkAndMarkCompleted(instance);

    } catch (Exception e) {
      log.error(
          "Error executing process instance {} for activity {}",
          instance.getProcessInstanceId(),
          activityId,
          e);
    } finally {
      instance.clearMultiInstanceContext();
      oweDependencies.processInstanceService().save(instance);
    }

    log.info(
        "{}===================================================================================",
        traceId);
  }

  @Override
  public void resumeActivityFromActivityId(ProcessInstance processInstance, String activityId) {
    log.info(
        "resuming from activityId {} for instance {}",
        activityId,
        processInstance.getProcessInstanceId());
    String traceId = UUID.randomUUID().toString();
    log.info(
        "{}===================================================================================",
        traceId);

    ProcessDefinition definition = processInstance.getProcessDefinition();

    BaseNode node =
        definition
            .getNode(activityId)
            .orElseThrow(
                () -> {
                  processInstance.setIncidentMessage(
                      "No activityId: " + activityId + " found in process!!");
                  processInstance.setHasIncident(true);
                  return new IllegalArgumentException(
                      "No activityId: " + activityId + " found in process!!");
                });
    try {
      executeNode(processInstance, node, null);
    } catch (Exception e) {
      log.error(
          "Error resuming from activityId {} for instance {}",
          activityId,
          processInstance.getProcessInstanceId(),
          e);
    } finally {
      oweDependencies.processInstanceService().save(processInstance);
    }
  }

  // ========================================================================================
  // ExecutionContext: Flow control (proceed, terminate, handleError)
  // ========================================================================================

  @Override
  public void proceed(ProcessInstance instance, BaseNode nextNode, String sourceNodeId) {
    if (instance == null) {
      log.warn("Context missing in proceed(), attempting to use passed node info if possible.");
      return;
    }

    if (instance.isHasIncident()) {
      log.warn(
          "Process instance {} has an incident. Stopping execution. Incident: {}",
          instance.getProcessInstanceId(),
          instance.getIncidentMessage());
      return;
    }

    Boolean joinResult = ParallelGatewayExecutor.handleJoinToken(instance, nextNode, sourceNodeId);

    if (joinResult == null) {
      executeNode(instance, nextNode, sourceNodeId);
    } else if (joinResult) {
      executeNode(instance, nextNode, sourceNodeId);
    } else {
      String sequenceFlowId =
          getSequenceFlowId(instance.getProcessDefinition(), sourceNodeId, nextNode.getId());
      instance.addExecutionLog(
          nextNode.getId(),
          nextNode.getName(),
          nextNode.getType(),
          sourceNodeId,
          sequenceFlowId,
          NodeState.PENDING,
          buildMetadata(instance, nextNode));
      log.debug(
          "Deferring execution of parallel gateway join {} - waiting for more tokens",
          nextNode.getId());
    }
  }

  @Override
  public void terminate(ProcessInstance instance, String scopeId) {
    if (scopeId == null || scopeId.equals(instance.getProcessDefinitionId())) {
      instance.setCompleted(true);
      instance.setState(PIState.TERMINATED);
      oweDependencies
          .eventRegisterService()
          .cleanupPendingRetryTimers(instance.getProcessInstanceId());
      log.info("Process instance {} terminated", instance.getProcessInstanceId());
    } else {
      log.info("Terminating scope {} in instance {}", scopeId, instance.getProcessInstanceId());

      Set<String> nodesToRemove = new HashSet<>();
      for (String activeNodeId : instance.getActiveNodeIds()) {
        // Handle both regular and MI-indexed active node keys
        String baseId = ProcessInstance.extractBaseNodeId(activeNodeId);
        instance
            .getProcessDefinition()
            .getNode(baseId)
            .ifPresent(
                node -> {
                  if (isChildOfScope(node, scopeId, instance.getProcessDefinition())) {
                    nodesToRemove.add(activeNodeId);
                  }
                });
      }

      nodesToRemove.forEach(id -> instance.getActiveNodeIds().remove(id));

      // Also remove MI-indexed nodes for the scope
      instance.removeAllMiActiveNodes(scopeId);

      checkAndMarkSubProcessCompleted(instance, scopeId);
    }
  }

  @Override
  public boolean handleError(ProcessInstance instance, String nodeId, String errorCode) {
    return errorPropagationChain.handleError(instance, nodeId, errorCode);
  }

  @Override
  public void sendMessageEvent(
      String messageName, String correlationId, Map<String, Object> variables) {
    handleMessageEvent(
        messageName, correlationId, Variables.builder().variables(variables).build());
  }

  @Override
  public void sendSignalEvent(String signalName, Map<String, Object> variables) {
    handleSignalEvent(signalName, Variables.builder().variables(variables).build());
  }

  @Override
  public DecisionDefinitionService getDecisionDefinitionService() {
    return oweDependencies.decisionDefinitionService();
  }

  @Override
  public DecisionInstanceService getDecisionInstanceService() {
    return oweDependencies.decisionInstanceService();
  }

  @Override
  public EventRegisterService getEventRegisterService() {
    return oweDependencies.eventRegisterService();
  }

  @Override
  public UserTaskService getUserTaskService() {
    return oweDependencies.userTaskService();
  }

  // ========================================================================================
  // WorkflowEngine: Error & Incident (delegated to ErrorPropagationChain)
  // ========================================================================================

  @Override
  public boolean handleError(
      String processInstanceId, String activityId, String errorCode, Variables variables) {
    return errorPropagationChain.handleError(processInstanceId, activityId, errorCode, variables);
  }

  @Override
  public boolean handleIncident(
      String processInstanceId, String activityId, String incidentMessage) {
    return errorPropagationChain.handleIncident(processInstanceId, activityId, incidentMessage);
  }

  // ========================================================================================
  // WorkflowEngine: Worker & Task Management
  // ========================================================================================

  @Override
  public void updateWorkerRegistry(WorkerRegistryRequest workerRegistryRequest) {
    oweDependencies.eventRegisterService().updateWorkerRegistry(workerRegistryRequest);
  }

  @Override
  public void startMissedTask(Set<WorkerRegistryRequest.WorkerInfo> workerInfos) {
    Set<String> workers =
        workerInfos.stream()
            .map(WorkerRegistryRequest.WorkerInfo::getType)
            .collect(Collectors.toSet());
    oweDependencies
        .eventRegisterService()
        .getMissedTask(workers)
        .forEach(
            task -> {
              Optional<ProcessInstance> processInstanceIfActive =
                  oweDependencies
                      .processInstanceService()
                      .getInstanceById(task.getProcessInstanceId());
              processInstanceIfActive.ifPresentOrElse(
                  processInstance -> {
                    log.info(
                        "resuming pending task for processDefinitionId: {}, workerId: {} with processDefinitionId: {}",
                        task.getProcessDefinitionId(),
                        task.getWorkerId(),
                        task.getProcessInstanceId());
                    resumeActivity(
                        processInstance.getProcessInstanceId(),
                        task.getWorkerEventRequest().getActivityId(),
                        null);
                    oweDependencies.eventRegisterService().deletePendingTask(task);
                    log.info(
                        "pending task for processDefinitionId: {}, workerId: {} with processDefinitionId: {} is deleted",
                        task.getProcessDefinitionId(),
                        task.getWorkerId(),
                        task.getProcessInstanceId());
                  },
                  () ->
                      log.info(
                          "No active processInstance found for pending task with processInstanceId: {}, workerId: {}",
                          task.getProcessInstanceId(),
                          task.getWorkerId()));
            });
  }

  @Override
  public void registerPendingTask(PendingTaskRequest data) {
    oweDependencies.eventRegisterService().registerPendingTask(data);
  }

  @Override
  public void retryActivity(RetryProcessEvent event) {
    Optional<ProcessInstance> processInstanceIfActive =
        oweDependencies
            .processInstanceService()
            .getProcessInstanceIfActive(event.getProcessInstanceId());
    if (processInstanceIfActive.isEmpty()) {
      return;
    }
    processInstanceIfActive.ifPresent(
        processInstance -> {
          if (processInstance.getIncidentSourceInstanceId() != null) {
            log.info(
                "Process instance {} has a propagated incident from child {}. Auto-redirecting retry to the actual incident source.",
                event.getProcessInstanceId(),
                processInstance.getIncidentSourceInstanceId());
            retryActualIncidentSource(processInstance);
            return;
          }

          processInstance.getProcessDefinition().getNode(event.getActivityId());
          processInstance.setHasIncident(false);
          processInstance.setIncidentMessage(null);
          processInstance.setIncidentSourceInstanceId(null);
          // if instance is from any expanded subprocess
          processInstance
              .getProcessDefinition()
              .getNode(event.getPreviousActivityId())
              .ifPresent(
                  baseNode -> {
                    if (ScopeType.SUBPROCESS.equals(baseNode.getScopeType())) {
                      processInstance.removeActiveNode(baseNode.getScopeId());
                      processInstance.addExecutionLog(
                          baseNode.getScopeId(),
                          baseNode.getName(),
                          SUB_PROCESS,
                          null,
                          null,
                          NodeState.COMPLETED,
                          null);
                    }
                  });
          processInstance
              .getActiveNodeIds()
              .remove(
                  event.getPreviousActivityId() != null
                      ? event.getPreviousActivityId()
                      : event.getActivityId());
          processInstance.addActiveNode(event.getActivityId());

          // Mark the retried activity as awaiting resolution. The resolution event is emitted
          // only once this activity actually completes (see sendIncidentResolutionIfPending),
          // not at retry-dispatch time. If the same task fails again, no resolution is sent.
          processInstance.putState(
              new ExecutionStateKey.PendingIncidentResolution(event.getActivityId()), true);

          oweDependencies.processInstanceService().save(processInstance);

          clearIncidentFromParentChain(processInstance);

          resumeActivityFromActivityId(processInstance, event.getActivityId());
        });
  }

  /**
   * Emits the incident resolution event for an activity that was retried after an incident, but
   * only once that activity has actually completed. The pending marker is set in {@link
   * #retryActivity} and cleared here. If the retried task fails again, the worker completion is
   * routed to incident handling instead of {@code resumeActivity}, so this is never reached and no
   * (false) resolution event is sent.
   */
  private void sendIncidentResolutionIfPending(ProcessInstance instance, BaseNode completedNode) {
    var key = new ExecutionStateKey.PendingIncidentResolution(completedNode.getId());
    if (!instance.containsState(key)) {
      return;
    }
    instance.removeState(key);
    IncidentEventPayload resolutionPayload =
        IncidentEventPayload.builder()
            .processInstanceId(instance.getProcessInstanceId())
            .processDefinitionId(instance.getProcessDefinitionId())
            .version(instance.getVersion())
            .activityId(completedNode.getId())
            .activityName(completedNode.getName())
            .correlationId(
                instance.getCorrelationIds() != null
                    ? String.join(",", instance.getCorrelationIds())
                    : "")
            .incidentMessage("Incident resolved - activity completed after retry")
            .build();
    oweDependencies.incidentEventHandlerAdapter().handleResolution(resolutionPayload);
  }

  /**
   * Clears propagated incident state from all parent processes up the call activity chain. Called
   * when the actual incident source child is retried.
   */
  private void clearIncidentFromParentChain(ProcessInstance childInstance) {
    ProcessInstance current = childInstance;
    while (current.getParentProcesActivity() != null) {
      String parentInstanceId = current.getParentProcesActivity().getProcessInstanceId();
      Optional<ProcessInstance> parentOpt =
          oweDependencies.processInstanceService().getInstanceById(parentInstanceId);
      if (parentOpt.isEmpty()) {
        break;
      }

      ProcessInstance parent = parentOpt.get();
      if (parent.getIncidentSourceInstanceId() == null) {
        break;
      }

      parent.setHasIncident(false);
      parent.setState(PIState.RUNNING);
      parent.setIncidentMessage(null);
      parent.setIncidentSourceInstanceId(null);
      oweDependencies.processInstanceService().save(parent);

      log.info(
          "Cleared propagated incident from parent instance {} after child retry",
          parentInstanceId);

      current = parent;
    }
  }

  /**
   * Follows the incidentSourceInstanceId chain to find the deepest actual incident source, then
   * retries that instance using its active node as the activity to retry.
   */
  private void retryActualIncidentSource(ProcessInstance parentInstance) {
    String sourceInstanceId = parentInstance.getIncidentSourceInstanceId();
    ProcessInstance current = null;
    int maxDepth = 50;
    int depth = 0;

    while (sourceInstanceId != null && depth < maxDepth) {
      Optional<ProcessInstance> sourceOpt =
          oweDependencies.processInstanceService().getInstanceById(sourceInstanceId);
      if (sourceOpt.isEmpty()) {
        log.warn(
            "Incident source instance {} not found while traversing chain from {}",
            sourceInstanceId,
            parentInstance.getProcessInstanceId());
        return;
      }
      current = sourceOpt.get();
      if (current.getIncidentSourceInstanceId() != null) {
        sourceInstanceId = current.getIncidentSourceInstanceId();
        depth++;
      } else {
        break;
      }
    }

    if (current == null) {
      log.warn(
          "Could not resolve incident source chain from instance {}",
          parentInstance.getProcessInstanceId());
      return;
    }

    Set<String> activeNodeIds = current.getActiveNodeIds();
    if (activeNodeIds == null || activeNodeIds.isEmpty()) {
      log.warn(
          "Incident source instance {} has no active nodes to retry",
          current.getProcessInstanceId());
      return;
    }

    String activityId = activeNodeIds.iterator().next();
    log.info(
        "Resolved incident source chain: parent {} -> actual source {}. Retrying activity {}",
        parentInstance.getProcessInstanceId(),
        current.getProcessInstanceId(),
        activityId);

    retryActivity(new RetryProcessEvent(current.getProcessInstanceId(), activityId, activityId));
  }

  @Override
  public void cleanupPendingRetryTimers(String processInstanceId) {
    oweDependencies.eventRegisterService().cleanupPendingRetryTimers(processInstanceId);
  }

  public boolean registerProgressiveRetryRetryIfAny(
      WorkerEventRequest eventRequest, String processInstanceId) {
    NodeState state = eventRequest.getState();
    Duration retryBackOff = eventRequest.getRetryBackOff();
    if (state.equals(NodeState.FAILED) && retryBackOff != null) {
      Optional<ProcessInstance> instanceOpt =
          oweDependencies.processInstanceService().getInstanceById(processInstanceId);
      if (instanceOpt.isPresent()) {
        ProcessInstance instance = instanceOpt.get();
        if (instance.isHasIncident()
            || List.of(PIState.CANCELLED, PIState.COMPLETED, PIState.TERMINATED, PIState.INCIDENT)
                .contains(instance.getState())) {
          log.warn(
              "Skipping progressive retry registration for activity {} in instance {} - process is in state {} (hasIncident={})",
              eventRequest.getActivityId(),
              processInstanceId,
              instance.getState(),
              instance.isHasIncident());
          return true;
        }
      }
      log.info(
          "registering the progressive retry for activity: {} in instanceId: {}, retires left: {}",
          eventRequest.getActivityId(),
          processInstanceId,
          eventRequest.getRetriesLeft());
      eventRequest.setState(NodeState.PENDING);
      eventRequest.getStateChanges().add(StateChangeUtils.buildStateChanges(NodeState.PENDING));
      eventRequest.setRetryBackOff(null);
      oweDependencies
          .eventRegisterService()
          .registerProgressiveRetry(processInstanceId, retryBackOff, eventRequest);
      return true;
    }
    return false;
  }

  // ========================================================================================
  // Internal: Node execution core
  // ========================================================================================

  private void executeNode(ProcessInstance instance, BaseNode node, String sourceNodeId) {
    if (checkIfInstanceHasIncident(instance)) {
      return;
    }

    instance.addActiveNode(node.getId());

    if (activityStateCache != null) {
      String activityCacheKey =
          instance.getProcessDefinitionId() + "_" + instance.getVersion() + "_" + node.getId();
      Optional<ActivityState> activityState = activityStateCache.get(activityCacheKey);
      if (activityState.isPresent() && !activityState.get().isEnabled()) {
        log.info(
            "Skipping disabled activity '{}' (node: {}) for processInstance {}",
            node.getName(),
            node.getId(),
            instance.getProcessInstanceId());
        ExecutionLogUtils.addExecutionLog(instance, node, sourceNodeId, NodeState.SKIPPED);
        instance.removeActiveNode(node.getId());
        processOutgoing(instance, node, node.getId());
        return;
      }
    }

    boolean isAsyncServiceTask = node.getType() == NodeType.SERVICE_TASK;
    // Async service tasks are dispatched to an external worker (or, for connector tasks, to the
    // connector service) — from the engine's point of view the lifecycle is TRIGGERED (now, on
    // dispatch) → STARTED / … / COMPLETED|INCIDENT (recorded externally and merged back when the
    // completion/resume event lands). Everything else runs in-process, so STARTED is the correct
    // entry-state for the log.
    NodeState entryState = isAsyncServiceTask ? NodeState.TRIGGERED : NodeState.STARTED;
    ExecutionLogUtils.addExecutionLog(instance, node, sourceNodeId, entryState);
    try {
      NodeExecutor executor =
          oweDependencies
              .executorRegistry()
              .getExecutor(node.getType(), node)
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "No executor registered for type: " + node.getType()));

      instance.setState(PIState.RUNNING);

      // Persist before dispatching a ServiceTask to an external worker (or connector task to
      // the connector service): the completion/resume event races with this method's outer
      // save and would otherwise read a stale snapshot where the node is not yet active.
      if (isAsyncServiceTask) {
        oweDependencies.processInstanceService().save(instance);
      }

      executor.execute(instance, node, this);

      boolean isServiceTask = node.getType() == NodeType.SERVICE_TASK;
      boolean isEndEvent = node.getType() == NodeType.END_EVENT;
      boolean isWaitState = ProcessInstanceUtils.isWaitState(node.getType());

      if (node instanceof EventNode e
          && (e.getEventType().equals(EventType.TIMER)
              || e.getEventType().equals(EventType.MESSAGE))) {
        instance.setState(PIState.HOLD);
        return;
      }

      if (isServiceTask) {
        // Async path: TRIGGERED is already logged. The worker / connector service owns
        // STARTED → COMPLETED / INCIDENT and those will be merged into the log entry when
        // the completion/resume event lands. We deliberately do NOT write PENDING here — it
        // would sit between TRIGGERED and the external STARTED and break the canonical order.
        log.info(
            "Service Task {} dispatched for processID '{}', waiting for completion event",
            node.getName(),
            instance.getProcessDefinitionId());
      } else if (!isWaitState && !isEndEvent) {
        instance.removeActiveNode(node.getId());
        instance.addExecutionLog(
            node.getId(),
            node.getName(),
            node.getType(),
            sourceNodeId,
            getSequenceFlowId(instance.getProcessDefinition(), null, node.getId()),
            NodeState.COMPLETED,
            buildMetadata(instance, node));
        checkAndMarkCompleted(instance);
      } else if (isEndEvent) {
        instance.removeActiveNode(node.getId());

        if (node instanceof EventNode eventNode && eventNode.getEventType() == EventType.ERROR) {
          // Error end events are fully handled by ErrorPropagationChain
          // (invoked from EndEventExecutor). It already cleans up the subprocess scope,
          // triggers the boundary event flow, and saves the instance.
          // We must NOT fall through to checkAndMarkSubProcessCompleted, which would
          // incorrectly treat the (now-empty) subprocess as normally completed and
          // follow its outgoing sequence flow.
          return;
        }

        ExecutionLogUtils.addExecutionLog(instance, node, sourceNodeId, NodeState.COMPLETED);

        if (node.getScopeType() == ScopeType.SUBPROCESS) {
          checkAndMarkSubProcessCompleted(instance, node.getScopeId());
        }

        checkAndMarkCompleted(instance);
      } else {
        ExecutionLogUtils.addExecutionLog(instance, node, sourceNodeId, NodeState.PENDING);
      }

    } catch (Exception e) {
      log.error("Error executing node {}", node.getId(), e);

      instance.setHasIncident(true);
      instance.setIncidentMessage("Engine failure during node execution: " + e.getMessage());
      instance.setState(PIState.INCIDENT);

      instance.removeActiveNode(node.getId());
      ExecutionLogUtils.addExecutionLog(instance, node, sourceNodeId, NodeState.FAILED);

      oweDependencies.processInstanceService().save(instance);
      errorPropagationChain.propagateIncidentToParents(
          instance,
          instance.getProcessInstanceId(),
          "Engine failure during node execution: " + e.getMessage());

      throw e;
    }
  }

  private void processOutgoing(
      ProcessInstance instance, BaseNode executedNode, String sourceNodeId) {
    if (instance == null) {
      log.warn("Context missing in processOutgoing()");
      return;
    }

    if (checkIfInstanceHasIncident(instance)) {
      return;
    }

    log.info("inside scope: {}", executedNode.getScopeId());

    List<BaseNode> outgoingNodes =
        instance.getProcessDefinition().getOutgoingNodes(executedNode.getId()).stream()
            .filter(Objects::nonNull)
            .toList();

    for (BaseNode outgoing : outgoingNodes) {
      Boolean joinResult =
          ParallelGatewayExecutor.handleJoinToken(instance, outgoing, sourceNodeId);

      if (joinResult == null) {
        executeNode(instance, outgoing, sourceNodeId);
      } else if (joinResult) {
        executeNode(instance, outgoing, sourceNodeId);
      } else {
        String sequenceFlowId =
            getSequenceFlowId(instance.getProcessDefinition(), sourceNodeId, outgoing.getId());
        instance.addExecutionLog(
            outgoing.getId(),
            outgoing.getName(),
            outgoing.getType(),
            sourceNodeId,
            sequenceFlowId,
            NodeState.PENDING,
            buildMetadata(instance, outgoing));
        log.debug(
            "Deferring execution of parallel gateway join {} - waiting for more tokens",
            outgoing.getId());
      }
    }
  }

  // ========================================================================================
  // Internal: Completion & SubProcess handling
  // ========================================================================================

  private void checkAndMarkSubProcessCompleted(ProcessInstance instance, String subProcessId) {
    Optional<BaseNode> optionalSubProcessNode =
        instance.getProcessDefinition().getNode(subProcessId);

    // Check if this is a multi-instance subprocess
    if (optionalSubProcessNode.isPresent() && isMultiInstance(optionalSubProcessNode.get())) {
      handleMultiInstanceSubProcessCompletion(
          instance, (SubProcessNode) optionalSubProcessNode.get());
      return;
    }

    // Standard subprocess completion: check if any active children remain
    boolean hasActiveChildren = hasActiveChildrenInScope(instance, subProcessId);

    if (!hasActiveChildren) {
      log.info("SubProcess {} completed", subProcessId);
      instance.removeActiveNode(subProcessId);
      if (optionalSubProcessNode.isPresent()) {
        BaseNode subProcessNode = optionalSubProcessNode.get();
        instance.addExecutionLog(
            subProcessId,
            subProcessNode.getName(),
            subProcessNode.getType(),
            null,
            null,
            NodeState.COMPLETED,
            buildMetadata(instance, subProcessNode));

        processOutgoing(instance, subProcessNode, subProcessId);

        if (subProcessNode.getScopeType() == ScopeType.SUBPROCESS) {
          checkAndMarkSubProcessCompleted(instance, subProcessNode.getScopeId());
        }
      }
    }
  }

  /**
   * Checks if a subprocess scope has any active children. Handles both regular active node IDs and
   * MI-indexed active node IDs.
   */
  private boolean hasActiveChildrenInScope(ProcessInstance instance, String subProcessId) {
    return instance.getActiveNodeIds().stream()
        .anyMatch(
            id -> {
              // Extract base node ID (handles both regular and MI-indexed keys)
              String baseId = ProcessInstance.extractBaseNodeId(id);
              Optional<BaseNode> nodeOpt = instance.getProcessDefinition().getNode(baseId);
              return nodeOpt.filter(n -> subProcessId.equals(n.getScopeId())).isPresent();
            });
  }

  /**
   * Handles completion of a single instance within a multi-instance subprocess. Called when an end
   * event inside the MI subprocess is reached.
   */
  private void handleMultiInstanceSubProcessCompletion(
      ProcessInstance instance, SubProcessNode subProcess) {
    String subProcessId = subProcess.getId();
    MultiInstanceLoopCharacteristics mi = subProcess.getMultiInstanceLoopCharacteristics();

    // Determine which instance just completed (from the current MI context)
    int completedInstanceIndex = instance.getCurrentMiInstanceIndex();
    log.info(
        "MI SubProcess {} instance {} reached end event", subProcessId, completedInstanceIndex);

    // Apply subprocess output mappings to capture instance output
    IODataMappingsUtils.setDataMappings(subProcess.getOutputMappings(), instance.getVariables());

    // Collect output for this instance
    collectMultiInstanceOutput(instance, subProcess, mi, completedInstanceIndex);

    // Clear the loop counter for the completed instance
    instance.removeState(loopCounterKey(subProcess, completedInstanceIndex));

    // Increment completed count
    var completedKey = new ExecutionStateKey.MultiInstanceSubProcessCompleted(subProcessId);
    int completedCount = instance.getState(completedKey, 0) + 1;
    instance.putState(completedKey, completedCount);

    int totalSize = instance.getState(loopSizeKey(subProcess), 0);
    log.info(
        "MI SubProcess {} completion: {} of {} instances done",
        subProcessId,
        completedCount,
        totalSize);

    // Check completion condition
    boolean allDone = completedCount >= totalSize;
    boolean completionConditionMet = false;
    if (mi.getCompletionCondition() != null && !mi.getCompletionCondition().isBlank()) {
      completionConditionMet =
          MultiInstanceUtils.evaluateCompletionCondition(
              mi.getCompletionCondition(), instance.getVariables());
      if (completionConditionMet) {
        log.info(
            "MI SubProcess {} completion condition met after {} instances",
            subProcessId,
            completedCount);
      } else {
        log.info(
            "MI SubProcess {} completion condition not met after {} instances",
            subProcessId,
            completedCount);
      }
    }

    if (allDone || completionConditionMet) {
      finalizeMultiInstanceSubProcess(instance, subProcess, mi);
    } else if (mi.isSequential()) {
      // Sequential: start the next instance
      int nextIndex = completedInstanceIndex + 1;
      if (nextIndex < totalSize) {
        log.info("MI SubProcess {} starting next sequential instance {}", subProcessId, nextIndex);
        List<Object> inputCollection =
            MultiInstanceUtils.evaluateInputCollection(mi, instance.getVariables());
        startNextSequentialInstance(instance, subProcess, mi, inputCollection, nextIndex);
      }
    }
    // For parallel: do nothing - other instances will complete independently
  }

  /** Collects output from a completed MI subprocess instance into the output collection. */
  @SuppressWarnings("unchecked")
  private void collectMultiInstanceOutput(
      ProcessInstance instance,
      SubProcessNode subProcess,
      MultiInstanceLoopCharacteristics mi,
      int instanceIndex) {
    if (mi.getOutputElement() == null || mi.getOutputCollection() == null) {
      return;
    }

    // Evaluate the output element expression
    Object output =
        VariablesUtils.getEvaluatedVariable(mi.getOutputElement(), instance.getVariables());

    // Update the output collection at the instance index
    var outputKey = new ExecutionStateKey.MultiInstanceSubProcessOutput(subProcess.getId());
    List<Object> outputCollection = instance.getState(outputKey);
    if (outputCollection != null && instanceIndex < outputCollection.size()) {
      outputCollection.set(instanceIndex, output);
      instance.putState(outputKey, outputCollection);

      // Also update the output collection variable so completion conditions can reference it
      if (mi.getOutputCollection() != null) {
        instance.getVariables().put(mi.getOutputCollection(), outputCollection);
      }
    }

    log.debug(
        "MI SubProcess {} instance {} output collected: {}",
        subProcess.getId(),
        instanceIndex,
        output);
  }

  /**
   * Finalizes a multi-instance subprocess when all instances are complete or completion condition
   * met.
   */
  private void finalizeMultiInstanceSubProcess(
      ProcessInstance instance, SubProcessNode subProcess, MultiInstanceLoopCharacteristics mi) {
    String subProcessId = subProcess.getId();
    log.info("MI SubProcess {} all instances completed. Finalizing.", subProcessId);

    // Apply final output collection to process variables
    var outputKey = new ExecutionStateKey.MultiInstanceSubProcessOutput(subProcessId);
    List<Object> outputs = instance.getState(outputKey);
    if (mi.getOutputCollection() != null && outputs != null) {
      instance.getVariables().put(mi.getOutputCollection(), outputs);
    }

    // Clean up all MI state (including MI-indexed active nodes)
    MultiInstanceUtils.cleanupMultiInstanceSubProcess(instance, subProcess);

    // Clear MI context BEFORE removing the subprocess node itself from active.
    // The subprocess was added as a regular (non-MI-indexed) active node,
    // so we need removeActiveNode to use the base ID, not an MI-indexed key.
    instance.clearMultiInstanceContext();

    // Remove the subprocess itself from active
    instance.removeActiveNode(subProcessId);

    instance.addExecutionLog(
        subProcessId,
        subProcess.getName(),
        subProcess.getType(),
        null,
        null,
        NodeState.COMPLETED,
        buildMetadata(instance, subProcess));

    // Process outgoing from the subprocess (outgoing nodes are outside the MI subprocess)
    processOutgoing(instance, subProcess, subProcessId);

    // Check if the subprocess is nested inside another subprocess
    if (subProcess.getScopeType() == ScopeType.SUBPROCESS) {
      checkAndMarkSubProcessCompleted(instance, subProcess.getScopeId());
    }
  }

  /** Starts the next sequential instance of an MI subprocess. */
  private void startNextSequentialInstance(
      ProcessInstance instance,
      SubProcessNode subProcess,
      MultiInstanceLoopCharacteristics mi,
      List<Object> inputCollection,
      int instanceIndex) {
    // Store loop counter
    instance.putState(loopCounterKey(subProcess, instanceIndex), instanceIndex);

    // Set element variable
    instance.getVariables().put("loopCounter", instanceIndex);
    if (mi.getElementVariable() != null) {
      instance.getVariables().put(mi.getElementVariable(), inputCollection.get(instanceIndex));
    }

    // Apply subprocess input mappings
    IODataMappingsUtils.setDataMappings(subProcess, instance.getVariables());

    // Set MI context and execute
    instance.setMultiInstanceContext(subProcess.getId(), instanceIndex);
    try {
      ProcessDefinition definition = instance.getProcessDefinition();
      BaseNode startNode = definition.getNodes().get(subProcess.getStartNodeId());
      if (startNode != null) {
        proceed(instance, startNode, subProcess.getId());
      }
    } finally {
      instance.clearMultiInstanceContext();
    }

    // Clean up temp variables
    instance.getVariables().remove("loopCounter");
    if (mi.getElementVariable() != null) {
      instance.getVariables().remove(mi.getElementVariable());
    }
  }

  private void checkAndMarkCompleted(ProcessInstance instance) {
    if (instance.isCompleted()) {
      return;
    }

    if (instance.getActiveNodeIds().isEmpty()
        && instance.getExecutionHistory().values().stream()
            .anyMatch(l -> l.getNodeType() == NodeType.END_EVENT)) {
      instance.setCompleted(true);
      instance.setState(PIState.COMPLETED);
      instance.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
      log.info("Process instance {} completed", instance.getProcessInstanceId());

      if (instance.getParentProcesActivity() != null) {
        // Persist COMPLETED state before resuming parent so that the parent's
        // active-children query sees this child as finished.
        oweDependencies.processInstanceService().save(instance);
        resumeParentProcess(instance);
      }
    }
  }

  private void resumeParentProcess(ProcessInstance childInstance) {
    ParentProcessActivity parentActivity = childInstance.getParentProcesActivity();
    String parentProcessInstanceId = parentActivity.getProcessInstanceId();
    BaseNode callActivityNode = parentActivity.getLinkedNode();

    Map<String, Object> activityOutputVariables =
        VariablesUtils.getActivityOutputVariables(
            (CallActivityNode) callActivityNode, childInstance);

    boolean isMultiInstanceCallActivity = isMultiInstance(callActivityNode);
    if (isMultiInstanceCallActivity) {
      log.info("inside multi instance block");
      // Query child instances directly to avoid race conditions where concurrent completions
      // overwrite each other's counter removals in the parent's execution state.
      boolean hasActiveChildren =
          oweDependencies.processInstanceService().hasActiveChildInstances(parentProcessInstanceId);
      if (hasActiveChildren) {
        log.info(
            "Multi-instance call activity {} still has active children, waiting",
            callActivityNode.getId());
        return;
      }
    }
    ProcessInstance parentInstance =
        oweDependencies
            .processInstanceService()
            .getInstanceById(parentProcessInstanceId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Parent process instance not found: " + parentProcessInstanceId));

    log.info(
        "Resuming parent process {} from Call Activity {}",
        parentProcessInstanceId,
        callActivityNode.getId());
    if (parentInstance.getProcessDefinition() == null) {
      ProcessDefinition parentDef =
          oweDependencies
              .processDefinitionService()
              .getLatestProcessDefinition(parentInstance.getProcessDefinitionId())
              .orElseThrow(() -> new IllegalStateException("Parent process definition not found"));
      parentInstance.setProcessDefinition(parentDef);
    }

    // Check if call activity is inside a multi-instance subprocess
    Optional<String> miActiveEntry = parentInstance.findMiActiveNode(callActivityNode.getId());
    boolean isInsideMiSubProcess = miActiveEntry.isPresent();

    if (isInsideMiSubProcess) {
      // Set MI context from the active node entry so add/removeActiveNode use MI-indexed keys
      int miIndex = ProcessInstance.extractMiInstanceIndex(miActiveEntry.get());
      String miSubProcessId = ProcessInstance.extractMiSubProcessId(miActiveEntry.get());
      parentInstance.setMultiInstanceContext(miSubProcessId, miIndex);
      log.info(
          "Call Activity {} is inside MI SubProcess {} instance {}",
          callActivityNode.getId(),
          miSubProcessId,
          miIndex);
    } else if (!parentInstance.getActiveNodeIds().contains(callActivityNode.getId())) {
      log.warn(
          "Call Activity {} is not active in parent instance {}. Likely already completed.",
          callActivityNode.getId(),
          parentInstance.getProcessInstanceId());
      return;
    }

    try {
      parentInstance.removeActiveNode(callActivityNode.getId());

      parentInstance.getVariables().putAll(activityOutputVariables);

      String sequenceFlowId =
          getSequenceFlowId(parentInstance.getProcessDefinition(), null, callActivityNode.getId());
      parentInstance.addExecutionLog(
          callActivityNode.getId(),
          callActivityNode.getName(),
          callActivityNode.getType(),
          null,
          sequenceFlowId,
          NodeState.COMPLETED,
          buildMetadata(parentInstance, callActivityNode));

      parentInstance.removeState(new ExecutionStateKey.CallActivityChild(callActivityNode.getId()));

      processOutgoing(parentInstance, callActivityNode, callActivityNode.getId());
      checkAndMarkCompleted(parentInstance);
    } catch (Exception e) {
      log.error(
          "Error resuming parent process {} from Call Activity {}",
          parentProcessInstanceId,
          callActivityNode.getId(),
          e);
    } finally {
      parentInstance.clearMultiInstanceContext();
      oweDependencies.processInstanceService().save(parentInstance);
    }
  }
}
