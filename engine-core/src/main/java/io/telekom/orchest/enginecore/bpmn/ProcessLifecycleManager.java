package io.telekom.orchest.enginecore.bpmn;

import io.telekom.orchest.api.core.adapters.data.model.DynamicProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ParentProcessActivity;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.utils.IDGenerator;
import io.telekom.orchest.enginecore.bpmn.service.ProcessDefinitionService;
import io.telekom.orchest.enginecore.bpmn.service.ProcessInstanceService;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import io.telekom.orchest.telemetry.metrics.MetricKey;
import io.telekom.orchest.telemetry.metrics.MetricType;
import io.telekom.orchest.telemetry.metrics.MetricsRecorder;
import io.telekom.orchest.telemetry.metrics.NoOpMetricsRecorder;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/** Manages the lifecycle of process instances: creation, start, and correlation ID extraction. */
@Slf4j
public class ProcessLifecycleManager {

  private final ProcessDefinitionService processDefinitionService;
  private final ProcessInstanceService processInstanceService;
  private final NodeExecutionCallback nodeExecutionCallback;
  private final OrchestEngineTelemetryService telemetryService;
  private final MetricsRecorder metricsRecorder;

  private static final List<String> CORRELATION_KEY_FIELDS =
      List.of("orderId", "publicIdentifier", "correlationId", "kkm");

  /**
   * Backwards-compatible constructor — defaults the persistent metrics recorder to a no-op so call
   * sites that haven't been migrated continue to work.
   */
  public ProcessLifecycleManager(
      ProcessDefinitionService processDefinitionService,
      ProcessInstanceService processInstanceService,
      NodeExecutionCallback nodeExecutionCallback,
      OrchestEngineTelemetryService telemetryService) {
    this(
        processDefinitionService,
        processInstanceService,
        nodeExecutionCallback,
        telemetryService,
        NoOpMetricsRecorder.INSTANCE);
  }

  public ProcessLifecycleManager(
      ProcessDefinitionService processDefinitionService,
      ProcessInstanceService processInstanceService,
      NodeExecutionCallback nodeExecutionCallback,
      OrchestEngineTelemetryService telemetryService,
      MetricsRecorder metricsRecorder) {
    this.processDefinitionService = processDefinitionService;
    this.processInstanceService = processInstanceService;
    this.nodeExecutionCallback = nodeExecutionCallback;
    this.telemetryService = telemetryService;
    this.metricsRecorder = metricsRecorder == null ? NoOpMetricsRecorder.INSTANCE : metricsRecorder;
  }

  /** Callback to execute a node in the engine's execution loop. */
  @FunctionalInterface
  public interface NodeExecutionCallback {
    void executeNode(ProcessInstance instance, BaseNode node, String sourceNodeId);
  }

  /** Starts a new process instance from a deployed definition. */
  public ProcessInstance startProcess(
      String definitionId,
      Integer version,
      String processInstanceId,
      Map<String, Object> variables,
      ParentProcessActivity parentProcessActivity) {
    ProcessDefinition definition;
    try {
      definition =
          processDefinitionService
              .getProcessDefinition(definitionId, version)
              .orElseThrow(
                  () -> {
                    telemetryService.incrementProcessInvocationCounter(
                        definitionId, version, "unknownDefinitionId");
                    return new IllegalArgumentException(
                        "Process definition not found: " + definitionId);
                  });
    } catch (RuntimeException ex) {
      // Definition lookup failures are still "start attempts that failed" for accounting
      metricsRecorder.increment(
          MetricKey.of(MetricType.PROCESS_INSTANCE_FAILED, definitionId, version));
      throw ex;
    }

    if (!definition.isExecutableOrLegacy()) {
      telemetryService.incrementProcessInvocationCounter(
          definitionId, definition.getVersion(), "notExecutable");
      if (parentProcessActivity != null) {
        throw new IllegalStateException(
            "ProcessDefinition '"
                + definition.getDefinitionId()
                + "' is not executable and cannot be invoked from a CallActivity");
      }
      log.info(
          "skipping start for non-executable processDefinitionId: {} (version: {})",
          definition.getDefinitionId(),
          definition.getVersion());
      return null;
    }

    String instanceId = processInstanceId != null ? processInstanceId : IDGenerator.generate();
    ProcessInstance instance =
        new ProcessInstance(instanceId, definitionId, definition.getVersion());
    instance.setVariables(variables);
    instance.setCorrelationIds(getCorrelationIds(variables));
    instance.setState(PIState.STARTED);
    instance.setParentProcesActivity(parentProcessActivity);

    // Lifetime accounting: a real ProcessInstance has been built and is about to enter the
    // engine. Increment is in-memory only; the flush job persists deltas to MongoDB
    // every N seconds across all engine pods.
    metricsRecorder.increment(
        MetricKey.of(
            MetricType.PROCESS_INSTANCE_STARTED,
            instance.getProcessDefinitionId(),
            instance.getVersion()));

    try {
      instance = processInstanceService.save(instance);
      instance.setProcessDefinition(definition);
      log.info(
          "Starting process instance {} for definition {}",
          instance.getProcessInstanceId(),
          definition.getName());

      BaseNode startNode = definition.getStartNode();
      nodeExecutionCallback.executeNode(instance, startNode, "PROCESS_START");
      telemetryService.incrementProcessInvocationCounter(
          instance.getProcessDefinitionId(), instance.getVersion(), "success");
    } catch (Exception e) {
      log.error(
          "Error starting process instance {} for definition {}",
          instance.getProcessInstanceId(),
          definition.getName(),
          e);
      metricsRecorder.increment(
          MetricKey.of(
              MetricType.PROCESS_INSTANCE_FAILED,
              instance.getProcessDefinitionId(),
              instance.getVersion()));
    } finally {
      processInstanceService.save(instance);
      telemetryService.incrementProcessInvocationCounter(
          definitionId, instance.getVersion(), "failed");
    }

    return instance;
  }

  /** Starts a dynamic process instance from an inline BPMN XML definition. */
  public ProcessInstance startDynamicProcess(
      String utf8BpmnXML, String processInstanceId, Map<String, Object> variables) {
    log.info("Starting dynamic process with instanceId: {}", processInstanceId);
    DynamicProcessDefinition processDefinition =
        processDefinitionService.deployDynamicProcessDefinition(utf8BpmnXML, processInstanceId);
    return startDynamicProcess(processDefinition, variables, null);
  }

  /** Starts a dynamic process instance from an already-deployed dynamic definition. */
  public ProcessInstance startDynamicProcess(
      DynamicProcessDefinition processDefinition,
      Map<String, Object> variables,
      ParentProcessActivity parentProcessActivity) {
    String instanceId =
        processDefinition.getProcessInstanceId() != null
            ? processDefinition.getProcessInstanceId()
            : IDGenerator.generate();
    ProcessInstance instance =
        new ProcessInstance(
            instanceId, processDefinition.getDefinitionId(), processDefinition.getVersion());
    instance.setVariables(variables);
    instance.setCorrelationIds(getCorrelationIds(variables));
    instance.setState(PIState.STARTED);
    instance.setDynamicFlow(true);
    instance.setParentProcesActivity(parentProcessActivity);

    instance = processInstanceService.save(instance);
    instance.setProcessDefinition(processDefinition);
    log.info("Starting dynamic process instance with Id:{}", instance.getProcessInstanceId());

    // Dynamic and static processes count under the same metric type — the dimension
    // (definitionId) lets consumers slice by definition if needed.
    metricsRecorder.increment(
        MetricKey.of(
            MetricType.PROCESS_INSTANCE_STARTED,
            instance.getProcessDefinitionId(),
            instance.getVersion()));

    try {
      BaseNode startNode = processDefinition.getStartNode();
      nodeExecutionCallback.executeNode(instance, startNode, "PROCESS_START");
    } catch (Exception e) {
      log.error(
          "Error starting process instance {} for definition {}",
          instance.getProcessInstanceId(),
          processDefinition.getName(),
          e);
      metricsRecorder.increment(
          MetricKey.of(
              MetricType.PROCESS_INSTANCE_FAILED,
              instance.getProcessDefinitionId(),
              instance.getVersion()));
    } finally {
      processInstanceService.save(instance);
    }
    return instance;
  }

  /** Extracts correlation IDs from process variables for searchability. */
  List<String> getCorrelationIds(Map<String, Object> variables) {
    if (variables == null) {
      return null;
    }
    return CORRELATION_KEY_FIELDS.stream()
        .filter(variables::containsKey)
        .map(key -> variables.get(key).toString())
        .distinct()
        .toList();
  }
}
