package io.telekom.orchest.client.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.client.annotations.ActivatedJob;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link WorkerHelper} utility methods: job building and log context map creation. */
class WorkerHelperTest {

  // ============================================================
  // buildActivatedJob
  // ============================================================

  @Test
  @DisplayName("buildActivatedJob maps all fields correctly from WorkerEventRequest")
  void buildActivatedJob_mapsAllFieldsCorrectly() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Process Order");
    serviceTask.setWorkerType("orderWorker");

    Map<String, Object> vars = new HashMap<>();
    vars.put("orderId", "ORD-001");
    vars.put("amount", 42.5);

    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-100")
            .processDefinitionId("pd-200")
            .version(3)
            .nodeInformation(serviceTask)
            .variables(new Variables(vars, Variables.VariableAction.UPDATE))
            .retriesLeft(2)
            .build();

    ActivatedJob job = WorkerHelper.buildActivatedJob(event);

    assertEquals("orderWorker", job.getType());
    assertEquals("pi-100", job.getProcessInstanceKey());
    assertEquals("pd-200", job.getBpmnProcessId());
    assertEquals(3, job.getProcessDefinitionVersion());
    assertEquals(2, job.getRetries());
    assertNotNull(job.getVariablesMap());
    assertEquals("ORD-001", job.getVariablesMap().get("orderId"));
    assertEquals(42.5, job.getVariablesMap().get("amount"));
  }

  @Test
  @DisplayName("buildActivatedJob with null version throws NullPointerException on int unboxing")
  void buildActivatedJob_nullVersion_throwsNPE() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-2", "Task");
    serviceTask.setWorkerType("worker");

    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-101")
            .processDefinitionId("pd-201")
            .version(null)
            .nodeInformation(serviceTask)
            .variables(new Variables(new HashMap<>(), Variables.VariableAction.UPDATE))
            .retriesLeft(0)
            .build();

    // Null Integer unboxed to int causes NullPointerException in the builder
    assertThrows(NullPointerException.class, () -> WorkerHelper.buildActivatedJob(event));
  }

  // ============================================================
  // getLogContextMap
  // ============================================================

  @Test
  @DisplayName("getLogContextMap includes worker, processId, processInstanceKey")
  void getLogContextMap_includesBasicFields() {
    ActivatedJob job = createMockActivatedJob();

    Map<String, String> logContext = WorkerHelper.getLogContextMap(job, false);

    assertEquals("testWorker", logContext.get("worker"));
    assertEquals("pd-300", logContext.get("processId"));
    assertEquals("pi-300", logContext.get("processInstanceKey"));
    assertFalse(logContext.containsKey("variables"));
  }

  @Test
  @DisplayName("getLogContextMap with logVariables=true includes variables")
  void getLogContextMap_logVariablesTrue_includesVariables() {
    ActivatedJob job = createMockActivatedJob();

    Map<String, String> logContext = WorkerHelper.getLogContextMap(job, true);

    assertEquals("testWorker", logContext.get("worker"));
    assertTrue(logContext.containsKey("variables"));
    assertNotNull(logContext.get("variables"));
  }

  // ============================================================
  // getLogVariablesMap
  // ============================================================

  @Test
  @DisplayName("getLogVariablesMap extracts matching variable keys")
  void getLogVariablesMap_extractsMatchingKeys() {
    ActivatedJob job = createMockActivatedJob();

    Map<String, String> logVariables = new HashMap<>();
    logVariables.put("orderId", "Order ID");
    logVariables.put("customerId", "Customer ID"); // not in variables

    Map<String, String> result = WorkerHelper.getLogVariablesMap(job, logVariables);

    assertEquals(1, result.size());
    assertEquals("ORD-001", result.get("orderId"));
    assertFalse(result.containsKey("customerId"));
  }

  @Test
  @DisplayName("getLogVariablesMap returns empty map when no matching keys")
  void getLogVariablesMap_noMatchingKeys_returnsEmptyMap() {
    ActivatedJob job = createMockActivatedJob();

    Map<String, String> logVariables = Map.of("nonExistent", "label");

    Map<String, String> result = WorkerHelper.getLogVariablesMap(job, logVariables);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("getLogVariablesMap returns empty map when logVariables is empty")
  void getLogVariablesMap_emptyLogVariables_returnsEmptyMap() {
    ActivatedJob job = createMockActivatedJob();

    Map<String, String> result = WorkerHelper.getLogVariablesMap(job, Map.of());

    assertTrue(result.isEmpty());
  }

  // ============================================================
  // Helper
  // ============================================================

  private ActivatedJob createMockActivatedJob() {
    ServiceTaskNode serviceTask = new ServiceTaskNode("task-1", "Test Task");
    serviceTask.setWorkerType("testWorker");

    Map<String, Object> vars = new HashMap<>();
    vars.put("orderId", "ORD-001");
    vars.put("amount", 99);

    WorkerEventRequest event =
        WorkerEventRequest.builder()
            .processInstanceId("pi-300")
            .processDefinitionId("pd-300")
            .version(1)
            .nodeInformation(serviceTask)
            .variables(new Variables(vars, Variables.VariableAction.UPDATE))
            .retriesLeft(3)
            .build();

    return WorkerHelper.buildActivatedJob(event);
  }
}
