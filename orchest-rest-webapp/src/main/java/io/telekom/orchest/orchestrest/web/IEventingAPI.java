package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.api.core.response.MessageEventResponse;
import io.telekom.orchest.api.core.response.SignalEventResponse;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.request.SendMessageEventRequest;
import io.telekom.orchest.orchestrest.api.request.SendSignalEventRequest;
import io.telekom.orchest.orchestrest.service.EventingAPIService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IEventingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for publishing BPMN message and signal events. */
@Component
@RequiredArgsConstructor
public class IEventingAPI implements IEventingService {

  private final EventingAPIService eventingAPIService;

  @Override
  public Mono<ResponseDTO<MessageEventResponse>> publishMessage(SendMessageEventRequest request) {
    return Mono.fromCallable(() -> eventingAPIService.publishMessage(request))
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> RestUtils.buildResponse(r, 200, "message event published"));
  }

  @Override
  public Mono<ResponseDTO<SignalEventResponse>> publishSignal(SendSignalEventRequest request) {
    return Mono.fromCallable(() -> eventingAPIService.publishSignal(request))
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> RestUtils.buildResponse(r, 200, "signal event published"));
  }
}
