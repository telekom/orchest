package io.telekom.orchest.orchestrest.configurations;

import io.telekom.orchest.adapter.mongo.*;
import io.telekom.orchest.api.core.adapters.data.model.ActivityState;
import io.telekom.orchest.api.core.adapters.data.repository.MessageEventRepository;
import io.telekom.orchest.api.core.adapters.data.repository.SignalEventRepository;
import io.telekom.orchest.cache.core.Cache;
import io.telekom.orchest.enginecore.bpmn.OWEDependencies;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.service.*;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import io.telekom.orchest.telemetry.metrics.MetricsRecorder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/** Wires up OrchesT workflow engine core beans (services, event registrars, OWE instance). */
@Configuration
public class EngineConfigurations {

  @Bean
  public ProcessInstanceService processInstanceService(
      MongoProcessInstanceRepositoryAdapter mongoProcessInstanceRepositoryAdapter) {
    return new ProcessInstanceService(mongoProcessInstanceRepositoryAdapter);
  }

  @Bean
  public ProcessDefinitionService processDefinitionService(
      MongoProcessDefinitionRepositoryAdapter mongoProcessDefinitionRepositoryAdapter,
      MongoDynamicProcessDefinitionRepositoryAdapter mongoDynamicProcessDefinitionRepositoryAdapter,
      EventRegisterService eventRegisterService,
      OrchestEngineTelemetryService telemetryService) {
    return new ProcessDefinitionService(
        mongoProcessDefinitionRepositoryAdapter,
        mongoDynamicProcessDefinitionRepositoryAdapter,
        eventRegisterService,
        telemetryService);
  }

  @Bean
  public DecisionDefinitionService decisionDefinitionService(
      MongoDecisionDefinitionRepositoryAdapter mongoDecisionDefinitionRepositoryAdapter,
      OrchestEngineTelemetryService telemetryService) {
    return new DecisionDefinitionService(
        mongoDecisionDefinitionRepositoryAdapter, telemetryService);
  }

  @Bean
  public TimerEventRegistrar timerEventRegistrar(
      MongoTimedEventRepositoryAdapter mongoTimedEventRepositoryAdapter) {
    return new TimerEventRegistrar(mongoTimedEventRepositoryAdapter);
  }

  @Bean
  public MessageEventRegistrar messageEventRegistrar(
      MongoMessageEventRepositoryAdapter mongoMessageEventRepositoryAdapter) {
    return new MessageEventRegistrar(mongoMessageEventRepositoryAdapter);
  }

  @Bean
  public SignalEventRegistrar signalEventRegistrar(
      MongoSignalEventRepositoryAdapter mongoSignalEventRepositoryAdapter) {
    return new SignalEventRegistrar(mongoSignalEventRepositoryAdapter);
  }

  @Bean
  public EventRegisterService eventRegisterService(
      TimerEventRegistrar timerEventRegistrar,
      MessageEventRegistrar messageEventRegistrar,
      SignalEventRegistrar signalEventRegistrar,
      MongoWorkerRegistryRepositoryAdapter mongoWorkerRegistryRepositoryAdapter,
      MongoPendingTaskRepositoryAdapter mongoPendingTaskRepositoryAdapter) {
    return new EventRegisterService(
        timerEventRegistrar,
        messageEventRegistrar,
        signalEventRegistrar,
        mongoWorkerRegistryRepositoryAdapter,
        mongoPendingTaskRepositoryAdapter);
  }

  @Bean
  public DecisionInstanceService decisionInstanceService(
      MongoDecisionInstanceRepositoryAdapter mongoDecisionInstanceRepositoryAdapter,
      MetricsRecorder metricsRecorder) {
    return new DecisionInstanceService(mongoDecisionInstanceRepositoryAdapter, metricsRecorder);
  }

  @Bean
  public UserTaskService userTaskService(
      MongoUserTaskRepositoryAdapter mongoUserTaskRepositoryAdapter) {
    return new UserTaskService(mongoUserTaskRepositoryAdapter);
  }

  @Bean
  public OWEDependencies oweDependencies(
      ProcessDefinitionService processDefinitionService,
      ProcessInstanceService processInstanceService,
      DecisionDefinitionService decisionDefinitionService,
      DecisionInstanceService decisionInstanceService,
      EventRegisterService eventRegisterService,
      MessageEventRepository messageEventRepository,
      SignalEventRepository signalEventRepository,
      UserTaskService userTaskService,
      MetricsRecorder metricsRecorder) {
    return new OWEDependencies(
        processDefinitionService,
        processInstanceService,
        decisionDefinitionService,
        decisionInstanceService,
        eventRegisterService,
        null,
        null,
        null,
        messageEventRepository,
        signalEventRepository,
        userTaskService,
        metricsRecorder);
  }

  @Bean
  public OrchestWorkflowEngine orchestWorkflowEngine(
      OWEDependencies oweDependencies,
      OrchestEngineTelemetryService telemetryService,
      @Lazy Cache<String, ActivityState> activityStateCache) {
    return new OrchestWorkflowEngine(oweDependencies, telemetryService, activityStateCache);
  }
}
