package io.telekom.orchest.orchestrest.service.processInstance;

import static io.telekom.orchest.orchestrest.configurations.security.AuthWebFilter.READ_USER_NON_SENSITIVE;
import static io.telekom.orchest.orchestrest.extensions.sensitivevariables.model.ProcessSensitiveVariables.DEFAULT_PLACEHOLDER;

import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.telekom.orchest.adapter.mongo.repository.MongoProcessInstanceRepository;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.ExecutionLogEntry;
import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.api.core.response.CompensateInstanceResponse;
import io.telekom.orchest.api.core.response.ProcessInvocationResponse;
import io.telekom.orchest.api.core.utils.IDGenerator;
import io.telekom.orchest.enginecore.bpmn.service.EventRegisterService;
import io.telekom.orchest.enginecore.bpmn.service.UserTaskService;
import io.telekom.orchest.enginecore.bpmn.utils.BPMNUtils;
import io.telekom.orchest.orchestrest.api.dto.PagedRequestDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceScrollDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceStats;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.api.validators.PageScrollValidator;
import io.telekom.orchest.orchestrest.api.validators.ProcessDefinitionValidator;
import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import io.telekom.orchest.orchestrest.event.EventProducer;
import io.telekom.orchest.orchestrest.extensions.sensitivevariables.ProcessSensitiveVariablesService;
import io.telekom.orchest.orchestrest.extensions.sensitivevariables.model.ProcessSensitiveVariables;
import io.telekom.orchest.orchestrest.service.processdefinition.ProcessDefinitionDataService;
import io.telekom.orchest.telemetry.OrchestRestTelemetryService;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

/** Service for process instance lifecycle operations (create, cancel, retry, compensate, query). */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessInstanceAPIService {

  /** Maximum number of rows a single scroll request may cover ({@code to - from}). */
  private final ProcessInstanceDataService processInstanceDataService;

  private final ProcessDefinitionDataService processDefinitionDataService;

  private final EventProducer eventProducer;
  private final OrchestRestTelemetryService orchestRestTelemetryService;
  private final MongoProcessInstanceRepository mongoProcessInstanceRepository;
  private final UserTaskService userTaskService;
  private final EventRegisterService eventRegisterService;
  private final ProcessSensitiveVariablesService processSensitiveVariablesService;

  public ProcessInvocationResponse createProcessInstance(
      ProcessInvocationRequest processInvocationRequest) {
    if (processInvocationRequest.getProcessInstanceId() == null) {
      processInvocationRequest.setProcessInstanceId(IDGenerator.generate());
    }
    String processDefinitionId = processInvocationRequest.getProcessDefinitionId();
    Integer version = processInvocationRequest.getVersion();
    Optional<ProcessDefinition> processDefinition;

    if (version == null) {
      processDefinition = processDefinitionDataService.getProcessDefinition(processDefinitionId);
    } else {
      processDefinition =
          processDefinitionDataService.getProcessDefinitionsWithVersion(
              processDefinitionId, version);
    }

    ProcessDefinition executable =
        ProcessDefinitionValidator.getIfExecutable(processDefinition, processDefinitionId);

    processInvocationRequest.setVersion(executable.getVersion());
    eventProducer.sendProcessInvocationEvent(processInvocationRequest);
    return new ProcessInvocationResponse(
        processInvocationRequest.getProcessInstanceId(),
        processDefinitionId,
        processInvocationRequest.getVersion());
  }

  public Optional<ProcessInstanceDTO> getProcessInstance(
      String processInstanceId, LoggedInUserContext userContext) {
    Optional<ProcessInstance> processInstanceOpt =
        processInstanceDataService.getProcessInstance(processInstanceId);
    if (processInstanceOpt.isEmpty()) {
      return Optional.empty();
    }
    ProcessInstance processInstance = processInstanceOpt.get();
    BpmnModelInstance bpmnModelInstance =
        BPMNUtils.getBpmnModelInstance(processInstance.getProcessDefinition().getDefinitionXML());
    Map<String, ExecutionLogEntry> sequenceExecutions = processInstance.getExecutionHistory();
    ProcessInstanceDTO processInstanceDTO =
        buildProcessInstanceDTO(processInstance, false, userContext);
    processInstanceDTO.setBpmnXML(BPMNUtils.addStrokes(bpmnModelInstance, sequenceExecutions));
    return Optional.of(processInstanceDTO);
  }

  public ProcessInstanceStats getProcessInstancesStats() {
    return processInstanceDataService.getProcessInstanceStats();
  }

  public Page<ProcessInstanceDTO> getProcessInstances(
      PagedRequestDTO requestDTO, LoggedInUserContext userContext) {
    Page<ProcessInstance> processInstances = getPageData(requestDTO);
    return PageableExecutionUtils.getPage(
        processInstances.getContent().stream()
            .map(pi -> buildProcessInstanceDTO(pi, true, userContext))
            .toList(),
        processInstances.getPageable(),
        processInstances::getTotalElements);
  }

  public Page<ProcessInstance> getPageData(PagedRequestDTO requestDTO) {
    return processInstanceDataService.getPageData(requestDTO);
  }

  /**
   * Scrolls process instances by offset range over the filtered, sorted sequence (half-open
   * interval {@code [from, to)}).
   *
   * <p>Does not compute a total matching count (no {@code count} query): {@code hasNext} is derived
   * by fetching one extra row past the requested window.
   */
  public ProcessInstanceScrollDTO scrollProcessInstances(
      PagedRequestDTO filterDTO, int from, int toExclusive, LoggedInUserContext userContext) {
    PageScrollValidator.validateScroll(from, toExclusive);
    int windowSize = toExclusive - from;
    List<ProcessInstance> raw =
        processInstanceDataService.getProcessInstancesSlice(filterDTO, from, windowSize + 1);
    boolean hasNext = raw.size() > windowSize;
    List<ProcessInstance> pageRows = hasNext ? raw.subList(0, windowSize) : raw;
    List<ProcessInstanceDTO> content =
        pageRows.stream().map(pi -> buildProcessInstanceDTO(pi, true, userContext)).toList();
    return ProcessInstanceScrollDTO.builder()
        .content(content)
        .from(from)
        .to(toExclusive)
        .hasNext(hasNext)
        .build();
  }

  public void cancelInstance(CancelInstanceRequest cancelInstanceRequest) {
    List<CancelInstanceRequest.Request> instances = cancelInstanceRequest.getInstances();
    if (instances == null || instances.isEmpty()) {
      log.info("processInstanceIds list is empty");
      return;
    }
    List<String> instanceIds =
        instances.stream()
            .map(CancelInstanceRequest.Request::instanceId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

    instanceIds.forEach(this::cancelWithCascade);

    instances.forEach(
        instance ->
            orchestRestTelemetryService.incrementCancelInstanceCounter(
                instance.processDefinitionId()));
  }

  /**
   * Cancels multiple process instances by ID. Caller must validate request (non-empty, max 500 IDs)
   * before invoking. Async dispatch is handled by the reactive controller via
   * Schedulers.boundedElastic().
   */
  public void cancelInstancesBatch(BatchInstanceRequest request) {
    List<String> ids = request.getProcessInstanceIds();
    if (ids == null || ids.isEmpty()) {
      log.info("batch cancel: processInstanceIds list is empty");
      return;
    }
    List<String> distinctIds = ids.stream().filter(Objects::nonNull).distinct().toList();

    distinctIds.forEach(this::cancelWithCascade);

    log.info(
        "batch cancel: cascading cancellation completed for {} root instance ids",
        distinctIds.size());
  }

  private void cancelWithCascade(String processInstanceId) {
    Deque<String> queue = new ArrayDeque<>();
    queue.add(processInstanceId);
    int cancelledCount = 0;

    while (!queue.isEmpty()) {
      String currentId = queue.poll();

      Optional<ProcessInstance> instanceOpt =
          processInstanceDataService.getProcessInstance(currentId);
      if (instanceOpt.isPresent()) {
        ProcessInstance instance = instanceOpt.get();
        PIState currentState = instance.getState();
        if (currentState == PIState.CANCELLED
            || currentState == PIState.COMPLETED
            || currentState == PIState.TERMINATED) {
          continue;
        }

        Set<String> activeNodes = instance.getActiveNodeIds();
        if (activeNodes != null && !activeNodes.isEmpty()) {
          for (String nodeId : activeNodes) {
            instance.addExecutionLog(nodeId, null, null, null, null, NodeState.CANCELLED, null);
          }
          activeNodes.clear();
        }

        instance.setState(PIState.CANCELLED);
        instance.setCompletedAt(OffsetDateTime.now());
        processInstanceDataService.save(instance);
      } else {
        Query query =
            new Query(
                Criteria.where("processInstanceId")
                    .is(currentId)
                    .and("state")
                    .nin("CANCELLED", "COMPLETED", "TERMINATED"));
        Update update = new Update().set("state", PIState.CANCELLED);
        processInstanceDataService.updateFirst(query, update, "processInstance");
      }
      cancelledCount++;

      cleanupInstanceResources(currentId);

      List<io.telekom.orchest.adapter.mongo.model.ProcessInstance> children =
          mongoProcessInstanceRepository.findAllChildInstanceIds(currentId);
      for (var child : children) {
        queue.add(child.getProcessInstanceId());
      }
    }

    log.info(
        "Cascading cancellation from root {} cancelled {} instance(s)",
        processInstanceId,
        cancelledCount);
  }

  private void cleanupInstanceResources(String processInstanceId) {
    try {
      userTaskService.cancelTasksForProcessInstance(processInstanceId);
    } catch (Exception e) {
      log.warn("Failed to cancel user tasks for {}: {}", processInstanceId, e.getMessage());
    }
    try {
      eventRegisterService.cleanupAllEventsForProcessInstance(processInstanceId);
    } catch (Exception e) {
      log.warn("Failed to cleanup events for {}: {}", processInstanceId, e.getMessage());
    }
  }

  public boolean modifyInstance(UpdateInstanceRequest updateInstanceRequest) {
    String processInstanceId = updateInstanceRequest.getProcessInstanceId();
    Optional<ProcessInstance> optionalProcessInstance =
        processInstanceDataService.getProcessInstance(
            processInstanceId, List.of("processInstanceId", "state"));
    if (optionalProcessInstance.isEmpty()) {
      throw new RestExceptions("ProcessInstance not found with id: " + processInstanceId, 404);
    }
    ProcessInstance processInstance = optionalProcessInstance.get();
    if (!processInstance.getState().equals(PIState.INCIDENT)) {
      throw new RestExceptions(
          "Modification is not allowed on " + processInstance.getState() + "state", 404);
    }
    eventProducer.sendRetryEvent(
        new RetryProcessEvent(
            updateInstanceRequest.getProcessInstanceId(),
            updateInstanceRequest.getToNodeId(),
            updateInstanceRequest.getFromNodeId()));
    return true;
  }

  public boolean retryInstance(RetryProcessEvent retryProcessEvent) {
    try {
      eventProducer.sendRetryEvent(retryProcessEvent);
      orchestRestTelemetryService.incrementRestRetryEventCounter();
      return true;
    } catch (Exception e) {
      log.error(
          "failed to send retry event for processInstanceId: {}",
          retryProcessEvent.getProcessInstanceId(),
          e);
      return false;
    }
  }

  /**
   * Batch retry. Async dispatch is handled by the reactive controller. Caller must validate request
   * (non-empty, max 500 IDs) before invoking.
   */
  public void retryInstancesBatch(BatchInstanceRequest request) {
    List<String> ids = request.getProcessInstanceIds();
    if (ids == null || ids.isEmpty()) {
      log.info("batch retry: processInstanceIds list is empty");
      return;
    }
    List<String> distinctIds = ids.stream().filter(Objects::nonNull).distinct().toList();
    int submitted = 0;
    int skipped = 0;
    for (String processInstanceId : distinctIds) {
      try {
        Optional<ProcessInstance> optional =
            processInstanceDataService.getProcessInstance(processInstanceId);
        if (optional.isEmpty()) {
          log.warn("batch retry: process instance not found, skipping: {}", processInstanceId);
          skipped++;
          continue;
        }
        ProcessInstance instance = optional.get();
        if (instance.getState() != PIState.INCIDENT) {
          log.warn(
              "batch retry: process instance {} is not in INCIDENT state (current: {}), skipping",
              processInstanceId,
              instance.getState());
          skipped++;
          continue;
        }
        Set<String> activeNodeIds = instance.getActiveNodeIds();
        if (activeNodeIds == null || activeNodeIds.isEmpty()) {
          log.warn(
              "batch retry: process instance {} has no active nodes, skipping", processInstanceId);
          skipped++;
          continue;
        }
        String activityId = activeNodeIds.iterator().next();
        RetryProcessEvent event = new RetryProcessEvent(processInstanceId, activityId, activityId);
        eventProducer.sendRetryEvent(event);
        orchestRestTelemetryService.incrementRestRetryEventCounter();
        submitted++;
      } catch (Exception e) {
        log.error(
            "batch retry: failed to send retry event for processInstanceId: {}",
            processInstanceId,
            e);
        skipped++;
      }
    }
    log.info(
        "batch retry: submitted {} retries, skipped {} for request size {}",
        submitted,
        skipped,
        distinctIds.size());
  }

  /** Resolves an incident on a process instance. Placeholder — not yet implemented. */
  public void resolveIncident(BatchInstanceRequest batchInstanceRequest) {
    batchInstanceRequest
        .getProcessInstanceIds()
        .forEach(
            processInstanceId -> {
              eventProducer.sendRetryEvent(new RetryProcessEvent(processInstanceId, null, null));
            });
  }

  public CompensateInstanceResponse compensateInstance(
      CompensateInstanceRequest compensateInstanceRequest, LoggedInUserContext userCtx) {
    String processInstanceId = compensateInstanceRequest.getProcessInstanceId();
    String processDefinitionId = compensateInstanceRequest.getProcessDefinitionId();
    Integer version = compensateInstanceRequest.getVersion();

    Optional<ProcessInstance> optionalProcessInstance =
        processInstanceDataService.getProcessInstance(processInstanceId);
    if (optionalProcessInstance.isEmpty()) {
      throw new RestExceptions(
          "ProcessInstance not found with id: " + processInstanceId + " to compensate", 404);
    }
    ProcessInstance processInstance = optionalProcessInstance.get();
    Map<String, Object> mergedVariables = processInstance.getVariables();
    Map<String, Object> updatedVariables = compensateInstanceRequest.getVariables();
    if (updatedVariables != null && updatedVariables.isEmpty()) {
      mergedVariables.putAll(updatedVariables);
    }

    mergedVariables.put("compensatedFor", processInstance.getProcessInstanceId());
    mergedVariables.put("isCompensated", "true");
    ProcessInvocationResponse compensatedProcess =
        createProcessInstance(
            ProcessInvocationRequest.builder()
                .processDefinitionId(processInstanceId)
                .processDefinitionId(processDefinitionId)
                .variables(mergedVariables)
                .version(version)
                .build());

    // update variable with Compensated InstanceId
    processInstance
        .getVariables()
        .put("compensatedInstanceId", compensatedProcess.getProcessInstanceId());
    processInstanceDataService.save(processInstance);

    // cancell current instance
    cancelWithCascade(processInstanceId);

    return CompensateInstanceResponse.builder()
        .processInstanceId(compensatedProcess.getProcessInstanceId())
        .compensatedInstanceId(processInstanceId)
        .processDefinitionId(processDefinitionId)
        .version(compensatedProcess.getVersion())
        .build();
  }

  private ProcessInstanceDTO buildProcessInstanceDTO(
      ProcessInstance processInstance, boolean isList, LoggedInUserContext userContext) {
    ProcessInstanceDTO processInstanceDTO =
        ProcessInstanceDTO.builder()
            .processInstanceId(processInstance.getProcessInstanceId())
            .parentProcessInstanceId(
                processInstance.getParentProcesActivity() != null
                    ? processInstance.getParentProcesActivity().getProcessInstanceId()
                    : null)
            .processDefinitionId(processInstance.getProcessDefinitionId())
            .incidentMessage(processInstance.getIncidentMessage())
            .activeElements(
                processInstance.getActiveNodeIds() != null
                    ? processInstance.getActiveNodeIds().stream().toList()
                    : new ArrayList<>())
            .state(processInstance.getState().name())
            .createdAt(processInstance.getCreatedAt().toString())
            .completedAt(
                processInstance.getCompletedAt() != null
                    ? processInstance.getCompletedAt().toString()
                    : null)
            .version(processInstance.getVersion())
            .build();

    if (!isList) {
      processInstanceDTO.setVariables(
          getFilteredVariables(
              processInstance.getProcessDefinitionId(),
              processInstance.getVariables(),
              userContext));
      processInstanceDTO.setBpmnXML(processInstance.getProcessDefinition().getDefinitionXML());
      processInstanceDTO.setSequenceExecutions(processInstance.getExecutionHistory());
      if (processInstance.getExecutionHistory() != null
          && !processInstance.getExecutionHistory().isEmpty()) {
        processInstanceDTO.setLastActivityAt(
            processInstance.getExecutionHistory().keySet().stream().toList().getLast());
      }
    }

    if (userContext != null
        && userContext.getPrimaryRole() != null
        && READ_USER_NON_SENSITIVE.equals(userContext.getPrimaryRole())) {
      processInstanceDTO.setVariables(null);
    }

    return processInstanceDTO;
  }

  private Map<String, Object> getFilteredVariables(
      String processDefinitionId, Map<String, Object> variables, LoggedInUserContext userContext) {
    if (variables == null || variables.isEmpty()) {
      return variables;
    }
    Optional<ProcessSensitiveVariables> processSensitiveVariables =
        processSensitiveVariablesService.get(processDefinitionId);
    if (processSensitiveVariables.isEmpty()) {
      return variables;
    }
    ProcessSensitiveVariables sensitiveVariables = processSensitiveVariables.get();
    if (!sensitiveVariables.isEnabled()
        || sensitiveVariables.getVariables() == null
        || sensitiveVariables.getVariables().isEmpty()) {
      return variables;
    }

    Set<String> hideNames = new HashSet<>();
    Set<String> showNames = new HashSet<>();
    for (ProcessSensitiveVariables.SensitiveVariables sv : sensitiveVariables.getVariables()) {
      if (sv.getType() == ProcessSensitiveVariables.Type.HIDE) {
        hideNames.add(sv.getName());
      } else if (sv.getType() == ProcessSensitiveVariables.Type.SHOW) {
        showNames.add(sv.getName());
      }
    }

    Map<String, Object> filtered = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry : variables.entrySet()) {
      String key = entry.getKey();
      if (hideNames.contains(key)) {
        filtered.put(key, DEFAULT_PLACEHOLDER);
      } else if (!showNames.isEmpty() && !showNames.contains(key)) {
        filtered.put(key, DEFAULT_PLACEHOLDER);
      } else {
        filtered.put(key, entry.getValue());
      }
    }
    return filtered;
  }

  public void compensateInstanceBatch(BatchCompensateInstanceRequest compensateInstanceRequest) {
    compensateInstanceRequest
        .getProcessInstanceIds()
        .forEach(
            processInstanceId -> {
              compensateInstance(
                  CompensateInstanceRequest.builder()
                      .processInstanceId(processInstanceId)
                      .processDefinitionId(compensateInstanceRequest.getProcessDefinitionId())
                      .version(compensateInstanceRequest.getVersion())
                      .build(),
                  null);
            });
  }
}
