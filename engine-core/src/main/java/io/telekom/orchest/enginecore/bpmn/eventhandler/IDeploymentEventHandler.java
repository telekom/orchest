package io.telekom.orchest.enginecore.bpmn.eventhandler;

import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event handler for resource deployment events. Handles deployment of BPMN process definitions and
 * DMN decision definitions.
 */
@Slf4j
@RequiredArgsConstructor
public class IDeploymentEventHandler implements EventHandler<ResourceDeploymentRequest, Object> {

  private final OrchestWorkflowEngine workflowEngine;

  /**
   * Processes a {@link ResourceDeploymentRequest}. Determines the resource type (BPMN or DMN) based
   * on content and delegates to the workflow engine.
   *
   * @param event The resource deployment request.
   */
  @Override
  public Object execute(ResourceDeploymentRequest event) {
    log.info("received deployment event request.");
    if (event.getResourceUTF8XML().contains("bpmn:definitions")) {
      return workflowEngine.deployProcessDefinition(event.getResourceUTF8XML(), false);
    } else {
      return workflowEngine.deployDecisionDefinition(event.getResourceUTF8XML());
    }
  }
}
