package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.api.core.response.ResourceDeploymentResponse;
import io.telekom.orchest.orchestrest.api.dto.ResourceDefinitionDTO;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.service.processdefinition.ProcessDefinitionAPIService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IProcessDefinitionAPI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for BPMN process definition operations. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessDefinitionAPI implements IProcessDefinitionAPI {

  private final ProcessDefinitionAPIService processDefinitionService;

  @Override
  public Mono<ResponseDTO<ResourceDeploymentResponse>> deployResource(
      ResourceDeploymentRequest request) {
    return Mono.fromCallable(() -> processDefinitionService.deployProcessDefinition(request))
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> RestUtils.buildResponse(r, 201, "process deployment success"));
  }

  @Override
  public Mono<ResponseDTO<List<ResourceDefinitionDTO>>> listProcessDefinitionsIds() {
    return Mono.fromCallable(processDefinitionService::listProcessIds)
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> RestUtils.buildResponse(r, 200, "success"));
  }

  @Override
  public Mono<ResponseDTO<ResourceDefinitionDTO>> getProcessDefinition(
      String processDefinitionId, Integer version) {
    return Mono.fromCallable(
            () -> processDefinitionService.getProcessDefinition(processDefinitionId, version))
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            opt ->
                opt.map(r -> RestUtils.buildResponse(r, 200, "success"))
                    .orElseThrow(
                        () ->
                            new RestExceptions(
                                "No processDefinition found with id: " + processDefinitionId,
                                404)));
  }

  @Override
  public Mono<Page<ResourceDefinitionDTO>> getProcessDefinitions(int page, int size) {
    return Mono.fromCallable(() -> processDefinitionService.getProcessDefinitions(page, size))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<ResponseDTO<Void>> deleteProcessDefinition(
      String processDefinitionId, Integer version) {
    return Mono.fromCallable(
            () -> processDefinitionService.deleteProcessDefinition(processDefinitionId, version))
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            deleted -> {
              if (deleted)
                return RestUtils.<Void>buildMeta(200, "Process definition deleted successfully");
              throw new RestExceptions(
                  "No process definition found with id: "
                      + processDefinitionId
                      + " and version: "
                      + version,
                  404);
            });
  }
}
