package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.api.core.request.DynamicProcessInvocationRequest;
import io.telekom.orchest.api.core.response.DynamicProcessInvocationResponse;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.service.DynamicProcessInstanceAPIService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IDynamicProcessInstanceAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for dynamic (ad-hoc) process instance creation. */
@Component
@RequiredArgsConstructor
public class DynamicProcessInstanceAPI implements IDynamicProcessInstanceAPI {

  private final DynamicProcessInstanceAPIService dynamicProcessInstanceAPIService;

  @Override
  public Mono<ResponseDTO<DynamicProcessInvocationResponse>> createProcessInstance(
      DynamicProcessInvocationRequest request) {
    return Mono.fromCallable(() -> dynamicProcessInstanceAPIService.createProcessInstance(request))
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> RestUtils.buildResponse(r, 200, "process instance created"));
  }
}
