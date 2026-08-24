package io.telekom.orchest.orchestrest.service.decisioninstance;

import io.telekom.orchest.api.core.adapters.data.model.DecisionInstance;
import io.telekom.orchest.orchestrest.api.dto.DecisionInstanceDTO;
import io.telekom.orchest.orchestrest.api.dto.DecisionInstanceScrollDTO;
import io.telekom.orchest.orchestrest.api.dto.PagedRequestDTO;
import io.telekom.orchest.orchestrest.api.validators.PageScrollValidator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

/**
 * Service for retrieving decision instance data. Bridges the REST API and the data interaction
 * layer for decision instances.
 */
@Component
@RequiredArgsConstructor
public class DecisionInstanceAPIService {

  private final DecisionInstanceDataService decisionInstanceDataService;

  /**
   * Retrieves a decision instance by ID.
   *
   * @param decisionInstanceId The decision instance ID.
   * @return An Optional containing the decision instance DTO.
   */
  public Optional<DecisionInstanceDTO> getDecisionInstance(String decisionInstanceId) {
    Optional<DecisionInstance> decisionInstance =
        decisionInstanceDataService.getDecisionInstance(decisionInstanceId);
    return decisionInstance.map(this::buildDecisionInstanceDTO);
  }

  /**
   * Retrieves a paginated list of decision instances.
   *
   * @param requestDTO Filter criteria.
   * @return A page of decision instance DTOs.
   */
  public Page<DecisionInstanceDTO> getDecisionInstances(PagedRequestDTO requestDTO) {
    Page<DecisionInstance> decisionInstances =
        decisionInstanceDataService.getDecisionInstances(requestDTO);
    List<DecisionInstanceDTO> list =
        decisionInstances.getContent().stream().map(this::buildDecisionInstanceDTO).toList();
    return PageableExecutionUtils.getPage(
        list, decisionInstances.getPageable(), decisionInstances::getTotalElements);
  }

  /**
   * Scrolls decision instances by offset range over the filtered, sorted sequence (half-open
   * interval {@code [from, to)}). Shares the same filters and sort conventions as {@link
   * #getDecisionInstances(PagedRequestDTO)}.
   *
   * <p>Does not compute a total matching count (no {@code count} query): {@code hasNext} is derived
   * by fetching one extra row past the requested window.
   */
  public DecisionInstanceScrollDTO scrollDecisionInstances(
      PagedRequestDTO filterDTO, int from, int toExclusive) {
    PageScrollValidator.validateScroll(from, toExclusive);
    int windowSize = toExclusive - from;

    List<DecisionInstance> raw =
        decisionInstanceDataService.getDecisionInstancesSlice(filterDTO, from, windowSize + 1);
    boolean hasNext = raw.size() > windowSize;
    List<DecisionInstance> pageRows = hasNext ? raw.subList(0, windowSize) : raw;
    List<DecisionInstanceDTO> content =
        pageRows.stream().map(this::buildDecisionInstanceDTO).toList();
    return DecisionInstanceScrollDTO.builder()
        .content(content)
        .from(from)
        .to(toExclusive)
        .hasNext(hasNext)
        .build();
  }

  private DecisionInstanceDTO buildDecisionInstanceDTO(DecisionInstance decisionInstance) {
    return DecisionInstanceDTO.builder()
        .decisionId(decisionInstance.getDefinitionId())
        .decisionInstanceId(decisionInstance.getDecisionInstanceId())
        .processInstanceId(decisionInstance.getProcessInstanceId())
        .matchedRuleIds(
            !decisionInstance.getMatchedRuleIds().isEmpty()
                ? decisionInstance.getMatchedRuleIds()
                : null)
        .inputVariables(decisionInstance.getInputVariables())
        .outputVariables(decisionInstance.getOutputVariables())
        .resourceUTF8XML(decisionInstance.getResourceXMLUTF8String())
        .version(decisionInstance.getVersion())
        .state(decisionInstance.getState().toString())
        .executedAt(decisionInstance.getExecutedAt().toString())
        .build();
  }
}
