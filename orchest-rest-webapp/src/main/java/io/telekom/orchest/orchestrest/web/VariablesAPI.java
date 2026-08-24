package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.request.VariablesRequest;
import io.telekom.orchest.orchestrest.api.request.VariablesResponse;
import io.telekom.orchest.orchestrest.service.VariablesAPIService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IVariablesAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for process instance variable operations. */
@Component
@RequiredArgsConstructor
public class VariablesAPI implements IVariablesAPI {

  private final VariablesAPIService variablesService;

  @Override
  public Mono<ResponseDTO<VariablesResponse>> getVariables(String processInstanceId) {
    return Mono.fromCallable(() -> variablesService.getVariables(processInstanceId))
        .subscribeOn(Schedulers.boundedElastic())
        .map(variables -> RestUtils.buildResponse(variables, 200, "processInstance variables"));
  }

  @Override
  public Mono<ResponseDTO<VariablesResponse>> modifyVariables(VariablesRequest variablesRequest) {
    return Mono.fromCallable(() -> variablesService.modifyVariables(variablesRequest))
        .subscribeOn(Schedulers.boundedElastic())
        .map(variables -> RestUtils.buildResponse(variables, 200, "variables updated"));
  }
}
