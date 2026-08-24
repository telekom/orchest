package io.telekom.orchest.adapter.mongo.model;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.model.bpmn.PIState;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link ProcessInstance} MongoDB document model verifying builder defaults,
 * Persistable isNew logic, field getters/setters, and encrypted variable storage.
 */
class ProcessInstanceTest {

  // ============================================================
  // Persistable<String> - isNew() logic
  // ============================================================

  @Test
  @DisplayName("isNew returns true when id is null (new document)")
  void isNew_idNull_returnsTrue() {
    ProcessInstance instance =
        ProcessInstance.builder().processInstanceId("pi-100").processDefinitionId("pd-200").build();
    // id is null by default when not set

    assertTrue(instance.isNew());
  }

  @Test
  @DisplayName("isNew returns false when id is not null (existing document)")
  void isNew_idNotNull_returnsFalse() {
    ProcessInstance instance =
        ProcessInstance.builder()
            .id("mongo-id-123")
            .processInstanceId("pi-100")
            .processDefinitionId("pd-200")
            .build();

    assertFalse(instance.isNew());
  }

  @Test
  @DisplayName("getId returns the value set as the document id")
  void getId_returnsSetValue() {
    ProcessInstance instance = ProcessInstance.builder().id("doc-id-456").build();

    assertEquals("doc-id-456", instance.getId());
  }

  // ============================================================
  // Builder creates correct defaults
  // ============================================================

  @Test
  @DisplayName("Builder creates instance - collections are null without @Builder.Default")
  void builder_defaultCollections_areNull() {
    ProcessInstance instance =
        ProcessInstance.builder().processInstanceId("pi-100").processDefinitionId("pd-200").build();

    // Lombok @Builder does not use field initializers without @Builder.Default
    assertNull(instance.getVariables());
    assertNull(instance.getActiveNodeIds());
    assertNull(instance.getExecutionHistory());
    assertNull(instance.getExecutionState());
  }

  @Test
  @DisplayName("Builder creates instance with correct default booleans")
  void builder_defaultBooleans_areFalse() {
    ProcessInstance instance = ProcessInstance.builder().processInstanceId("pi-100").build();

    assertFalse(instance.isHasIncident());
    assertFalse(instance.isCompleted());
    assertFalse(instance.isDynamicFlow());
  }

  // ============================================================
  // All fields set and retrieved correctly
  // ============================================================

  @Test
  @DisplayName("All fields can be set via builder and retrieved via getters")
  void allFields_setAndRetrieved() {
    OffsetDateTime now = OffsetDateTime.now();
    Map<String, Object> variables = new HashMap<>();
    variables.put("key1", "value1");
    Set<String> activeNodes = new HashSet<>(Set.of("node-1", "node-2"));
    List<String> correlationIds = List.of("corr-1", "corr-2");

    ProcessInstance instance =
        ProcessInstance.builder()
            .id("doc-id")
            .processInstanceId("pi-500")
            .processDefinitionId("pd-600")
            .version(2)
            .variables(variables)
            .activeNodeIds(activeNodes)
            .hasIncident(true)
            .dynamicFlow(true)
            .incidentMessage("something failed")
            .completed(true)
            .state(PIState.COMPLETED)
            .correlationIds(correlationIds)
            .createdAt(now)
            .completedAt(now)
            .lastModifiedAt(now)
            .build();

    assertEquals("doc-id", instance.getId());
    assertEquals("pi-500", instance.getProcessInstanceId());
    assertEquals("pd-600", instance.getProcessDefinitionId());
    assertEquals(2, instance.getVersion());
    assertEquals("value1", instance.getVariables().get("key1"));
    assertTrue(instance.getActiveNodeIds().contains("node-1"));
    assertTrue(instance.getActiveNodeIds().contains("node-2"));
    assertTrue(instance.isHasIncident());
    assertTrue(instance.isDynamicFlow());
    assertEquals("something failed", instance.getIncidentMessage());
    assertTrue(instance.isCompleted());
    assertEquals(PIState.COMPLETED, instance.getState());
    assertEquals(2, instance.getCorrelationIds().size());
    assertEquals(now, instance.getCreatedAt());
    assertEquals(now, instance.getCompletedAt());
    assertEquals(now, instance.getLastModifiedAt());
  }

  // ============================================================
  // isNew transitions when id changes
  // ============================================================

  @Test
  @DisplayName("isNew transitions from true to false when id is set after construction")
  void isNew_transitionsWhenIdSet() {
    ProcessInstance instance = ProcessInstance.builder().processInstanceId("pi-100").build();

    assertTrue(instance.isNew());

    instance.setId("assigned-id");

    assertFalse(instance.isNew());
  }

  // ============================================================
  // NoArgsConstructor and setters (Lombok @Data)
  // ============================================================

  @Test
  @DisplayName("NoArgsConstructor creates instance with all null fields and isNew returns true")
  void noArgsConstructor_allNull_isNewTrue() {
    ProcessInstance instance = new ProcessInstance();

    assertNull(instance.getId());
    assertNull(instance.getProcessInstanceId());
    assertTrue(instance.isNew());
  }

  @Test
  @DisplayName("Setters work correctly via Lombok @Data")
  void setters_workCorrectly() {
    ProcessInstance instance = new ProcessInstance();
    instance.setId("set-id");
    instance.setProcessInstanceId("pi-set");
    instance.setProcessDefinitionId("pd-set");
    instance.setVersion(7);
    instance.setHasIncident(true);
    instance.setDynamicFlow(true);
    instance.setCompleted(true);

    assertEquals("set-id", instance.getId());
    assertEquals("pi-set", instance.getProcessInstanceId());
    assertEquals("pd-set", instance.getProcessDefinitionId());
    assertEquals(7, instance.getVersion());
    assertTrue(instance.isHasIncident());
    assertTrue(instance.isDynamicFlow());
    assertTrue(instance.isCompleted());
    assertFalse(instance.isNew()); // id is set
  }

  // ============================================================
  // encVariables field
  // ============================================================

  @Test
  @DisplayName("encVariables can be set and retrieved for encrypted variable storage")
  void encVariables_setAndRetrieved() {
    ProcessInstance instance =
        ProcessInstance.builder().encVariables("encrypted-blob-data").build();

    assertEquals("encrypted-blob-data", instance.getEncVariables());
  }

  // ============================================================
  // parentProcessActivity field
  // ============================================================

  @Test
  @DisplayName("parentProcessActivity is null by default")
  void parentProcessActivity_defaultIsNull() {
    ProcessInstance instance = ProcessInstance.builder().build();

    assertNull(instance.getParentProcesActivity());
  }
}
