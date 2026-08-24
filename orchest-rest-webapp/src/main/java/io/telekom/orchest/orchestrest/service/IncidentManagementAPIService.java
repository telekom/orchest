package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import io.telekom.orchest.api.core.request.BatchInstanceRequest;
import io.telekom.orchest.api.core.request.BatchRaiseIncidentRequest;
import io.telekom.orchest.api.core.request.RaiseIncidentRequest;
import io.telekom.orchest.api.core.request.RetryProcessEvent;
import io.telekom.orchest.orchestrest.api.dto.IncidentDTO;
import io.telekom.orchest.orchestrest.api.dto.PagedRequestDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.event.EventProducer;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

/** Service for querying, raising, and resolving process instance incidents. */
@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentManagementAPIService {

  private final DataInteractionService dataInteractionService;
  private final EventProducer eventProducer;

  public Optional<IncidentDTO> getIncident(String processInstanceId) {
    Optional<ProcessInstance> processInstanceOpt =
        dataInteractionService.getProcessInstance(processInstanceId);
    if (processInstanceOpt.isEmpty()) {
      return Optional.empty();
    }
    ProcessInstance processInstance = processInstanceOpt.get();
    if (processInstance.getState() != PIState.INCIDENT) {
      return Optional.empty();
    }
    return Optional.of(buildIncidentDTO(processInstance));
  }

  public Page<IncidentDTO> getIncidents(PagedRequestDTO requestDTO) {
    requestDTO.setState("INCIDENT");
    Page<ProcessInstance> processInstances = dataInteractionService.getProcessInstances(requestDTO);
    return PageableExecutionUtils.getPage(
        processInstances.getContent().stream().map(this::buildIncidentDTO).toList(),
        processInstances.getPageable(),
        processInstances::getTotalElements);
  }

  public void resolveIncident(String processInstanceId) {
    Optional<ProcessInstance> processInstanceOpt =
        dataInteractionService.getProcessInstance(processInstanceId);
    if (processInstanceOpt.isEmpty()) {
      throw new RestExceptions("Incident not found with id: " + processInstanceId, 404);
    }
    ProcessInstance processInstance = processInstanceOpt.get();
    if (processInstance.getState() != PIState.INCIDENT) {
      throw new RestExceptions(
          "Process instance is not in INCIDENT state, current state: " + processInstance.getState(),
          409);
    }
    String targetInstanceId = resolveActualIncidentSource(processInstance);
    eventProducer.sendRetryEvent(new RetryProcessEvent(targetInstanceId, null, null));
  }

  private String resolveActualIncidentSource(ProcessInstance processInstance) {
    String sourceInstanceId = processInstance.getIncidentSourceInstanceId();
    if (sourceInstanceId == null) {
      return processInstance.getProcessInstanceId();
    }
    int maxDepth = 50;
    int depth = 0;
    String currentId = sourceInstanceId;
    while (depth < maxDepth) {
      Optional<ProcessInstance> sourceOpt = dataInteractionService.getProcessInstance(currentId);
      if (sourceOpt.isEmpty()) {
        log.warn(
            "Incident source instance {} not found in chain from {}",
            currentId,
            processInstance.getProcessInstanceId());
        return currentId;
      }
      ProcessInstance source = sourceOpt.get();
      if (source.getIncidentSourceInstanceId() == null) {
        return source.getProcessInstanceId();
      }
      currentId = source.getIncidentSourceInstanceId();
      depth++;
    }
    log.warn(
        "Exceeded max depth traversing incident source chain from {}",
        processInstance.getProcessInstanceId());
    return currentId;
  }

  public void resolveIncidentsBatch(BatchInstanceRequest batchInstanceRequest) {
    batchInstanceRequest
        .getProcessInstanceIds()
        .forEach(
            processInstanceId ->
                eventProducer.sendRetryEvent(new RetryProcessEvent(processInstanceId, null, null)));
  }

  public void raiseIncident(RaiseIncidentRequest request) {
    Optional<ProcessInstance> instanceOpt =
        dataInteractionService.getProcessInstance(request.getProcessInstanceId());
    if (instanceOpt.isEmpty()) {
      throw new RestExceptions(
          "Process instance not found: " + request.getProcessInstanceId(), 404);
    }
    ProcessInstance instance = getProcessInstance(request, instanceOpt);
    markAsIncident(instance, request.getIncidentMessage());
  }

  private static @NonNull ProcessInstance getProcessInstance(
      RaiseIncidentRequest request, Optional<ProcessInstance> instanceOpt) {
    ProcessInstance instance = instanceOpt.get();
    if (instance.getState() == PIState.INCIDENT) {
      throw new RestExceptions(
          "Process instance already in INCIDENT state: " + request.getProcessInstanceId(), 409);
    }
    if (instance.getState() == PIState.COMPLETED
        || instance.getState() == PIState.CANCELLED
        || instance.getState() == PIState.TERMINATED) {
      throw new RestExceptions(
          "Cannot raise incident on a terminated process instance: "
              + request.getProcessInstanceId(),
          409);
    }
    return instance;
  }

  public void raiseIncidentsBatch(BatchRaiseIncidentRequest request) {
    request
        .getProcessInstanceIds()
        .forEach(
            processInstanceId -> {
              Optional<ProcessInstance> instanceOpt =
                  dataInteractionService.getProcessInstance(processInstanceId);
              if (instanceOpt.isEmpty()) {
                log.warn("Skipping raise incident for missing instance: {}", processInstanceId);
                return;
              }
              ProcessInstance instance = instanceOpt.get();
              if (instance.getState() == PIState.INCIDENT
                  || instance.getState() == PIState.COMPLETED
                  || instance.getState() == PIState.CANCELLED
                  || instance.getState() == PIState.TERMINATED) {
                log.warn(
                    "Skipping raise incident for instance {} in state {}",
                    processInstanceId,
                    instance.getState());
                return;
              }
              markAsIncident(instance, request.getIncidentMessage());
            });
  }

  private void markAsIncident(ProcessInstance instance, String incidentMessage) {
    instance.setHasIncident(true);
    instance.setState(PIState.INCIDENT);
    instance.setIncidentMessage(incidentMessage);
    dataInteractionService.saveProcessInstance(instance);
  }

  private IncidentDTO buildIncidentDTO(ProcessInstance processInstance) {
    return IncidentDTO.builder()
        .processInstanceId(processInstance.getProcessInstanceId())
        .processDefinitionId(processInstance.getProcessDefinitionId())
        .incidentMessage(processInstance.getIncidentMessage())
        .activeElements(
            processInstance.getActiveNodeIds() != null
                ? processInstance.getActiveNodeIds().stream().toList()
                : new ArrayList<>())
        .state(processInstance.getState().name())
        .version(processInstance.getVersion())
        .createdAt(processInstance.getCreatedAt().toString())
        .completedAt(
            processInstance.getCompletedAt() != null
                ? processInstance.getCompletedAt().toString()
                : null)
        .build();
  }
}
