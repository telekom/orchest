package io.telekom.orchest.enginecore.bpmn;

import io.telekom.orchest.api.core.adapters.data.repository.MessageEventRepository;
import io.telekom.orchest.api.core.adapters.data.repository.SignalEventRepository;
import io.telekom.orchest.enginecore.IncidentEventHandlerAdapter;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutorRegistry;
import io.telekom.orchest.enginecore.bpmn.service.*;
import io.telekom.orchest.telemetry.metrics.MetricsRecorder;

/**
 * Bundles every collaborator that {@link OrchestWorkflowEngine} needs at construction time. {@code
 * metricsRecorder} is non-null in every well-wired runtime — modules that do not run the engine
 * (e.g. {@code orchest-rest}, {@code sentinel}) pass {@link
 * io.telekom.orchest.telemetry.metrics.NoOpMetricsRecorder#INSTANCE}.
 */
public record OWEDependencies(
    ProcessDefinitionService processDefinitionService,
    ProcessInstanceService processInstanceService,
    DecisionDefinitionService decisionDefinitionService,
    DecisionInstanceService decisionInstanceService,
    EventRegisterService eventRegisterService,
    NodeExecutorRegistry executorRegistry,
    ServiceTaskHandlerAdapter serviceTaskHandlerAdapter,
    IncidentEventHandlerAdapter incidentEventHandlerAdapter,
    MessageEventRepository messageEventRepository,
    SignalEventRepository signalEventRepository,
    UserTaskService userTaskService,
    MetricsRecorder metricsRecorder) {}
