package io.telekom.orchest.enginecore.bpmn.execution.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.GatewayNode;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests parallel gateway fork/join token handling and synchronization logic. */
@ExtendWith(MockitoExtension.class)
class ParallelGatewayExecutorTest {

  @Mock private ExecutionContext context;

  private ParallelGatewayExecutor executor;

  @BeforeEach
  void setUp() {
    executor = new ParallelGatewayExecutor();
  }

  // ========================================================================================
  // handleJoinToken: first token arrives -> not ready
  // ========================================================================================

  @Test
  void handleJoinToken_firstTokenArrives_notReady() {
    GatewayNode joinGateway = new GatewayNode("gw-join", "ParallelJoin", NodeType.PARALLEL_GATEWAY);
    // Set up 2 incoming sequence flows
    Map<String, String> incoming = new HashMap<>();
    incoming.put("flow-1", "task-a");
    incoming.put("flow-2", "task-b");
    joinGateway.setIncomingSequenceFlowIds(incoming);

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("gw-join", joinGateway);
    nodes.put("task-a", new ServiceTaskNode("task-a", "TaskA"));
    nodes.put("task-b", new ServiceTaskNode("task-b", "TaskB"));

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);

    Boolean result = ParallelGatewayExecutor.handleJoinToken(instance, joinGateway, "task-a");

    assertNotNull(result);
    assertFalse(result, "First token should not fire the join gateway");
    assertEquals(1, instance.getParallelGatewayTokenCount("gw-join"));
  }

  // ========================================================================================
  // handleJoinToken: all tokens arrived -> ready to fire
  // ========================================================================================

  @Test
  void handleJoinToken_allTokensArrived_readyToFire() {
    GatewayNode joinGateway = new GatewayNode("gw-join", "ParallelJoin", NodeType.PARALLEL_GATEWAY);
    Map<String, String> incoming = new HashMap<>();
    incoming.put("flow-1", "task-a");
    incoming.put("flow-2", "task-b");
    joinGateway.setIncomingSequenceFlowIds(incoming);

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("gw-join", joinGateway);
    nodes.put("task-a", new ServiceTaskNode("task-a", "TaskA"));
    nodes.put("task-b", new ServiceTaskNode("task-b", "TaskB"));

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);

    // First token
    Boolean firstResult = ParallelGatewayExecutor.handleJoinToken(instance, joinGateway, "task-a");
    assertFalse(firstResult);

    // Second token (all arrived)
    Boolean secondResult = ParallelGatewayExecutor.handleJoinToken(instance, joinGateway, "task-b");
    assertTrue(secondResult, "All tokens received, should fire the join gateway");

    // Tokens should be reset after all received
    assertEquals(0, instance.getParallelGatewayTokenCount("gw-join"));
  }

  // ========================================================================================
  // handleJoinToken: not a parallel gateway -> returns null
  // ========================================================================================

  @Test
  void handleJoinToken_notParallelGateway_returnsNull() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Task");
    serviceTask.setType(NodeType.SERVICE_TASK);

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);

    Boolean result = ParallelGatewayExecutor.handleJoinToken(instance, serviceTask, "source-1");

    assertNull(result, "Non-parallel-gateway should return null");
  }

  // ========================================================================================
  // handleJoinToken: single incoming (fork) -> returns null
  // ========================================================================================

  @Test
  void handleJoinToken_singleIncoming_returnsNull() {
    GatewayNode forkGateway = new GatewayNode("gw-fork", "ParallelFork", NodeType.PARALLEL_GATEWAY);
    Map<String, String> incoming = new HashMap<>();
    incoming.put("flow-1", "task-a");
    forkGateway.setIncomingSequenceFlowIds(incoming);

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("gw-fork", forkGateway);
    nodes.put("task-a", new ServiceTaskNode("task-a", "TaskA"));

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);

    Boolean result = ParallelGatewayExecutor.handleJoinToken(instance, forkGateway, "task-a");

    assertNull(result, "Fork gateway (single incoming) should return null");
  }

  // ========================================================================================
  // execute: split gateway -> proceeds for each outgoing path
  // ========================================================================================

  @Test
  void execute_forkGateway_proceedsForEachOutgoingPath() {
    GatewayNode forkGateway = new GatewayNode("gw-fork", "ParallelFork", NodeType.PARALLEL_GATEWAY);
    // 1 incoming
    Map<String, String> incoming = new HashMap<>();
    incoming.put("flow-in", "start");
    forkGateway.setIncomingSequenceFlowIds(incoming);
    // 3 outgoing
    Map<String, String> outgoing = new HashMap<>();
    outgoing.put("flow-1", "task-a");
    outgoing.put("flow-2", "task-b");
    outgoing.put("flow-3", "task-c");
    forkGateway.setOutgoingSequenceFlowIds(outgoing);

    ServiceTaskNode taskA = new ServiceTaskNode("task-a", "TaskA");
    ServiceTaskNode taskB = new ServiceTaskNode("task-b", "TaskB");
    ServiceTaskNode taskC = new ServiceTaskNode("task-c", "TaskC");
    ServiceTaskNode start = new ServiceTaskNode("start", "Start");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("gw-fork", forkGateway);
    nodes.put("task-a", taskA);
    nodes.put("task-b", taskB);
    nodes.put("task-c", taskC);
    nodes.put("start", start);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);

    executor.execute(instance, forkGateway, context);

    // Should proceed for all 3 outgoing paths
    verify(context, times(3)).proceed(eq(instance), any(BaseNode.class), eq("gw-fork"));
  }

  // ========================================================================================
  // execute: join gateway -> proceeds for single outgoing
  // ========================================================================================

  @Test
  void execute_joinGateway_proceedsForSingleOutgoing() {
    GatewayNode joinGateway = new GatewayNode("gw-join", "ParallelJoin", NodeType.PARALLEL_GATEWAY);
    // 2 incoming
    Map<String, String> incoming = new HashMap<>();
    incoming.put("flow-1", "task-a");
    incoming.put("flow-2", "task-b");
    joinGateway.setIncomingSequenceFlowIds(incoming);
    // 1 outgoing
    Map<String, String> outgoing = new HashMap<>();
    outgoing.put("flow-out", "end");
    joinGateway.setOutgoingSequenceFlowIds(outgoing);

    ServiceTaskNode taskA = new ServiceTaskNode("task-a", "TaskA");
    ServiceTaskNode taskB = new ServiceTaskNode("task-b", "TaskB");
    ServiceTaskNode end = new ServiceTaskNode("end", "End");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("gw-join", joinGateway);
    nodes.put("task-a", taskA);
    nodes.put("task-b", taskB);
    nodes.put("end", end);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);

    executor.execute(instance, joinGateway, context);

    verify(context, times(1)).proceed(eq(instance), eq(end), eq("gw-join"));
  }
}
