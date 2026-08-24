package io.telekom.orchest.enginecore.bpmn.service;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.adapters.data.model.TimedEvent;
import io.telekom.orchest.api.core.adapters.data.repository.TimedEventRepository;
import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.utils.IsoDateTimeParser;
import io.telekom.orchest.api.core.utils.StateChangeUtils;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles all timer-related event registration: intermediate timer events, start timer events,
 * gateway-linked timer events, progressive retry timers, and cycled timer re-registration.
 */
@Slf4j
@RequiredArgsConstructor
public class TimerEventRegistrar {

  private final TimedEventRepository timedEventRepository;

  /**
   * Re-registers a cycled timer event with a new trigger time.
   *
   * @param timedEvent the timer event to re-register
   */
  public void registerCycledTimedEvent(TimedEvent timedEvent) {
    timedEvent.setTriggerAt(getTriggerAt(timedEvent.getValue()));
    timedEventRepository.registerEvent(timedEvent);
  }

  /**
   * Registers an intermediate timer event for a process instance.
   *
   * @param processInstance the process instance
   * @param duration the ISO-8601 duration or date expression
   * @param node the timer catch event node
   * @return the persisted timed event
   */
  public TimedEvent registerTimerEvent(
      ProcessInstance processInstance, String duration, BaseNode node) {
    TimedEvent timedEvent =
        TimedEvent.builder()
            .processInstanceId(processInstance.getProcessInstanceId())
            .state(IntermediateEventState.REGISTERED)
            .type(TimedEvent.Type.TIMER)
            .value(duration)
            .triggerAt(getTriggerAt(duration))
            .nodeInformation(node)
            .stateChanges(List.of(StateChangeUtils.buildStateChanges(NodeState.REGISTERED)))
            .build();
    return timedEventRepository.registerEvent(timedEvent);
  }

  /**
   * Registers or updates a start timer event for a process definition.
   *
   * @param duration the ISO-8601 duration/date/cycle expression
   * @param node the timer start event node
   * @param definitionId the process definition ID
   * @param version the process definition version
   * @return the persisted timed event
   */
  public TimedEvent registerStartTimerEvent(
      String duration, BaseNode node, String definitionId, Integer version) {
    Optional<TimedEvent> startTimedEvent = timedEventRepository.findStartTimedEvent(definitionId);

    TimedEvent timedEvent;
    if (startTimedEvent.isPresent()) {
      timedEvent = startTimedEvent.get();
      timedEvent.setTriggerAt(getTriggerAt(duration));
      timedEvent.setValue(duration);
    } else {
      timedEvent =
          TimedEvent.builder()
              .processDefinitionId(definitionId)
              .version(version)
              .state(IntermediateEventState.REGISTERED)
              .type(TimedEvent.Type.START_EVENT_TIMER)
              .isStartEvent(true)
              .value(duration)
              .triggerAt(getTriggerAt(duration))
              .nodeInformation(node)
              .stateChanges(List.of(StateChangeUtils.buildStateChanges(NodeState.REGISTERED)))
              .build();
    }
    return timedEventRepository.registerEvent(timedEvent);
  }

  /**
   * Registers a timer event linked to an Event-Based Gateway for exclusive race semantics.
   *
   * @param processInstance the process instance
   * @param duration the ISO-8601 duration expression
   * @param node the timer catch event node
   * @param linkedEventId the shared linked event ID for gateway cleanup
   * @return the persisted timed event
   */
  public TimedEvent registerTimerEventForGateway(
      ProcessInstance processInstance, String duration, BaseNode node, String linkedEventId) {
    TimedEvent timedEvent =
        TimedEvent.builder()
            .processInstanceId(processInstance.getProcessInstanceId())
            .state(IntermediateEventState.REGISTERED)
            .type(TimedEvent.Type.TIMER)
            .value(duration)
            .triggerAt(getTriggerAt(duration))
            .nodeInformation(node)
            .isLinkedEvent(true)
            .linkedEventId(linkedEventId)
            .stateChanges(List.of(StateChangeUtils.buildStateChanges(NodeState.REGISTERED)))
            .build();
    return timedEventRepository.registerEvent(timedEvent);
  }

  /**
   * Removes a timed event from the repository.
   *
   * @param timedEvent the event to remove
   */
  public void clearEvent(TimedEvent timedEvent) {
    timedEventRepository.remove(timedEvent);
  }

  /**
   * Registers a progressive retry timer that re-dispatches a failed service task after a backoff.
   *
   * @param processInstanceId the process instance ID
   * @param retryBackOff the duration to wait before retrying
   * @param workerEventRequest the original worker event to re-dispatch
   */
  public void registerProgressiveRetry(
      String processInstanceId, Duration retryBackOff, WorkerEventRequest workerEventRequest) {
    workerEventRequest.setState(NodeState.PENDING);
    workerEventRequest.getStateChanges().add(StateChangeUtils.buildStateChanges(NodeState.PENDING));
    workerEventRequest.setRetryBackOff(null);
    Optional<TimedEvent> duplicateEvent =
        timedEventRepository.isDuplicateEvent(
            processInstanceId, workerEventRequest.getActivityId());
    if (duplicateEvent.isPresent()) {
      log.warn(
          "received duplicate task retry event for processInstanceId: {}, activityId: {}",
          processInstanceId,
          workerEventRequest.getActivityId());
      return;
    }
    timedEventRepository.registerEvent(
        TimedEvent.builder()
            .processInstanceId(processInstanceId)
            .state(IntermediateEventState.REGISTERED)
            .type(TimedEvent.Type.TASK_RETRY)
            .value(retryBackOff.toString())
            .triggerAt(LocalDateTime.now().plus(retryBackOff))
            .stateChanges(List.of(StateChangeUtils.buildStateChanges(NodeState.REGISTERED)))
            .eventRequest(workerEventRequest)
            .build());
  }

  /**
   * Removes all pending retry timers for a process instance.
   *
   * @param processInstanceId the process instance ID
   */
  public void cleanupPendingRetryTimers(String processInstanceId) {
    log.info("Cleaning up pending retry timers for instance {}", processInstanceId);
    timedEventRepository.deletePendingRetryTimersByProcessInstanceId(processInstanceId);
  }

  public void deleteStartEventByDefinitionId(String definitionId) {
    timedEventRepository.deleteStartEventByDefinitionId(definitionId);
  }

  public void deleteByLinkedEventId(String linkedEventId) {
    timedEventRepository.deleteByLinkedEventId(linkedEventId);
  }

  private LocalDateTime getTriggerAt(String timerExpression) {
    return IsoDateTimeParser.parseToLocalDateTime(timerExpression);
  }
}
