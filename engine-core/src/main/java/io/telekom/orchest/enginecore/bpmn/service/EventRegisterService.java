package io.telekom.orchest.enginecore.bpmn.service;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.adapters.data.model.*;
import io.telekom.orchest.api.core.adapters.data.repository.PendingTaskRepository;
import io.telekom.orchest.api.core.adapters.data.repository.WorkerRegistryRepository;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.EventNode;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.request.PendingTaskRequest;
import io.telekom.orchest.api.core.request.WorkerRegistryRequest;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Coordinator for event registration across all event types. Delegates event-specific operations to
 * {@link TimerEventRegistrar}, {@link MessageEventRegistrar}, and {@link SignalEventRegistrar}.
 * Directly manages worker registry and pending task operations.
 */
@Slf4j
@RequiredArgsConstructor
public class EventRegisterService {

  private final TimerEventRegistrar timerEventRegistrar;
  private final MessageEventRegistrar messageEventRegistrar;
  private final SignalEventRegistrar signalEventRegistrar;
  private final WorkerRegistryRepository workerRegistryRepository;
  private final PendingTaskRepository pendingTaskRepository;

  // ========================================================================================
  // Timer event delegation
  // ========================================================================================

  public void registerCycledTimedEvent(TimedEvent timedEvent) {
    timerEventRegistrar.registerCycledTimedEvent(timedEvent);
  }

  public TimedEvent registerTimerEvent(
      ProcessInstance processInstance, String duration, BaseNode node) {
    return timerEventRegistrar.registerTimerEvent(processInstance, duration, node);
  }

  public TimedEvent registerStartTimerEvent(
      String duration, BaseNode node, String definitionId, Integer version) {
    return timerEventRegistrar.registerStartTimerEvent(duration, node, definitionId, version);
  }

  public TimedEvent registerTimerEventForGateway(
      ProcessInstance processInstance, String duration, BaseNode node, String linkedEventId) {
    return timerEventRegistrar.registerTimerEventForGateway(
        processInstance, duration, node, linkedEventId);
  }

  public void clearEvent(TimedEvent timedEvent) {
    timerEventRegistrar.clearEvent(timedEvent);
  }

  public void registerProgressiveRetry(
      String processInstanceId, Duration retryBackOff, WorkerEventRequest workerEventRequest) {
    timerEventRegistrar.registerProgressiveRetry(
        processInstanceId, retryBackOff, workerEventRequest);
  }

  public void cleanupPendingRetryTimers(String processInstanceId) {
    timerEventRegistrar.cleanupPendingRetryTimers(processInstanceId);
  }

  public void cleanupAllEventsForProcessInstance(String processInstanceId) {
    timerEventRegistrar.cleanupPendingRetryTimers(processInstanceId);
    messageEventRegistrar.deleteByProcessInstanceId(processInstanceId);
    signalEventRegistrar.deleteByProcessInstanceId(processInstanceId);
    pendingTaskRepository.deleteByProcessInstanceId(processInstanceId);
  }

  // ========================================================================================
  // Message event delegation
  // ========================================================================================

  public void registerStartMessageEvent(
      String messageName, BaseNode messageNode, String processDefinitionId) {
    messageEventRegistrar.registerStartMessageEvent(messageName, messageNode, processDefinitionId);
  }

  public MessageEventStore registerMessageEvent(
      String messageName, String correlationValue, String processInstanceId, BaseNode messageNode) {
    return messageEventRegistrar.registerMessageEvent(
        messageName, correlationValue, processInstanceId, messageNode);
  }

  public MessageEventStore registerMessageEvent(
      String messageName,
      String correlationValue,
      String processInstanceId,
      BaseNode messageNode,
      String processDefinitionId,
      boolean isStartEvent) {
    return messageEventRegistrar.registerMessageEvent(
        messageName,
        correlationValue,
        processInstanceId,
        messageNode,
        processDefinitionId,
        isStartEvent);
  }

  public MessageEventStore registerMessageEventForGateway(
      String messageName,
      String correlationValue,
      String processInstanceId,
      BaseNode messageNode,
      String linkedEventId) {
    return messageEventRegistrar.registerMessageEventForGateway(
        messageName, correlationValue, processInstanceId, messageNode, linkedEventId);
  }

  // ========================================================================================
  // Signal event delegation
  // ========================================================================================

  public SignalEvent registerStartSignalEvent(
      String signalName, BaseNode signalNode, String processDefinitionId) {
    return signalEventRegistrar.registerStartSignalEvent(
        signalName, signalNode, processDefinitionId);
  }

  public SignalEvent registerSignalEvent(
      String signalName,
      String processInstanceId,
      BaseNode signalNode,
      String processDefinitionId,
      boolean isStartEvent) {
    return signalEventRegistrar.registerSignalEvent(
        signalName, processInstanceId, signalNode, processDefinitionId, isStartEvent);
  }

  public SignalEvent registerSignalEventForGateway(
      String signalName, String processInstanceId, BaseNode signalNode, String linkedEventId) {
    return signalEventRegistrar.registerSignalEventForGateway(
        signalName, processInstanceId, signalNode, linkedEventId);
  }

  // ========================================================================================
  // Cross-cutting: linked event cancellation (Event-Based Gateway)
  // ========================================================================================

  public void cancelLinkedEvents(String linkedEventId) {
    log.info("Cancelling all linked event subscriptions for linkedEventId: {}", linkedEventId);
    timerEventRegistrar.deleteByLinkedEventId(linkedEventId);
    messageEventRegistrar.deleteByLinkedEventId(linkedEventId);
    signalEventRegistrar.deleteByLinkedEventId(linkedEventId);
  }

  // ========================================================================================
  // Cross-cutting: start event orchestration
  // ========================================================================================

  public void handleStartEventIfAny(ProcessDefinition process) {
    if (!process.getIsExecutable()) {
      log.info(
          "skipping start event registration for non-executable processDefinitionId: {} (version: {})",
          process.getDefinitionId(),
          process.getVersion());
      clearStartEventIfPresent(process.getDefinitionId(), true, true, true);
      return;
    }
    Optional<BaseNode> processStart = process.getNode(process.getStartNodeId());
    if (processStart.isPresent()) {
      String definitionId = process.getDefinitionId();
      BaseNode startNode = processStart.get();

      if (startNode instanceof EventNode node) {
        switch (node.getEventType()) {
          case MESSAGE -> {
            String messageName = node.getMessageName();
            messageEventRegistrar.registerStartMessageEvent(messageName, startNode, definitionId);
            clearStartEventIfPresent(definitionId, true, false, true);
            log.info(
                "registered message start event for processDefinitionId: {}, messageName: {}",
                definitionId,
                messageName);
          }
          case SIGNAL -> {
            String signalName = node.getSignalRef();
            signalEventRegistrar.registerStartSignalEvent(signalName, startNode, definitionId);
            clearStartEventIfPresent(definitionId, true, true, false);
            log.info(
                "registered signal start event for processDefinitionId: {}, signalName: {}",
                definitionId,
                signalName);
          }
          case TIMER -> {
            String timerDuration = null;
            if (node.getTimerDuration() != null) {
              timerDuration = node.getTimerDuration();
            } else if (node.getTimerCycle() != null) {
              timerDuration = node.getTimerCycle();
            } else if (node.getTimerDate() != null) {
              timerDuration = node.getTimerDate();
            }
            timerEventRegistrar.registerStartTimerEvent(
                timerDuration, startNode, definitionId, process.getVersion());
            clearStartEventIfPresent(definitionId, false, true, true);
            log.info(
                "registered timer start event for processDefinitionId: {}, timerDuration: {}",
                definitionId,
                timerDuration);
          }
          case NONE -> {
            clearStartEventIfPresent(definitionId, true, true, true);
          }
          default -> log.info("Unsupported StartEvent: {}", node.getEventType());
        }
      }
    }
  }

  private void clearStartEventIfPresent(
      String definitionId,
      boolean deleteTimedEvent,
      boolean deleteMessageEvent,
      boolean deleteSignalEvent) {
    if (deleteTimedEvent) {
      timerEventRegistrar.deleteStartEventByDefinitionId(definitionId);
    }
    if (deleteMessageEvent) {
      messageEventRegistrar.deleteStartEventByDefinitionId(definitionId);
    }
    if (deleteSignalEvent) {
      signalEventRegistrar.deleteStartEventByDefinitionId(definitionId);
    }
  }

  // ========================================================================================
  // Worker registry (not event-type-specific)
  // ========================================================================================

  public void updateWorkerRegistry(WorkerRegistryRequest workerRegistryRequest) {
    Optional<WorkerRegistry> workerRegistryWithNameSpace =
        workerRegistryRepository.findByNameSpace(workerRegistryRequest.getNamespace());

    WorkerRegistry workerRegistry;
    if (workerRegistryWithNameSpace.isPresent()) {
      workerRegistry = workerRegistryWithNameSpace.get();
      workerRegistry.setWorkerInfos(workerRegistryRequest.getWorkers());
    } else {
      workerRegistry =
          WorkerRegistry.builder()
              .nameSpace(workerRegistryRequest.getNamespace())
              .workerInfos(workerRegistryRequest.getWorkers())
              .build();
    }
    workerRegistryRepository.save(workerRegistry);
  }

  // ========================================================================================
  // Pending task management (not event-type-specific)
  // ========================================================================================

  public Stream<PendingTask> getMissedTask(Set<String> workerIds) {
    return pendingTaskRepository.findAllByWorkerIdIn(workerIds);
  }

  public void deletePendingTask(PendingTask task) {
    pendingTaskRepository.remove(task);
  }

  public void registerPendingTask(PendingTaskRequest data) {
    WorkerEventRequest workerEvent = data.getWorkerEvent();
    // setting this null is important
    // as process variables might get updated during a hold period, so on resume variables will be
    // picked from ProcessInstance
    workerEvent.setVariables(null);

    PendingTask pendingTask =
        PendingTask.builder()
            .processDefinitionId(data.getProcessDefinitionId())
            .processInstanceId(workerEvent.getProcessInstanceId())
            .workerId(((ServiceTaskNode) workerEvent.getNodeInformation()).getWorkerType())
            .workerEventRequest(workerEvent)
            .build();
    // TODO: set metrics to alert teams for pending task
    pendingTaskRepository.save(pendingTask);
  }
}
