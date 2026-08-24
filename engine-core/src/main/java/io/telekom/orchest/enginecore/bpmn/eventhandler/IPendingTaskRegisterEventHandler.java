package io.telekom.orchest.enginecore.bpmn.eventhandler;

import io.telekom.orchest.api.core.request.PendingTaskRequest;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import lombok.RequiredArgsConstructor;

/** Event handler that registers a pending task when no worker is available to execute it. */
@RequiredArgsConstructor
public class IPendingTaskRegisterEventHandler implements EventHandler<PendingTaskRequest, Void> {

  private final OrchestWorkflowEngine orchestWorkflowEngine;

  @Override
  public Void execute(PendingTaskRequest data) {
    orchestWorkflowEngine.registerPendingTask(data);
    return null;
  }
}
