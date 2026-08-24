package io.telekom.orchest.enginecore.bpmn.execution.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.SequenceFlow;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.GatewayNode;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.feel.ConditionEvaluator;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests exclusive gateway condition evaluation and default path selection. */
@ExtendWith(MockitoExtension.class)
class ExclusiveGatewayExecutorTest {

  @Mock private ConditionEvaluator conditionEvaluator;

  @Mock private ExecutionContext context;

  private ExclusiveGatewayExecutor executor;

  @BeforeEach
  void setUp() {
    executor = new ExclusiveGatewayExecutor(conditionEvaluator);
  }

  // ========================================================================================
  // evaluates conditions and takes first matching path
  // ========================================================================================

  @Test
  void execute_removesGatewayFromActiveBeforeProceed_soSubProcessCompletionCanRun() {
    GatewayNode gateway = new GatewayNode("gw-1", "XOR", NodeType.EXCLUSIVE_GATEWAY);

    ServiceTaskNode taskA = new ServiceTaskNode("task-a", "TaskA");
    ServiceTaskNode taskB = new ServiceTaskNode("task-b", "TaskB");

    Map<String, String> outgoing = new HashMap<>();
    outgoing.put("flow-a", "task-a");
    outgoing.put("flow-b", "task-b");
    gateway.setOutgoingSequenceFlowIds(outgoing);

    gateway.setCondition("task-a", "= true");
    gateway.setCondition("task-b", "= false");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("gw-1", gateway);
    nodes.put("task-a", taskA);
    nodes.put("task-b", taskB);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);
    instance.addActiveNode("gw-1");

    when(conditionEvaluator.evaluate(eq("= true"), eq(instance))).thenReturn(true);

    executor.execute(instance, gateway, context);

    assertFalse(
        instance.getActiveNodeIds().contains("gw-1"),
        "Gateway must not stay active during nested proceed(); subprocess end-event completion relies on this");
    verify(context).proceed(instance, taskA, "gw-1");
  }

  @Test
  void execute_takesFirstMatchingConditionPath() {
    GatewayNode gateway = new GatewayNode("gw-1", "XOR", NodeType.EXCLUSIVE_GATEWAY);

    ServiceTaskNode taskA = new ServiceTaskNode("task-a", "TaskA");
    ServiceTaskNode taskB = new ServiceTaskNode("task-b", "TaskB");

    // Outgoing flows
    Map<String, String> outgoing = new HashMap<>();
    outgoing.put("flow-a", "task-a");
    outgoing.put("flow-b", "task-b");
    gateway.setOutgoingSequenceFlowIds(outgoing);

    // Conditions: task-a has a condition that matches, task-b has one that does not
    gateway.setCondition("task-a", "= amount > 100");
    gateway.setCondition("task-b", "= amount <= 100");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("gw-1", gateway);
    nodes.put("task-a", taskA);
    nodes.put("task-b", taskB);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);

    // First condition matches
    when(conditionEvaluator.evaluate(eq("= amount > 100"), eq(instance))).thenReturn(true);

    executor.execute(instance, gateway, context);

    verify(context).proceed(instance, taskA, "gw-1");
    // Should only proceed to the first matching path (exclusive)
    verify(context, times(1)).proceed(eq(instance), any(BaseNode.class), eq("gw-1"));
  }

  // ========================================================================================
  // takes default path when no conditions match
  // ========================================================================================

  @Test
  void execute_takesDefaultPath_whenNoConditionsMatch() {
    GatewayNode gateway = new GatewayNode("gw-1", "XOR", NodeType.EXCLUSIVE_GATEWAY);

    ServiceTaskNode taskA = new ServiceTaskNode("task-a", "TaskA");
    ServiceTaskNode taskB = new ServiceTaskNode("task-b", "TaskB");
    ServiceTaskNode defaultTask = new ServiceTaskNode("task-default", "DefaultTask");

    // Outgoing flows
    Map<String, String> outgoing = new HashMap<>();
    outgoing.put("flow-a", "task-a");
    outgoing.put("flow-b", "task-b");
    outgoing.put("flow-default", "task-default");
    gateway.setOutgoingSequenceFlowIds(outgoing);

    // Conditions on A and B only; default path has no condition
    gateway.setCondition("task-a", "= x > 10");
    gateway.setCondition("task-b", "= x < 0");
    // Set the default node to the sequence flow ID
    gateway.setDefaultNode("flow-default");

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("gw-1", gateway);
    nodes.put("task-a", taskA);
    nodes.put("task-b", taskB);
    nodes.put("task-default", defaultTask);

    Map<String, SequenceFlow> sequenceFlows = new HashMap<>();
    sequenceFlows.put("flow-default", new SequenceFlow("flow-default", "gw-1", "task-default"));
    sequenceFlows.put("flow-a", new SequenceFlow("flow-a", "gw-1", "task-a"));
    sequenceFlows.put("flow-b", new SequenceFlow("flow-b", "gw-1", "task-b"));

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(sequenceFlows)
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);

    // No conditions match
    when(conditionEvaluator.evaluate(anyString(), eq(instance))).thenReturn(false);

    executor.execute(instance, gateway, context);

    verify(context).proceed(instance, defaultTask, "gw-1");
  }

  // ========================================================================================
  // converging/merge gateway: single outgoing, no conditions
  // ========================================================================================

  @Test
  void execute_convergingGateway_proceedsToSingleOutgoing() {
    GatewayNode gateway = new GatewayNode("gw-merge", "XOR-Merge", NodeType.EXCLUSIVE_GATEWAY);

    ServiceTaskNode nextTask = new ServiceTaskNode("task-next", "NextTask");

    // Single outgoing flow (converging gateway)
    Map<String, String> outgoing = new HashMap<>();
    outgoing.put("flow-out", "task-next");
    gateway.setOutgoingSequenceFlowIds(outgoing);

    // No conditions set on the gateway (merge/converge scenario)

    Map<String, BaseNode> nodes = new HashMap<>();
    nodes.put("gw-merge", gateway);
    nodes.put("task-next", nextTask);

    ProcessDefinition definition =
        ProcessDefinition.builder()
            .definitionId("def-1")
            .nodes(nodes)
            .sequenceFlows(new HashMap<>())
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setProcessDefinition(definition);

    executor.execute(instance, gateway, context);

    verify(context).proceed(instance, nextTask, "gw-merge");
  }

  // ========================================================================================
  // throws exception for non-GatewayNode
  // ========================================================================================

  @Test
  void execute_throwsIllegalArgumentException_forNonGatewayNode() {
    ServiceTaskNode nonGateway = new ServiceTaskNode("task-1", "NotAGateway");
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);

    assertThrows(
        IllegalArgumentException.class, () -> executor.execute(instance, nonGateway, context));
  }
}
