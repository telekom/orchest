package io.telekom.orchest.config.engine;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.adapter.mongo.*;
import io.telekom.orchest.api.core.adapters.data.model.ActivityState;
import io.telekom.orchest.api.core.adapters.data.repository.IncidentRepository;
import io.telekom.orchest.api.core.adapters.data.repository.MessageEventRepository;
import io.telekom.orchest.api.core.adapters.data.repository.SignalEventRepository;
import io.telekom.orchest.cache.core.Cache;
import io.telekom.orchest.enginecore.IncidentEventHandlerAdapter;
import io.telekom.orchest.enginecore.bpmn.ConnectorDispatchAdapter;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.ServiceTaskHandlerAdapter;
import io.telekom.orchest.enginecore.bpmn.execution.NodeExecutorRegistry;
import io.telekom.orchest.enginecore.bpmn.service.*;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import io.telekom.orchest.telemetry.metrics.MetricsRecorder;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests that {@link EngineConfigurations} correctly wires all bean factories and their
 * dependencies.
 */
@ExtendWith(MockitoExtension.class)
class EngineConfigurationsBeanFactoriesTest {

  @Mock private KafkaClientEventProducer kafkaClientEventProducer;

  @Mock private MongoProcessInstanceRepositoryAdapter mongoProcessInstanceRepositoryAdapter;

  @Mock private MongoProcessDefinitionRepositoryAdapter mongoProcessDefinitionRepositoryAdapter;

  @Mock
  private MongoDynamicProcessDefinitionRepositoryAdapter
      mongoDynamicProcessDefinitionRepositoryAdapter;

  @Mock private MongoDecisionDefinitionRepositoryAdapter mongoDecisionDefinitionRepositoryAdapter;

  @Mock private MongoDecisionInstanceRepositoryAdapter mongoDecisionInstanceRepositoryAdapter;

  @Mock private MongoTimedEventRepositoryAdapter mongoTimedEventRepositoryAdapter;

  @Mock private MongoMessageEventRepositoryAdapter mongoMessageEventRepositoryAdapter;

  @Mock private MongoSignalEventRepositoryAdapter mongoSignalEventRepositoryAdapter;

  @Mock private MongoWorkerRegistryRepositoryAdapter mongoWorkerRegistryRepositoryAdapter;

  @Mock private MongoPendingTaskRepositoryAdapter mongoPendingTaskRepositoryAdapter;

  @Mock private MongoUserTaskRepositoryAdapter mongoUserTaskRepositoryAdapter;

  @Mock private ConnectorDispatchAdapter connectorDispatchAdapter;

  @Mock private ServiceTaskHandlerAdapter serviceTaskHandlerAdapter;

  @Mock private Map<String, Boolean> commonWorkersMap;

  @Mock private IncidentEventHandlerAdapter incidentEventHandlerAdapter;

  @Mock private MessageEventRepository messageEventRepository;

  @Mock private SignalEventRepository signalEventRepository;

  @Mock private OrchestWorkflowEngine orchestWorkflowEngine;

  @Mock private OrchestEngineTelemetryService telemetryService;

  @Mock private Cache<String, ActivityState> activityStateCache;

  @Mock private MetricsRecorder metricsRecorder;

  private EngineConfigurations configuration;

  @BeforeEach
  void setUp() {
    configuration = new EngineConfigurations();
  }

  @Test
  @DisplayName("serviceTaskHandlerInterface wires Kafka producer")
  void serviceTaskHandlerInterface() {
    ServiceTaskHandlerAdapter adapter =
        configuration.serviceTaskHandlerInterface(kafkaClientEventProducer);
    assertNotNull(adapter);
  }

  @Test
  @DisplayName("incidentEventHandlerAdapter wires Kafka producer, incident repo, and alert SPI")
  void incidentEventHandlerAdapter() {
    IncidentEventHandlerAdapter adapter =
        configuration.incidentEventHandlerAdapter(
            kafkaClientEventProducer,
            mock(IncidentRepository.class),
            mock(org.springframework.beans.factory.ObjectProvider.class));
    assertNotNull(adapter);
  }

  @Test
  @DisplayName("nodeExecutorRegistry registers connector dispatcher")
  void nodeExecutorRegistry() {
    NodeExecutorRegistry registry =
        configuration.nodeExecutorRegistry(
            serviceTaskHandlerAdapter,
            orchestWorkflowEngine,
            connectorDispatchAdapter,
            commonWorkersMap);
    assertNotNull(registry);
  }

  @Test
  @DisplayName("connectorDispatchAdapter wires Kafka producer")
  void connectorDispatchAdapter() {
    ConnectorDispatchAdapter adapter =
        configuration.connectorDispatchAdapter(kafkaClientEventProducer);
    assertNotNull(adapter);
  }

  @Test
  @DisplayName("processInstanceService bean")
  void processInstanceService() {
    assertNotNull(configuration.processInstanceService(mongoProcessInstanceRepositoryAdapter));
  }

  @Test
  @DisplayName("processDefinitionService bean")
  void processDefinitionService() {
    EventRegisterService eventRegisterService = mock(EventRegisterService.class);
    assertNotNull(
        configuration.processDefinitionService(
            mongoProcessDefinitionRepositoryAdapter,
            mongoDynamicProcessDefinitionRepositoryAdapter,
            eventRegisterService,
            telemetryService));
  }

  @Test
  @DisplayName("decisionDefinitionService bean")
  void decisionDefinitionService() {
    assertNotNull(
        configuration.decisionDefinitionService(
            mongoDecisionDefinitionRepositoryAdapter, telemetryService));
  }

  @Test
  @DisplayName("timerEventRegistrar bean")
  void timerEventRegistrar() {
    assertNotNull(configuration.timerEventRegistrar(mongoTimedEventRepositoryAdapter));
  }

  @Test
  @DisplayName("messageEventRegistrar bean")
  void messageEventRegistrar() {
    assertNotNull(configuration.messageEventRegistrar(mongoMessageEventRepositoryAdapter));
  }

  @Test
  @DisplayName("signalEventRegistrar bean")
  void signalEventRegistrar() {
    assertNotNull(configuration.signalEventRegistrar(mongoSignalEventRepositoryAdapter));
  }

  @Test
  @DisplayName("eventRegisterService bean")
  void eventRegisterService() {
    assertNotNull(
        configuration.eventRegisterService(
            configuration.timerEventRegistrar(mongoTimedEventRepositoryAdapter),
            configuration.messageEventRegistrar(mongoMessageEventRepositoryAdapter),
            configuration.signalEventRegistrar(mongoSignalEventRepositoryAdapter),
            mongoWorkerRegistryRepositoryAdapter,
            mongoPendingTaskRepositoryAdapter));
  }

  @Test
  @DisplayName("decisionInstanceService bean")
  void decisionInstanceService() {
    assertNotNull(
        configuration.decisionInstanceService(
            mongoDecisionInstanceRepositoryAdapter, metricsRecorder));
  }

  @Test
  @DisplayName("userTaskService bean")
  void userTaskService() {
    assertNotNull(configuration.userTaskService(mongoUserTaskRepositoryAdapter));
  }

  @Test
  @DisplayName("oweDependencies aggregates engine services")
  void oweDependencies() {
    ProcessDefinitionService processDefinitionService =
        configuration.processDefinitionService(
            mongoProcessDefinitionRepositoryAdapter,
            mongoDynamicProcessDefinitionRepositoryAdapter,
            mock(EventRegisterService.class),
            telemetryService);
    ProcessInstanceService processInstanceService =
        configuration.processInstanceService(mongoProcessInstanceRepositoryAdapter);
    DecisionDefinitionService decisionDefinitionService =
        configuration.decisionDefinitionService(
            mongoDecisionDefinitionRepositoryAdapter, telemetryService);
    DecisionInstanceService decisionInstanceService =
        configuration.decisionInstanceService(
            mongoDecisionInstanceRepositoryAdapter, metricsRecorder);
    EventRegisterService eventRegisterService =
        configuration.eventRegisterService(
            configuration.timerEventRegistrar(mongoTimedEventRepositoryAdapter),
            configuration.messageEventRegistrar(mongoMessageEventRepositoryAdapter),
            configuration.signalEventRegistrar(mongoSignalEventRepositoryAdapter),
            mongoWorkerRegistryRepositoryAdapter,
            mongoPendingTaskRepositoryAdapter);
    NodeExecutorRegistry nodeExecutorRegistry =
        configuration.nodeExecutorRegistry(
            serviceTaskHandlerAdapter,
            orchestWorkflowEngine,
            connectorDispatchAdapter,
            commonWorkersMap);
    UserTaskService userTaskService = configuration.userTaskService(mongoUserTaskRepositoryAdapter);

    assertNotNull(
        configuration.oweDependencies(
            processDefinitionService,
            processInstanceService,
            decisionDefinitionService,
            decisionInstanceService,
            eventRegisterService,
            nodeExecutorRegistry,
            serviceTaskHandlerAdapter,
            incidentEventHandlerAdapter,
            messageEventRepository,
            signalEventRepository,
            userTaskService,
            metricsRecorder));
  }

  @Test
  @DisplayName("orchestWorkflowEngine bean")
  void orchestWorkflowEngine() {
    var owe =
        configuration.oweDependencies(
            configuration.processDefinitionService(
                mongoProcessDefinitionRepositoryAdapter,
                mongoDynamicProcessDefinitionRepositoryAdapter,
                mock(EventRegisterService.class),
                telemetryService),
            configuration.processInstanceService(mongoProcessInstanceRepositoryAdapter),
            configuration.decisionDefinitionService(
                mongoDecisionDefinitionRepositoryAdapter, telemetryService),
            configuration.decisionInstanceService(
                mongoDecisionInstanceRepositoryAdapter, metricsRecorder),
            configuration.eventRegisterService(
                configuration.timerEventRegistrar(mongoTimedEventRepositoryAdapter),
                configuration.messageEventRegistrar(mongoMessageEventRepositoryAdapter),
                configuration.signalEventRegistrar(mongoSignalEventRepositoryAdapter),
                mongoWorkerRegistryRepositoryAdapter,
                mongoPendingTaskRepositoryAdapter),
            configuration.nodeExecutorRegistry(
                serviceTaskHandlerAdapter,
                orchestWorkflowEngine,
                connectorDispatchAdapter,
                commonWorkersMap),
            serviceTaskHandlerAdapter,
            incidentEventHandlerAdapter,
            messageEventRepository,
            signalEventRepository,
            configuration.userTaskService(mongoUserTaskRepositoryAdapter),
            metricsRecorder);
    assertNotNull(configuration.orchestWorkflowEngine(owe, telemetryService, activityStateCache));
  }
}
