package io.telekom.orchest.enginecore.bpmn.eventhandler;

import io.telekom.orchest.api.core.request.MessageEventRequest;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Event handler that delivers inbound message events to the workflow engine for correlation. */
@Slf4j
@RequiredArgsConstructor
public class IMessageIntermediateThrowEventEventHandler
    implements EventHandler<MessageEventRequest, Void> {

  private final OrchestWorkflowEngine orchestWorkflowEngine;

  @Override
  public Void execute(MessageEventRequest event) {
    orchestWorkflowEngine.handleMessageEvent(
        event.getMessageName(), event.getCorrelationKey(), event.getVariables());
    return null;
  }
}
