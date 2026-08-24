package io.telekom.orchest.enginecore.bpmn.execution.impl.connector;

import static io.telekom.orchest.connectors.BpmnConnectorParser.CONNECTOR_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.dto.ConnectorTaskRequest;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.enginecore.bpmn.ConnectorDispatchAdapter;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests connector dispatch executor for correct request building and adapter invocation. */
@ExtendWith(MockitoExtension.class)
class ConnectorDispatchExecutorTest {

  @Mock private ConnectorDispatchAdapter connectorDispatchAdapter;

  @Mock private ExecutionContext context;

  @Test
  void execute_dispatchesConnectorTaskEventAndDoesNotProceed() {
    ConnectorDispatchExecutor executor = new ConnectorDispatchExecutor(connectorDispatchAdapter);

    ProcessInstance instance = mock(ProcessInstance.class);
    when(instance.getProcessInstanceId()).thenReturn("pi-1");
    when(instance.getProcessDefinitionId()).thenReturn("pd-1");
    when(instance.getVersion()).thenReturn(2);

    BaseNode node = mock(BaseNode.class);
    when(node.getId()).thenReturn("node-1");
    when(node.getName()).thenReturn("Call REST");
    Map<String, Object> properties = new HashMap<>();
    properties.put(CONNECTOR_TYPE, "io.orchest.http-json:1");
    when(node.getProperties()).thenReturn(properties);

    executor.execute(instance, node, context);

    ArgumentCaptor<ConnectorTaskRequest> captor =
        ArgumentCaptor.forClass(ConnectorTaskRequest.class);
    verify(connectorDispatchAdapter).dispatch(captor.capture());
    ConnectorTaskRequest event = captor.getValue();
    assertEquals("pi-1", event.getProcessInstanceId());
    assertEquals("pd-1", event.getProcessDefinitionId());
    assertEquals("node-1", event.getActivityId());
    assertEquals("Call REST", event.getActivityName());
    assertEquals("io.orchest.http-json:1", event.getConnectorType());
    assertEquals(2, event.getVersion());

    // Wait state: must NOT advance the flow itself.
    verifyNoInteractions(context);
  }

  @Test
  void execute_throwsWhenConnectorTypeMissing() {
    ConnectorDispatchExecutor executor = new ConnectorDispatchExecutor(connectorDispatchAdapter);

    ProcessInstance instance = mock(ProcessInstance.class);
    BaseNode node = mock(BaseNode.class);
    when(node.getId()).thenReturn("node-1");
    when(node.getProperties()).thenReturn(new HashMap<>());

    assertThrows(IllegalStateException.class, () -> executor.execute(instance, node, context));
    verifyNoInteractions(connectorDispatchAdapter);
  }
}
