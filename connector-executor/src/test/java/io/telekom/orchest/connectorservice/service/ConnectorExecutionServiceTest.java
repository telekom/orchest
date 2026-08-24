package io.telekom.orchest.connectorservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.dto.ConnectorTaskRequest;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.ConnectorException;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.connectorservice.handler.ConnectorHandler;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.service.ProcessInstanceService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Tests connector execution orchestration: handler dispatch, engine resumption, and error routing.
 */
class ConnectorExecutionServiceTest {

  private static final String CONNECTOR_TYPE = "io.orchest.http-json:1";
  private static final String PI = "pi-1";
  private static final String ACTIVITY = "node-1";

  private final OrchestWorkflowEngine engine = mock(OrchestWorkflowEngine.class);
  private final ProcessInstanceService processInstanceService = mock(ProcessInstanceService.class);

  private ConnectorTaskRequest request() {
    return ConnectorTaskRequest.builder()
        .processInstanceId(PI)
        .activityId(ACTIVITY)
        .connectorType(CONNECTOR_TYPE)
        .build();
  }

  private ConnectorHandler handler(Map<String, Object> output, RuntimeException toThrow) {
    return new ConnectorHandler() {
      @Override
      public String connectorType() {
        return CONNECTOR_TYPE;
      }

      @Override
      public String errorCode() {
        return "REST_CONNECTOR_ERROR";
      }

      @Override
      public Map<String, Object> execute(ProcessInstance instance, BaseNode node) {
        if (toThrow != null) {
          throw toThrow;
        }
        return output;
      }
    };
  }

  private void mockInstanceWithNode() {
    ProcessInstance instance = mock(ProcessInstance.class);
    ProcessDefinition definition = mock(ProcessDefinition.class);
    BaseNode node = mock(BaseNode.class);
    when(instance.getProcessDefinition()).thenReturn(definition);
    when(definition.getNode(ACTIVITY)).thenReturn(Optional.of(node));
    when(processInstanceService.getInstanceById(PI)).thenReturn(Optional.of(instance));
  }

  @Test
  void execute_resumesActivityWithOutputVariablesOnSuccess() {
    mockInstanceWithNode();
    ConnectorExecutionService service =
        new ConnectorExecutionService(
            engine,
            processInstanceService,
            List.of(handler(Map.of("restResponse", Map.of("statusCode", 200)), null)));

    service.execute(request());

    var captor = org.mockito.ArgumentCaptor.forClass(Variables.class);
    verify(engine).resumeActivity(eq(PI), eq(ACTIVITY), captor.capture());
    assertEquals(
        Map.of("restResponse", Map.of("statusCode", 200)), captor.getValue().getVariables());
    verify(engine, never()).handleError(any(), any(), any(), any());
  }

  @Test
  void execute_routesToErrorHandlingWhenHandlerThrows() {
    mockInstanceWithNode();
    ConnectorExecutionService service =
        new ConnectorExecutionService(
            engine, processInstanceService, List.of(handler(null, new ConnectorException("boom"))));

    service.execute(request());

    verify(engine)
        .handleError(eq(PI), eq(ACTIVITY), eq("REST_CONNECTOR_ERROR"), any(Variables.class));
    verify(engine, never()).resumeActivity(any(), any(), any());
  }

  @Test
  void execute_raisesIncidentWhenNoHandlerRegistered() {
    ConnectorExecutionService service =
        new ConnectorExecutionService(engine, processInstanceService, List.of());

    service.execute(request());

    verify(engine).handleIncident(eq(PI), eq(ACTIVITY), contains(CONNECTOR_TYPE));
    verify(engine, never()).resumeActivity(any(), any(), any());
    verifyNoInteractions(processInstanceService);
  }

  @Test
  void execute_returnsQuietlyWhenInstanceNotFound() {
    when(processInstanceService.getInstanceById(PI)).thenReturn(Optional.empty());
    ConnectorExecutionService service =
        new ConnectorExecutionService(
            engine, processInstanceService, List.of(handler(Map.of(), null)));

    service.execute(request());

    verify(engine, never()).resumeActivity(any(), any(), any());
    verify(engine, never()).handleError(any(), any(), any(), any());
  }
}
