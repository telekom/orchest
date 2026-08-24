package io.telekom.orchest.enginecore.bpmn;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests error propagation, boundary event matching, and incident handling in ErrorPropagationChain.
 */
@ExtendWith(MockitoExtension.class)
class ErrorPropagationChainTest {

  @Mock private ProcessInstanceService processInstanceService;

  @Mock private IncidentEventHandlerAdapter incidentEventHandlerAdapter;

  @Mock private EventRegisterService eventRegisterService;

  @Mock private ErrorPropagationChain.NodeExecutionCallback nodeExecutionCallback;

  private ErrorPropagationChain errorPropagationChain;

  @BeforeEach
  void setUp() {
    errorPropagationChain =
        new ErrorPropagationChain(
            processInstanceService,
            incidentEventHandlerAdapter,
            eventRegisterService,
            nodeExecutionCallback);
  }

  // ========================================================================================
  // handleError: finds matching error boundary event -> returns true
  // ========================================================================================

  @Test
  void handleError_withMatchingBoundaryEvent_returnsTrue() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "MyTask");
    serviceTask.setScopeId("process-1");
    serviceTask.setScopeType(ScopeType.PROCESS);

    EventNode errorBoundary =
        new EventNode("boundary-1", "ErrorBoundary", NodeType.BOUNDARY_EVENT, EventType.ERROR);
    errorBoundary.setErrorCode("ERR-001");
    errorBoundary.setAttachedToId("task-1");
    errorBoundary.setCancelActivity(true);

    serviceTask.addBoundaryEvent("boundary-1");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("task-1", serviceTask);
    nodes.put("boundary-1", errorBoundary);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("task-1");
    instance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any())).thenReturn(instance);

    boolean result = errorPropagationChain.handleError("pi-1", "task-1", "ERR-001", null);

    assertTrue(result);
    verify(nodeExecutionCallback).executeNode(eq(instance), eq(errorBoundary), eq("task-1"));
    verify(processInstanceService).save(instance);
  }

  // ========================================================================================
  // handleError: catch-all boundary event (no error code) -> matches
  // ========================================================================================

  @Test
  void handleError_withCatchAllBoundaryEvent_matches() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "MyTask");
    serviceTask.setScopeId("process-1");
    serviceTask.setScopeType(ScopeType.PROCESS);

    EventNode errorBoundary =
        new EventNode("boundary-1", "CatchAll", NodeType.BOUNDARY_EVENT, EventType.ERROR);
    errorBoundary.setErrorCode(null);
    errorBoundary.setAttachedToId("task-1");
    errorBoundary.setCancelActivity(false);

    serviceTask.addBoundaryEvent("boundary-1");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("task-1", serviceTask);
    nodes.put("boundary-1", errorBoundary);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("task-1");
    instance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any())).thenReturn(instance);

    boolean result = errorPropagationChain.handleError("pi-1", "task-1", "ANY-ERROR", null);

    assertTrue(result);
    verify(nodeExecutionCallback).executeNode(eq(instance), eq(errorBoundary), eq("task-1"));
  }

  // ========================================================================================
  // handleError: empty error code on boundary event -> matches any
  // ========================================================================================

  @Test
  void handleError_withEmptyErrorCodeBoundaryEvent_matchesAny() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "MyTask");
    serviceTask.setScopeId("process-1");
    serviceTask.setScopeType(ScopeType.PROCESS);

    EventNode errorBoundary =
        new EventNode("boundary-1", "CatchAll", NodeType.BOUNDARY_EVENT, EventType.ERROR);
    errorBoundary.setErrorCode(""); // empty string, not null
    errorBoundary.setAttachedToId("task-1");
    errorBoundary.setCancelActivity(false);

    serviceTask.addBoundaryEvent("boundary-1");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("task-1", serviceTask);
    nodes.put("boundary-1", errorBoundary);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("task-1");
    instance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any())).thenReturn(instance);

    boolean result = errorPropagationChain.handleError("pi-1", "task-1", "ANYTHING", null);

    assertTrue(result);
    verify(nodeExecutionCallback).executeNode(eq(instance), eq(errorBoundary), eq("task-1"));
  }

  // ========================================================================================
  // handleError: no boundary event -> raises incident -> returns false
  // ========================================================================================

  @Test
  void handleError_withNoBoundaryEvent_raisesIncidentAndReturnsFalse() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "MyTask");
    serviceTask.setScopeId("process-1");
    serviceTask.setScopeType(ScopeType.PROCESS);

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("task-1", serviceTask);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("task-1");
    instance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any())).thenReturn(instance);

    boolean result = errorPropagationChain.handleError("pi-1", "task-1", "ERR-999", null);

    assertFalse(result);
    assertTrue(instance.isHasIncident());
    assertEquals(PIState.INCIDENT, instance.getState());
    verify(incidentEventHandlerAdapter).handle(any());
    verify(nodeExecutionCallback, never()).executeNode(any(), any(), anyString());
  }

  // ========================================================================================
  // handleError: incident event payload has correct fields
  // ========================================================================================

  @Test
  void handleError_raisesIncident_withCorrectPayloadFields() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "MyTask");
    serviceTask.setScopeId("process-1");
    serviceTask.setScopeType(ScopeType.PROCESS);

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("task-1", serviceTask);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("task-1");
    instance.setCorrelationIds(List.of("corr-A", "corr-B"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any())).thenReturn(instance);

    errorPropagationChain.handleError("pi-1", "task-1", "ERR-X", null);

    ArgumentCaptor<IncidentEventPayload> captor =
        ArgumentCaptor.forClass(IncidentEventPayload.class);
    verify(incidentEventHandlerAdapter).handle(captor.capture());
    IncidentEventPayload payload = captor.getValue();
    assertEquals("pi-1", payload.getProcessInstanceId());
    assertEquals("corr-A,corr-B", payload.getCorrelationId());
    assertEquals("def-1", payload.getProcessDefinitionId());
    assertEquals("task-1", payload.getActivityId());
    assertTrue(payload.getIncidentMessage().contains("ERR-X"));
  }

  // ========================================================================================
  // handleError: propagates to parent process (Call Activity)
  // ========================================================================================

  @Test
  void handleError_propagatesToParentProcess() {
    ServiceTaskNode childTask = new ServiceTaskNode("child-task", "ChildTask");
    childTask.setScopeId("child-proc");
    childTask.setScopeType(ScopeType.PROCESS);

    Map<String, BaseNode> childNodes = new HashMap<>();
    childNodes.put("child-task", childTask);

    ProcessDefinition childDef =
        ProcessDefinition.builder()
            .definitionId("child-def")
            .nodes(childNodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ServiceTaskNode callActivityNode = new ServiceTaskNode("call-activity-1", "CallActivity");
    callActivityNode.setScopeId("parent-proc");
    callActivityNode.setScopeType(ScopeType.PROCESS);

    EventNode parentBoundary =
        new EventNode(
            "parent-boundary", "ParentErrorBoundary", NodeType.BOUNDARY_EVENT, EventType.ERROR);
    parentBoundary.setErrorCode("ERR-CHILD");
    parentBoundary.setAttachedToId("call-activity-1");
    parentBoundary.setCancelActivity(true);

    callActivityNode.addBoundaryEvent("parent-boundary");

    Map<String, BaseNode> parentNodes = new HashMap<>();
    parentNodes.put("call-activity-1", callActivityNode);
    parentNodes.put("parent-boundary", parentBoundary);

    ProcessDefinition parentDef =
        ProcessDefinition.builder()
            .definitionId("parent-def")
            .nodes(parentNodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance parentInstance = new ProcessInstance("parent-pi", "parent-def", 1);
    parentInstance.setProcessDefinition(parentDef);
    parentInstance.addActiveNode("call-activity-1");
    parentInstance.setCorrelationIds(List.of("corr-parent"));

    ParentProcessActivity ppa = new ParentProcessActivity("parent-pi", callActivityNode);

    ProcessInstance childInstance = new ProcessInstance("child-pi", "child-def", 1);
    childInstance.setProcessDefinition(childDef);
    childInstance.addActiveNode("child-task");
    childInstance.setParentProcesActivity(ppa);
    childInstance.setCorrelationIds(List.of("corr-child"));
    childInstance.setVariables(new HashMap<>());

    when(processInstanceService.getInstanceById("child-pi")).thenReturn(Optional.of(childInstance));
    when(processInstanceService.getInstanceById("parent-pi"))
        .thenReturn(Optional.of(parentInstance));
    when(processInstanceService.save(any())).thenAnswer(inv -> inv.getArgument(0));

    boolean result = errorPropagationChain.handleError("child-pi", "child-task", "ERR-CHILD", null);

    assertTrue(result);
    assertTrue(childInstance.isCompleted());
    assertEquals(PIState.TERMINATED, childInstance.getState());
    verify(nodeExecutionCallback)
        .executeNode(eq(parentInstance), eq(parentBoundary), eq("call-activity-1"));
  }

  // ========================================================================================
  // handleError: parent fails to handle -> raises incident on child
  // ========================================================================================

  @Test
  void handleError_parentFailsToHandle_raisesIncidentOnChild() {
    ServiceTaskNode childTask = new ServiceTaskNode("child-task", "ChildTask");
    childTask.setScopeId("child-proc");
    childTask.setScopeType(ScopeType.PROCESS);

    Map<String, BaseNode> childNodes = new HashMap<>();
    childNodes.put("child-task", childTask);

    ProcessDefinition childDef =
        ProcessDefinition.builder()
            .definitionId("child-def")
            .nodes(childNodes)
            .sequenceFlows(new HashMap<>())
            .build();

    // Parent has no matching boundary event
    ServiceTaskNode callActivityNode = new ServiceTaskNode("call-activity-1", "CallActivity");
    callActivityNode.setScopeId("parent-proc");
    callActivityNode.setScopeType(ScopeType.PROCESS);
    // No boundary events on parent

    Map<String, BaseNode> parentNodes = new HashMap<>();
    parentNodes.put("call-activity-1", callActivityNode);

    ProcessDefinition parentDef =
        ProcessDefinition.builder()
            .definitionId("parent-def")
            .nodes(parentNodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance parentInstance = new ProcessInstance("parent-pi", "parent-def", 1);
    parentInstance.setProcessDefinition(parentDef);
    parentInstance.addActiveNode("call-activity-1");
    parentInstance.setCorrelationIds(List.of("corr-parent"));

    ParentProcessActivity ppa = new ParentProcessActivity("parent-pi", callActivityNode);

    ProcessInstance childInstance = new ProcessInstance("child-pi", "child-def", 1);
    childInstance.setProcessDefinition(childDef);
    childInstance.addActiveNode("child-task");
    childInstance.setParentProcesActivity(ppa);
    childInstance.setCorrelationIds(List.of("corr-child"));
    childInstance.setVariables(new HashMap<>());

    when(processInstanceService.getInstanceById("child-pi")).thenReturn(Optional.of(childInstance));
    when(processInstanceService.getInstanceById("parent-pi"))
        .thenReturn(Optional.of(parentInstance));
    when(processInstanceService.save(any())).thenAnswer(inv -> inv.getArgument(0));

    boolean result =
        errorPropagationChain.handleError("child-pi", "child-task", "UNHANDLED-ERR", null);

    assertFalse(result);
    // Child instance should have an incident raised
    assertTrue(childInstance.isHasIncident());
    assertEquals(PIState.INCIDENT, childInstance.getState());
  }

  // ========================================================================================
  // handleError: interrupting boundary event cleans up subprocess children
  // ========================================================================================

  @Test
  void handleError_withInterruptingBoundary_cleansUpSubprocessChildren() {
    SubProcessNode subProcess = new SubProcessNode("sub-1", "SubProcess");
    subProcess.setScopeId("process-1");
    subProcess.setScopeType(ScopeType.PROCESS);

    ServiceTaskNode failingTask = new ServiceTaskNode("task-fail", "FailTask");
    failingTask.setScopeId("sub-1");
    failingTask.setScopeType(ScopeType.SUBPROCESS);

    ServiceTaskNode otherTask = new ServiceTaskNode("task-ok", "OtherTask");
    otherTask.setScopeId("sub-1");
    otherTask.setScopeType(ScopeType.SUBPROCESS);

    EventNode errorBoundary =
        new EventNode("boundary-sub", "SubErrorBoundary", NodeType.BOUNDARY_EVENT, EventType.ERROR);
    errorBoundary.setErrorCode("ERR-SUB");
    errorBoundary.setAttachedToId("sub-1");
    errorBoundary.setCancelActivity(true);

    subProcess.addBoundaryEvent("boundary-sub");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("sub-1", subProcess);
    nodes.put("task-fail", failingTask);
    nodes.put("task-ok", otherTask);
    nodes.put("boundary-sub", errorBoundary);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("task-fail");
    instance.addActiveNode("task-ok");
    instance.addActiveNode("sub-1");
    instance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any())).thenReturn(instance);

    boolean result = errorPropagationChain.handleError("pi-1", "task-fail", "ERR-SUB", null);

    assertTrue(result);
    assertFalse(instance.getActiveNodeIds().contains("task-fail"));
    assertFalse(instance.getActiveNodeIds().contains("task-ok"));
    assertFalse(instance.getActiveNodeIds().contains("sub-1"));
  }

  // ========================================================================================
  // handleError: non-interrupting boundary does NOT clean up children
  // ========================================================================================

  @Test
  void handleError_withNonInterruptingBoundary_doesNotCleanUpSubprocessChildren() {
    SubProcessNode subProcess = new SubProcessNode("sub-1", "SubProcess");
    subProcess.setScopeId("process-1");
    subProcess.setScopeType(ScopeType.PROCESS);

    ServiceTaskNode failingTask = new ServiceTaskNode("task-fail", "FailTask");
    failingTask.setScopeId("sub-1");
    failingTask.setScopeType(ScopeType.SUBPROCESS);

    ServiceTaskNode otherTask = new ServiceTaskNode("task-ok", "OtherTask");
    otherTask.setScopeId("sub-1");
    otherTask.setScopeType(ScopeType.SUBPROCESS);

    EventNode errorBoundary =
        new EventNode("boundary-sub", "SubErrorBoundary", NodeType.BOUNDARY_EVENT, EventType.ERROR);
    errorBoundary.setErrorCode("ERR-SUB");
    errorBoundary.setAttachedToId("sub-1");
    errorBoundary.setCancelActivity(false); // non-interrupting

    subProcess.addBoundaryEvent("boundary-sub");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("sub-1", subProcess);
    nodes.put("task-fail", failingTask);
    nodes.put("task-ok", otherTask);
    nodes.put("boundary-sub", errorBoundary);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("task-fail");
    instance.addActiveNode("task-ok");
    instance.addActiveNode("sub-1");
    instance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any())).thenReturn(instance);

    boolean result = errorPropagationChain.handleError("pi-1", "task-fail", "ERR-SUB", null);

    assertTrue(result);
    // task-fail is removed by handleError, but task-ok and sub-1 should remain
    assertFalse(instance.getActiveNodeIds().contains("task-fail"));
    assertTrue(instance.getActiveNodeIds().contains("task-ok"));
    assertTrue(instance.getActiveNodeIds().contains("sub-1"));
  }

  // ========================================================================================
  // handleError: save throws -> caught, returns false
  // ========================================================================================

  @Test
  void handleError_saveThrowsException_returnsFalse() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "MyTask");
    serviceTask.setScopeId("process-1");
    serviceTask.setScopeType(ScopeType.PROCESS);

    EventNode errorBoundary =
        new EventNode("boundary-1", "ErrorBoundary", NodeType.BOUNDARY_EVENT, EventType.ERROR);
    errorBoundary.setErrorCode("ERR-001");
    errorBoundary.setAttachedToId("task-1");
    errorBoundary.setCancelActivity(true);

    serviceTask.addBoundaryEvent("boundary-1");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("task-1", serviceTask);
    nodes.put("boundary-1", errorBoundary);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("task-1");
    instance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any()))
        .thenThrow(new RuntimeException("concurrent modification"));

    boolean result = errorPropagationChain.handleError("pi-1", "task-1", "ERR-001", null);

    assertFalse(result);
  }

  // ========================================================================================
  // handleError: node execution throws -> caught, returns false
  // ========================================================================================

  @Test
  void handleError_nodeExecutionThrowsException_returnsFalse() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "MyTask");
    serviceTask.setScopeId("process-1");
    serviceTask.setScopeType(ScopeType.PROCESS);

    EventNode errorBoundary =
        new EventNode("boundary-1", "ErrorBoundary", NodeType.BOUNDARY_EVENT, EventType.ERROR);
    errorBoundary.setErrorCode("ERR-001");
    errorBoundary.setAttachedToId("task-1");
    errorBoundary.setCancelActivity(true);

    serviceTask.addBoundaryEvent("boundary-1");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("task-1", serviceTask);
    nodes.put("boundary-1", errorBoundary);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("task-1");
    instance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    doThrow(new RuntimeException("generic failure"))
        .when(nodeExecutionCallback)
        .executeNode(any(), any(), anyString());

    boolean result = errorPropagationChain.handleError("pi-1", "task-1", "ERR-001", null);

    assertFalse(result);
  }

  // ========================================================================================
  // handleError: instance not found -> throws IllegalArgumentException
  // ========================================================================================

  @Test
  void handleError_instanceNotFound_throwsIllegalArgumentException() {
    when(processInstanceService.getInstanceById("pi-missing")).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> errorPropagationChain.handleError("pi-missing", "task-1", "ERR-001", null));
  }

  // ========================================================================================
  // handleError: node not found -> throws IllegalArgumentException
  // ========================================================================================

  @Test
  void handleError_nodeNotFound_throwsIllegalArgumentException() {
    Map<String, BaseNode> nodes = new HashMap<>(); // no nodes

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));

    // Node "task-missing" is not in the definition -- orElseThrow at line 73 is before the try
    // block
    assertThrows(
        IllegalArgumentException.class,
        () -> errorPropagationChain.handleError("pi-1", "task-missing", "ERR-001", null));
  }

  // ========================================================================================
  // handleError: merges variables before error handling
  // ========================================================================================

  @Test
  void handleError_mergesVariablesIntoInstance() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "MyTask");
    serviceTask.setScopeId("process-1");
    serviceTask.setScopeType(ScopeType.PROCESS);
    // No boundary events -> will raise incident

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("task-1", serviceTask);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("task-1");
    instance.setCorrelationIds(List.of("corr-1"));
    instance.setVariables(new HashMap<>(Map.of("existing", "val")));

    Variables newVars = Variables.builder().variables(Map.of("newKey", "newVal")).build();

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any())).thenReturn(instance);

    errorPropagationChain.handleError("pi-1", "task-1", "ERR-X", newVars);

    // Variables should have been merged
    assertEquals("val", instance.getVariables().get("existing"));
    assertEquals("newVal", instance.getVariables().get("newKey"));
  }

  // ========================================================================================
  // handleIncident: fresh instance -> sets INCIDENT state
  // ========================================================================================

  @Test
  void handleIncident_onFreshInstance_setsIncidentState() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setState(PIState.RUNNING);
    instance.setCorrelationIds(List.of("corr-1"));
    instance.setHasIncident(false);

    ProcessInstance savedInstance = new ProcessInstance("pi-1", "def-1", 1);
    savedInstance.setState(PIState.INCIDENT);
    savedInstance.setHasIncident(true);
    savedInstance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any())).thenReturn(savedInstance);

    boolean result = errorPropagationChain.handleIncident("pi-1", "activity-1", "Test incident");

    assertTrue(result);
    verify(eventRegisterService).cleanupPendingRetryTimers("pi-1");
    verify(incidentEventHandlerAdapter).handle(any());
  }

  // ========================================================================================
  // handleIncident: verify incident event payload
  // ========================================================================================

  @Test
  void handleIncident_sendsCorrectIncidentEventPayload() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setState(PIState.RUNNING);
    instance.setCorrelationIds(List.of("corr-A"));
    instance.setHasIncident(false);

    ProcessInstance savedInstance = new ProcessInstance("pi-1", "def-1", 1);
    savedInstance.setState(PIState.INCIDENT);
    savedInstance.setHasIncident(true);
    savedInstance.setCorrelationIds(List.of("corr-A"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any())).thenReturn(savedInstance);

    errorPropagationChain.handleIncident("pi-1", "activity-1", "Incident happened");

    ArgumentCaptor<IncidentEventPayload> captor =
        ArgumentCaptor.forClass(IncidentEventPayload.class);
    verify(incidentEventHandlerAdapter).handle(captor.capture());
    IncidentEventPayload payload = captor.getValue();

    assertEquals("pi-1", payload.getProcessInstanceId());
    assertEquals("corr-A", payload.getCorrelationId());
    assertEquals("Incident happened", payload.getIncidentMessage());
    assertEquals("def-1", payload.getProcessDefinitionId());
    assertEquals("activity-1", payload.getActivityId());
  }

  // ========================================================================================
  // handleIncident: already-incident instance -> skips (idempotent)
  // ========================================================================================

  @Test
  void handleIncident_onAlreadyIncidentInstance_skipsUpdate() {
    ProcessInstance alreadyIncident = new ProcessInstance("pi-1", "def-1", 1);
    alreadyIncident.setState(PIState.INCIDENT);
    alreadyIncident.setHasIncident(true);

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(alreadyIncident));

    boolean result =
        errorPropagationChain.handleIncident("pi-1", "activity-1", "Duplicate incident");

    assertTrue(result);
    verify(processInstanceService, never()).save(any());
    verify(incidentEventHandlerAdapter, never()).handle(any());
    verify(eventRegisterService, never()).cleanupPendingRetryTimers(anyString());
  }

  // ========================================================================================
  // handleIncident: non-existent instance -> returns false
  // ========================================================================================

  @Test
  void handleIncident_withNonExistentInstance_returnsFalse() {
    when(processInstanceService.getInstanceById("pi-missing")).thenReturn(Optional.empty());

    boolean result =
        errorPropagationChain.handleIncident("pi-missing", "activity-1", "Incident msg");

    assertFalse(result);
    verify(incidentEventHandlerAdapter, never()).handle(any());
  }

  // ========================================================================================
  // handleIncident: loads by id then save
  // ========================================================================================

  @Test
  void handleIncident_usesGetByIdAndSave() {
    ProcessInstance loaded = new ProcessInstance("pi-1", "def-1", 1);
    loaded.setState(PIState.RUNNING);
    loaded.setHasIncident(false);
    loaded.setCorrelationIds(List.of("corr-1"));

    ProcessInstance savedInstance = new ProcessInstance("pi-1", "def-1", 1);
    savedInstance.setHasIncident(true);
    savedInstance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(loaded));
    when(processInstanceService.save(any())).thenReturn(savedInstance);

    errorPropagationChain.handleIncident("pi-1", "act-1", "msg");

    verify(processInstanceService).getInstanceById("pi-1");
    verify(processInstanceService).save(any());
  }

  // ========================================================================================
  // handleIncident: lambda correctly sets fields
  // ========================================================================================

  @Test
  void handleIncident_lambdaSetsIncidentFields() {
    ProcessInstance freshInstance = new ProcessInstance("pi-1", "def-1", 1);
    freshInstance.setState(PIState.RUNNING);
    freshInstance.setHasIncident(false);

    ProcessInstance savedInstance = new ProcessInstance("pi-1", "def-1", 1);
    savedInstance.setHasIncident(true);
    savedInstance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(freshInstance));
    when(processInstanceService.save(any())).thenReturn(savedInstance);

    errorPropagationChain.handleIncident("pi-1", "act-1", "my incident msg");

    // The lambda should have set these on freshInstance
    assertTrue(freshInstance.isHasIncident());
    assertEquals(PIState.INCIDENT, freshInstance.getState());
    assertEquals("my incident msg", freshInstance.getIncidentMessage());
  }

  // ========================================================================================
  // handleError: scope traversal finds boundary on parent subprocess
  // ========================================================================================

  @Test
  void handleError_traversesScopesToFindBoundaryOnParentSubprocess() {
    // Nested structure: task -> subprocess-inner -> subprocess-outer (has boundary)
    SubProcessNode outerSubProcess = new SubProcessNode("outer-sub", "OuterSubProcess");
    outerSubProcess.setScopeId("process-1");
    outerSubProcess.setScopeType(ScopeType.PROCESS);

    SubProcessNode innerSubProcess = new SubProcessNode("inner-sub", "InnerSubProcess");
    innerSubProcess.setScopeId("outer-sub");
    innerSubProcess.setScopeType(ScopeType.SUBPROCESS);

    ServiceTaskNode task = new ServiceTaskNode("task-deep", "DeepTask");
    task.setScopeId("inner-sub");
    task.setScopeType(ScopeType.SUBPROCESS);
    // No boundary events on task or inner subprocess

    EventNode errorBoundary =
        new EventNode("boundary-outer", "OuterBoundary", NodeType.BOUNDARY_EVENT, EventType.ERROR);
    errorBoundary.setErrorCode("ERR-DEEP");
    errorBoundary.setAttachedToId("outer-sub");
    errorBoundary.setCancelActivity(true);

    outerSubProcess.addBoundaryEvent("boundary-outer");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("outer-sub", outerSubProcess);
    nodes.put("inner-sub", innerSubProcess);
    nodes.put("task-deep", task);
    nodes.put("boundary-outer", errorBoundary);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("task-deep");
    instance.addActiveNode("inner-sub");
    instance.addActiveNode("outer-sub");
    instance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any())).thenReturn(instance);

    boolean result = errorPropagationChain.handleError("pi-1", "task-deep", "ERR-DEEP", null);

    assertTrue(result);
    // The outer boundary event should be triggered
    verify(nodeExecutionCallback).executeNode(eq(instance), eq(errorBoundary), eq("task-deep"));
  }

  // ========================================================================================
  // handleError: mismatched error code does not trigger boundary event
  // ========================================================================================

  @Test
  void handleError_mismatchedErrorCode_doesNotMatchBoundaryEvent() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "MyTask");
    serviceTask.setScopeId("process-1");
    serviceTask.setScopeType(ScopeType.PROCESS);

    EventNode errorBoundary =
        new EventNode("boundary-1", "ErrorBoundary", NodeType.BOUNDARY_EVENT, EventType.ERROR);
    errorBoundary.setErrorCode("ERR-SPECIFIC");
    errorBoundary.setAttachedToId("task-1");
    errorBoundary.setCancelActivity(true);

    serviceTask.addBoundaryEvent("boundary-1");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("task-1", serviceTask);
    nodes.put("boundary-1", errorBoundary);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("task-1");
    instance.setCorrelationIds(List.of("corr-1"));

    when(processInstanceService.getInstanceById("pi-1")).thenReturn(Optional.of(instance));
    when(processInstanceService.save(any())).thenReturn(instance);

    boolean result = errorPropagationChain.handleError("pi-1", "task-1", "ERR-DIFFERENT", null);

    assertFalse(result);
    verify(nodeExecutionCallback, never()).executeNode(any(), any(), anyString());
    assertTrue(instance.isHasIncident());
  }
}
