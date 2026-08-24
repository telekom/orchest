package io.telekom.orchest.api.core;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.ExecutionLogEntry;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ProcessInstance} state management, execution logging, and parallel gateway
 * tokens.
 */
class ProcessInstanceTest {

  private ProcessInstance instance;

  @BeforeEach
  void setUp() {
    instance = new ProcessInstance("pi-1", "pd-1", 1);
  }

  @Nested
  class ConstructorAndRequiredFields {

    @Test
    void shouldSetRequiredFieldsViaConstructor() {
      assertEquals("pi-1", instance.getProcessInstanceId());
      assertEquals("pd-1", instance.getProcessDefinitionId());
      assertEquals(1, instance.getVersion());
    }

    @Test
    void shouldInitializeOptionalFieldsToDefaults() {
      assertNull(instance.getId());
      assertNull(instance.getState());
      assertNull(instance.getCorrelationIds());
      assertNull(instance.getProcessDefinition());
      assertNull(instance.getParentProcesActivity());
      assertNull(instance.getIncidentMessage());
      assertNull(instance.getCreatedAt());
      assertNull(instance.getCompletedAt());
      assertNull(instance.getLastModifiedAt());
      assertFalse(instance.isCompleted());
      assertFalse(instance.isHasIncident());
      assertFalse(instance.isDynamicFlow());
    }

    @Test
    void shouldInitializeCollectionsAsEmpty() {
      assertNotNull(instance.getVariables());
      assertTrue(instance.getVariables().isEmpty());
      assertNotNull(instance.getActiveNodeIds());
      assertTrue(instance.getActiveNodeIds().isEmpty());
      assertNotNull(instance.getExecutionHistory());
      assertTrue(instance.getExecutionHistory().isEmpty());
      assertNotNull(instance.getExecutionState());
      assertTrue(instance.getExecutionState().isEmpty());
    }

    @Test
    void shouldAcceptNullProcessInstanceId() {
      ProcessInstance pi = new ProcessInstance(null, "pd-1", 1);
      assertNull(pi.getProcessInstanceId());
    }

    @Test
    void shouldAcceptNullProcessDefinitionId() {
      ProcessInstance pi = new ProcessInstance("pi-1", null, 1);
      assertNull(pi.getProcessDefinitionId());
    }

    @Test
    void shouldAcceptNullVersion() {
      ProcessInstance pi = new ProcessInstance("pi-1", "pd-1", null);
      assertNull(pi.getVersion());
    }
  }

  @Nested
  class ActiveNodes {

    @Test
    void shouldAddNodeId() {
      instance.addActiveNode("node-1");

      assertTrue(instance.getActiveNodeIds().contains("node-1"));
      assertEquals(1, instance.getActiveNodeIds().size());
    }

    @Test
    void shouldNotDuplicateNodeId() {
      instance.addActiveNode("node-1");
      instance.addActiveNode("node-1");

      assertEquals(1, instance.getActiveNodeIds().size());
    }

    @Test
    void shouldTrackMultipleNodes() {
      instance.addActiveNode("node-1");
      instance.addActiveNode("node-2");
      instance.addActiveNode("node-3");

      assertEquals(3, instance.getActiveNodeIds().size());
      assertTrue(instance.getActiveNodeIds().containsAll(Set.of("node-1", "node-2", "node-3")));
    }

    @Test
    void shouldRemoveExistingNode() {
      instance.addActiveNode("node-1");
      instance.addActiveNode("node-2");

      instance.removeActiveNode("node-1");

      assertFalse(instance.getActiveNodeIds().contains("node-1"));
      assertTrue(instance.getActiveNodeIds().contains("node-2"));
      assertEquals(1, instance.getActiveNodeIds().size());
    }

    @Test
    void shouldNotFailOnRemoveNonExistentNode() {
      instance.addActiveNode("node-1");

      instance.removeActiveNode("non-existent");

      assertEquals(1, instance.getActiveNodeIds().size());
    }

    @Test
    void shouldBeInitiallyEmpty() {
      assertTrue(instance.getActiveNodeIds().isEmpty());
    }

    @Test
    void shouldRemoveAllNodes() {
      instance.addActiveNode("node-1");
      instance.addActiveNode("node-2");

      instance.removeActiveNode("node-1");
      instance.removeActiveNode("node-2");

      assertTrue(instance.getActiveNodeIds().isEmpty());
    }
  }

  @Nested
  class ExecutionLogs {

    @Test
    void shouldCreateEntryForNewNode() {
      instance.addExecutionLog(
          "node-1",
          "Task 1",
          NodeType.SERVICE_TASK,
          "start-1",
          "flow-1",
          NodeState.TRIGGERED,
          null);

      ExecutionLogEntry entry = instance.getExecutionHistory().get("node-1");
      assertNotNull(entry);
      assertEquals("node-1", entry.getNodeId());
      assertEquals("Task 1", entry.getNodeName());
      assertEquals(NodeType.SERVICE_TASK, entry.getNodeType());
      assertEquals("start-1", entry.getSourceNodeId());
      assertEquals(1, entry.getStateChanges().size());
      assertEquals(NodeState.TRIGGERED, entry.getStateChanges().getFirst().getState());
      assertTrue(entry.getSequenceFlowIds().contains("flow-1"));
    }

    @Test
    void shouldAppendStateChangeForExistingNode() {
      instance.addExecutionLog(
          "node-1",
          "Task 1",
          NodeType.SERVICE_TASK,
          "start-1",
          "flow-1",
          NodeState.TRIGGERED,
          null);
      instance.addExecutionLog(
          "node-1", "Task 1", NodeType.SERVICE_TASK, "start-1", null, NodeState.STARTED, null);

      ExecutionLogEntry entry = instance.getExecutionHistory().get("node-1");
      assertEquals(2, entry.getStateChanges().size());
      assertEquals(NodeState.TRIGGERED, entry.getStateChanges().get(0).getState());
      assertEquals(NodeState.STARTED, entry.getStateChanges().get(1).getState());
    }

    @Test
    void shouldAddNewSequenceFlowForExistingNode() {
      instance.addExecutionLog(
          "node-1",
          "Task 1",
          NodeType.SERVICE_TASK,
          "start-1",
          "flow-1",
          NodeState.TRIGGERED,
          null);
      instance.addExecutionLog(
          "node-1", "Task 1", NodeType.SERVICE_TASK, "start-1", "flow-2", NodeState.STARTED, null);

      ExecutionLogEntry entry = instance.getExecutionHistory().get("node-1");
      assertTrue(entry.getSequenceFlowIds().contains("flow-1"));
      assertTrue(entry.getSequenceFlowIds().contains("flow-2"));
    }

    @Test
    void shouldMergeMetaDataForExistingNode() {
      instance.addExecutionLog(
          "node-1",
          "Task 1",
          NodeType.SERVICE_TASK,
          "start-1",
          "flow-1",
          NodeState.TRIGGERED,
          null);

      Map<String, Object> metaData = Map.of("key1", "value1", "key2", 42);
      instance.addExecutionLog(
          "node-1", "Task 1", NodeType.SERVICE_TASK, "start-1", null, NodeState.STARTED, metaData);

      ExecutionLogEntry entry = instance.getExecutionHistory().get("node-1");
      assertEquals("value1", entry.getMetaData().get("key1"));
      assertEquals(42, entry.getMetaData().get("key2"));
    }

    @Test
    void shouldNotAddSequenceFlowWhenNull() {
      instance.addExecutionLog(
          "node-1", "Task 1", NodeType.SERVICE_TASK, "start-1", null, NodeState.TRIGGERED, null);

      ExecutionLogEntry entry = instance.getExecutionHistory().get("node-1");
      assertTrue(entry.getSequenceFlowIds().isEmpty());
    }

    @Test
    void shouldNotMergeNullMetaData() {
      instance.addExecutionLog(
          "node-1",
          "Task 1",
          NodeType.SERVICE_TASK,
          "start-1",
          "flow-1",
          NodeState.TRIGGERED,
          null);
      instance.addExecutionLog(
          "node-1", "Task 1", NodeType.SERVICE_TASK, "start-1", null, NodeState.STARTED, null);

      ExecutionLogEntry entry = instance.getExecutionHistory().get("node-1");
      assertTrue(entry.getMetaData().isEmpty());
    }

    @Test
    void shouldNotMergeEmptyMetaData() {
      instance.addExecutionLog(
          "node-1",
          "Task 1",
          NodeType.SERVICE_TASK,
          "start-1",
          "flow-1",
          NodeState.TRIGGERED,
          null);
      instance.addExecutionLog(
          "node-1", "Task 1", NodeType.SERVICE_TASK, "start-1", null, NodeState.STARTED, Map.of());

      ExecutionLogEntry entry = instance.getExecutionHistory().get("node-1");
      assertTrue(entry.getMetaData().isEmpty());
    }

    @Test
    void shouldStoreEntryByNodeIdViaAddLog() {
      ExecutionLogEntry entry =
          ExecutionLogEntry.builder()
              .nodeId("node-1")
              .nodeName("Task 1")
              .nodeType(NodeType.TASK)
              .build();

      instance.addLog(entry);

      assertSame(entry, instance.getExecutionHistory().get("node-1"));
    }

    @Test
    void shouldBeInitiallyEmpty() {
      assertTrue(instance.getExecutionHistory().isEmpty());
    }

    @Test
    void shouldTrackMultipleNodesIndependently() {
      instance.addExecutionLog(
          "node-1", "Task 1", NodeType.SERVICE_TASK, "start", "flow-1", NodeState.TRIGGERED, null);
      instance.addExecutionLog(
          "node-2", "Task 2", NodeType.USER_TASK, "node-1", "flow-2", NodeState.TRIGGERED, null);

      assertEquals(2, instance.getExecutionHistory().size());
      assertNotNull(instance.getExecutionHistory().get("node-1"));
      assertNotNull(instance.getExecutionHistory().get("node-2"));
      assertEquals(
          NodeType.SERVICE_TASK, instance.getExecutionHistory().get("node-1").getNodeType());
      assertEquals(NodeType.USER_TASK, instance.getExecutionHistory().get("node-2").getNodeType());
    }

    @Test
    void shouldTrackFullLifecycleStateChanges() {
      instance.addExecutionLog(
          "node-1", "Task", NodeType.TASK, "start", "flow-1", NodeState.TRIGGERED, null);
      instance.addExecutionLog(
          "node-1", "Task", NodeType.TASK, "start", null, NodeState.STARTED, null);
      instance.addExecutionLog(
          "node-1", "Task", NodeType.TASK, "start", null, NodeState.COMPLETED, null);

      ExecutionLogEntry entry = instance.getExecutionHistory().get("node-1");
      assertEquals(3, entry.getStateChanges().size());
      assertEquals(NodeState.TRIGGERED, entry.getStateChanges().get(0).getState());
      assertEquals(NodeState.STARTED, entry.getStateChanges().get(1).getState());
      assertEquals(NodeState.COMPLETED, entry.getStateChanges().get(2).getState());
    }
  }

  @Nested
  class StateManagement {

    @Test
    void shouldUpdateState() {
      instance.setState(PIState.RUNNING);

      assertEquals(PIState.RUNNING, instance.getState());
    }

    @Test
    void shouldAllowAllPIStates() {
      for (PIState piState : PIState.values()) {
        instance.setState(piState);
        assertEquals(piState, instance.getState());
      }
    }

    @Test
    void shouldDefaultCompletedToFalse() {
      assertFalse(instance.isCompleted());
    }

    @Test
    void shouldSetCompletedFlag() {
      instance.setCompleted(true);

      assertTrue(instance.isCompleted());
    }

    @Test
    void shouldDefaultHasIncidentToFalse() {
      assertFalse(instance.isHasIncident());
    }

    @Test
    void shouldSetHasIncidentFlag() {
      instance.setHasIncident(true);

      assertTrue(instance.isHasIncident());
    }

    @Test
    void shouldStoreIncidentMessage() {
      instance.setIncidentMessage("Something went wrong");

      assertEquals("Something went wrong", instance.getIncidentMessage());
    }

    @Test
    void shouldAllowNullIncidentMessage() {
      instance.setIncidentMessage("error");
      instance.setIncidentMessage(null);

      assertNull(instance.getIncidentMessage());
    }
  }

  @Nested
  class TypedExecutionState {

    @Test
    void shouldRoundTripPutAndGet() {
      var key = new ExecutionStateKey.ParallelGatewayToken("gw-1");
      instance.putState(key, 5);

      assertEquals(5, instance.getState(key));
    }

    @Test
    void shouldReturnNullForMissingKey() {
      var key = new ExecutionStateKey.ParallelGatewayToken("gw-missing");

      assertNull(instance.getState(key));
    }

    @Test
    void shouldReturnDefaultForMissingKey() {
      var key = new ExecutionStateKey.ParallelGatewayToken("gw-missing");

      assertEquals(0, instance.getState(key, 0));
    }

    @Test
    void shouldReturnStoredValueOverDefault() {
      var key = new ExecutionStateKey.ParallelGatewayToken("gw-1");
      instance.putState(key, 7);

      assertEquals(7, instance.getState(key, 0));
    }

    @Test
    void shouldReturnTrueForExistingKey() {
      var key = new ExecutionStateKey.MultiInstanceLoopSize("node-1");
      instance.putState(key, 10);

      assertTrue(instance.containsState(key));
    }

    @Test
    void shouldReturnFalseForMissingKey() {
      var key = new ExecutionStateKey.MultiInstanceLoopSize("node-1");

      assertFalse(instance.containsState(key));
    }

    @Test
    void shouldRemoveKey() {
      var key = new ExecutionStateKey.ParallelGatewayToken("gw-1");
      instance.putState(key, 3);

      instance.removeState(key);

      assertFalse(instance.containsState(key));
      assertNull(instance.getState(key));
    }

    @Test
    void shouldSupportDifferentKeyTypesCoexisting() {
      var tokenKey = new ExecutionStateKey.ParallelGatewayToken("gw-1");
      var loopKey = new ExecutionStateKey.MultiInstanceLoopSize("node-1");
      var eventKey = new ExecutionStateKey.EventGatewayLinkedId("gw-2");
      var childKey = new ExecutionStateKey.CallActivityChild("ca-1");

      instance.putState(tokenKey, 2);
      instance.putState(loopKey, 5);
      instance.putState(eventKey, "linked-id");
      instance.putState(childKey, "child-pi-1");

      assertEquals(2, instance.getState(tokenKey));
      assertEquals(5, instance.getState(loopKey));
      assertEquals("linked-id", instance.getState(eventKey));
      assertEquals("child-pi-1", instance.getState(childKey));
    }

    @Test
    void shouldOverwriteExistingValue() {
      var key = new ExecutionStateKey.ParallelGatewayToken("gw-1");
      instance.putState(key, 1);
      instance.putState(key, 2);

      assertEquals(2, instance.getState(key));
    }

    @Test
    void shouldSupportMultiInstanceLoopCounter() {
      var key = new ExecutionStateKey.MultiInstanceLoopCounter(0, "node-1");
      instance.putState(key, 42);

      assertEquals(42, instance.getState(key));
      assertEquals("mi:loop:0:node-1", key.toStorageKey());
    }

    @Test
    void shouldSupportEventGatewayCatchEvents() {
      var key = new ExecutionStateKey.EventGatewayCatchEvents("gw-1");
      instance.putState(key, List.of("catch-1", "catch-2"));

      List<String> result = instance.getState(key);
      assertEquals(2, result.size());
    }

    @Test
    void shouldSupportDecisionInstanceKey() {
      var key = new ExecutionStateKey.DecisionInstance("brt-1");
      instance.putState(key, "decision-123");

      assertEquals("decision-123", instance.getState(key));
      assertEquals("decisionInstanceId:brt-1", key.toStorageKey());
    }
  }

  @Nested
  class LegacyExecutionState {

    @Test
    void shouldReturnValueForLegacyAccessor() {
      instance.getExecutionState().put("legacyKey", "legacyValue");

      assertEquals("legacyValue", instance.getExecutionState("legacyKey", "default"));
    }

    @Test
    void shouldReturnDefaultForMissingLegacyKey() {
      assertEquals("fallback", instance.getExecutionState("missing", "fallback"));
    }

    @Test
    void shouldReturnStoredValueWhenTypeMatches() {
      instance.getExecutionState().put("intKey", 42);

      // Due to type erasure, (T) cast succeeds at the method level and returns
      // the raw Object. The ClassCastException only surfaces at the caller's
      // assignment site, outside the try-catch in getExecutionState.
      Integer result = instance.getExecutionState("intKey", 0);
      assertEquals(42, result);
    }

    @Test
    void shouldReturnDefaultForNullValue() {
      instance.getExecutionState().put("nullKey", null);

      assertEquals("default", instance.getExecutionState("nullKey", "default"));
    }
  }

  @Nested
  class ParallelGatewayTokens {

    @Test
    void shouldReturnFalseWhenBelowRequired() {
      assertFalse(instance.addParallelGatewayToken("gw-1", 3));
      assertEquals(1, instance.getParallelGatewayTokenCount("gw-1"));
    }

    @Test
    void shouldReturnTrueWhenReachingRequired() {
      instance.addParallelGatewayToken("gw-1", 2);
      assertTrue(instance.addParallelGatewayToken("gw-1", 2));
    }

    @Test
    void shouldReturnTrueWhenExceedingRequired() {
      instance.addParallelGatewayToken("gw-1", 2);
      instance.addParallelGatewayToken("gw-1", 2);
      assertTrue(instance.addParallelGatewayToken("gw-1", 2));
      assertEquals(3, instance.getParallelGatewayTokenCount("gw-1"));
    }

    @Test
    void shouldReturnZeroForNoTokens() {
      assertEquals(0, instance.getParallelGatewayTokenCount("gw-1"));
    }

    @Test
    void shouldClearCountOnReset() {
      instance.addParallelGatewayToken("gw-1", 3);
      instance.addParallelGatewayToken("gw-1", 3);

      instance.resetParallelGatewayTokens("gw-1");

      assertEquals(0, instance.getParallelGatewayTokenCount("gw-1"));
    }

    @Test
    void shouldTrackMultipleGatewaysIndependently() {
      instance.addParallelGatewayToken("gw-1", 3);
      instance.addParallelGatewayToken("gw-2", 3);
      instance.addParallelGatewayToken("gw-2", 3);

      assertEquals(1, instance.getParallelGatewayTokenCount("gw-1"));
      assertEquals(2, instance.getParallelGatewayTokenCount("gw-2"));
    }

    @Test
    void shouldResetOnlySpecifiedGateway() {
      instance.addParallelGatewayToken("gw-1", 3);
      instance.addParallelGatewayToken("gw-2", 3);

      instance.resetParallelGatewayTokens("gw-1");

      assertEquals(0, instance.getParallelGatewayTokenCount("gw-1"));
      assertEquals(1, instance.getParallelGatewayTokenCount("gw-2"));
    }

    @Test
    void shouldReturnTrueForSingleRequiredToken() {
      assertTrue(instance.addParallelGatewayToken("gw-1", 1));
    }
  }

  @Nested
  class VariablesManipulation {

    @Test
    void shouldBeInitiallyEmpty() {
      assertTrue(instance.getVariables().isEmpty());
    }

    @Test
    void shouldStoreVariablesMap() {
      Map<String, Object> vars = new HashMap<>();
      vars.put("orderId", "12345");
      vars.put("amount", 100);

      instance.setVariables(vars);

      assertEquals("12345", instance.getVariables().get("orderId"));
      assertEquals(100, instance.getVariables().get("amount"));
    }

    @Test
    void shouldAllowDirectModification() {
      instance.getVariables().put("key", "value");

      assertEquals("value", instance.getVariables().get("key"));
    }

    @Test
    void shouldAllowComplexVariableValues() {
      Map<String, Object> nested = Map.of("inner", "data");
      instance.getVariables().put("complex", nested);

      assertEquals(nested, instance.getVariables().get("complex"));
    }
  }

  @Nested
  class CorrelationIds {

    @Test
    void shouldDefaultToNull() {
      assertNull(instance.getCorrelationIds());
    }

    @Test
    void shouldStoreList() {
      instance.setCorrelationIds(List.of("order-1", "public-id-1"));

      assertEquals(2, instance.getCorrelationIds().size());
      assertTrue(instance.getCorrelationIds().contains("order-1"));
      assertTrue(instance.getCorrelationIds().contains("public-id-1"));
    }

    @Test
    void shouldAcceptEmptyList() {
      instance.setCorrelationIds(List.of());

      assertNotNull(instance.getCorrelationIds());
      assertTrue(instance.getCorrelationIds().isEmpty());
    }

    @Test
    void shouldAcceptNull() {
      instance.setCorrelationIds(List.of("c1"));
      instance.setCorrelationIds(null);

      assertNull(instance.getCorrelationIds());
    }
  }

  @Nested
  class Timestamps {

    @Test
    void shouldSetAndGetCreatedAt() {
      OffsetDateTime now = OffsetDateTime.now();
      instance.setCreatedAt(now);

      assertEquals(now, instance.getCreatedAt());
    }

    @Test
    void shouldSetAndGetCompletedAt() {
      OffsetDateTime now = OffsetDateTime.now();
      instance.setCompletedAt(now);

      assertEquals(now, instance.getCompletedAt());
    }

    @Test
    void shouldSetAndGetLastModifiedAt() {
      OffsetDateTime now = OffsetDateTime.now();
      instance.setLastModifiedAt(now);

      assertEquals(now, instance.getLastModifiedAt());
    }
  }

  @Nested
  class DynamicFlowFlag {

    @Test
    void shouldDefaultToFalse() {
      assertFalse(instance.isDynamicFlow());
    }

    @Test
    void shouldSetDynamicFlow() {
      instance.setDynamicFlow(true);

      assertTrue(instance.isDynamicFlow());
    }
  }

  @Nested
  class ToStringTest {

    @Test
    void shouldContainProcessInstanceId() {
      String str = instance.toString();

      assertTrue(str.contains("pi-1"));
    }

    @Test
    void shouldContainProcessDefinitionId() {
      String str = instance.toString();

      assertTrue(str.contains("pd-1"));
    }

    @Test
    void shouldContainCompletedFlag() {
      String str = instance.toString();

      assertTrue(str.contains("completed=false"));
    }

    @Test
    void shouldReflectCompletedTrueAfterSet() {
      instance.setCompleted(true);
      String str = instance.toString();

      assertTrue(str.contains("completed=true"));
    }
  }

  @Nested
  class IdField {

    @Test
    void shouldDefaultIdToNull() {
      assertNull(instance.getId());
    }

    @Test
    void shouldSetId() {
      instance.setId("mongo-id-123");

      assertEquals("mongo-id-123", instance.getId());
    }
  }
}
