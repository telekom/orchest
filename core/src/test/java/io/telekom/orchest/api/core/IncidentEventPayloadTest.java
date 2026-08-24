package io.telekom.orchest.api.core;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.adapters.data.dto.IncidentEventPayload;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link IncidentEventPayload} builder, constructors, and field accessors. */
class IncidentEventPayloadTest {

  @Nested
  class BuilderTests {

    @Test
    void shouldBuildWithAllFields() {
      IncidentEventPayload payload =
          IncidentEventPayload.builder()
              .processDefinitionId("pd-1")
              .processInstanceId("pi-1")
              .correlationId("corr-1")
              .incidentMessage("Task timed out")
              .namespace("production")
              .activityId("act-1")
              .activityName("Process Payment")
              .build();

      assertEquals("pd-1", payload.getProcessDefinitionId());
      assertEquals("pi-1", payload.getProcessInstanceId());
      assertEquals("corr-1", payload.getCorrelationId());
      assertEquals("Task timed out", payload.getIncidentMessage());
      assertEquals("production", payload.getNamespace());
      assertEquals("act-1", payload.getActivityId());
      assertEquals("Process Payment", payload.getActivityName());
    }

    @Test
    void shouldBuildWithNoFields() {
      IncidentEventPayload payload = IncidentEventPayload.builder().build();

      assertNull(payload.getProcessDefinitionId());
      assertNull(payload.getProcessInstanceId());
      assertNull(payload.getCorrelationId());
      assertNull(payload.getIncidentMessage());
      assertNull(payload.getNamespace());
      assertNull(payload.getActivityId());
      assertNull(payload.getActivityName());
    }

    @Test
    void shouldBuildWithPartialFields() {
      IncidentEventPayload payload =
          IncidentEventPayload.builder()
              .processInstanceId("pi-1")
              .incidentMessage("error occurred")
              .build();

      assertEquals("pi-1", payload.getProcessInstanceId());
      assertEquals("error occurred", payload.getIncidentMessage());
      assertNull(payload.getProcessDefinitionId());
      assertNull(payload.getCorrelationId());
      assertNull(payload.getNamespace());
      assertNull(payload.getActivityId());
      assertNull(payload.getActivityName());
    }
  }

  @Nested
  class NoArgConstructor {

    @Test
    void shouldCreateInstanceWithAllFieldsNull() {
      IncidentEventPayload payload = new IncidentEventPayload();

      assertNull(payload.getProcessDefinitionId());
      assertNull(payload.getProcessInstanceId());
      assertNull(payload.getCorrelationId());
      assertNull(payload.getIncidentMessage());
      assertNull(payload.getNamespace());
      assertNull(payload.getActivityId());
      assertNull(payload.getActivityName());
    }
  }

  @Nested
  class AllArgsConstructor {

    @Test
    void shouldSetAllFieldsViaConstructor() {
      IncidentEventPayload payload =
          new IncidentEventPayload(
              "pd-1", 1, "pi-1", "corr-1", "error msg", "staging", "act-1", "My Activity");

      assertEquals("pd-1", payload.getProcessDefinitionId());
      assertEquals(1, payload.getVersion());
      assertEquals("pi-1", payload.getProcessInstanceId());
      assertEquals("corr-1", payload.getCorrelationId());
      assertEquals("error msg", payload.getIncidentMessage());
      assertEquals("staging", payload.getNamespace());
      assertEquals("act-1", payload.getActivityId());
      assertEquals("My Activity", payload.getActivityName());
    }

    @Test
    void shouldAcceptAllNullsInConstructor() {
      IncidentEventPayload payload =
          new IncidentEventPayload(null, null, null, null, null, null, null, null);

      assertNull(payload.getProcessDefinitionId());
      assertNull(payload.getProcessInstanceId());
      assertNull(payload.getCorrelationId());
      assertNull(payload.getIncidentMessage());
      assertNull(payload.getNamespace());
      assertNull(payload.getActivityId());
      assertNull(payload.getActivityName());
    }
  }

  @Nested
  class SettersAndGetters {

    @Test
    void shouldSetAndGetProcessDefinitionId() {
      IncidentEventPayload payload = new IncidentEventPayload();
      payload.setProcessDefinitionId("pd-42");

      assertEquals("pd-42", payload.getProcessDefinitionId());
    }

    @Test
    void shouldSetAndGetProcessInstanceId() {
      IncidentEventPayload payload = new IncidentEventPayload();
      payload.setProcessInstanceId("pi-42");

      assertEquals("pi-42", payload.getProcessInstanceId());
    }

    @Test
    void shouldSetAndGetCorrelationId() {
      IncidentEventPayload payload = new IncidentEventPayload();
      payload.setCorrelationId("order-123");

      assertEquals("order-123", payload.getCorrelationId());
    }

    @Test
    void shouldSetAndGetIncidentMessage() {
      IncidentEventPayload payload = new IncidentEventPayload();
      payload.setIncidentMessage("Connection refused");

      assertEquals("Connection refused", payload.getIncidentMessage());
    }

    @Test
    void shouldSetAndGetNamespace() {
      IncidentEventPayload payload = new IncidentEventPayload();
      payload.setNamespace("production");

      assertEquals("production", payload.getNamespace());
    }

    @Test
    void shouldSetAndGetActivityId() {
      IncidentEventPayload payload = new IncidentEventPayload();
      payload.setActivityId("task-7");

      assertEquals("task-7", payload.getActivityId());
    }

    @Test
    void shouldSetAndGetActivityName() {
      IncidentEventPayload payload = new IncidentEventPayload();
      payload.setActivityName("Send Email");

      assertEquals("Send Email", payload.getActivityName());
    }

    @Test
    void shouldAllowSettingFieldToNull() {
      IncidentEventPayload payload =
          IncidentEventPayload.builder().processInstanceId("pi-1").build();

      payload.setProcessInstanceId(null);

      assertNull(payload.getProcessInstanceId());
    }

    @Test
    void shouldAcceptEmptyStrings() {
      IncidentEventPayload payload =
          IncidentEventPayload.builder()
              .processDefinitionId("")
              .processInstanceId("")
              .incidentMessage("")
              .build();

      assertEquals("", payload.getProcessDefinitionId());
      assertEquals("", payload.getProcessInstanceId());
      assertEquals("", payload.getIncidentMessage());
    }

    @Test
    void shouldAcceptLongIncidentMessage() {
      String longMessage = "Error: ".repeat(1000);
      IncidentEventPayload payload =
          IncidentEventPayload.builder().incidentMessage(longMessage).build();

      assertEquals(longMessage, payload.getIncidentMessage());
    }
  }

  @Nested
  class EqualsAndHashCode {

    @Test
    void shouldBeEqualForIdenticalValues() {
      IncidentEventPayload p1 =
          IncidentEventPayload.builder()
              .processDefinitionId("pd-1")
              .processInstanceId("pi-1")
              .correlationId("corr-1")
              .incidentMessage("msg")
              .namespace("ns")
              .activityId("act-1")
              .activityName("name")
              .build();

      IncidentEventPayload p2 =
          IncidentEventPayload.builder()
              .processDefinitionId("pd-1")
              .processInstanceId("pi-1")
              .correlationId("corr-1")
              .incidentMessage("msg")
              .namespace("ns")
              .activityId("act-1")
              .activityName("name")
              .build();

      assertEquals(p1, p2);
      assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentValues() {
      IncidentEventPayload p1 = IncidentEventPayload.builder().processInstanceId("pi-1").build();
      IncidentEventPayload p2 = IncidentEventPayload.builder().processInstanceId("pi-2").build();

      assertNotEquals(p1, p2);
    }

    @Test
    void shouldNotEqualNull() {
      IncidentEventPayload payload = IncidentEventPayload.builder().build();

      assertNotEquals(null, payload);
    }

    @Test
    void shouldBeEqualWhenBothEmpty() {
      IncidentEventPayload p1 = IncidentEventPayload.builder().build();
      IncidentEventPayload p2 = IncidentEventPayload.builder().build();

      assertEquals(p1, p2);
      assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void shouldBeReflexive() {
      IncidentEventPayload payload =
          IncidentEventPayload.builder().processInstanceId("pi-1").build();

      assertEquals(payload, payload);
    }
  }

  @Nested
  class ToStringTest {

    @Test
    void shouldContainFieldValues() {
      IncidentEventPayload payload =
          IncidentEventPayload.builder()
              .processInstanceId("pi-1")
              .incidentMessage("timeout")
              .build();

      String str = payload.toString();

      assertNotNull(str);
      assertTrue(str.contains("pi-1"));
      assertTrue(str.contains("timeout"));
    }

    @Test
    void shouldNotThrowForEmptyPayload() {
      IncidentEventPayload payload = new IncidentEventPayload();

      assertDoesNotThrow(() -> payload.toString());
    }
  }
}
