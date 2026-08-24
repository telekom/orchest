package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.adapter.mongo.model.Connector;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.service.ConnectorsService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IConnectorsAPI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for connector management operations. */
@Component
@RequiredArgsConstructor
public class ConnectorsAPI implements IConnectorsAPI {

  private final ConnectorsService connectorsService;

  @Override
  public Mono<Page<Connector>> getConnectors(int page, int size) {
    return Mono.fromCallable(() -> connectorsService.getConnectors(page, size))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<ResponseDTO<Connector>> getConnector(String name) {
    return Mono.fromCallable(() -> connectorsService.getConnector(name))
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            opt ->
                opt.map(c -> RestUtils.buildResponse(c, 200, "success"))
                    .orElseThrow(
                        () -> new RestExceptions("No connector found with name: " + name, 404)));
  }

  @Override
  public Mono<ResponseDTO<List<Connector>>> uploadConnector(
      List<Map<String, Object>> connectorsData) {
    return Mono.fromCallable(() -> connectorsService.upload(connectorsData))
        .subscribeOn(Schedulers.boundedElastic())
        .map(result -> RestUtils.buildResponse(result, 200, "success"));
  }
}
