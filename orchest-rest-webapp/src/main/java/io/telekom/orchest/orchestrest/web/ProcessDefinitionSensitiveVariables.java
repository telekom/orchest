package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.extensions.sensitivevariables.ProcessSensitiveVariablesService;
import io.telekom.orchest.orchestrest.extensions.sensitivevariables.model.ProcessSensitiveVariables;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IProcessDefinitionSensitiveVariables;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for managing sensitive variable configurations. */
@Component
@RequiredArgsConstructor
public class ProcessDefinitionSensitiveVariables implements IProcessDefinitionSensitiveVariables {

  private final ProcessSensitiveVariablesService service;

  @Override
  public Mono<ResponseDTO<ProcessSensitiveVariables>> addProcessSensitiveVariables(
      ProcessSensitiveVariables processSensitiveVariables) {
    return Mono.fromCallable(
            () -> {
              Optional<ProcessSensitiveVariables> existing =
                  service.findByProcessDefinitionId(
                      processSensitiveVariables.getProcessDefinitionId());
              if (existing.isPresent()) {
                return RestUtils.<ProcessSensitiveVariables>buildMeta(
                    409,
                    "Sensitive variables configuration already exists for this process definition");
              }
              ProcessSensitiveVariables saved = service.create(processSensitiveVariables);
              return RestUtils.buildResponse(
                  saved, 201, "Sensitive variables configuration created");
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<ResponseDTO<String>> deleteProcessSensitiveVariables(
      ProcessSensitiveVariables processSensitiveVariables) {
    return Mono.fromCallable(
            () -> {
              Optional<ProcessSensitiveVariables> existing =
                  service.findByProcessDefinitionId(
                      processSensitiveVariables.getProcessDefinitionId());
              if (existing.isEmpty()) {
                return RestUtils.<String>buildMeta(
                    404, "Sensitive variables configuration not found");
              }
              service.remove(existing.get());
              return RestUtils.buildResponse(
                  "Deleted", 200, "Sensitive variables configuration deleted");
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<ResponseDTO<ProcessSensitiveVariables>> updateProcessSensitiveVariables(
      ProcessSensitiveVariables processSensitiveVariables) {
    return Mono.fromCallable(
            () -> {
              Optional<ProcessSensitiveVariables> existing =
                  service.findByProcessDefinitionId(
                      processSensitiveVariables.getProcessDefinitionId());
              if (existing.isEmpty()) {
                return RestUtils.<ProcessSensitiveVariables>buildMeta(
                    404, "Sensitive variables configuration not found");
              }
              ProcessSensitiveVariables current = existing.get();
              current.setVariables(processSensitiveVariables.getVariables());
              current.setEnabled(processSensitiveVariables.isEnabled());
              ProcessSensitiveVariables updated = service.update(current);
              return RestUtils.buildResponse(
                  updated, 200, "Sensitive variables configuration updated");
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<ResponseDTO<List<ProcessSensitiveVariables>>> getProcessSensitiveVariables(
      String processDefinitionId) {
    return Mono.fromCallable(
            () -> {
              Optional<ProcessSensitiveVariables> result =
                  service.findByProcessDefinitionId(processDefinitionId);
              return result
                  .map(
                      processSensitiveVariables ->
                          RestUtils.buildResponse(
                              List.of(processSensitiveVariables),
                              200,
                              "Found sensitive variables configuration"))
                  .orElseGet(
                      () ->
                          RestUtils.buildResponse(
                              List.of(), 404, "No sensitive variables configuration found"));
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<Page<ProcessSensitiveVariables>> getAll(int page, int size) {
    return Mono.fromCallable(() -> service.findAll(page, size))
        .subscribeOn(Schedulers.boundedElastic());
  }
}
