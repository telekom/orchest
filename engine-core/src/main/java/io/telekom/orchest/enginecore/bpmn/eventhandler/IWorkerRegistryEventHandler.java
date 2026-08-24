package io.telekom.orchest.enginecore.bpmn.eventhandler;

import io.telekom.orchest.api.core.request.WorkerRegistryRequest;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Event handler that updates the worker registry and re-triggers any pending tasks. */
@Slf4j
@RequiredArgsConstructor
public class IWorkerRegistryEventHandler implements EventHandler<WorkerRegistryRequest, Void> {

  private final OrchestWorkflowEngine orchestWorkflowEngine;

  @Override
  public Void execute(WorkerRegistryRequest data) {
    log.info("received worker registry request from namespace: {}", data.getNamespace());
    orchestWorkflowEngine.updateWorkerRegistry(data);
    log.info("updated worker registry from namespace: {}", data.getNamespace());
    log.info("Starting pending task thread from namespace: {}", data.getNamespace());
    // re-trigger pending task
    orchestWorkflowEngine.startMissedTask(data.getWorkers());
    return null;
  }
}
