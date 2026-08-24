package io.telekom.orchest.orchestrest.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.api.request.VariablesRequest;
import io.telekom.orchest.orchestrest.api.request.VariablesResponse;
import io.telekom.orchest.telemetry.OrchestRestTelemetryService;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link io.telekom.orchest.orchestrest.service.VariablesAPIService}. */
@ExtendWith(MockitoExtension.class)
class VariablesAPIServiceTest {

  @Mock private DataInteractionService dataInteractionService;

  @Mock private OrchestRestTelemetryService orchestRestTelemetryService;

  @InjectMocks private VariablesAPIService service;

  private ProcessInstance createProcessInstance(
      String piId, PIState state, Map<String, Object> variables) {
    ProcessInstance pi = new ProcessInstance(piId, "proc-def", 1);
    pi.setState(state);
    pi.setCreatedAt(OffsetDateTime.now());
    pi.setVariables(variables);
    return pi;
  }

  @Nested
  @DisplayName("getVariables")
  class GetVariables {

    @Test
    @DisplayName("should return variables when process instance exists")
    void getVariables_found() {
      Map<String, Object> variables = new HashMap<>(Map.of("key1", "value1", "key2", 42));
      ProcessInstance instance = createProcessInstance("pi-123", PIState.RUNNING, variables);

      when(dataInteractionService.getProcessInstance("pi-123")).thenReturn(Optional.of(instance));

      VariablesResponse result = service.getVariables("pi-123");

      assertThat(result.getVariables()).containsEntry("key1", "value1");
      assertThat(result.getVariables()).containsEntry("key2", 42);
    }

    @Test
    @DisplayName("should throw RestExceptions 404 when process instance not found")
    void getVariables_notFound() {
      when(dataInteractionService.getProcessInstance("nonexistent")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getVariables("nonexistent"))
          .isInstanceOf(RestExceptions.class)
          .hasMessageContaining("ProcessInstance not found")
          .extracting("code")
          .isEqualTo(404);
    }
  }

  @Nested
  @DisplayName("modifyVariables")
  class ModifyVariables {

    @Test
    @DisplayName("should add/update variables with UPDATE action")
    void modifyVariables_update() {
      Map<String, Object> currentVars = new HashMap<>(Map.of("existing", "old"));
      ProcessInstance instance = createProcessInstance("pi-123", PIState.RUNNING, currentVars);

      when(dataInteractionService.getProcessInstance("pi-123")).thenReturn(Optional.of(instance));

      VariablesRequest request = new VariablesRequest();
      request.setProcessInstanceId("pi-123");
      request.setAction(VariablesRequest.Action.UPDATE);
      request.setVariables(Map.of("newKey", "newValue", "existing", "updated"));

      VariablesResponse result = service.modifyVariables(request);

      assertThat(result.getVariables()).containsEntry("newKey", "newValue");
      assertThat(result.getVariables()).containsEntry("existing", "updated");
      verify(dataInteractionService).saveProcessInstance(instance);
      verify(orchestRestTelemetryService).incrementVariablesUpdateCounter();
    }

    @Test
    @DisplayName("should remove variables with DELETE action")
    void modifyVariables_delete() {
      Map<String, Object> currentVars = new HashMap<>(Map.of("keep", "yes", "remove", "no"));
      ProcessInstance instance = createProcessInstance("pi-123", PIState.RUNNING, currentVars);

      when(dataInteractionService.getProcessInstance("pi-123")).thenReturn(Optional.of(instance));

      VariablesRequest request = new VariablesRequest();
      request.setProcessInstanceId("pi-123");
      request.setAction(VariablesRequest.Action.DELETE);
      request.setVariables(Map.of("remove", ""));

      VariablesResponse result = service.modifyVariables(request);

      assertThat(result.getVariables()).containsEntry("keep", "yes");
      assertThat(result.getVariables()).doesNotContainKey("remove");
      verify(dataInteractionService).saveProcessInstance(instance);
    }

    @Test
    @DisplayName("should throw RestExceptions 404 when process instance not found")
    void modifyVariables_notFound() {
      when(dataInteractionService.getProcessInstance("nonexistent")).thenReturn(Optional.empty());

      VariablesRequest request = new VariablesRequest();
      request.setProcessInstanceId("nonexistent");
      request.setAction(VariablesRequest.Action.UPDATE);
      request.setVariables(Map.of("key", "val"));

      assertThatThrownBy(() -> service.modifyVariables(request))
          .isInstanceOf(RestExceptions.class)
          .hasMessageContaining("ProcessInstance not found")
          .extracting("code")
          .isEqualTo(404);
    }

    @Test
    @DisplayName("should add variables with ADD action (same as UPDATE behavior)")
    void modifyVariables_add() {
      Map<String, Object> currentVars = new HashMap<>(Map.of("existing", "val"));
      ProcessInstance instance = createProcessInstance("pi-123", PIState.RUNNING, currentVars);

      when(dataInteractionService.getProcessInstance("pi-123")).thenReturn(Optional.of(instance));

      VariablesRequest request = new VariablesRequest();
      request.setProcessInstanceId("pi-123");
      request.setAction(VariablesRequest.Action.ADD);
      request.setVariables(Map.of("new", "value"));

      VariablesResponse result = service.modifyVariables(request);

      assertThat(result.getVariables()).containsEntry("existing", "val");
      assertThat(result.getVariables()).containsEntry("new", "value");
    }
  }
}
