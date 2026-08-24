package io.telekom.orchest.config.engine;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.telekom.orchest.adapter.mongo.MongoMessageEventRepositoryAdapter;
import io.telekom.orchest.adapter.mongo.MongoSignalEventRepositoryAdapter;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.eventhandler.EventHandler;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests that {@link EngineEventHandlersConfigurations} correctly creates all event handler beans.
 */
@ExtendWith(MockitoExtension.class)
class EngineEventHandlersConfigurationsTest {

  @Mock private OrchestWorkflowEngine orchestWorkflowEngine;

  @Mock private OrchestEngineTelemetryService telemetryService;

  @Mock private MongoMessageEventRepositoryAdapter messageEventRepositoryAdapter;

  @Mock private MongoSignalEventRepositoryAdapter signalEventRepositoryAdapter;

  private EngineEventHandlersConfigurations configuration;

  @BeforeEach
  void setUp() {
    configuration = new EngineEventHandlersConfigurations();
  }

  @Test
  @DisplayName("deploymentEventHandler bean")
  void deploymentEventHandler() {
    EventHandler<ResourceDeploymentRequest, Object> handler =
        configuration.deploymentEventHandler(orchestWorkflowEngine);
    assertNotNull(handler);
  }

  @Test
  @DisplayName("messageEventRequestEventHandler bean")
  void messageEventRequestEventHandler() {
    EventHandler<MessageEventRequest, Void> handler =
        configuration.messageEventRequestEventHandler(
            orchestWorkflowEngine, messageEventRepositoryAdapter);
    assertNotNull(handler);
  }

  @Test
  @DisplayName("pendingTaskRequestEventHandler bean")
  void pendingTaskRequestEventHandler() {
    EventHandler<PendingTaskRequest, Void> handler =
        configuration.pendingTaskRequestEventHandler(orchestWorkflowEngine);
    assertNotNull(handler);
  }

  @Test
  @DisplayName("processInvocationRequestEventHandler bean")
  void processInvocationRequestEventHandler() {
    EventHandler<ProcessInvocationRequest, Void> handler =
        configuration.processInvocationRequestEventHandler(orchestWorkflowEngine);
    assertNotNull(handler);
  }

  @Test
  @DisplayName("dynamicProcessInvocationRequestEventHandler bean")
  void dynamicProcessInvocationRequestEventHandler() {
    EventHandler<DynamicProcessInvocationRequest, Void> handler =
        configuration.dynamicProcessInvocationRequestEventHandler(orchestWorkflowEngine);
    assertNotNull(handler);
  }

  @Test
  @DisplayName("retryProcessEventEventHandler bean")
  void retryProcessEventEventHandler() {
    EventHandler<RetryProcessEvent, Void> handler =
        configuration.retryProcessEventEventHandler(orchestWorkflowEngine);
    assertNotNull(handler);
  }

  @Test
  @DisplayName("signalEventRequestEventHandler bean")
  void signalEventRequestEventHandler() {
    EventHandler<SignalEventRequest, Void> handler =
        configuration.signalEventRequestEventHandler(
            orchestWorkflowEngine, signalEventRepositoryAdapter);
    assertNotNull(handler);
  }

  @Test
  @DisplayName("workerEventRequestEventHandler bean")
  void workerEventRequestEventHandler() {
    EventHandler<WorkerEventRequest, Void> handler =
        configuration.workerEventRequestEventHandler(orchestWorkflowEngine, telemetryService);
    assertNotNull(handler);
  }

  @Test
  @DisplayName("workerRegistryRequestEventHandler bean")
  void workerRegistryRequestEventHandler() {
    EventHandler<WorkerRegistryRequest, Void> handler =
        configuration.workerRegistryRequestEventHandler(orchestWorkflowEngine);
    assertNotNull(handler);
  }
}
