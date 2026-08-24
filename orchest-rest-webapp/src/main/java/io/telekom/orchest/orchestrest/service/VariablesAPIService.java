package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.api.request.VariablesRequest;
import io.telekom.orchest.orchestrest.api.request.VariablesResponse;
import io.telekom.orchest.telemetry.OrchestRestTelemetryService;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Service for reading and modifying process instance variables via the REST API. */
@Component
@RequiredArgsConstructor
public class VariablesAPIService {

  private final DataInteractionService dataInteractionService;
  private final OrchestRestTelemetryService orchestRestTelemetryService;

  public VariablesResponse getVariables(String processInstanceId) {
    Optional<ProcessInstance> optionalProcessInstance =
        dataInteractionService.getProcessInstance(processInstanceId);
    if (optionalProcessInstance.isEmpty()) {
      throw new RestExceptions("ProcessInstance not found with id: " + processInstanceId, 404);
    }
    return new VariablesResponse(optionalProcessInstance.get().getVariables());
  }

  private static final Set<PIState> TERMINAL_STATES =
      Set.of(PIState.COMPLETED, PIState.CANCELLED, PIState.TERMINATED);

  public VariablesResponse modifyVariables(VariablesRequest variablesRequest) {
    String processInstanceId = variablesRequest.getProcessInstanceId();
    Optional<ProcessInstance> optionalProcessInstance =
        dataInteractionService.getProcessInstance(processInstanceId);
    if (optionalProcessInstance.isEmpty()) {
      throw new RestExceptions("ProcessInstance not found with id: " + processInstanceId, 404);
    }
    ProcessInstance processInstance = optionalProcessInstance.get();
    if (TERMINAL_STATES.contains(processInstance.getState())) {
      throw new RestExceptions(
          "Cannot modify variables on a process instance in "
              + processInstance.getState()
              + " state",
          409);
    }
    Map<String, Object> currentVariables = processInstance.getVariables();
    Map<String, Object> newVariables = variablesRequest.getVariables();

    if (Objects.requireNonNull(variablesRequest.getAction()) == VariablesRequest.Action.DELETE) {
      newVariables.keySet().forEach(currentVariables::remove);
    } else {
      currentVariables.putAll(newVariables);
    }

    processInstance.setVariables(currentVariables);
    dataInteractionService.saveProcessInstance(processInstance);
    orchestRestTelemetryService.incrementVariablesUpdateCounter();
    return new VariablesResponse(processInstance.getVariables());
  }
}
