package io.telekom.orchest.enginecore.bpmn.execution.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.MultiInstanceLoopCharacteristics;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.node.GatewayNode;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.enginecore.bpmn.ServiceTaskHandlerAdapter;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests service task dispatch logic including multi-instance handling and worker event creation.
 */
@ExtendWith(MockitoExtension.class)
class ServiceTaskExecutorTest {

  @Mock private ServiceTaskHandlerAdapter serviceTaskHandlerAdapter;

  @Mock private ExecutionContext context;

  private ServiceTaskExecutor executor;

  @BeforeEach
  void setUp() {
    executor = new ServiceTaskExecutor(serviceTaskHandlerAdapter, new HashMap<>());
  }

  // ========================================================================================
  // execute: creates WorkerEventRequest with correct fields
  // ========================================================================================

  @Test
  void execute_createsWorkerEventRequestWithCorrectFields() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "SendEmail");
    serviceTask.setWorkerType("email-worker");
    serviceTask.setRetries(5);

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(Map.of("task-1", serviceTask))
            .sequenceFlows(new HashMap<>())
            .build());
    instance.setVariables(new HashMap<>(Map.of("recipient", "test@test.com")));

    executor.execute(instance, serviceTask, context);

    ArgumentCaptor<WorkerEventRequest> captor = ArgumentCaptor.forClass(WorkerEventRequest.class);
    verify(serviceTaskHandlerAdapter).handle(captor.capture());

    WorkerEventRequest event = captor.getValue();
    assertEquals("pi-1", event.getProcessInstanceId());
    assertEquals("def-1", event.getProcessDefinitionId());
    assertEquals("task-1", event.getActivityId());
    assertEquals("SendEmail", event.getActivityName());
    assertEquals("email-worker", event.getType());
    assertEquals(NodeState.TRIGGERED, event.getState());
    assertEquals(1, event.getVersion());
    assertEquals(5, event.getRetries());
    assertNotNull(event.getEventId(), "eventId should be generated");
    assertFalse(event.getEventId().isEmpty(), "eventId should not be empty");
    assertNotNull(event.getNodeInformation());
    assertNotNull(event.getVariables());
    assertNotNull(event.getStateChanges());
    assertFalse(event.getStateChanges().isEmpty());
  }

  // ========================================================================================
  // execute: UUID eventId generation
  // ========================================================================================

  @Test
  void execute_generatesUuidEventId() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Task");
    serviceTask.setWorkerType("worker");

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setVariables(new HashMap<>());

    executor.execute(instance, serviceTask, context);

    ArgumentCaptor<WorkerEventRequest> captor = ArgumentCaptor.forClass(WorkerEventRequest.class);
    verify(serviceTaskHandlerAdapter).handle(captor.capture());

    String eventId = captor.getValue().getEventId();
    assertNotNull(eventId);
    // UUID format validation
    assertTrue(
        eventId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
        "eventId should be a valid UUID: " + eventId);
  }

  @Test
  void execute_generatesDifferentEventIdPerCall() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Task");
    serviceTask.setWorkerType("worker");

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setVariables(new HashMap<>());

    executor.execute(instance, serviceTask, context);
    executor.execute(instance, serviceTask, context);

    ArgumentCaptor<WorkerEventRequest> captor = ArgumentCaptor.forClass(WorkerEventRequest.class);
    verify(serviceTaskHandlerAdapter, times(2)).handle(captor.capture());

    String id1 = captor.getAllValues().get(0).getEventId();
    String id2 = captor.getAllValues().get(1).getEventId();
    assertNotEquals(id1, id2, "Each invocation should produce a unique eventId");
  }

  // ========================================================================================
  // execute: sets nodeInformation on event
  // ========================================================================================

  @Test
  void execute_setsNodeInformationOnEvent() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Task");
    serviceTask.setWorkerType("worker");

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setVariables(new HashMap<>());

    executor.execute(instance, serviceTask, context);

    ArgumentCaptor<WorkerEventRequest> captor = ArgumentCaptor.forClass(WorkerEventRequest.class);
    verify(serviceTaskHandlerAdapter).handle(captor.capture());

    assertSame(serviceTask, captor.getValue().getNodeInformation());
  }

  // ========================================================================================
  // execute: includes instance variables in event
  // ========================================================================================

  @Test
  void execute_includesInstanceVariablesInEvent() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Task");
    serviceTask.setWorkerType("worker");

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    Map<String, Object> vars = new HashMap<>();
    vars.put("orderId", "ORD-123");
    vars.put("amount", 99.99);
    instance.setVariables(vars);

    executor.execute(instance, serviceTask, context);

    ArgumentCaptor<WorkerEventRequest> captor = ArgumentCaptor.forClass(WorkerEventRequest.class);
    verify(serviceTaskHandlerAdapter).handle(captor.capture());

    Map<String, Object> eventVars = captor.getValue().getVariables().getVariables();
    assertEquals("ORD-123", eventVars.get("orderId"));
    assertEquals(99.99, eventVars.get("amount"));
  }

  // ========================================================================================
  // execute: stateChanges contains TRIGGERED entry
  // ========================================================================================

  @Test
  void execute_stateChangesContainsTriggered() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Task");
    serviceTask.setWorkerType("worker");

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setVariables(new HashMap<>());

    executor.execute(instance, serviceTask, context);

    ArgumentCaptor<WorkerEventRequest> captor = ArgumentCaptor.forClass(WorkerEventRequest.class);
    verify(serviceTaskHandlerAdapter).handle(captor.capture());

    assertNotNull(captor.getValue().getStateChanges());
    assertEquals(1, captor.getValue().getStateChanges().size());
    assertEquals(NodeState.TRIGGERED, captor.getValue().getStateChanges().get(0).getState());
  }

  // ========================================================================================
  // execute: throws for non-ServiceTaskNode
  // ========================================================================================

  @Test
  void execute_throwsForNonServiceTaskNode() {
    GatewayNode nonServiceTask = new GatewayNode("gw-1", "Gateway", NodeType.EXCLUSIVE_GATEWAY);
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);

    assertThrows(
        IllegalStateException.class, () -> executor.execute(instance, nonServiceTask, context));
    verifyNoInteractions(serviceTaskHandlerAdapter);
  }

  // ========================================================================================
  // execute: uses default retries (3) when not explicitly set
  // ========================================================================================

  @Test
  void execute_usesDefaultRetriesWhenNotSet() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Task");
    serviceTask.setWorkerType("worker");
    // retries not set -- defaults to 3

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setVariables(new HashMap<>());

    executor.execute(instance, serviceTask, context);

    ArgumentCaptor<WorkerEventRequest> captor = ArgumentCaptor.forClass(WorkerEventRequest.class);
    verify(serviceTaskHandlerAdapter).handle(captor.capture());

    assertEquals(3, captor.getValue().getRetries());
  }

  // ========================================================================================
  // execute: empty variables
  // ========================================================================================

  @Test
  void execute_withEmptyVariables_sendsEmptyVariablesMap() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Task");
    serviceTask.setWorkerType("worker");

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setVariables(new HashMap<>());

    executor.execute(instance, serviceTask, context);

    ArgumentCaptor<WorkerEventRequest> captor = ArgumentCaptor.forClass(WorkerEventRequest.class);
    verify(serviceTaskHandlerAdapter).handle(captor.capture());

    assertNotNull(captor.getValue().getVariables());
    assertTrue(captor.getValue().getVariables().getVariables().isEmpty());
  }

  // ========================================================================================
  // execute: multi-instance parallel sends multiple events
  // ========================================================================================

  @Test
  void execute_withMultiInstanceParallel_sendsMultipleWorkerEvents() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-mi", "MITask");
    serviceTask.setWorkerType("mi-worker");
    MultiInstanceLoopCharacteristics mi =
        MultiInstanceLoopCharacteristics.builder()
            .isSequential(false)
            .collection("=items")
            .elementVariable("item")
            .build();
    serviceTask.setMultiInstanceLoopCharacteristics(mi);

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    Map<String, Object> variables = new HashMap<>();
    variables.put("items", List.of("A", "B", "C"));
    instance.setVariables(variables);
    instance.setProcessDefinition(
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(Map.of("task-mi", serviceTask))
            .sequenceFlows(new HashMap<>())
            .build());

    executor.execute(instance, serviceTask, context);

    verify(serviceTaskHandlerAdapter, times(3)).handle(any(WorkerEventRequest.class));
    assertTrue(instance.getActiveNodeIds().contains("task-mi"));
  }

  // ========================================================================================
  // execute: multi-instance sequential sends single event for first iteration
  // ========================================================================================

  @Test
  void execute_withMultiInstanceSequential_sendsOneWorkerEventForFirstIteration() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-mi-seq", "MISeqTask");
    serviceTask.setWorkerType("mi-seq-worker");
    MultiInstanceLoopCharacteristics mi =
        MultiInstanceLoopCharacteristics.builder()
            .isSequential(true)
            .collection("=items")
            .elementVariable("item")
            .build();
    serviceTask.setMultiInstanceLoopCharacteristics(mi);

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    Map<String, Object> variables = new HashMap<>();
    variables.put("items", List.of("X", "Y", "Z"));
    instance.setVariables(variables);
    instance.setProcessDefinition(
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(Map.of("task-mi-seq", serviceTask))
            .sequenceFlows(new HashMap<>())
            .build());

    executor.execute(instance, serviceTask, context);

    verify(serviceTaskHandlerAdapter, times(1)).handle(any(WorkerEventRequest.class));
    assertTrue(instance.getActiveNodeIds().contains("task-mi-seq"));
  }

  // ========================================================================================
  // execute: each parallel event has a unique eventId
  // ========================================================================================

  @Test
  void execute_withMultiInstanceParallel_eachEventHasUniqueEventId() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-mi", "MITask");
    serviceTask.setWorkerType("mi-worker");
    MultiInstanceLoopCharacteristics mi =
        MultiInstanceLoopCharacteristics.builder()
            .isSequential(false)
            .collection("=items")
            .elementVariable("item")
            .build();
    serviceTask.setMultiInstanceLoopCharacteristics(mi);

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    Map<String, Object> variables = new HashMap<>();
    variables.put("items", List.of("A", "B"));
    instance.setVariables(variables);
    instance.setProcessDefinition(
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(Map.of("task-mi", serviceTask))
            .sequenceFlows(new HashMap<>())
            .build());

    executor.execute(instance, serviceTask, context);

    ArgumentCaptor<WorkerEventRequest> captor = ArgumentCaptor.forClass(WorkerEventRequest.class);
    verify(serviceTaskHandlerAdapter, times(2)).handle(captor.capture());

    String eventId1 = captor.getAllValues().get(0).getEventId();
    String eventId2 = captor.getAllValues().get(1).getEventId();
    assertNotNull(eventId1);
    assertNotNull(eventId2);
    assertNotEquals(
        eventId1, eventId2, "Each parallel multi-instance event should have a unique eventId");
  }

  // ========================================================================================
  // execute: version from instance is propagated
  // ========================================================================================

  @Test
  void execute_propagatesVersionFromInstance() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Task");
    serviceTask.setWorkerType("worker");

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 42);
    instance.setVariables(new HashMap<>());

    executor.execute(instance, serviceTask, context);

    ArgumentCaptor<WorkerEventRequest> captor = ArgumentCaptor.forClass(WorkerEventRequest.class);
    verify(serviceTaskHandlerAdapter).handle(captor.capture());

    assertEquals(42, captor.getValue().getVersion());
  }
}
