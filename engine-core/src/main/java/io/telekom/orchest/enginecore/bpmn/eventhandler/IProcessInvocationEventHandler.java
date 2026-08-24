package io.telekom.orchest.enginecore.bpmn.eventhandler;

import io.telekom.orchest.api.core.request.ProcessInvocationRequest;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import lombok.RequiredArgsConstructor;

/**
 * Event handler for processing process invocation requests. Triggers the start of a new process
 * instance.
 */
@RequiredArgsConstructor
public class IProcessInvocationEventHandler
    implements EventHandler<ProcessInvocationRequest, Void> {

  private final OrchestWorkflowEngine orchestWorkflowEngine;

  /**
   * Handles a {@link ProcessInvocationRequest} to start a new process instance.
   *
   * @param event The process invocation request details.
   */
  @Override
  public Void execute(ProcessInvocationRequest event) {
    orchestWorkflowEngine.startProcess(
        event.getProcessDefinitionId(),
        event.getVersion(),
        event.getProcessInstanceId(),
        event.getVariables());
    return null;
  }
}
