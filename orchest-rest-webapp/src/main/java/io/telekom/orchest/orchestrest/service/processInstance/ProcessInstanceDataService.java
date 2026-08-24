package io.telekom.orchest.orchestrest.service.processInstance;

import static io.telekom.orchest.orchestrest.service.DataInteractionService.createSortParameter;

import com.mongodb.client.result.UpdateResult;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessInstanceRepository;
import io.telekom.orchest.orchestrest.api.dto.PagedRequestDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceStats;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Data access service for process instance queries, statistics aggregation, and persistence. */
@Service
@RequiredArgsConstructor
public class ProcessInstanceDataService {

  private final ProcessInstanceRepository processInstanceRepository;
  private final MongoTemplate mongoTemplate;

  public ProcessInstance save(ProcessInstance processInstance) {
    return processInstanceRepository.save(processInstance);
  }

  public Optional<ProcessInstance> getProcessInstance(String processInstanceId) {
    return processInstanceRepository.getById(processInstanceId);
  }

  public Optional<ProcessInstance> getProcessInstance(
      String processInstanceId, List<String> fields) {
    return processInstanceRepository.getById(processInstanceId, fields);
  }

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
    return mongoTemplate.find(query, ProcessInstance.class);
  }

  public Page<ProcessInstance> getPageData(PagedRequestDTO requestDTO) {
    Pageable pageRequest =
        PageRequest.of(
            requestDTO.getPage(), requestDTO.getSize(), createSortParameter(requestDTO.getSort()));
    Query query = new Query(buildProcessInstanceListCriteria(requestDTO)).with(pageRequest);
    List<ProcessInstance> processInstances = mongoTemplate.find(query, ProcessInstance.class);
    query.fields().include("_id");
    return PageableExecutionUtils.getPage(
        processInstances,
        pageRequest,
        () -> mongoTemplate.count(query.limit(-1).skip(-1), ProcessInstance.class));
  }

  /**
   * Aggregates process instance statistics by state. Counts how many instances are in each state
   * (STARTED, RUNNING, COMPLETED, etc.).
   *
   * @return An object containing counts for each process instance state.
   */
  public ProcessInstanceStats getProcessInstanceStats() {
    GroupOperation groupByState = Aggregation.group("state").count().as("count");
    Aggregation aggregation =
        Aggregation.newAggregation(groupByState)
            .withOptions(AggregationOptions.builder().hint(Document.parse("{ state: 1 }")).build());

    AggregationResults<Map> results =
        mongoTemplate.aggregate(
            aggregation, // Aggregation pipeline
            ProcessInstance.class, // Collection name
            Map.class // Result class
            );
    return getProcessInstanceStats(results);
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

  public UpdateResult updateFirst(Query query, Update update, String collectionName) {
    return mongoTemplate.updateFirst(query, update, collectionName);
  }
}
