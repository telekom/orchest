package io.telekom.orchest.enginecore.bpmn.eventhandler;

import io.telekom.orchest.api.core.request.DynamicProcessInvocationRequest;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import lombok.RequiredArgsConstructor;

/**
 * Event handler for processing dynamic process invocation requests. Triggers the start of a new
 * process instance.
 */
@RequiredArgsConstructor
public class IDynamicProcessInvocationEventHandler
    implements EventHandler<DynamicProcessInvocationRequest, Void> {

  private final OrchestWorkflowEngine orchestWorkflowEngine;

  /**
   * Handles a {@link DynamicProcessInvocationRequest} to start a new process instance.
   *
   * @param event The process invocation request details.
   */
  @Override
  public Void execute(DynamicProcessInvocationRequest event) {
    orchestWorkflowEngine.startDynamicProcess(
        event.getUtf8BpmnXML(), event.getProcessInstanceId(), event.getVariables());
    return null;
  }
}
