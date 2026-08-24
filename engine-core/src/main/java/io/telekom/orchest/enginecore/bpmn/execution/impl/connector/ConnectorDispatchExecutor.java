package io.telekom.orchest.enginecore.bpmn.execution.impl.connector;

import static io.telekom.orchest.connectors.BpmnConnectorParser.CONNECTOR_TYPE;

import io.telekom.orchest.api.core.adapters.data.dto.ConnectorTaskRequest;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.enginecore.bpmn.ConnectorDispatchAdapter;
import io.telekom.orchest.enginecore.bpmn.execution.ExecutionContext;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutor;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executor for connector service tasks. Instead of executing the connector in-process, it
 * dispatches a {@link ConnectorTaskRequest} to the connector service and leaves the node in a wait
 * state. The connector service executes the connector implementation and resumes the activity once
 * it completes.
 */
@Slf4j
@RequiredArgsConstructor
public class ConnectorDispatchExecutor implements NodeExecutor {

  private final ConnectorDispatchAdapter connectorDispatchAdapter;

  /**
   * Dispatches a connector task request and enters a wait state until the connector service
   * completes execution and resumes the activity.
   *
   * @param instance the active process instance
   * @param node the connector service task node to execute
   * @param context the execution context
   * @throws IllegalStateException if the connector type property is missing from the node
   */
  @Override
  public void execute(ProcessInstance instance, BaseNode node, ExecutionContext context) {
    Object connectorType =
        node.getProperties() != null ? node.getProperties().get(CONNECTOR_TYPE) : null;
    if (connectorType == null) {
      throw new IllegalStateException("Connector type missing for node: " + node.getId());
    }

    ConnectorTaskRequest event =
        ConnectorTaskRequest.builder()
            .eventId(UUID.randomUUID().toString())
            .processInstanceId(instance.getProcessInstanceId())
            .processDefinitionId(instance.getProcessDefinitionId())
            .activityId(node.getId())
            .activityName(node.getName())
            .connectorType(connectorType.toString())
            .version(instance.getVersion())
            .build();

    log.info(
        "Dispatching connector task for instanceId: {}, activityId: {}, connectorType: {}",
        instance.getProcessInstanceId(),
        node.getId(),
        connectorType);
    connectorDispatchAdapter.dispatch(event);
    // Wait state: the connector service resumes this activity once the connector completes.
  }
}
