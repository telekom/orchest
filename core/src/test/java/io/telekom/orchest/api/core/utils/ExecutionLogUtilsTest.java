package io.telekom.orchest.api.core.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.EventType;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.node.CallActivityNode;
import io.telekom.orchest.api.core.model.bpmn.node.EventNode;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.model.bpmn.state.ExecutionStateKey;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ExecutionLogUtils} covering special start-event detection and execution log
 * creation.
 */
class ExecutionLogUtilsTest {

  @Test
  @DisplayName("isNodeASpecialStartEvent is true for timer/signal/message start events")
  void specialStartEvents_recognized() {
    assertTrue(
        ExecutionLogUtils.isNodeASpecialStartEvent(
            new EventNode("1", "t", NodeType.START_EVENT, EventType.TIMER)));
    assertTrue(
        ExecutionLogUtils.isNodeASpecialStartEvent(
            new EventNode("2", "s", NodeType.START_EVENT, EventType.SIGNAL)));
    assertTrue(
        ExecutionLogUtils.isNodeASpecialStartEvent(
            new EventNode("3", "m", NodeType.START_EVENT, EventType.MESSAGE)));
  }

  @Test
  @DisplayName("isNodeASpecialStartEvent is false for non-start or non-special types")
  void nonSpecialEvents_rejected() {
    assertFalse(
        ExecutionLogUtils.isNodeASpecialStartEvent(
            new EventNode("1", "e", NodeType.END_EVENT, EventType.TIMER)));
    assertFalse(
        ExecutionLogUtils.isNodeASpecialStartEvent(
            new EventNode("2", "s", NodeType.START_EVENT, EventType.NONE)));
    assertFalse(ExecutionLogUtils.isNodeASpecialStartEvent(new ServiceTaskNode("t", "task")));
  }

  @Test
  @DisplayName("addExecutionLog resolves sequence flow from source outgoing mappings")
  void addExecutionLog_sequenceFlowResolved() {
    ServiceTaskNode source = new ServiceTaskNode("src", "source");
    source.getOutgoingSequenceFlowIds().put("flow-1", "tgt");

    CallActivityNode target = new CallActivityNode("tgt", "call");
    ProcessDefinition def =
        ProcessDefinition.builder()
            .nodes(new HashMap<>(Map.of("src", source, "tgt", target)))
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "pd-1", 1);
    instance.setProcessDefinition(def);

    ExecutionLogUtils.addExecutionLog(instance, target, "src", NodeState.COMPLETED);

    assertTrue(instance.getExecutionHistory().containsKey("tgt"));
    assertTrue(instance.getExecutionHistory().get("tgt").getSequenceFlowIds().contains("flow-1"));
  }

  @Test
  @DisplayName(
      "second call activity log merges child metadata (first log entry does not merge metadata)")
  void addExecutionLog_callActivityMetadataOnSecondStateChange() {
    ServiceTaskNode source = new ServiceTaskNode("src", "source");
    source.getOutgoingSequenceFlowIds().put("f1", "call1");

    CallActivityNode call = new CallActivityNode("call1", "call");
    ProcessDefinition def =
        ProcessDefinition.builder()
            .nodes(new HashMap<>(Map.of("src", source, "call1", call)))
            .build();

    ProcessInstance instance = new ProcessInstance("pi-1", "pd-1", 1);
    instance.setProcessDefinition(def);
    instance.putState(new ExecutionStateKey.CallActivityChild("call1"), "child-42");

    ExecutionLogUtils.addExecutionLog(instance, call, "src", NodeState.STARTED);
    ExecutionLogUtils.addExecutionLog(instance, call, "src", NodeState.COMPLETED);

    assertEquals(
        "child-42",
        instance.getExecutionHistory().get("call1").getMetaData().get("childProcessInstanceId"));
  }
}
