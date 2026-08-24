package io.telekom.orchest.api.core;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.request.StateChange;
import io.telekom.orchest.api.core.request.Variables;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link WorkerEventRequest} builder defaults, nested DTOs, and field accessors. */
class WorkerEventRequestTest {

  @Nested
  class BuilderDefaults {

    @Test
    void shouldDefaultRetriesLeftToThree() {
      WorkerEventRequest request = WorkerEventRequest.builder().build();

      assertEquals(3, request.getRetriesLeft());
    }

    @Test
    void shouldDefaultRetriesToThree() {
      WorkerEventRequest request = WorkerEventRequest.builder().build();

      assertEquals(3, request.getRetries());
    }

    @Test
    void shouldDefaultStateChangesToEmptyList() {
      WorkerEventRequest request = WorkerEventRequest.builder().build();

      assertNotNull(request.getStateChanges());
      assertTrue(request.getStateChanges().isEmpty());
    }

    @Test
    void shouldDefaultStateChangesAsMutableList() {
      WorkerEventRequest request = WorkerEventRequest.builder().build();

      request.getStateChanges().add(new StateChange(NodeState.TRIGGERED));

      assertEquals(1, request.getStateChanges().size());
    }

    @Test
    void shouldDefaultAllStringFieldsToNull() {
      WorkerEventRequest request = WorkerEventRequest.builder().build();

      assertNull(request.getProcessInstanceId());
      assertNull(request.getProcessDefinitionId());
      assertNull(request.getActivityId());
      assertNull(request.getActivityName());
      assertNull(request.getType());
      assertNull(request.getErrorCode());
      assertNull(request.getParentProcessInstanceId());
      assertNull(request.getIncidentMessage());
    }

    @Test
    void shouldDefaultObjectFieldsToNull() {
      WorkerEventRequest request = WorkerEventRequest.builder().build();

      assertNull(request.getVersion());
      assertNull(request.getState());
      assertNull(request.getVariables());
      assertNull(request.getNodeInformation());
      assertNull(request.getMessageEvent());
      assertNull(request.getErrorEvent());
      assertNull(request.getRetryBackOff());
    }
  }

  @Nested
  class EventIdField {

    @Test
    void shouldSetEventIdViaBuilder() {
      WorkerEventRequest request = WorkerEventRequest.builder().eventId("evt-123").build();

      assertEquals("evt-123", request.getEventId());
    }

    @Test
    void shouldDefaultEventIdToNull_backwardCompatibility() {
      WorkerEventRequest request = WorkerEventRequest.builder().build();

      assertNull(request.getEventId());
    }

    @Test
    void shouldSetEventIdViaSetter() {
      WorkerEventRequest request = new WorkerEventRequest();
      request.setEventId("evt-456");

      assertEquals("evt-456", request.getEventId());
    }

    @Test
    void shouldAllowNullEventIdViaSetter() {
      WorkerEventRequest request = WorkerEventRequest.builder().eventId("evt-123").build();
      request.setEventId(null);

      assertNull(request.getEventId());
    }

    @Test
    void shouldAcceptEmptyEventId() {
      WorkerEventRequest request = WorkerEventRequest.builder().eventId("").build();

      assertEquals("", request.getEventId());
    }

    @Test
    void shouldAcceptUuidStyleEventId() {
      String uuid = "550e8400-e29b-41d4-a716-446655440000";
      WorkerEventRequest request = WorkerEventRequest.builder().eventId(uuid).build();

      assertEquals(uuid, request.getEventId());
    }
  }

  @Nested
  class BuilderWithAllFields {

    @Test
    void shouldSetAllFieldsViaBuilder() {
      List<StateChange> stateChanges = new ArrayList<>();
      stateChanges.add(new StateChange(NodeState.TRIGGERED));
      Variables variables = Variables.builder().variables(Map.of("key", "value")).build();
      ServiceTaskNode nodeInfo = new ServiceTaskNode("st-1", "Service");

      WorkerEventRequest request =
          WorkerEventRequest.builder()
              .eventId("evt-100")
              .processInstanceId("pi-1")
              .processDefinitionId("pd-1")
              .activityId("act-1")
              .activityName("My Activity")
              .type("SERVICE_TASK")
              .errorCode("ERR_001")
              .parentProcessInstanceId("parent-pi-1")
              .version(1)
              .state(NodeState.STARTED)
              .variables(variables)
              .nodeInformation(nodeInfo)
              .stateChanges(stateChanges)
              .incidentMessage("Some incident")
              .retriesLeft(2)
              .retries(5)
              .retryBackOff(Duration.ofSeconds(30))
              .build();

      assertEquals("evt-100", request.getEventId());
      assertEquals("pi-1", request.getProcessInstanceId());
      assertEquals("pd-1", request.getProcessDefinitionId());
      assertEquals("act-1", request.getActivityId());
      assertEquals("My Activity", request.getActivityName());
      assertEquals("SERVICE_TASK", request.getType());
      assertEquals("ERR_001", request.getErrorCode());
      assertEquals("parent-pi-1", request.getParentProcessInstanceId());
      assertEquals(1, request.getVersion());
      assertEquals(NodeState.STARTED, request.getState());
      assertSame(variables, request.getVariables());
      assertSame(nodeInfo, request.getNodeInformation());
      assertEquals(1, request.getStateChanges().size());
      assertEquals("Some incident", request.getIncidentMessage());
      assertEquals(2, request.getRetriesLeft());
      assertEquals(5, request.getRetries());
      assertEquals(Duration.ofSeconds(30), request.getRetryBackOff());
    }

    @Test
    void shouldOverrideDefaultRetriesValues() {
      WorkerEventRequest request = WorkerEventRequest.builder().retriesLeft(0).retries(10).build();

      assertEquals(0, request.getRetriesLeft());
      assertEquals(10, request.getRetries());
    }

    @Test
    void shouldAcceptZeroDurationRetryBackOff() {
      WorkerEventRequest request = WorkerEventRequest.builder().retryBackOff(Duration.ZERO).build();

      assertEquals(Duration.ZERO, request.getRetryBackOff());
    }

    @Test
    void shouldAcceptNegativeRetriesLeft() {
      WorkerEventRequest request = WorkerEventRequest.builder().retriesLeft(-1).build();

      assertEquals(-1, request.getRetriesLeft());
    }

    @Test
    void shouldAllowAllNodeStates() {
      for (NodeState state : NodeState.values()) {
        WorkerEventRequest request = WorkerEventRequest.builder().state(state).build();

        assertEquals(state, request.getState());
      }
    }
  }

  @Nested
  class ErrorEventInner {

    @Test
    void shouldBuildWithAllFields() {
      WorkerEventRequest.ErrorEvent error =
          WorkerEventRequest.ErrorEvent.builder()
              .errorName("BusinessError")
              .errorCode("BIZ_ERR_01")
              .incidentMessage("Something failed")
              .build();

      assertEquals("BusinessError", error.getErrorName());
      assertEquals("BIZ_ERR_01", error.getErrorCode());
      assertEquals("Something failed", error.getIncidentMessage());
    }

    @Test
    void shouldBuildWithNoFields() {
      WorkerEventRequest.ErrorEvent error = WorkerEventRequest.ErrorEvent.builder().build();

      assertNull(error.getErrorName());
      assertNull(error.getErrorCode());
      assertNull(error.getIncidentMessage());
    }

    @Test
    void shouldSupportNoArgConstructor() {
      WorkerEventRequest.ErrorEvent error = new WorkerEventRequest.ErrorEvent();

      assertNull(error.getErrorName());
    }

    @Test
    void shouldSupportAllArgConstructor() {
      WorkerEventRequest.ErrorEvent error =
          new WorkerEventRequest.ErrorEvent("name", "code", "msg");

      assertEquals("name", error.getErrorName());
      assertEquals("code", error.getErrorCode());
      assertEquals("msg", error.getIncidentMessage());
    }

    @Test
    void shouldSupportSetters() {
      WorkerEventRequest.ErrorEvent error = new WorkerEventRequest.ErrorEvent();
      error.setErrorName("err");
      error.setErrorCode("E001");
      error.setIncidentMessage("incident");

      assertEquals("err", error.getErrorName());
      assertEquals("E001", error.getErrorCode());
      assertEquals("incident", error.getIncidentMessage());
    }

    @Test
    void shouldAttachToRequest() {
      WorkerEventRequest.ErrorEvent error =
          WorkerEventRequest.ErrorEvent.builder().errorName("Error").build();

      WorkerEventRequest request = WorkerEventRequest.builder().errorEvent(error).build();

      assertNotNull(request.getErrorEvent());
      assertSame(error, request.getErrorEvent());
      assertEquals("Error", request.getErrorEvent().getErrorName());
    }

    @Test
    void shouldHaveEqualsAndHashCode() {
      WorkerEventRequest.ErrorEvent e1 =
          WorkerEventRequest.ErrorEvent.builder().errorName("err").errorCode("E1").build();
      WorkerEventRequest.ErrorEvent e2 =
          WorkerEventRequest.ErrorEvent.builder().errorName("err").errorCode("E1").build();

      assertEquals(e1, e2);
      assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void shouldHaveToString() {
      WorkerEventRequest.ErrorEvent error =
          WorkerEventRequest.ErrorEvent.builder().errorName("err").build();

      String str = error.toString();
      assertNotNull(str);
      assertTrue(str.contains("err"));
    }
  }

  @Nested
  class BoundaryMessageEventInner {

    @Test
    void shouldBuildWithAllFields() {
      WorkerEventRequest.BoundaryMessageEvent msg =
          WorkerEventRequest.BoundaryMessageEvent.builder()
              .messageName("OrderReceived")
              .correlation("order-123")
              .build();

      assertEquals("OrderReceived", msg.getMessageName());
      assertEquals("order-123", msg.getCorrelation());
    }

    @Test
    void shouldBuildWithNoFields() {
      WorkerEventRequest.BoundaryMessageEvent msg =
          WorkerEventRequest.BoundaryMessageEvent.builder().build();

      assertNull(msg.getMessageName());
      assertNull(msg.getCorrelation());
    }

    @Test
    void shouldSupportNoArgConstructor() {
      WorkerEventRequest.BoundaryMessageEvent msg = new WorkerEventRequest.BoundaryMessageEvent();

      assertNull(msg.getMessageName());
    }

    @Test
    void shouldSupportAllArgConstructor() {
      WorkerEventRequest.BoundaryMessageEvent msg =
          new WorkerEventRequest.BoundaryMessageEvent("msg", "corr");

      assertEquals("msg", msg.getMessageName());
      assertEquals("corr", msg.getCorrelation());
    }

    @Test
    void shouldAttachToRequest() {
      WorkerEventRequest.BoundaryMessageEvent msg =
          WorkerEventRequest.BoundaryMessageEvent.builder().messageName("Msg").build();

      WorkerEventRequest request = WorkerEventRequest.builder().messageEvent(msg).build();

      assertNotNull(request.getMessageEvent());
      assertSame(msg, request.getMessageEvent());
    }

    @Test
    void shouldHaveEqualsAndHashCode() {
      WorkerEventRequest.BoundaryMessageEvent m1 =
          WorkerEventRequest.BoundaryMessageEvent.builder()
              .messageName("msg")
              .correlation("c")
              .build();
      WorkerEventRequest.BoundaryMessageEvent m2 =
          WorkerEventRequest.BoundaryMessageEvent.builder()
              .messageName("msg")
              .correlation("c")
              .build();

      assertEquals(m1, m2);
      assertEquals(m1.hashCode(), m2.hashCode());
    }
  }

  @Nested
  class NoArgConstructor {

    @Test
    void shouldInitializeFieldDefaults() {
      WorkerEventRequest request = new WorkerEventRequest();

      assertEquals(3, request.getRetriesLeft());
      assertEquals(3, request.getRetries());
    }

    @Test
    void shouldInitializeStateChangesAsEmptyList() {
      WorkerEventRequest request = new WorkerEventRequest();

      assertNotNull(request.getStateChanges());
      assertTrue(request.getStateChanges().isEmpty());
    }

    @Test
    void shouldAllowSettingFieldsViaSetter() {
      WorkerEventRequest request = new WorkerEventRequest();
      request.setProcessInstanceId("pi-1");
      request.setProcessDefinitionId("pd-1");
      request.setType("SERVICE_TASK");
      request.setState(NodeState.TRIGGERED);
      request.setRetriesLeft(5);
      request.setRetries(5);

      assertEquals("pi-1", request.getProcessInstanceId());
      assertEquals("pd-1", request.getProcessDefinitionId());
      assertEquals("SERVICE_TASK", request.getType());
      assertEquals(NodeState.TRIGGERED, request.getState());
      assertEquals(5, request.getRetriesLeft());
      assertEquals(5, request.getRetries());
    }
  }

  @Nested
  class EqualsAndHashCode {

    @Test
    void identicalBuilderObjectsShouldBeEqual() {
      WorkerEventRequest r1 =
          WorkerEventRequest.builder().eventId("e1").processInstanceId("pi").build();
      WorkerEventRequest r2 =
          WorkerEventRequest.builder().eventId("e1").processInstanceId("pi").build();

      assertEquals(r1, r2);
      assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void differentEventIdsShouldNotBeEqual() {
      WorkerEventRequest r1 = WorkerEventRequest.builder().eventId("e1").build();
      WorkerEventRequest r2 = WorkerEventRequest.builder().eventId("e2").build();

      assertNotEquals(r1, r2);
    }

    @Test
    void shouldNotEqualNull() {
      WorkerEventRequest r1 = WorkerEventRequest.builder().build();

      assertNotEquals(null, r1);
    }
  }

  @Nested
  class ToStringTest {

    @Test
    void shouldContainFieldValues() {
      WorkerEventRequest request =
          WorkerEventRequest.builder().eventId("evt-1").processInstanceId("pi-1").build();

      String str = request.toString();
      assertNotNull(str);
      assertTrue(str.contains("evt-1"));
      assertTrue(str.contains("pi-1"));
    }
  }

  @Nested
  class StateChangesList {

    @Test
    void shouldAcceptCustomStateChangesList() {
      List<StateChange> changes = new ArrayList<>();
      changes.add(new StateChange(NodeState.TRIGGERED));
      changes.add(new StateChange(NodeState.STARTED));
      changes.add(new StateChange(NodeState.COMPLETED));

      WorkerEventRequest request = WorkerEventRequest.builder().stateChanges(changes).build();

      assertEquals(3, request.getStateChanges().size());
      assertEquals(NodeState.TRIGGERED, request.getStateChanges().get(0).getState());
      assertEquals(NodeState.COMPLETED, request.getStateChanges().get(2).getState());
    }

    @Test
    void shouldAllowMutatingBuilderDefaultList() {
      WorkerEventRequest request = WorkerEventRequest.builder().build();
      request.getStateChanges().add(new StateChange(NodeState.TRIGGERED));
      request.getStateChanges().add(new StateChange(NodeState.STARTED));

      assertEquals(2, request.getStateChanges().size());
    }
  }
}
