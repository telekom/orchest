package io.telekom.orchest.enginecore.bpmn.eventhandler;

import io.telekom.orchest.api.core.adapters.data.dto.ResumeActivityEventRequest;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Event handler that resumes a waiting activity in a process instance with optional variables. */
@Slf4j
@RequiredArgsConstructor
public class IResumeActivityEventHandler implements EventHandler<ResumeActivityEventRequest, Void> {

  private final OrchestWorkflowEngine orchestWorkflowEngine;

  @Override
  public Void execute(ResumeActivityEventRequest event) {
    String processInstanceId = event.getProcessInstanceId();
    String activityId = event.getActivityId();

    orchestWorkflowEngine.resumeActivity(processInstanceId, activityId, event.getVariables());
    return null;
  }
}
