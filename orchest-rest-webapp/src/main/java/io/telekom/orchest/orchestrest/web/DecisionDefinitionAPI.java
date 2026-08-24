package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.api.core.adapters.data.dto.DecisionEvaluationResponseDTO;
import io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition;
import io.telekom.orchest.api.core.adapters.data.repository.DecisionDefinitionRepository;
import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.api.core.response.ResourceDeploymentResponse;
import io.telekom.orchest.enginecore.dmn.DmnEngine;
import io.telekom.orchest.orchestrest.api.dto.ResourceDefinitionDTO;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.service.decisiondefinition.DecisionDefinitionAPIService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IDecisionDefinitionAPI;
import io.telekom.orchest.orchestrest.web.interfaces.IDecisionInstanceAPI;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for DMN decision definition operations. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DecisionDefinitionAPI implements IDecisionDefinitionAPI {

  private final DecisionDefinitionAPIService decisionDefinitionService;
  private final DecisionDefinitionRepository decisionDefinitionRepository;
  private final DmnEngine dmnEngine = new DmnEngine();

  @Override
  public Mono<ResponseDTO<ResourceDeploymentResponse>> deployResource(
      ResourceDeploymentRequest request) {
    return Mono.fromCallable(() -> decisionDefinitionService.deploy(request))
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> RestUtils.buildResponse(r, 201, "process deployment success"));
  }

  @Override
  public Mono<ResponseDTO<List<ResourceDefinitionDTO>>> listDecisionDefinitions() {
    return Mono.fromCallable(decisionDefinitionService::listDecisionIds)
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> RestUtils.buildResponse(r, 200, "success"));
  }

  @Override
  public Mono<ResponseDTO<ResourceDefinitionDTO>> getDecisionDefinition(
      String decisionDefinitionId, Integer version) {
    return Mono.fromCallable(
            () -> decisionDefinitionService.getDecisionDefinition(decisionDefinitionId, version))
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            opt ->
                opt.map(r -> RestUtils.buildResponse(r, 200, "success"))
                    .orElseThrow(
                        () ->
                            new RestExceptions(
                                "No decisionDefinition found with id: " + decisionDefinitionId,
                                404)));
  }

  @Override
  public Mono<Page<ResourceDefinitionDTO>> getDecisionDefinitions(int page, int size) {
    return Mono.fromCallable(() -> decisionDefinitionService.getDecisionDefinitions(page, size))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<ResponseDTO<DecisionEvaluationResponseDTO>> evaluateDecision(
      IDecisionInstanceAPI.EvaluateDecisionRequest req) {
    return Mono.fromCallable(
            () -> {
              Optional<DecisionDefinition> def =
                  req.version() == null
                      ? decisionDefinitionRepository.getLatestById(req.decisionId())
                      : decisionDefinitionRepository.getByDecisionIdAndVersion(
                          req.decisionId(), req.version());
              if (def.isPresent()) {
                DecisionEvaluationResponseDTO result =
                    dmnEngine.evaluate(def.get(), req.decisionId(), req.inputVariables());
                return RestUtils.buildResponse(result, 200, "Executed");
              }
              throw new RestExceptions(
                  "No DecisionDefinition found with id: " + req.decisionId(), 400);
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<ResponseDTO<Void>> deleteDecisionDefinition(
      String decisionDefinitionId, Integer version) {
    return Mono.fromCallable(
            () -> decisionDefinitionService.deleteDecisionDefinition(decisionDefinitionId, version))
        .subscribeOn(Schedulers.boundedElastic())
        .handle(
            (deleted, sink) -> {
              if (deleted) {
                sink.next(RestUtils.buildMeta(200, "Decision definition deleted successfully"));
                return;
              }
              sink.error(
                  new RestExceptions(
                      "No decision definition found with id: "
                          + decisionDefinitionId
                          + " and version: "
                          + version,
                      404));
            });
  }
}
