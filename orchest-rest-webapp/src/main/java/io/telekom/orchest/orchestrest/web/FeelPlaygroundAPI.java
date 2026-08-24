package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.orchestrest.api.dto.FeelEvaluationDTO;
import io.telekom.orchest.orchestrest.api.dto.FeelValidationDTO;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.request.FeelEvaluateRequest;
import io.telekom.orchest.orchestrest.api.request.FeelValidateRequest;
import io.telekom.orchest.orchestrest.service.FeelPlaygroundAPIService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IFeelPlaygroundAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for the FEEL expression playground. */
@Component
@RequiredArgsConstructor
public class FeelPlaygroundAPI implements IFeelPlaygroundAPI {

  private final FeelPlaygroundAPIService feelPlaygroundService;

  @Override
  public Mono<ResponseDTO<FeelEvaluationDTO>> evaluate(FeelEvaluateRequest request) {
    return Mono.fromCallable(
            () -> feelPlaygroundService.evaluate(request.getExpression(), request.getVariables()))
        .subscribeOn(Schedulers.boundedElastic())
        .map(dto -> RestUtils.buildResponse(dto, 200, "success"));
  }

  @Override
  public Mono<ResponseDTO<FeelValidationDTO>> validate(FeelValidateRequest request) {
    return Mono.fromCallable(() -> feelPlaygroundService.validate(request.getExpression()))
        .subscribeOn(Schedulers.boundedElastic())
        .map(dto -> RestUtils.buildResponse(dto, 200, "success"));
  }
}
