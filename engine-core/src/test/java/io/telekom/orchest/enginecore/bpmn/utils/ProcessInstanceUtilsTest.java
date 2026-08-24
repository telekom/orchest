package io.telekom.orchest.enginecore.bpmn.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests ProcessInstanceUtils helper methods: wait state detection and incident checking. */
class ProcessInstanceUtilsTest {

  // ========================================================================================
  // checkIfInstanceHasIncident
  // ========================================================================================

  @Test
  void checkIfInstanceHasIncident_returnsTrue_whenHasIncident() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setHasIncident(true);
    instance.setIncidentMessage("Something failed");

    boolean result = ProcessInstanceUtils.checkIfInstanceHasIncident(instance);

    assertTrue(result);
  }

  @Test
  void checkIfInstanceHasIncident_returnsFalse_whenNoIncident() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setHasIncident(false);

    boolean result = ProcessInstanceUtils.checkIfInstanceHasIncident(instance);

    assertFalse(result);
  }

  @Test
  void checkIfInstanceHasIncident_returnsFalse_forDefaultInstance() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    // hasIncident defaults to false in ProcessInstance constructor

    assertFalse(ProcessInstanceUtils.checkIfInstanceHasIncident(instance));
  }

  @Test
  void checkIfInstanceHasIncident_returnsTrue_evenWithNullIncidentMessage() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setHasIncident(true);
    instance.setIncidentMessage(null);

    assertTrue(ProcessInstanceUtils.checkIfInstanceHasIncident(instance));
  }

  @Test
  void checkIfInstanceHasIncident_returnsTrue_withIncidentStateAndFlag() {
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setHasIncident(true);
    instance.setState(PIState.INCIDENT);
    instance.setIncidentMessage("Failure during processing");

    assertTrue(ProcessInstanceUtils.checkIfInstanceHasIncident(instance));
  }

  @Test
  void checkIfInstanceHasIncident_returnsFalse_whenStateIsIncidentButFlagIsFalse() {
    // The method checks only hasIncident flag, not state
    ProcessInstance instance = new ProcessInstance("pi-1", "def-1", 1);
    instance.setHasIncident(false);
    instance.setState(PIState.INCIDENT);

    assertFalse(ProcessInstanceUtils.checkIfInstanceHasIncident(instance));
  }

  // ========================================================================================
  // isWaitState: wait-state node types
  // ========================================================================================

  @Test
  void isWaitState_returnsTrue_forUserTask() {
    assertTrue(ProcessInstanceUtils.isWaitState(NodeType.USER_TASK));
  }

  @Test
  void isWaitState_returnsTrue_forReceiveTask() {
    assertTrue(ProcessInstanceUtils.isWaitState(NodeType.RECEIVE_TASK));
  }

  @Test
  void isWaitState_returnsTrue_forEventBasedGateway() {
    assertTrue(ProcessInstanceUtils.isWaitState(NodeType.EVENT_BASED_GATEWAY));
  }

  @Test
  void isWaitState_returnsTrue_forServiceTask() {
    assertTrue(ProcessInstanceUtils.isWaitState(NodeType.SERVICE_TASK));
  }

  @Test
  void isWaitState_returnsTrue_forCallActivity() {
    assertTrue(ProcessInstanceUtils.isWaitState(NodeType.CALL_ACTIVITY));
  }

  @Test
  void isWaitState_returnsTrue_forSubProcess() {
    assertTrue(ProcessInstanceUtils.isWaitState(NodeType.SUB_PROCESS));
  }

  // ========================================================================================
  // isWaitState: non-wait-state node types
  // ========================================================================================

  @Test
  void isWaitState_returnsFalse_forStartEvent() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.START_EVENT));
  }

  @Test
  void isWaitState_returnsFalse_forEndEvent() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.END_EVENT));
  }

  @Test
  void isWaitState_returnsFalse_forExclusiveGateway() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.EXCLUSIVE_GATEWAY));
  }

  @Test
  void isWaitState_returnsFalse_forParallelGateway() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.PARALLEL_GATEWAY));
  }

  @Test
  void isWaitState_returnsFalse_forTask() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.TASK));
  }

  @Test
  void isWaitState_returnsFalse_forSendTask() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.SEND_TASK));
  }

  @Test
  void isWaitState_returnsFalse_forScriptTask() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.SCRIPT_TASK));
  }

  @Test
  void isWaitState_returnsFalse_forBusinessRuleTask() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.BUSINESS_RULE_TASK));
  }

  @Test
  void isWaitState_returnsFalse_forManualTask() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.MANUAL_TASK));
  }

  @Test
  void isWaitState_returnsFalse_forBoundaryEvent() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.BOUNDARY_EVENT));
  }

  @Test
  void isWaitState_returnsFalse_forIntermediateCatchEvent() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.INTERMEDIATE_CATCH_EVENT));
  }

  @Test
  void isWaitState_returnsFalse_forIntermediateThrowEvent() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.INTERMEDIATE_THROW_EVENT));
  }

  @Test
  void isWaitState_returnsFalse_forInclusiveGateway() {
    assertFalse(ProcessInstanceUtils.isWaitState(NodeType.INCLUSIVE_GATEWAY));
  }

  // ========================================================================================
  // isWaitState: parameterized tests for exhaustive coverage
  // ========================================================================================

  @ParameterizedTest
  @EnumSource(
      value = NodeType.class,
      names = {
        "USER_TASK", "RECEIVE_TASK", "EVENT_BASED_GATEWAY",
        "SERVICE_TASK", "CALL_ACTIVITY", "SUB_PROCESS"
      })
  void isWaitState_allWaitStateTypes_returnTrue(NodeType type) {
    assertTrue(ProcessInstanceUtils.isWaitState(type));
  }

  @ParameterizedTest
  @EnumSource(
      value = NodeType.class,
      names = {
        "USER_TASK", "RECEIVE_TASK", "EVENT_BASED_GATEWAY",
        "SERVICE_TASK", "CALL_ACTIVITY", "SUB_PROCESS"
      },
      mode = EnumSource.Mode.EXCLUDE)
  void isWaitState_allNonWaitStateTypes_returnFalse(NodeType type) {
    assertFalse(ProcessInstanceUtils.isWaitState(type));
  }
}
