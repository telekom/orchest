package io.telekom.orchest.enginecore.bpmn.eventhandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests worker event dispatching logic including retry, incident, error, and resume paths. */
@ExtendWith(MockitoExtension.class)
class IWorkerEventEventHandlerTest {

  @Mock private OrchestWorkflowEngine orchestWorkflowEngine;

  @Mock private OrchestEngineTelemetryService telemetryService;

  private IWorkerEventEventHandler handler;

  @BeforeEach
  void setUp() {
    handler = new IWorkerEventEventHandler(orchestWorkflowEngine, telemetryService);
  }

  // ========================================================================================
  // dispatch: successful first attempt
  // ========================================================================================

  @Test
  void handle_successfulDispatchOnFirstAttempt_callsResumeActivityOnce() {
    Variables variables = Variables.builder().variables(Map.of("result", 42)).build();
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-1")
            .activityId("activity-1")
            .state(NodeState.COMPLETED)
            .variables(variables)
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-1")).thenReturn(false);

    handler.handle(event);

    verify(orchestWorkflowEngine, times(1)).resumeActivity("pi-1", "activity-1", variables);
    verify(orchestWorkflowEngine, never()).handleIncident(anyString(), anyString(), anyString());
    verify(orchestWorkflowEngine, never())
        .handleError(anyString(), anyString(), anyString(), any());
  }

  // ========================================================================================
  // dispatch: INCIDENT state
  // ========================================================================================

  @Test
  void handle_withIncidentState_callsHandleIncident() {
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-1")
            .activityId("activity-1")
            .state(NodeState.INCIDENT)
            .incidentMessage("Something went wrong")
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-1")).thenReturn(false);

    handler.handle(event);

    verify(orchestWorkflowEngine).handleIncident("pi-1", "activity-1", "Something went wrong");
    verify(orchestWorkflowEngine, never()).resumeActivity(anyString(), anyString(), any());
    verify(orchestWorkflowEngine, never())
        .handleError(anyString(), anyString(), anyString(), any());
  }

  // ========================================================================================
  // dispatch: errorEvent present
  // ========================================================================================

  @Test
  void handle_withErrorEvent_callsHandleError() {
    WorkerEventRequest.ErrorEvent errorEvent =
        WorkerEventRequest.ErrorEvent.builder().errorCode("ERR-001").build();
    Variables variables = Variables.builder().variables(Map.of("key", "val")).build();
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-2")
            .activityId("activity-2")
            .state(NodeState.COMPLETED)
            .errorEvent(errorEvent)
            .variables(variables)
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-2")).thenReturn(false);

    handler.handle(event);

    verify(orchestWorkflowEngine).handleError("pi-2", "activity-2", "ERR-001", variables);
    verify(orchestWorkflowEngine, never()).resumeActivity(anyString(), anyString(), any());
    verify(orchestWorkflowEngine, never()).handleIncident(anyString(), anyString(), anyString());
  }

  @Test
  void handle_withErrorEvent_fallsBackToEventErrorCode_whenErrorEventCodeIsNull() {
    WorkerEventRequest.ErrorEvent errorEvent =
        WorkerEventRequest.ErrorEvent.builder().errorCode(null).build();
    Variables variables = Variables.builder().variables(Map.of("key", "val")).build();
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-2")
            .activityId("activity-2")
            .state(NodeState.COMPLETED)
            .errorEvent(errorEvent)
            .errorCode("FALLBACK-ERR")
            .variables(variables)
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-2")).thenReturn(false);

    handler.handle(event);

    verify(orchestWorkflowEngine).handleError("pi-2", "activity-2", "FALLBACK-ERR", variables);
  }

  // ========================================================================================
  // dispatch: normal completion
  // ========================================================================================

  @Test
  void handle_withNormalCompletion_callsResumeActivity() {
    Variables variables = Variables.builder().variables(Map.of("result", 42)).build();
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-3")
            .activityId("activity-3")
            .state(NodeState.COMPLETED)
            .variables(variables)
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-3")).thenReturn(false);

    handler.handle(event);

    verify(orchestWorkflowEngine).resumeActivity("pi-3", "activity-3", variables);
    verify(orchestWorkflowEngine, never()).handleIncident(anyString(), anyString(), anyString());
    verify(orchestWorkflowEngine, never())
        .handleError(anyString(), anyString(), anyString(), any());
  }

  // ========================================================================================
  // activityId fallback to nodeInformation.getId()
  // ========================================================================================

  @Test
  void handle_withNullActivityId_fallsBackToNodeInformationId() {
    ServiceTaskNode nodeInfo = new ServiceTaskNode("node-from-info", "TaskNode");
    Variables variables = Variables.builder().variables(Map.of()).build();
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-4")
            .activityId(null)
            .nodeInformation(nodeInfo)
            .state(NodeState.COMPLETED)
            .variables(variables)
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-4")).thenReturn(false);

    handler.handle(event);

    verify(orchestWorkflowEngine).resumeActivity("pi-4", "node-from-info", variables);
  }

  // ========================================================================================
  // activityId null everywhere -> logs error and returns
  // ========================================================================================

  @Test
  void handle_withNullActivityIdAndNullNodeInformation_logsErrorAndReturns() {
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-5")
            .activityId(null)
            .nodeInformation(null)
            .state(NodeState.COMPLETED)
            .build();

    handler.handle(event);

    verifyNoInteractions(orchestWorkflowEngine);
  }

  @Test
  void handle_withNullActivityIdAndNodeInfoHasNullId_logsErrorAndReturns() {
    ServiceTaskNode nodeInfo = new ServiceTaskNode(null, "TaskNode");
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-5b")
            .activityId(null)
            .nodeInformation(nodeInfo)
            .state(NodeState.COMPLETED)
            .build();

    handler.handle(event);

    // activityId is still null after fallback to nodeInfo.getId() which is null
    verifyNoInteractions(orchestWorkflowEngine);
  }

  // ========================================================================================
  // Engine throws: propagates (no stale retry loop)
  // ========================================================================================

  @Test
  void handle_resumeActivityThrows_propagates() {
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-6")
            .activityId("activity-6")
            .state(NodeState.COMPLETED)
            .variables(Variables.builder().variables(Map.of()).build())
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-6")).thenReturn(false);
    doThrow(new RuntimeException("engine failure"))
        .when(orchestWorkflowEngine)
        .resumeActivity(eq("pi-6"), eq("activity-6"), any());

    assertThrows(RuntimeException.class, () -> handler.handle(event));
    verify(orchestWorkflowEngine, times(1)).resumeActivity(eq("pi-6"), eq("activity-6"), any());
  }

  // ========================================================================================
  // Progressive retry detection: isRetryEvent=true -> early return
  // ========================================================================================

  @Test
  void handle_withProgressiveRetryEvent_returnsEarly() {
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-8")
            .activityId("activity-8")
            .state(NodeState.FAILED)
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-8")).thenReturn(true);

    handler.handle(event);

    // Progressive retry returns true, so dispatch is never called
    verify(orchestWorkflowEngine, never()).resumeActivity(anyString(), anyString(), any());
    verify(orchestWorkflowEngine, never()).handleIncident(anyString(), anyString(), anyString());
    verify(orchestWorkflowEngine, never())
        .handleError(anyString(), anyString(), anyString(), any());
  }

  // ========================================================================================
  // INCIDENT dispatch does NOT attempt resumeActivity or handleError
  // ========================================================================================

  @Test
  void handle_incidentState_onlyCallsHandleIncident_noOtherDispatches() {
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-inc-only")
            .activityId("act-inc")
            .state(NodeState.INCIDENT)
            .incidentMessage("failure msg")
            .errorEvent(null)
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-inc-only"))
        .thenReturn(false);

    handler.handle(event);

    verify(orchestWorkflowEngine).handleIncident("pi-inc-only", "act-inc", "failure msg");
    verify(orchestWorkflowEngine, never()).resumeActivity(anyString(), anyString(), any());
    verify(orchestWorkflowEngine, never())
        .handleError(anyString(), anyString(), anyString(), any());
  }

  // ========================================================================================
  // INCIDENT dispatch returns immediately (no retry loop for INCIDENT)
  // ========================================================================================

  @Test
  void handle_incidentState_succeedsOnFirstAttempt_noRetryNeeded() {
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-inc-1")
            .activityId("act-inc-1")
            .state(NodeState.INCIDENT)
            .incidentMessage("incident msg")
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-inc-1"))
        .thenReturn(false);

    handler.handle(event);

    verify(orchestWorkflowEngine, times(1)).handleIncident("pi-inc-1", "act-inc-1", "incident msg");
  }

  // ========================================================================================
  // Error event with both error code sources null -> passes null
  // ========================================================================================

  @Test
  void handle_withErrorEvent_bothErrorCodesNull_passesNullToHandleError() {
    WorkerEventRequest.ErrorEvent errorEvent =
        WorkerEventRequest.ErrorEvent.builder().errorCode(null).build();
    Variables variables = Variables.builder().variables(Map.of()).build();
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-null-err")
            .activityId("act-null-err")
            .state(NodeState.COMPLETED)
            .errorEvent(errorEvent)
            .errorCode(null)
            .variables(variables)
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-null-err"))
        .thenReturn(false);

    handler.handle(event);

    verify(orchestWorkflowEngine).handleError("pi-null-err", "act-null-err", null, variables);
  }

  @Test
  void handle_handleIncidentThrows_propagates() {
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-inc-retry")
            .activityId("act-inc-retry")
            .state(NodeState.INCIDENT)
            .incidentMessage("inc msg")
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-inc-retry"))
        .thenReturn(false);
    when(orchestWorkflowEngine.handleIncident("pi-inc-retry", "act-inc-retry", "inc msg"))
        .thenThrow(new RuntimeException("incident handling failed"));

    assertThrows(RuntimeException.class, () -> handler.handle(event));
    verify(orchestWorkflowEngine, times(1))
        .handleIncident("pi-inc-retry", "act-inc-retry", "inc msg");
  }

  @Test
  void handle_handleErrorThrows_propagates() {
    WorkerEventRequest.ErrorEvent errorEvent =
        WorkerEventRequest.ErrorEvent.builder().errorCode("ERR-RETRY").build();
    Variables variables = Variables.builder().variables(Map.of()).build();
    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-err-retry")
            .activityId("act-err-retry")
            .state(NodeState.COMPLETED)
            .errorEvent(errorEvent)
            .variables(variables)
            .build();

    when(orchestWorkflowEngine.registerProgressiveRetryRetryIfAny(event, "pi-err-retry"))
        .thenReturn(false);
    when(orchestWorkflowEngine.handleError("pi-err-retry", "act-err-retry", "ERR-RETRY", variables))
        .thenThrow(new RuntimeException("error handling failed"));

    assertThrows(RuntimeException.class, () -> handler.handle(event));
    verify(orchestWorkflowEngine, times(1))
        .handleError("pi-err-retry", "act-err-retry", "ERR-RETRY", variables);
  }
}
