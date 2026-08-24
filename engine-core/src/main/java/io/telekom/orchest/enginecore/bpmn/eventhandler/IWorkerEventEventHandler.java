package io.telekom.orchest.enginecore.bpmn.eventhandler;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.ExecutionLogEntry;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.request.StateChange;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.service.ProcessInstanceService;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event handler that processes worker completion/failure events and resumes or errors the engine.
 */
@Slf4j
@RequiredArgsConstructor
public class IWorkerEventEventHandler implements EventHandler<WorkerEventRequest, Void> {

  private final OrchestWorkflowEngine orchestWorkflowEngine;
  private final OrchestEngineTelemetryService telemetryService;

  @Override
  public Void execute(WorkerEventRequest event) {
    String processInstanceId = event.getProcessInstanceId();
    String activityId = event.getActivityId();
    if (activityId == null && event.getNodeInformation() != null) {
      activityId = event.getNodeInformation().getId();
    }

    if (activityId == null) {
      log.error("Received worker event without activityId for instance {}", processInstanceId);
      telemetryService.incrementReceivedWorkerCounter(
          "invalid", event.getType(), event.getProcessDefinitionId(), event.getVersion());
      return null;
    }

    boolean isRetryEvent =
        orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, processInstanceId);
    if (isRetryEvent) {
      telemetryService.incrementReceivedWorkerCounter(
          "retry", event.getType(), event.getProcessDefinitionId(), event.getVersion());
      return null;
      // will do the progressiveRetry
    }
    mergeWorkerStateHistory(event, processInstanceId, activityId);
    dispatch(event, processInstanceId, activityId);
    telemetryService.incrementReceivedWorkerCounter(
        "success", event.getType(), event.getProcessDefinitionId(), event.getVersion());
    return null;
  }

  /**
   * Merges the worker's reported state-change history (STARTED → … → COMPLETED|FAILED|INCIDENT)
   * onto the activity's {@link ExecutionLogEntry}, so the persisted timeline is {@code [TRIGGERED,
   * STARTED, …, COMPLETED|INCIDENT]} — i.e. the full engine↔worker lifecycle. Engine-side TRIGGERED
   * was written at dispatch time; the terminal COMPLETED/INCIDENT that {@link #dispatch} triggers
   * later is collapsed by {@link ExecutionLogEntry#addStateChange} when it duplicates the last
   * state.
   */
  private void mergeWorkerStateHistory(
      WorkerEventRequest event, String processInstanceId, String activityId) {
    List<StateChange> workerHistory = event.getStateChanges();
    if (workerHistory == null || workerHistory.isEmpty()) {
      return;
    }
    ProcessInstanceService service =
        orchestWorkflowEngine.getOweDependencies().processInstanceService();
    Optional<ProcessInstance> instanceOpt = service.getInstanceById(processInstanceId);
    if (instanceOpt.isEmpty()) {
      return; // dispatch() will surface the not-found case
    }
    ProcessInstance instance = instanceOpt.get();
    ExecutionLogEntry logEntry = instance.getExecutionHistory().get(activityId);
    if (logEntry == null) {
      // Nothing to merge onto — dispatch() will create/update the entry via the normal flow.
      return;
    }
    // Only append transitions the engine hasn't already seen. TRIGGERED is engine-owned and
    // stays at the head of the timeline; skip it from the worker's echo if present.
    // Timestamp-strict idempotence: appends only happen for worker states produced strictly
    // AFTER the last already-logged state. This makes the merge safe against Kafka
    // redelivery / engine-side StaleState retries, where the same worker event may be
    // replayed and the worker's history list is otherwise appended multiple times.
    OffsetDateTime latestLogged = latestTimestamp(logEntry.getStateChanges());
    int sizeBefore = logEntry.getStateChanges().size();
    for (StateChange change : workerHistory) {
      if (change == null || change.getState() == null || change.getState() == NodeState.TRIGGERED) {
        continue;
      }
      OffsetDateTime ts = change.getTimestamp();
      if (ts != null && latestLogged != null && !ts.isAfter(latestLogged)) {
        continue; // already merged in a prior delivery
      }
      logEntry.addStateChange(change);
      if (ts != null && (latestLogged == null || ts.isAfter(latestLogged))) {
        latestLogged = ts;
      }
    }
    if (logEntry.getStateChanges().size() == sizeBefore) {
      return; // nothing actually appended → don't touch the repository
    }
    try {
      service.save(instance);
    } catch (RuntimeException ex) {
      // Non-fatal: the log merge is advisory. The subsequent dispatch path owns the
      // authoritative state transition and will re-save (with retry) as needed.
      log.warn(
          "Failed to persist merged worker state history for instance {} activity {}: {}",
          processInstanceId,
          activityId,
          ex.getMessage());
    }
  }

  private static OffsetDateTime latestTimestamp(List<StateChange> changes) {
    if (changes == null || changes.isEmpty()) {
      return null;
    }
    OffsetDateTime max = null;
    for (StateChange change : changes) {
      OffsetDateTime ts = change != null ? change.getTimestamp() : null;
      if (ts != null && (max == null || ts.isAfter(max))) {
        max = ts;
      }
    }
    return max;
  }

  private void dispatch(WorkerEventRequest event, String processInstanceId, String activityId) {
    if (event.getState().equals(NodeState.INCIDENT)) {
      orchestWorkflowEngine.handleIncident(
          processInstanceId, activityId, event.getIncidentMessage());
      return;
    }

    if (event.getErrorEvent() != null) {
      String errorCode = event.getErrorEvent().getErrorCode();
      if (errorCode == null) {
        errorCode = event.getErrorCode();
      }
      orchestWorkflowEngine.handleError(
          processInstanceId, activityId, errorCode, event.getVariables());
    } else {
      orchestWorkflowEngine.resumeActivity(processInstanceId, activityId, event.getVariables());
    }
  }
}
