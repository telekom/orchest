package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.api.core.adapters.data.model.ProcessEnvVariables;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.service.ProcessDefinitionEnvironmentAPIService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IProcessDefinitionEnvironmentAPI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for process definition environment variable management. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessDefinitionEnvironmentAPI implements IProcessDefinitionEnvironmentAPI {

  private final ProcessDefinitionEnvironmentAPIService definitionEnvironmentAPIService;

  @Override
  public Mono<ResponseDTO<ProcessEnvVariables>> addProcessEnvVariable(
      ProcessEnvVariables processEnvVariables) {
    return Mono.fromCallable(
            () -> definitionEnvironmentAPIService.addProcessEnvVariable(processEnvVariables))
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> RestUtils.buildResponse(r, 200, "env variable created"));
  }

  @Override
  public Mono<ResponseDTO<ProcessEnvVariables>> deleteProcessEnvVariable(
      ProcessEnvVariables processEnvVariables) {
    return Mono.fromCallable(
            () -> definitionEnvironmentAPIService.deleteProcessEnvVariable(processEnvVariables))
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> RestUtils.buildResponse(r, 200, "env variable deleted"));
  }

  @Override
  public Mono<ResponseDTO<ProcessEnvVariables>> updateProcessEnvVariable(
      ProcessEnvVariables processEnvVariables) {
    return Mono.fromCallable(
            () -> definitionEnvironmentAPIService.updateProcessEnvVariable(processEnvVariables))
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> RestUtils.buildResponse(r, 200, "env variable updated"));
  }

  @Override
  public Mono<ResponseDTO<List<ProcessEnvVariables>>> getProcessDefinition(
      String processDefinitionId) {
    return Mono.fromCallable(
            () -> definitionEnvironmentAPIService.getProcessDefinition(processDefinitionId))
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> RestUtils.buildResponse(r, 200, "success"));
  }

  @Override
  public Mono<Page<ProcessEnvVariables>> getProcessDefinitions(int page, int size) {
    return Mono.fromCallable(
            () -> definitionEnvironmentAPIService.getProcessDefinitions(page, size))
        .subscribeOn(Schedulers.boundedElastic());
  }
}
