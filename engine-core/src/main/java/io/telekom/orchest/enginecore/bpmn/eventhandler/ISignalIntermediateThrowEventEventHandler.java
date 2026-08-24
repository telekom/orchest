package io.telekom.orchest.enginecore.bpmn.eventhandler;

import io.telekom.orchest.api.core.request.SignalEventRequest;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Event handler that delivers inbound signal events to the workflow engine for broadcast. */
@Slf4j
@RequiredArgsConstructor
public class ISignalIntermediateThrowEventEventHandler
    implements EventHandler<SignalEventRequest, Void> {

  private final OrchestWorkflowEngine orchestWorkflowEngine;

  @Override
  public Void execute(SignalEventRequest event) {
    orchestWorkflowEngine.handleSignalEvent(event.getSignalName(), event.getVariables());
    return null;
  }
}
