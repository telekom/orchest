package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.api.core.request.BatchInstanceRequest;
import io.telekom.orchest.api.core.request.BatchRaiseIncidentRequest;
import io.telekom.orchest.api.core.request.RaiseIncidentRequest;
import io.telekom.orchest.orchestrest.api.dto.IncidentDTO;
import io.telekom.orchest.orchestrest.api.dto.PagedRequestDTO;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.service.IncidentManagementAPIService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IIncidentManagementAPI;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for incident management operations. */
@Component
@RequiredArgsConstructor
public class IncidentManagementAPI implements IIncidentManagementAPI {

  private final IncidentManagementAPIService incidentManagementAPIService;
  private static final int BATCH_RESOLVE_MAX_SIZE = 500;

  @Override
  public Mono<ResponseDTO<IncidentDTO>> getIncident(String processInstanceId) {
    return Mono.fromCallable(() -> incidentManagementAPIService.getIncident(processInstanceId))
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            opt ->
                opt.map(dto -> RestUtils.buildResponse(dto, 200, "Found incident"))
                    .orElseThrow(
                        () ->
                            new RestExceptions(
                                "No incident found with id: " + processInstanceId, 404)));
  }

  @Override
  public Mono<Page<IncidentDTO>> getIncidents(
      String processDefinitionId,
      Integer version,
      String searchText,
      LocalDateTime from,
      LocalDateTime to,
      int page,
      int size,
      String sort) {
    return Mono.fromCallable(
            () -> {
              PagedRequestDTO req =
                  PagedRequestDTO.builder()
                      .definitionId(processDefinitionId)
                      .version(version)
                      .searchText(searchText)
                      .from(from)
                      .to(to)
                      .page(page)
                      .size(size)
                      .sort(sort)
                      .build();
              return incidentManagementAPIService.getIncidents(req);
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<String> resolveIncident(String processInstanceId) {
    return Mono.fromRunnable(() -> incidentManagementAPIService.resolveIncident(processInstanceId))
        .subscribeOn(Schedulers.boundedElastic())
        .thenReturn("ACCEPTED");
  }

  @Override
  public Mono<String> resolveIncidentsBatch(BatchInstanceRequest batchInstanceRequest) {
    if (batchInstanceRequest == null
        || batchInstanceRequest.getProcessInstanceIds() == null
        || batchInstanceRequest.getProcessInstanceIds().isEmpty()) {
      return Mono.error(new RestExceptions("processInstanceIds must not be empty", 400));
    }
    if (batchInstanceRequest.getProcessInstanceIds().size() > BATCH_RESOLVE_MAX_SIZE) {
      return Mono.error(
          new RestExceptions(
              "processInstanceIds must not exceed " + BATCH_RESOLVE_MAX_SIZE + " items per request",
              400));
    }
    return Mono.fromRunnable(
            () -> incidentManagementAPIService.resolveIncidentsBatch(batchInstanceRequest))
        .subscribeOn(Schedulers.boundedElastic())
        .thenReturn("ACCEPTED");
  }

  @Override
  public Mono<String> raiseIncident(RaiseIncidentRequest raiseIncidentRequest) {
    if (raiseIncidentRequest == null
        || raiseIncidentRequest.getProcessInstanceId() == null
        || raiseIncidentRequest.getProcessInstanceId().isBlank()) {
      return Mono.error(new RestExceptions("processInstanceId must not be empty", 400));
    }
    return Mono.fromRunnable(() -> incidentManagementAPIService.raiseIncident(raiseIncidentRequest))
        .subscribeOn(Schedulers.boundedElastic())
        .thenReturn("ACCEPTED");
  }

  @Override
  public Mono<String> raiseIncidentsBatch(BatchRaiseIncidentRequest batchRaiseIncidentRequest) {
    if (batchRaiseIncidentRequest == null
        || batchRaiseIncidentRequest.getProcessInstanceIds() == null
        || batchRaiseIncidentRequest.getProcessInstanceIds().isEmpty()) {
      return Mono.error(new RestExceptions("processInstanceIds must not be empty", 400));
    }
    if (batchRaiseIncidentRequest.getProcessInstanceIds().size() > BATCH_RESOLVE_MAX_SIZE) {
      return Mono.error(
          new RestExceptions(
              "processInstanceIds must not exceed " + BATCH_RESOLVE_MAX_SIZE + " items per request",
              400));
    }
    return Mono.fromRunnable(
            () -> incidentManagementAPIService.raiseIncidentsBatch(batchRaiseIncidentRequest))
        .subscribeOn(Schedulers.boundedElastic())
        .thenReturn("ACCEPTED");
  }
}
