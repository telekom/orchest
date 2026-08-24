package io.telekom.orchest.connectorservice.service;

import io.telekom.orchest.api.core.adapters.data.dto.ConnectorTaskRequest;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.connectorservice.handler.ConnectorHandler;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.service.ProcessInstanceService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Consumes connector task requests, executes the matching {@link ConnectorHandler} and resumes the
 * workflow via the embedded {@link OrchestWorkflowEngine}.
 *
 * <p>Mirrors the worker completion path: on success the connector output is merged into the
 * instance via {@code resumeActivity}; on failure the engine error/incident handling is invoked
 * (which routes to an error boundary event when one is defined, or raises an incident otherwise).
 */
@Slf4j
@Service
public class ConnectorExecutionService {

  private final OrchestWorkflowEngine orchestWorkflowEngine;
  private final ProcessInstanceService processInstanceService;
  private final Map<String, ConnectorHandler> handlers;

  /**
   * Constructs the service and registers available connector handlers by type.
   *
   * @param orchestWorkflowEngine the embedded workflow engine for resuming activities
   * @param processInstanceService the process instance lookup service
   * @param connectorHandlers all available connector handler implementations
   */
  public ConnectorExecutionService(
      OrchestWorkflowEngine orchestWorkflowEngine,
      ProcessInstanceService processInstanceService,
      List<ConnectorHandler> connectorHandlers) {
    this.orchestWorkflowEngine = orchestWorkflowEngine;
    this.processInstanceService = processInstanceService;
    this.handlers =
        connectorHandlers.stream()
            .collect(Collectors.toMap(ConnectorHandler::connectorType, Function.identity()));
    log.info("Registered connector handlers: {}", handlers.keySet());
  }

  /**
   * Executes the connector referenced by the request and resumes the workflow.
   *
   * @param request The connector task request consumed from Kafka.
   */
  public void execute(ConnectorTaskRequest request) {
    String processInstanceId = request.getProcessInstanceId();
    String activityId = request.getActivityId();
    String connectorType = request.getConnectorType();
    log.info(
        "Executing connector task for instanceId: {}, activityId: {}, connectorType: {}",
        processInstanceId,
        activityId,
        connectorType);

    ConnectorHandler handler = handlers.get(connectorType);
    if (handler == null) {
      log.error(
          "No connector handler registered for type: {} (instanceId: {}, activityId: {})",
          connectorType,
          processInstanceId,
          activityId);
      orchestWorkflowEngine.handleIncident(
          processInstanceId,
          activityId,
          "No connector handler registered for type: " + connectorType);
      return;
    }

    Optional<ProcessInstance> instanceOpt =
        processInstanceService.getInstanceById(processInstanceId);
    if (instanceOpt.isEmpty()) {
      log.error(
          "Process instance {} not found for connector task on activity {}",
          processInstanceId,
          activityId);
      return;
    }
    ProcessInstance instance = instanceOpt.get();

    Optional<BaseNode> nodeOpt = instance.getProcessDefinition().getNode(activityId);
    if (nodeOpt.isEmpty()) {
      log.error(
          "Node {} not found in process definition for instance {}", activityId, processInstanceId);
      orchestWorkflowEngine.handleIncident(
          processInstanceId, activityId, "Connector node not found: " + activityId);
      return;
    }

    try {
      Map<String, Object> output = handler.execute(instance, nodeOpt.get());
      Variables variables =
          Variables.builder()
              .variables(output == null ? new HashMap<>() : new HashMap<>(output))
              .build();
      orchestWorkflowEngine.resumeActivity(processInstanceId, activityId, variables);
    } catch (Exception e) {
      log.error(
          "Connector execution failed for instanceId: {}, activityId: {}, connectorType: {}",
          processInstanceId,
          activityId,
          connectorType,
          e);
      // Route through the engine: matches an error boundary event if configured, else raises an
      // incident.
      orchestWorkflowEngine.handleError(
          processInstanceId,
          activityId,
          handler.errorCode(),
          Variables.builder().variables(new HashMap<>()).build());
    }
  }
}
