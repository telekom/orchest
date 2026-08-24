package io.telekom.orchest.configuration.engine;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.adapter.mongo.*;
import io.telekom.orchest.api.core.adapters.data.repository.IncidentRepository;
import io.telekom.orchest.enginecore.IncidentEventHandlerAdapter;
import io.telekom.orchest.enginecore.bpmn.OWEDependencies;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.ServiceTaskHandlerAdapter;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutorRegistry;
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

/** Spring configuration that wires up the OrchesT workflow engine and its dependencies. */
@Configuration
@EnableConfigurationProperties(EngineProperties.class)
public class EngineConfigurations {

  /**
   * Creates the service task handler adapter that dispatches tasks via Kafka.
   *
   * @param kafkaClientEventProducer the Kafka event producer
   * @return the service task handler adapter
   */
  @Bean
  public ServiceTaskHandlerAdapter serviceTaskHandlerInterface(
      KafkaClientEventProducer kafkaClientEventProducer) {
    return new ServiceTaskHandler(kafkaClientEventProducer);
  }

  /**
   * Creates the incident event handler that persists incidents and publishes via Kafka.
   *
   * @param kafkaClientEventProducer the Kafka event producer
   * @param incidentRepository the incident persistence repository
   * @return the incident event handler adapter
   */
  @Bean
  public IncidentEventHandlerAdapter incidentEventHandlerAdapter(
      KafkaClientEventProducer kafkaClientEventProducer, IncidentRepository incidentRepository) {
    return new IncidentEventHandler(kafkaClientEventProducer, incidentRepository);
  }

  /**
   * Creates the node executor registry used for dispatching BPMN node executions.
   *
   * @param serviceTaskHandlerAdapter service task handler
   * @param orchestWorkflowEngine the workflow engine (lazy to break circular dep)
   * @param commonWorkersMap map of common worker types
   * @return the node executor registry
   */
  @Bean
  public NodeExecutorRegistry nodeExecutorRegistry(
      ServiceTaskHandlerAdapter serviceTaskHandlerAdapter,
      @Lazy OrchestWorkflowEngine orchestWorkflowEngine,
      @Qualifier("commonWorkersMap") Map<String, Boolean> commonWorkersMap) {
    return new NodeExecutorRegistry(
        new FeelConditionEvaluator(),
        serviceTaskHandlerAdapter,
        orchestWorkflowEngine,
        commonWorkersMap);
  }

  /**
   * Creates the process instance service.
   *
   * @param mongoProcessInstanceRepositoryAdapter the Mongo repository adapter
   * @return the process instance service
   */
  @Bean
  public ProcessInstanceService processInstanceService(
      MongoProcessInstanceRepositoryAdapter mongoProcessInstanceRepositoryAdapter) {
    return new ProcessInstanceService(mongoProcessInstanceRepositoryAdapter);
  }

  /**
   * Creates the process definition service.
   *
   * @param mongoProcessDefinitionRepositoryAdapter static definition repo adapter
   * @param mongoDynamicProcessDefinitionRepositoryAdapter dynamic definition repo adapter
   * @param eventRegisterService event registration service
   * @param telemetryService telemetry service
   * @return the process definition service
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
   * @param mongoDecisionDefinitionRepositoryAdapter decision definition repo adapter
   * @param telemetryService telemetry service
   * @return the decision definition service
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
   * @param mongoTimedEventRepositoryAdapter the timed event repo adapter
   * @return the timer event registrar
   */
  @Bean
  public TimerEventRegistrar timerEventRegistrar(
      MongoTimedEventRepositoryAdapter mongoTimedEventRepositoryAdapter) {
    return new TimerEventRegistrar(mongoTimedEventRepositoryAdapter);
  }

  /**
   * Creates the message event registrar.
   *
   * @param mongoMessageEventRepositoryAdapter the message event repo adapter
   * @return the message event registrar
   */
  @Bean
  public MessageEventRegistrar messageEventRegistrar(
      MongoMessageEventRepositoryAdapter mongoMessageEventRepositoryAdapter) {
    return new MessageEventRegistrar(mongoMessageEventRepositoryAdapter);
  }

  /**
   * Creates the signal event registrar.
   *
   * @param mongoSignalEventRepositoryAdapter the signal event repo adapter
   * @return the signal event registrar
   */
  @Bean
  public SignalEventRegistrar signalEventRegistrar(
      MongoSignalEventRepositoryAdapter mongoSignalEventRepositoryAdapter) {
    return new SignalEventRegistrar(mongoSignalEventRepositoryAdapter);
  }

  /**
   * Creates the event register service coordinating timer, message, and signal registrars.
   *
   * @param timerEventRegistrar timer event registrar
   * @param messageEventRegistrar message event registrar
   * @param signalEventRegistrar signal event registrar
   * @param mongoWorkerRegistryRepositoryAdapter worker registry repo adapter
   * @param mongoPendingTaskRepositoryAdapter pending task repo adapter
   * @return the event register service
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
   * Creates the decision instance service for DMN execution tracking.
   *
   * @param mongoDecisionInstanceRepositoryAdapter decision instance repo adapter
   * @param metricsRecorder metrics recorder
   * @return the decision instance service
   */
  @Bean
  public DecisionInstanceService decisionInstanceService(
      MongoDecisionInstanceRepositoryAdapter mongoDecisionInstanceRepositoryAdapter,
      MetricsRecorder metricsRecorder) {
    return new DecisionInstanceService(mongoDecisionInstanceRepositoryAdapter, metricsRecorder);
  }

  /**
   * Assembles the dependency bundle for the OrchesT Workflow Engine.
   *
   * @return the OWE dependencies container
   */
  @Bean
  public OWEDependencies oweDependencies(
      ProcessInstanceService processInstanceService,
      ProcessDefinitionService processDefinitionService,
      DecisionDefinitionService decisionDefinitionService,
      DecisionInstanceService decisionInstanceService,
      EventRegisterService eventRegisterService,
      NodeExecutorRegistry nodeExecutorRegistry,
      ServiceTaskHandlerAdapter serviceTaskHandlerInterface,
      IncidentEventHandlerAdapter incidentEventHandlerAdapter,
      MetricsRecorder metricsRecorder) {
    return new OWEDependencies(
        processDefinitionService,
        processInstanceService,
        decisionDefinitionService,
        decisionInstanceService,
        eventRegisterService,
        nodeExecutorRegistry,
        serviceTaskHandlerInterface,
        incidentEventHandlerAdapter,
        null,
        null,
        null,
        metricsRecorder);
  }

  /**
   * Creates the OrchesT Workflow Engine.
   *
   * @param oweDependencies the engine dependency bundle
   * @param telemetryService telemetry service
   * @return the workflow engine
   */
  @Bean
  public OrchestWorkflowEngine orchestWorkflowEngine(
      OWEDependencies oweDependencies, OrchestEngineTelemetryService telemetryService) {
    return new OrchestWorkflowEngine(oweDependencies, telemetryService, null);
  }
}
