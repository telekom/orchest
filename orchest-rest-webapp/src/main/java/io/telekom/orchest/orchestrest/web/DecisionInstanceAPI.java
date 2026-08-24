package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.orchestrest.api.dto.DecisionInstanceDTO;
import io.telekom.orchest.orchestrest.api.dto.DecisionInstanceScrollDTO;
import io.telekom.orchest.orchestrest.api.dto.PagedRequestDTO;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.service.decisioninstance.DecisionInstanceAPIService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IDecisionInstanceAPI;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for decision instance queries. */
@Component
@RequiredArgsConstructor
public class DecisionInstanceAPI implements IDecisionInstanceAPI {

  private final DecisionInstanceAPIService decisionInstanceAPIService;

  @Override
  public Mono<Page<DecisionInstanceDTO>> getDecisionInstances(
      String decisionId,
      LocalDateTime from,
      LocalDateTime to,
      Integer version,
      String searchText,
      int page,
      int size,
      String sort) {
    return Mono.fromCallable(
            () -> {
              PagedRequestDTO req =
                  PagedRequestDTO.builder()
                      .definitionId(decisionId)
                      .version(version)
                      .searchText(searchText)
                      .from(from)
                      .to(to)
                      .page(page)
                      .size(size)
                      .sort(sort)
                      .build();
              return decisionInstanceAPIService.getDecisionInstances(req);
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<DecisionInstanceScrollDTO> scrollDecisionInstances(
      String decisionId,
      Integer version,
      String searchText,
      LocalDateTime executedFrom,
      LocalDateTime executedTo,
      int from,
      int to,
      String sort) {
    return Mono.fromCallable(
            () -> {
              PagedRequestDTO filterDTO =
                  PagedRequestDTO.builder()
                      .definitionId(decisionId)
                      .version(version)
                      .searchText(searchText)
                      .from(executedFrom)
                      .to(executedTo)
                      .page(0)
                      .size(1)
                      .sort(sort)
                      .build();
              return decisionInstanceAPIService.scrollDecisionInstances(filterDTO, from, to);
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<ResponseDTO<DecisionInstanceDTO>> getDecisionInstance(String decisionInstanceId) {
    return Mono.fromCallable(
            () -> decisionInstanceAPIService.getDecisionInstance(decisionInstanceId))
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            opt ->
                opt.map(dto -> RestUtils.buildResponse(dto, 200, "Found"))
                    .orElseThrow(
                        () ->
                            new RestExceptions(
                                "No decisionInstance found with id: " + decisionInstanceId, 404)));
  }
}
