package io.telekom.orchest.enginecore.bpmn.eventhandler;

import io.telekom.orchest.api.core.request.RetryProcessEvent;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import lombok.RequiredArgsConstructor;

/** Event handler that retries a failed activity in a process instance after an incident. */
@RequiredArgsConstructor
public class IRetryProcessInstanceEventHandler implements EventHandler<RetryProcessEvent, Void> {
  private final OrchestWorkflowEngine orchestWorkflowEngine;

  @Override
  public Void execute(RetryProcessEvent data) {
    orchestWorkflowEngine.retryActivity(data);
    return null;
  }
}
