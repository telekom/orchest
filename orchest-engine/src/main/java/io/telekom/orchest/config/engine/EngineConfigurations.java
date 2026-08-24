package io.telekom.orchest.config.engine;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.adapter.mongo.*;
import io.telekom.orchest.alerting.service.AlertLifecycleService;
import io.telekom.orchest.api.core.adapters.data.model.ActivityState;
import io.telekom.orchest.api.core.adapters.data.repository.IncidentRepository;
import io.telekom.orchest.api.core.adapters.data.repository.MessageEventRepository;
import io.telekom.orchest.api.core.adapters.data.repository.SignalEventRepository;
import io.telekom.orchest.cache.core.Cache;
import io.telekom.orchest.engine.handlers.ConnectorDispatchHandler;
import io.telekom.orchest.engine.handlers.IncidentEventHandler;
import io.telekom.orchest.engine.handlers.ServiceTaskHandler;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Configuration class for the Orchest Engine core components. Sets up the workflow engine,
 * services, repositories, and handlers. Binds {@link EngineProperties} for configuration injection.
 */
@Configuration
@EnableConfigurationProperties(EngineProperties.class)
public class EngineConfigurations {

  /**
   * Configures the handler for service tasks.
   *
   * @param kafkaClientEventProducer The Kafka event producer for sending worker events.
   * @return The configured ServiceTaskHandlerAdapter.
   */
  @Bean
  public ServiceTaskHandlerAdapter serviceTaskHandlerInterface(
      KafkaClientEventProducer kafkaClientEventProducer) {
    return new ServiceTaskHandler(kafkaClientEventProducer);
  }

  /**
   * Configures the handler for incident events such as job failures and retries.
   *
   * @param kafkaClientEventProducer the Kafka event producer for incident notifications
   * @param incidentRepository the repository for persisting incidents
   * @param alertLifecycleService optional alert lifecycle service for triggering alerts
   * @return the configured IncidentEventHandlerAdapter
   */
  @Bean
  public IncidentEventHandlerAdapter incidentEventHandlerAdapter(
      KafkaClientEventProducer kafkaClientEventProducer,
      IncidentRepository incidentRepository,
      ObjectProvider<AlertLifecycleService> alertLifecycleService) {
    return new IncidentEventHandler(
        kafkaClientEventProducer, incidentRepository, alertLifecycleService);
  }

  /**
   * Configures the adapter that dispatches connector tasks to the connector service.
   *
   * @param kafkaClientEventProducer The Kafka event producer for sending connector task events.
   * @return The configured ConnectorDispatchAdapter.
   */
  @Bean
  public ConnectorDispatchAdapter connectorDispatchAdapter(
      KafkaClientEventProducer kafkaClientEventProducer) {
    return new ConnectorDispatchHandler(kafkaClientEventProducer);
  }

  /**
   * Configures the registry of node executors.
   *
   * @param serviceTaskHandlerAdapter The service task handler.
   * @param orchestWorkflowEngine The workflow engine (lazy loaded to avoid circular dependency).
   * @param connectorDispatchAdapter The adapter used to dispatch connector tasks.
   * @return The NodeExecutorRegistry.
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
    // connector service tasks are dispatched to the connector service for execution
    nodeExecutorRegistry.registerConnectorDispatcher(
        new ConnectorDispatchExecutor(connectorDispatchAdapter));
    return nodeExecutorRegistry;
  }

  /**
   * Configures the service for managing process instances.
   *
   * @param mongoProcessInstanceRepositoryAdapter The MongoDB repository adapter.
   * @return The ProcessInstanceService.
   */
  @Bean
  public ProcessInstanceService processInstanceService(
      MongoProcessInstanceRepositoryAdapter mongoProcessInstanceRepositoryAdapter) {
    return new ProcessInstanceService(mongoProcessInstanceRepositoryAdapter);
  }

  /**
   * Configures the service for managing process definitions.
   *
   * @param mongoProcessDefinitionRepositoryAdapter The MongoDB repository adapter.
   * @param eventRegisterService The event registration service.
   * @return The ProcessDefinitionService.
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
   * Configures the service for managing decision definitions.
   *
   * @param mongoDecisionDefinitionRepositoryAdapter The MongoDB repository adapter.
   * @return The DecisionDefinitionService.
   */
  @Bean
  public DecisionDefinitionService decisionDefinitionService(
      MongoDecisionDefinitionRepositoryAdapter mongoDecisionDefinitionRepositoryAdapter,
      OrchestEngineTelemetryService telemetryService) {
    return new DecisionDefinitionService(
        mongoDecisionDefinitionRepositoryAdapter, telemetryService);
  }

  /**
   * Configures the registrar for timer-based events.
   *
   * @param mongoTimedEventRepositoryAdapter the MongoDB repository adapter for timed events
   * @return the TimerEventRegistrar
   */
  @Bean
  public TimerEventRegistrar timerEventRegistrar(
      MongoTimedEventRepositoryAdapter mongoTimedEventRepositoryAdapter) {
    return new TimerEventRegistrar(mongoTimedEventRepositoryAdapter);
  }

  /**
   * Configures the registrar for message-based events.
   *
   * @param mongoMessageEventRepositoryAdapter the MongoDB repository adapter for message events
   * @return the MessageEventRegistrar
   */
  @Bean
  public MessageEventRegistrar messageEventRegistrar(
      MongoMessageEventRepositoryAdapter mongoMessageEventRepositoryAdapter) {
    return new MessageEventRegistrar(mongoMessageEventRepositoryAdapter);
  }

  /**
   * Configures the registrar for signal-based events.
   *
   * @param mongoSignalEventRepositoryAdapter the MongoDB repository adapter for signal events
   * @return the SignalEventRegistrar
   */
  @Bean
  public SignalEventRegistrar signalEventRegistrar(
      MongoSignalEventRepositoryAdapter mongoSignalEventRepositoryAdapter) {
    return new SignalEventRegistrar(mongoSignalEventRepositoryAdapter);
  }

  /**
   * Configures the service that coordinates event registration across timer, message, and signal
   * registrars.
   *
   * @param timerEventRegistrar the timer event registrar
   * @param messageEventRegistrar the message event registrar
   * @param signalEventRegistrar the signal event registrar
   * @param mongoWorkerRegistryRepositoryAdapter the worker registry repository adapter
   * @param mongoPendingTaskRepositoryAdapter the pending task repository adapter
   * @return the EventRegisterService
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
   * Configures the service for managing decision instances.
   *
   * @param mongoDecisionInstanceRepositoryAdapter The MongoDB repository adapter.
   * @return The DecisionInstanceService.
   */
  @Bean
  public DecisionInstanceService decisionInstanceService(
      MongoDecisionInstanceRepositoryAdapter mongoDecisionInstanceRepositoryAdapter,
      MetricsRecorder metricsRecorder) {
    return new DecisionInstanceService(mongoDecisionInstanceRepositoryAdapter, metricsRecorder);
  }

  /**
   * Configures the service for managing user tasks within process instances.
   *
   * @param mongoUserTaskRepositoryAdapter the MongoDB repository adapter for user tasks
   * @return the UserTaskService
   */
  @Bean
  public UserTaskService userTaskService(
      MongoUserTaskRepositoryAdapter mongoUserTaskRepositoryAdapter) {
    return new UserTaskService(mongoUserTaskRepositoryAdapter);
  }

  /**
   * Assembles the dependency bundle required by the OrchesT Workflow Engine.
   *
   * @param processDefinitionService the process definition service
   * @param processInstanceService the process instance service
   * @param decisionDefinitionService the decision definition service
   * @param decisionInstanceService the decision instance service
   * @param eventRegisterService the event registration service
   * @param executorRegistry the node executor registry
   * @param serviceTaskHandlerAdapter the service task handler
   * @param incidentEventHandlerAdapter the incident event handler
   * @param messageEventRepository the message event repository
   * @param signalEventRepository the signal event repository
   * @param userTaskService the user task service
   * @param metricsRecorder the metrics recorder
   * @return the OWEDependencies bundle
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
   * Configures the main OrchesT Workflow Engine instance.
   *
   * @param oweDependencies the dependency bundle for the engine
   * @param orchestEngineTelemetryService the telemetry service for engine metrics
   * @param activityStateCache the cache for activity states (lazy loaded)
   * @return the OrchestWorkflowEngine
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
