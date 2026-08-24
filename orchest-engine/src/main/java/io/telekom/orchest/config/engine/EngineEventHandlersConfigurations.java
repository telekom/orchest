package io.telekom.orchest.config.engine;

import io.telekom.orchest.adapter.mongo.MongoMessageEventRepositoryAdapter;
import io.telekom.orchest.adapter.mongo.MongoSignalEventRepositoryAdapter;
import io.telekom.orchest.api.core.adapters.data.dto.ResumeActivityEventRequest;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.eventhandler.*;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for defining engine event handlers. Each handler processes a specific type of
 * event (e.g., process invocation, deployment, worker events) and delegates to the workflow engine.
 */
@Configuration
public class EngineEventHandlersConfigurations {

  /**
   * Creates a handler for resource deployment events.
   *
   * @param orchestWorkflowEngine The workflow engine.
   * @return The deployment event handler.
   */
  @Bean
  public EventHandler<ResourceDeploymentRequest, Object> deploymentEventHandler(
      OrchestWorkflowEngine orchestWorkflowEngine) {
    return new IDeploymentEventHandler(orchestWorkflowEngine);
  }

  /**
   * Creates a handler for resume activity events.
   *
   * @param orchestWorkflowEngine The workflow engine.
   * @return The resume activity event handler.
   */
  @Bean
  public EventHandler<ResumeActivityEventRequest, Void> resumeActivityEventHandler(
      OrchestWorkflowEngine orchestWorkflowEngine) {
    return new IResumeActivityEventHandler(orchestWorkflowEngine);
  }

  /**
   * Creates a handler for message event requests.
   *
   * @param orchestWorkflowEngine The workflow engine.
   * @param mongoMessageEventRepositoryAdapter The repository adapter for message events.
   * @return The message event handler.
   */
  @Bean
  public EventHandler<MessageEventRequest, Void> messageEventRequestEventHandler(
      OrchestWorkflowEngine orchestWorkflowEngine,
      MongoMessageEventRepositoryAdapter mongoMessageEventRepositoryAdapter) {
    return new IMessageIntermediateThrowEventEventHandler(orchestWorkflowEngine);
  }

  /**
   * Creates a handler for pending task requests.
   *
   * @param orchestWorkflowEngine The workflow engine.
   * @return The pending task handler.
   */
  @Bean
  public EventHandler<PendingTaskRequest, Void> pendingTaskRequestEventHandler(
      OrchestWorkflowEngine orchestWorkflowEngine) {
    return new IPendingTaskRegisterEventHandler(orchestWorkflowEngine);
  }

  /**
   * Creates a handler for process invocation requests.
   *
   * @param orchestWorkflowEngine The workflow engine.
   * @return The process invocation handler.
   */
  @Bean
  public EventHandler<ProcessInvocationRequest, Void> processInvocationRequestEventHandler(
      OrchestWorkflowEngine orchestWorkflowEngine) {
    return new IProcessInvocationEventHandler(orchestWorkflowEngine);
  }

  /**
   * Creates a handler for process invocation requests.
   *
   * @param orchestWorkflowEngine The workflow engine.
   * @return The process invocation handler.
   */
  @Bean
  public EventHandler<DynamicProcessInvocationRequest, Void>
      dynamicProcessInvocationRequestEventHandler(OrchestWorkflowEngine orchestWorkflowEngine) {
    return new IDynamicProcessInvocationEventHandler(orchestWorkflowEngine);
  }

  /**
   * Creates a handler for retry process events.
   *
   * @param orchestWorkflowEngine The workflow engine.
   * @return The retry process handler.
   */
  @Bean
  public EventHandler<RetryProcessEvent, Void> retryProcessEventEventHandler(
      OrchestWorkflowEngine orchestWorkflowEngine) {
    return new IRetryProcessInstanceEventHandler(orchestWorkflowEngine);
  }

  /**
   * Creates a handler for signal event requests.
   *
   * @param orchestWorkflowEngine The workflow engine.
   * @param signalEventRepository The repository adapter for signal events.
   * @return The signal event handler.
   */
  @Bean
  public EventHandler<SignalEventRequest, Void> signalEventRequestEventHandler(
      OrchestWorkflowEngine orchestWorkflowEngine,
      MongoSignalEventRepositoryAdapter signalEventRepository) {
    return new ISignalIntermediateThrowEventEventHandler(orchestWorkflowEngine);
  }

  /**
   * Creates a handler for worker event requests.
   *
   * @param orchestWorkflowEngine The workflow engine.
   * @return The worker event handler.
   */
  @Bean
  public EventHandler<WorkerEventRequest, Void> workerEventRequestEventHandler(
      OrchestWorkflowEngine orchestWorkflowEngine, OrchestEngineTelemetryService telemetryService) {
    return new IWorkerEventEventHandler(orchestWorkflowEngine, telemetryService);
  }

  /**
   * Creates a handler for worker registry requests.
   *
   * @param orchestWorkflowEngine The workflow engine.
   * @return The worker registry handler.
   */
  @Bean
  public EventHandler<WorkerRegistryRequest, Void> workerRegistryRequestEventHandler(
      OrchestWorkflowEngine orchestWorkflowEngine) {
    return new IWorkerRegistryEventHandler(orchestWorkflowEngine);
  }
}
