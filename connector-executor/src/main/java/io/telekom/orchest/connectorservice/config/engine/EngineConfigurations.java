package io.telekom.orchest.connectorservice.config.engine;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.adapter.mongo.*;
import io.telekom.orchest.api.core.adapters.data.model.ActivityState;
import io.telekom.orchest.api.core.adapters.data.repository.IncidentRepository;
import io.telekom.orchest.api.core.adapters.data.repository.MessageEventRepository;
import io.telekom.orchest.api.core.adapters.data.repository.SignalEventRepository;
import io.telekom.orchest.cache.core.Cache;
import io.telekom.orchest.enginecore.IncidentEventHandlerAdapter;
import io.telekom.orchest.enginecore.bpmn.ConnectorDispatchAdapter;
import io.telekom.orchest.enginecore.bpmn.OWEDependencies;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.ServiceTaskHandlerAdapter;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutorRegistry;
import io.telekom.orchest.enginecore.bpmn.execution.impl.connector.ConnectorDispatchExecutor;
import io.telekom.orchest.enginecore.bpmn.service.*;
import io.telekom.orchest.enginecore.feel.FeelConditionEvaluator;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import io.telekom.orchest.telemetry.metrics.MetricsRecorder;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Wires the embedded {@link OrchestWorkflowEngine} for the connector service. Mirrors the engine
 * configuration so that {@code resumeActivity} (and any downstream node execution it triggers)
 * behaves identically to the engine over the shared Mongo/Kafka.
 */
@Configuration
@EnableConfigurationProperties(EngineProperties.class)
public class EngineConfigurations {

  /**
   * Creates the service task handler that dispatches worker events to Kafka.
   *
   * @param kafkaClientEventProducer the Kafka event producer
   * @return a ServiceTaskHandlerAdapter implementation
   */
  @Bean
  public ServiceTaskHandlerAdapter serviceTaskHandlerInterface(
      KafkaClientEventProducer kafkaClientEventProducer) {
    return new ServiceTaskHandler(kafkaClientEventProducer);
  }

  /**
   * Creates the incident event handler that persists and publishes incidents.
   *
   * @param kafkaClientEventProducer the Kafka event producer
   * @param incidentRepository the incident persistence repository
   * @return an IncidentEventHandlerAdapter implementation
   */
  @Bean
  public IncidentEventHandlerAdapter incidentEventHandlerAdapter(
      KafkaClientEventProducer kafkaClientEventProducer, IncidentRepository incidentRepository) {
    return new IncidentEventHandler(kafkaClientEventProducer, incidentRepository);
  }

  /**
   * Creates the connector dispatch adapter for re-dispatching connector tasks to Kafka.
   *
   * @param kafkaClientEventProducer the Kafka event producer
   * @return a ConnectorDispatchAdapter implementation
   */
  @Bean
  public ConnectorDispatchAdapter connectorDispatchAdapter(
      KafkaClientEventProducer kafkaClientEventProducer) {
    return new ConnectorDispatchHandler(kafkaClientEventProducer);
  }

  /**
   * Builds the node executor registry with connector dispatch support.
   *
   * @param serviceTaskHandlerAdapter the service task handler
   * @param orchestWorkflowEngine the embedded workflow engine
   * @param connectorDispatchAdapter the connector dispatch adapter
   * @param commonWorkersMap map of common worker types
   * @return configured NodeExecutorRegistry
   */
  @Bean
  public NodeExecutorRegistry nodeExecutorRegistry(
      ServiceTaskHandlerAdapter serviceTaskHandlerAdapter,
      @Lazy OrchestWorkflowEngine orchestWorkflowEngine,
      ConnectorDispatchAdapter connectorDispatchAdapter,
      @Qualifier("commonWorkersMap") Map<String, Boolean> commonWorkersMap) {
    NodeExecutorRegistry nodeExecutorRegistry =
        new NodeExecutorRegistry(
            new FeelConditionEvaluator(),
            serviceTaskHandlerAdapter,
            orchestWorkflowEngine,
            commonWorkersMap);
    nodeExecutorRegistry.registerConnectorDispatcher(
        new ConnectorDispatchExecutor(connectorDispatchAdapter));
    return nodeExecutorRegistry;
  }

  /**
   * Creates the process instance service backed by MongoDB.
   *
   * @param mongoProcessInstanceRepositoryAdapter the Mongo repository adapter
   * @return a ProcessInstanceService
   */
  @Bean
  public ProcessInstanceService processInstanceService(
      MongoProcessInstanceRepositoryAdapter mongoProcessInstanceRepositoryAdapter) {
    return new ProcessInstanceService(mongoProcessInstanceRepositoryAdapter);
  }

  /**
   * Creates the process definition service backed by MongoDB.
   *
   * @param mongoProcessDefinitionRepositoryAdapter the static process definition repository
   * @param mongoDynamicProcessDefinitionRepositoryAdapter the dynamic process definition repository
   * @param eventRegisterService the event registrar service
   * @param telemetryService the telemetry service
   * @return a ProcessDefinitionService
   */
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

  /**
   * Creates the decision definition service for DMN evaluation.
   *
   * @param mongoDecisionDefinitionRepositoryAdapter the Mongo repository adapter
   * @param telemetryService the telemetry service
   * @return a DecisionDefinitionService
   */
  @Bean
  public DecisionDefinitionService decisionDefinitionService(
      MongoDecisionDefinitionRepositoryAdapter mongoDecisionDefinitionRepositoryAdapter,
      OrchestEngineTelemetryService telemetryService) {
    return new DecisionDefinitionService(
        mongoDecisionDefinitionRepositoryAdapter, telemetryService);
  }

  /**
   * Creates the timer event registrar.
   *
   * @param mongoTimedEventRepositoryAdapter the timed event repository adapter
   * @return a TimerEventRegistrar
   */
  @Bean
  public TimerEventRegistrar timerEventRegistrar(
      MongoTimedEventRepositoryAdapter mongoTimedEventRepositoryAdapter) {
    return new TimerEventRegistrar(mongoTimedEventRepositoryAdapter);
  }

  /**
   * Creates the message event registrar.
   *
   * @param mongoMessageEventRepositoryAdapter the message event repository adapter
   * @return a MessageEventRegistrar
   */
  @Bean
  public MessageEventRegistrar messageEventRegistrar(
      MongoMessageEventRepositoryAdapter mongoMessageEventRepositoryAdapter) {
    return new MessageEventRegistrar(mongoMessageEventRepositoryAdapter);
  }

  /**
   * Creates the signal event registrar.
   *
   * @param mongoSignalEventRepositoryAdapter the signal event repository adapter
   * @return a SignalEventRegistrar
   */
  @Bean
  public SignalEventRegistrar signalEventRegistrar(
      MongoSignalEventRepositoryAdapter mongoSignalEventRepositoryAdapter) {
    return new SignalEventRegistrar(mongoSignalEventRepositoryAdapter);
  }

  /**
   * Creates the event register service composing timer, message, and signal registrars.
   *
   * @param timerEventRegistrar the timer event registrar
   * @param messageEventRegistrar the message event registrar
   * @param signalEventRegistrar the signal event registrar
   * @param mongoWorkerRegistryRepositoryAdapter the worker registry repository
   * @param mongoPendingTaskRepositoryAdapter the pending task repository
   * @return an EventRegisterService
   */
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

  /**
   * Creates the decision instance service for persisting DMN evaluation results.
   *
   * @param mongoDecisionInstanceRepositoryAdapter the Mongo repository adapter
   * @param metricsRecorder the metrics recorder
   * @return a DecisionInstanceService
   */
  @Bean
  public DecisionInstanceService decisionInstanceService(
      MongoDecisionInstanceRepositoryAdapter mongoDecisionInstanceRepositoryAdapter,
      MetricsRecorder metricsRecorder) {
    return new DecisionInstanceService(mongoDecisionInstanceRepositoryAdapter, metricsRecorder);
  }

  /**
   * Creates the user task service.
   *
   * @param mongoUserTaskRepositoryAdapter the user task repository adapter
   * @return a UserTaskService
   */
  @Bean
  public UserTaskService userTaskService(
      MongoUserTaskRepositoryAdapter mongoUserTaskRepositoryAdapter) {
    return new UserTaskService(mongoUserTaskRepositoryAdapter);
  }

  /**
   * Assembles the dependency bundle required by {@link OrchestWorkflowEngine}.
   *
   * @return an OWEDependencies instance wiring all required services
   */
  @Bean
  public OWEDependencies oweDependencies(
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
      MetricsRecorder metricsRecorder) {
    return new OWEDependencies(
        processDefinitionService,
        processInstanceService,
        decisionDefinitionService,
        decisionInstanceService,
        eventRegisterService,
        executorRegistry,
        serviceTaskHandlerAdapter,
        incidentEventHandlerAdapter,
        messageEventRepository,
        signalEventRepository,
        userTaskService,
        metricsRecorder);
  }

  /**
   * Creates the embedded workflow engine used to resume activities after connector execution.
   *
   * @param oweDependencies the engine dependency bundle
   * @param orchestEngineTelemetryService the telemetry service
   * @param activityStateCache the activity state cache
   * @return an OrchestWorkflowEngine instance
   */
  @Bean
  public OrchestWorkflowEngine orchestWorkflowEngine(
      OWEDependencies oweDependencies,
      OrchestEngineTelemetryService orchestEngineTelemetryService,
      @Lazy Cache<String, ActivityState> activityStateCache) {
    return new OrchestWorkflowEngine(
        oweDependencies, orchestEngineTelemetryService, activityStateCache);
  }
}
