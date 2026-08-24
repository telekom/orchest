package io.telekom.orchest.orchestrest.service.decisioninstance;

import static io.telekom.orchest.orchestrest.service.DataInteractionService.createSortParameter;

import io.telekom.orchest.api.core.adapters.data.model.DecisionInstance;
import io.telekom.orchest.api.core.adapters.data.repository.DecisionInstanceRepository;
import io.telekom.orchest.orchestrest.api.dto.PagedRequestDTO;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/** Data access service for decision instance queries with filtering and pagination. */
@Service
@RequiredArgsConstructor
public class DecisionInstanceDataService {

  private final DecisionInstanceRepository decisionInstanceRepository;
  private final MongoTemplate mongoTemplate;

  public DecisionInstance save(DecisionInstance decisionInstance) {
    return decisionInstanceRepository.save(decisionInstance);
  }

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

  public Optional<DecisionInstance> getDecisionInstance(String decisionInstanceId) {
    return decisionInstanceRepository.getById(decisionInstanceId);
  }

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
}
