package io.telekom.orchest.api.core;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.request.StateChange;
import io.telekom.orchest.api.core.utils.StateChangeUtils;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link StateChangeUtils} factory methods that build state-change records. */
class StateChangeUtilsTest {

  @Nested
  class BuildStateChangesWithState {

    @Test
    void shouldSetTriggeredState() {
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.TRIGGERED);

      assertEquals(NodeState.TRIGGERED, change.getState());
      assertNotNull(change.getTimestamp());
    }

    @Test
    void shouldSetStartedState() {
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.STARTED);

      assertEquals(NodeState.STARTED, change.getState());
    }

    @Test
    void shouldSetCompletedState() {
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.COMPLETED);

      assertEquals(NodeState.COMPLETED, change.getState());
    }

    @Test
    void shouldSetIncidentState() {
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.INCIDENT);

      assertEquals(NodeState.INCIDENT, change.getState());
    }

    @Test
    void shouldSetFailedState() {
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.FAILED);

      assertEquals(NodeState.FAILED, change.getState());
    }

    @Test
    void shouldSetCancelledState() {
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.CANCELLED);

      assertEquals(NodeState.CANCELLED, change.getState());
    }

    @Test
    void shouldSetPendingState() {
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.PENDING);

      assertEquals(NodeState.PENDING, change.getState());
    }

    @Test
    void shouldSetRegisteredState() {
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.REGISTERED);

      assertEquals(NodeState.REGISTERED, change.getState());
    }

    @Test
    void shouldWorkForAllNodeStates() {
      for (NodeState state : NodeState.values()) {
        StateChange change = StateChangeUtils.buildStateChanges(state);

        assertEquals(state, change.getState());
        assertNotNull(change.getTimestamp());
      }
    }

    @Test
    void shouldSetTimestampAutomatically() {
      OffsetDateTime before = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(1);
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.STARTED);
      OffsetDateTime after = OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(1);

      assertNotNull(change.getTimestamp());
      assertTrue(change.getTimestamp().isAfter(before) || change.getTimestamp().isEqual(before));
      assertTrue(change.getTimestamp().isBefore(after) || change.getTimestamp().isEqual(after));
    }

    @Test
    void shouldReturnNewInstanceEachCall() {
      StateChange first = StateChangeUtils.buildStateChanges(NodeState.TRIGGERED);
      StateChange second = StateChangeUtils.buildStateChanges(NodeState.TRIGGERED);

      assertNotSame(first, second);
    }
  }

  @Nested
  class BuildStateChangesWithStateAndTimestamp {

    @Test
    void shouldSetBothStateAndTimestamp() {
      OffsetDateTime start = OffsetDateTime.of(2025, 6, 15, 10, 30, 0, 0, ZoneOffset.UTC);
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.STARTED, start);

      assertEquals(NodeState.STARTED, change.getState());
      assertEquals(start, change.getTimestamp());
    }

    @Test
    void shouldPreserveExactTimestamp() {
      OffsetDateTime customTime = OffsetDateTime.now().minusHours(1);
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.COMPLETED, customTime);

      assertEquals(customTime, change.getTimestamp());
    }

    @Test
    void shouldAcceptPastTimestamp() {
      OffsetDateTime past = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.TRIGGERED, past);

      assertEquals(past, change.getTimestamp());
    }

    @Test
    void shouldAcceptFutureTimestamp() {
      OffsetDateTime future = OffsetDateTime.now().plusDays(30);
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.PENDING, future);

      assertEquals(future, change.getTimestamp());
    }

    @Test
    void shouldAcceptNullTimestamp() {
      StateChange change = StateChangeUtils.buildStateChanges(NodeState.STARTED, null);

      assertEquals(NodeState.STARTED, change.getState());
      assertNull(change.getTimestamp());
    }
  }

  @Nested
  class GetAllStates {

    @Test
    void shouldReturnTriggeredStartedCompleted() {
      List<StateChange> states = StateChangeUtils.getAllStates();

      assertEquals(3, states.size());
      assertEquals(NodeState.TRIGGERED, states.get(0).getState());
      assertEquals(NodeState.STARTED, states.get(1).getState());
      assertEquals(NodeState.COMPLETED, states.get(2).getState());
    }

    @Test
    void shouldReturnNewListEachCall() {
      List<StateChange> first = StateChangeUtils.getAllStates();
      List<StateChange> second = StateChangeUtils.getAllStates();

      assertNotSame(first, second);
    }

    @Test
    void shouldReturnMutableList() {
      List<StateChange> states = StateChangeUtils.getAllStates();

      states.add(StateChangeUtils.buildStateChanges(NodeState.INCIDENT));

      assertEquals(4, states.size());
    }

    @Test
    void shouldHaveTimestampsOnAllEntries() {
      List<StateChange> states = StateChangeUtils.getAllStates();

      for (StateChange change : states) {
        assertNotNull(change.getTimestamp());
      }
    }

    @Test
    void shouldReturnExactlyThreeElements() {
      List<StateChange> states = StateChangeUtils.getAllStates();

      assertEquals(3, states.size());
    }

    @Test
    void shouldReturnNewStateChangeInstancesEachCall() {
      List<StateChange> first = StateChangeUtils.getAllStates();
      List<StateChange> second = StateChangeUtils.getAllStates();

      // The individual StateChange objects should also be different instances
      assertNotSame(first.get(0), second.get(0));
    }
  }

  @Nested
  class UtilityClassDesign {

    @Test
    void shouldReturnStateChangeTypeFromBuild() {
      Object result = StateChangeUtils.buildStateChanges(NodeState.TRIGGERED);

      assertInstanceOf(StateChange.class, result);
    }
  }
}
