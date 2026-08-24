package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.adapter.mongo.mapper.ProcessInstanceMapper;
import io.telekom.orchest.api.core.adapters.data.model.*;
import io.telekom.orchest.api.core.adapters.data.repository.DecisionInstanceRepository;
import io.telekom.orchest.api.core.adapters.data.repository.DynamicProcessDefinitionRepository;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessDefinitionRepository;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessInstanceRepository;
import io.telekom.orchest.orchestrest.api.dto.PagedRequestDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceStats;
import io.telekom.orchest.orchestrest.extensions.ratelimit.model.RateLimit;
import io.telekom.orchest.orchestrest.extensions.ratelimit.repository.RateLimitRepository;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Service providing core data interaction capabilities for the REST API. Encapsulates complex
 * queries and data retrieval logic for Process Definitions, Process Instances, Decision Instances,
 * and other entities.
 */
@Service
@RequiredArgsConstructor
public class DataInteractionService {

  private final ProcessDefinitionRepository processDefinitionRepository;
  private final DynamicProcessDefinitionRepository dynamicProcessDefinitionRepository;
  private final ProcessInstanceRepository processInstanceRepository;
  private final DecisionInstanceRepository decisionInstanceRepository;
  private final RateLimitRepository rateLimitRepository;
  private final MongoTemplate mongoTemplate;
  private final ProcessInstanceMapper processInstanceMapper;

  public Optional<ProcessDefinition> getProcessDefinition(String processDefinitionId) {
    return processDefinitionRepository.getLatestById(processDefinitionId);
  }

  public Optional<ProcessDefinition> getProcessDefinitionsWithVersion(
      String processDefinitionId, Integer version) {
    return processDefinitionRepository.getByIdAndVersion(processDefinitionId, version);
  }

  public List<ProcessDefinition> getProcessDefinitions(int page, int size) {
    return processDefinitionRepository.findAllDefinitions(page, size);
  }

  public List<ProcessDefinition> getProcessDefinitions() {
    return processDefinitionRepository.findDistinctProcessIds();
  }

  public boolean deleteProcessDefinition(String processDefinitionId, Integer version) {
    return processDefinitionRepository.deleteByDefinitionIdAndVersion(processDefinitionId, version);
  }

  public Optional<ProcessInstance> getProcessInstance(String processInstanceId) {
    return processInstanceRepository.getById(processInstanceId);
  }

  private Criteria buildProcessInstanceListCriteria(PagedRequestDTO requestDTO) {
    Criteria criteria = new Criteria();
    String searchText = requestDTO.getSearchText();
    if (StringUtils.hasText(searchText)) {
      criteria.orOperator(
          Criteria.where("processInstanceId").is(searchText),
          Criteria.where("correlationIds").is(searchText));
    } else {
      if (requestDTO.getDefinitionId() != null) {
        criteria.and("processDefinitionId").is(requestDTO.getDefinitionId());
      }
      if (requestDTO.getVersion() != null) {
        criteria.and("version").is(requestDTO.getVersion());
      }
      if (requestDTO.getState() != null) {
        criteria.and("state").is(requestDTO.getState());
      }
    }

    if (requestDTO.getFrom() != null || requestDTO.getTo() != null) {
      Criteria createdAt = Criteria.where("createdAt");
      if (requestDTO.getFrom() != null) {
        createdAt.gte(requestDTO.getFrom().toInstant(ZoneOffset.UTC));
      }
      if (requestDTO.getTo() != null) {
        createdAt.lt(requestDTO.getTo().toInstant(ZoneOffset.UTC));
      }
      criteria.andOperator(createdAt);
    }

    return criteria;
  }

  /**
   * Returns a contiguous slice {@code [skip, skip + limit)} of process instances matching the
   * filters, using the same sort order as paging.
   */
  public List<ProcessInstance> getProcessInstancesSlice(
      PagedRequestDTO requestDTO, int skip, int limit) {
    Sort sort = createSortParameter(requestDTO.getSort());
    Query query =
        new Query(buildProcessInstanceListCriteria(requestDTO)).with(sort).skip(skip).limit(limit);
    query
        .fields()
        .include(
            "processInstanceId",
            "processDefinitionId",
            "version",
            "state",
            "parentProcesActivity",
            "createdAt",
            "completedAt");
    List<io.telekom.orchest.adapter.mongo.model.ProcessInstance> processInstance =
        mongoTemplate.find(query, io.telekom.orchest.adapter.mongo.model.ProcessInstance.class);
    return processInstance.stream().map(processInstanceMapper::toDomain).toList();
  }

  /**
   * Retrieves a paginated list of process instances based on filtering criteria.
   *
   * @param requestDTO Filter criteria (definitionId, version, state, date range, search text).
   * @return A page of matching ProcessInstances.
   */
  public Page<ProcessInstance> getProcessInstances(PagedRequestDTO requestDTO) {
    Criteria criteria = buildProcessInstanceListCriteria(requestDTO);

    Pageable pageRequest =
        PageRequest.of(
            requestDTO.getPage(), requestDTO.getSize(), createSortParameter(requestDTO.getSort()));
    Query query = new Query(criteria).with(pageRequest);
    query
        .fields()
        .include(
            "processInstanceId",
            "processDefinitionId",
            "version",
            "state",
            "createdAt",
            "completedAt",
            "parentProcesActivity");
    List<ProcessInstance> processInstances = mongoTemplate.find(query, ProcessInstance.class);
    if (CollectionUtils.isEmpty(processInstances)) {
      return PageableExecutionUtils.getPage(processInstances, pageRequest, () -> 0);
    }
    return PageableExecutionUtils.getPage(
        processInstances,
        pageRequest,
        () -> mongoTemplate.count(query.limit(-1).skip(-1), ProcessInstance.class));
  }

  /**
   * Retrieves lightweight page data (IDs only) for process instances. Useful for efficiently
   * loading lists where full details aren't needed initially.
   *
   * @param requestDTO Filter criteria.
   * @return A page of process instance IDs.
   */
  public Page<String> getPageData(PagedRequestDTO requestDTO) {
    Criteria criteria = new Criteria();
    String searchText = requestDTO.getSearchText();
    if (StringUtils.hasText(searchText)) {
      criteria.orOperator(
          Criteria.where("processInstanceId").is(searchText),
          Criteria.where("correlationId").is(searchText));
    } else {
      if (requestDTO.getDefinitionId() != null) {
        criteria.and("processDefinitionId").is(requestDTO.getDefinitionId());
      }
      if (requestDTO.getVersion() != null) {
        criteria.and("version").is(requestDTO.getVersion());
      }
      if (requestDTO.getState() != null) {
        criteria.and("state").is(requestDTO.getState());
      }
    }

    if (requestDTO.getFrom() != null) {
      criteria.and("createdAt").gt(requestDTO.getFrom().toInstant(ZoneOffset.UTC));
    }
    if (requestDTO.getTo() != null) {
      criteria.and("createdAt").lte(requestDTO.getTo().toInstant(ZoneOffset.UTC));
    }

    Pageable pageRequest =
        PageRequest.of(
            requestDTO.getPage(), requestDTO.getSize(), createSortParameter(requestDTO.getSort()));
    Query query = new Query(criteria).with(pageRequest);
    query.fields().include("_id");

    return PageableExecutionUtils.getPage(
        new ArrayList<>(),
        pageRequest,
        () -> mongoTemplate.count(query.limit(-1).skip(-1), ProcessInstance.class));
  }

  //    public ProcessInstance updateProcessInstance(ProcessInstance processInstance) {
  //        return processInstanceRepository.save(processInstance);
  //    }
  //
  //    public MessageEventStore registerMessageEvent(MessageEventStore messageEventStore) {
  //        return messageEventStoreRepository.save(messageEventStore);
  //    }
  //
  //    public Optional<MessageEventStore> getMessageStartEvent(String messageName) {
  //        return messageEventStoreRepository.findByMessageNameAndIsStartEvent(messageName, true);
  //    }
  //
  //    public SignalEvent registerSignalEvent(SignalEvent signalEvent) {
  //        return saveSignalEvent(signalEvent);
  //    }
  //
  //    public SignalEvent saveSignalEvent(SignalEvent signalEvent) {
  //        return signalEventStoreRepository.save(signalEvent);
  //    }
  //
  //    public Optional<EventGatewayEvent> getEventGatewayEvent(String eventGatewayId) {
  //        return eventBasedGatewayRepository.findById(eventGatewayId);
  //    }
  //
  //    public TimedEvent registerTimedEvent(TimedEvent timedEvent) {
  //        TimedEventRegistry timedEventRegistry =
  // timedEventRegistryRepository.save(TimedEventRegistry.builder().triggerOn(timedEvent.getTriggerAt()).build());
  //        timedEvent.setTimedEventRegistryId(timedEventRegistry.getId());
  //        return timedEventRepository.save(timedEvent);
  //    }
  //
  //    public Optional<TimedEvent> getTimedEvent(String timerEventId) {
  //        return timedEventRepository.findByTimedEventRegistryId(timerEventId);
  //    }
  //
  //    public List<WorkerRegistry> getWorkerRegistry(String processDefinitionId) {
  //        return workerRegistryRepository.findAll();
  //    }
  //
  //    public Optional<WorkerRegistry> getWorkerRegistryWithNameSpace(String namespace) {
  //        return workerRegistryRepository.findByNameSpace(namespace);
  //    }
  //
  //    public WorkerRegistry saveWorkerRegistry(WorkerRegistry workerRegistry) {
  //        return workerRegistryRepository.save(workerRegistry);
  //    }
  //
  //    public PendingTask savePendingTask(PendingTask pendingTask) {
  //        return pendingTaskRepository.save(pendingTask);
  //    }
  //
  //    public DecisionDefinition saveDecisionDefinition(DecisionDefinition decisionDefinition) {
  //        return decisionDefinitionRepository.save(decisionDefinition);
  //    }
  //
  //    public DecisionInstance saveDecisionInstance(DecisionInstance decisionInstance) {
  //        return decisionInstanceRepository.save(decisionInstance);
  //    }
  //
  public Optional<DecisionInstance> getDecisionInstance(String decisionInstanceId) {
    return decisionInstanceRepository.getById(decisionInstanceId);
  }

  private Criteria buildDecisionInstanceListCriteria(PagedRequestDTO requestDTO) {
    Criteria criteria = new Criteria();
    String searchText = requestDTO.getSearchText();
    if (searchText != null) {
      criteria.orOperator(
          Criteria.where("processInstanceId").is(searchText),
          Criteria.where("decisionId").is(searchText));
    } else {
      if (requestDTO.getDefinitionId() != null) {
        criteria.and("decisionId").is(requestDTO.getDefinitionId());
      }
      if (requestDTO.getVersion() != null) {
        criteria.and("version").is(requestDTO.getVersion());
      }
    }

    if (requestDTO.getFrom() != null || requestDTO.getTo() != null) {
      Criteria executedAt = Criteria.where("executedAt");
      if (requestDTO.getFrom() != null) {
        executedAt.gte(requestDTO.getFrom().toInstant(ZoneOffset.UTC));
      }
      if (requestDTO.getTo() != null) {
        executedAt.lt(requestDTO.getTo().toInstant(ZoneOffset.UTC));
      }
      criteria.andOperator(executedAt);
    }

    return criteria;
  }

  /**
   * Returns a contiguous slice {@code [skip, skip + limit)} of decision instances matching the
   * filters, using the same sort order as paging.
   */
  public List<DecisionInstance> getDecisionInstancesSlice(
      PagedRequestDTO requestDTO, int skip, int limit) {
    Sort sort = createSortParameter(requestDTO.getSort());
    Query query =
        new Query(buildDecisionInstanceListCriteria(requestDTO)).with(sort).skip(skip).limit(limit);
    query
        .fields()
        .include(
            "decisionInstanceId",
            "processInstanceId",
            "definitionId",
            "version",
            "state",
            "executedAt",
            "matchedRuleIds");
    return mongoTemplate.find(query, DecisionInstance.class);
  }

  /**
   * Retrieves a paginated list of decision instances based on filtering criteria.
   *
   * @param requestDTO Filter criteria.
   * @return A page of matching DecisionInstances.
   */
  public Page<DecisionInstance> getDecisionInstances(PagedRequestDTO requestDTO) {
    Criteria criteria = buildDecisionInstanceListCriteria(requestDTO);

    Pageable pageRequest =
        PageRequest.of(
            requestDTO.getPage(), requestDTO.getSize(), createSortParameter(requestDTO.getSort()));
    Query query = new Query(criteria).with(pageRequest);
    query
        .fields()
        .include(
            "decisionInstanceId",
            "processInstanceId",
            "definitionId",
            "version",
            "state",
            "executedAt",
            "matchedRuleIds");
    List<DecisionInstance> decisionInstances = mongoTemplate.find(query, DecisionInstance.class);
    if (CollectionUtils.isEmpty(decisionInstances)) {
      return PageableExecutionUtils.getPage(decisionInstances, pageRequest, () -> 0);
    }
    return PageableExecutionUtils.getPage(
        decisionInstances,
        pageRequest,
        () -> mongoTemplate.count(query.limit(-1).skip(-1), DecisionInstance.class));
  }

  //
  //
  //    public Optional<DecisionDefinition> getDecisionDefinitionById(String decisionId) {
  //        return
  // decisionDefinitionRepository.findFirstByDefinitionIdOrderByVersionDesc(decisionId);
  //    }
  //
  public List<RateLimit> getRateLimits() {
    return rateLimitRepository.findAll();
  }

  public Optional<RateLimit> getRateLimit(String processDefinitionId) {
    return rateLimitRepository.findByProcessId(processDefinitionId);
  }

  public RateLimit saveRateLimit(RateLimit rateLimit) {
    return rateLimitRepository.save(rateLimit);
  }

  //
  //    public EventGatewayEvent saveEventBasedGateway(EventGatewayEvent eventBasedGateway) {
  //        return eventBasedGatewayRepository.save(eventBasedGateway);
  //    }

  /**
   * Aggregates process instance statistics by state. Counts how many instances are in each state
   * (STARTED, RUNNING, COMPLETED, etc.).
   *
   * @return An object containing counts for each process instance state.
   */
  public ProcessInstanceStats getProcessInstanceStats() {

    GroupOperation groupByState = Aggregation.group("state").count().as("count");
    Aggregation aggregation = Aggregation.newAggregation(groupByState);

    AggregationResults<Map> results =
        mongoTemplate.aggregate(
            aggregation, // Aggregation pipeline
            "processInstance", // Collection name
            Map.class // Result class
            );
    return getProcessInstanceStats(results);
  }

  private static ProcessInstanceStats getProcessInstanceStats(AggregationResults<Map> results) {
    List<Map> mappedResults = results.getMappedResults();
    if (mappedResults.isEmpty()) {
      return new ProcessInstanceStats();
    }
    ProcessInstanceStats processInstanceStats = new ProcessInstanceStats();
    mappedResults.forEach(
        map -> {
          String state = map.get("_id").toString();
          int count = (int) map.get("count");
          switch (state) {
            case "COMPLETED" -> processInstanceStats.setCompleted(count);
            case "STARTED" -> processInstanceStats.setStarted(count);
            case "RUNNING" -> processInstanceStats.setActive(count);
            case "HOLD" -> processInstanceStats.setHold(count);
            case "FAILED" -> processInstanceStats.setFailed(count);
            case "INCIDENT" -> processInstanceStats.setIncidents(count);
            case "TERMINATED" -> processInstanceStats.setTerminated(count);
          }
        });
    return processInstanceStats;
  }

  //    public void removeLinkedGatewayEvents(String eventGatewayId) {
  //        eventBasedGatewayRepository.deleteById(eventGatewayId);
  //        messageEventStoreRepository.deleteByLinkedEventId(eventGatewayId);
  //        signalEventStoreRepository.deleteByLinkedEventId(eventGatewayId);
  //        timedEventRepository.deleteByLinkedEventId(eventGatewayId);
  //    }

  /**
   * Retrieves a paginated list of user tasks available to the specified user. A task is available
   * if the user is the assignee, claimedBy, a candidate user, a member of a candidate group, or an
   * open task with no assignment restrictions. Only returns tasks in CREATED or CLAIMED state.
   *
   * @param userId The user identifier.
   * @param userGroups The groups the user belongs to.
   * @param page The page number (0-indexed).
   * @param size The page size.
   * @param sort Sort parameter (e.g. "-createdAt").
   * @return A page of matching UserTaskInstance documents.
   */
  public Page<io.telekom.orchest.adapter.mongo.model.UserTaskInstance> getUserTasks(
      String userId, List<String> userGroups, int page, int size, String sort) {
    List<String> groups = userGroups != null ? userGroups : List.of();

    Criteria criteria =
        new Criteria()
            .andOperator(
                Criteria.where("state").in("CREATED", "CLAIMED"),
                new Criteria()
                    .orOperator(
                        Criteria.where("assignee").is(userId),
                        Criteria.where("claimedBy").is(userId),
                        Criteria.where("candidateUsers").is(userId),
                        Criteria.where("candidateGroups").in(groups),
                        new Criteria()
                            .andOperator(
                                Criteria.where("assignee").is(null),
                                Criteria.where("candidateUsers").size(0),
                                Criteria.where("candidateGroups").size(0))));

    Pageable pageRequest = PageRequest.of(page, size, createSortParameter(sort));
    Query query = new Query(criteria).with(pageRequest);

    List<io.telekom.orchest.adapter.mongo.model.UserTaskInstance> tasks =
        mongoTemplate.find(query, io.telekom.orchest.adapter.mongo.model.UserTaskInstance.class);
    if (CollectionUtils.isEmpty(tasks)) {
      return PageableExecutionUtils.getPage(tasks, pageRequest, () -> 0);
    }
    return PageableExecutionUtils.getPage(
        tasks,
        pageRequest,
        () ->
            mongoTemplate.count(
                query.limit(-1).skip(-1),
                io.telekom.orchest.adapter.mongo.model.UserTaskInstance.class));
  }

  public Page<io.telekom.orchest.adapter.mongo.model.UserTaskInstance> getAllUserTasks(
      int page, int size, String sort) {
    Pageable pageRequest = PageRequest.of(page, size, createSortParameter(sort));
    Query query = new Query().with(pageRequest);

    List<io.telekom.orchest.adapter.mongo.model.UserTaskInstance> tasks =
        mongoTemplate.find(query, io.telekom.orchest.adapter.mongo.model.UserTaskInstance.class);
    if (CollectionUtils.isEmpty(tasks)) {
      return PageableExecutionUtils.getPage(tasks, pageRequest, () -> 0);
    }
    return PageableExecutionUtils.getPage(
        tasks,
        pageRequest,
        () ->
            mongoTemplate.count(
                new Query(), io.telekom.orchest.adapter.mongo.model.UserTaskInstance.class));
  }

  /**
   * Retrieves a paginated list of active user tasks for a specific process instance. Only returns
   * tasks in CREATED or CLAIMED state.
   *
   * @param processInstanceId The process instance ID.
   * @param page The page number (0-indexed).
   * @param size The page size.
   * @param sort Sort parameter (e.g. "-createdAt").
   * @return A page of matching UserTaskInstance documents.
   */
  public Page<io.telekom.orchest.adapter.mongo.model.UserTaskInstance>
      getUserTasksForProcessInstance(String processInstanceId, int page, int size, String sort) {
    Criteria criteria =
        Criteria.where("processInstanceId")
            .is(processInstanceId)
            .and("state")
            .in("CREATED", "CLAIMED");

    Pageable pageRequest = PageRequest.of(page, size, createSortParameter(sort));
    Query query = new Query(criteria).with(pageRequest);

    List<io.telekom.orchest.adapter.mongo.model.UserTaskInstance> tasks =
        mongoTemplate.find(query, io.telekom.orchest.adapter.mongo.model.UserTaskInstance.class);
    if (CollectionUtils.isEmpty(tasks)) {
      return PageableExecutionUtils.getPage(tasks, pageRequest, () -> 0);
    }
    return PageableExecutionUtils.getPage(
        tasks,
        pageRequest,
        () ->
            mongoTemplate.count(
                query.limit(-1).skip(-1),
                io.telekom.orchest.adapter.mongo.model.UserTaskInstance.class));
  }

  public static Sort createSortParameter(String sortParam) {
    String field = sortParam.substring(1);
    if (sortParam.startsWith("+")) {
      return Sort.by(new Sort.Order(Sort.Direction.ASC, field));
    }
    if (sortParam.startsWith("-")) {
      return Sort.by(new Sort.Order(Sort.Direction.DESC, field));
    }
    return Sort.by(Sort.Direction.DESC, field);
  }

  public void saveProcessInstance(ProcessInstance processInstance) {
    processInstanceRepository.save(processInstance);
  }
}
