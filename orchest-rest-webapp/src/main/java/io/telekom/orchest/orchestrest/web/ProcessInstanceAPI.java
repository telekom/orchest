package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.api.core.response.CompensateInstanceResponse;
import io.telekom.orchest.api.core.response.ProcessInvocationResponse;
import io.telekom.orchest.orchestrest.api.dto.PagedRequestDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceScrollDTO;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import io.telekom.orchest.orchestrest.service.processInstance.ProcessInstanceAPIService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IProcessInstanceAPI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for process instance lifecycle operations. */
@Component
@RequiredArgsConstructor
public class ProcessInstanceAPI implements IProcessInstanceAPI {

  private final ProcessInstanceAPIService processInstanceService;

  @Override
  public Mono<ResponseDTO<ProcessInvocationResponse>> createProcessInstance(
      ProcessInvocationRequest request) {
    return Mono.fromCallable(() -> processInstanceService.createProcessInstance(request))
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> RestUtils.buildResponse(r, 200, "process instance created"));
  }

  @Override
  public Mono<ResponseDTO<ProcessInstanceDTO>> getProcessInstance(String processInstanceId) {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx =
              ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, new LoggedInUserContext());
          return Mono.fromCallable(
                  () -> processInstanceService.getProcessInstance(processInstanceId, userCtx))
              .subscribeOn(Schedulers.boundedElastic())
              .map(
                  opt ->
                      opt.map(dto -> RestUtils.buildResponse(dto, 200, "Found processInstance"))
                          .orElseThrow(
                              () ->
                                  new RestExceptions(
                                      "No processInstance found with id: " + processInstanceId,
                                      404)));
        });
  }

  @Override
  public Mono<Page<ProcessInstance>> getPageData(
      String processDefinitionId,
      Integer version,
      String state,
      String searchText,
      OffsetDateTime createdFrom,
      OffsetDateTime createdTo,
      int page,
      int size,
      String sort) {
    return Mono.fromCallable(
            () -> {
              PagedRequestDTO req =
                  PagedRequestDTO.builder()
                      .definitionId(processDefinitionId)
                      .version(version)
                      .state(state)
                      .from(toUtcLocalDateTime(createdFrom))
                      .to(toUtcLocalDateTime(createdTo))
                      .searchText(searchText)
                      .page(page)
                      .size(size)
                      .sort(sort)
                      .build();
              return processInstanceService.getPageData(req);
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<ProcessInstanceScrollDTO> scrollProcessInstances(
      String processDefinitionId,
      Integer version,
      String state,
      String searchText,
      OffsetDateTime createdFrom,
      OffsetDateTime createdTo,
      int from,
      int to,
      String sort) {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx =
              ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, new LoggedInUserContext());
          return Mono.fromCallable(
                  () -> {
                    PagedRequestDTO filterDTO =
                        PagedRequestDTO.builder()
                            .definitionId(processDefinitionId)
                            .version(version)
                            .state(state)
                            .searchText(searchText)
                            .from(toUtcLocalDateTime(createdFrom))
                            .to(toUtcLocalDateTime(createdTo))
                            .page(0)
                            .size(1)
                            .sort(sort)
                            .build();
                    return processInstanceService.scrollProcessInstances(
                        filterDTO, from, to, userCtx);
                  })
              .subscribeOn(Schedulers.boundedElastic());
        });
  }

  @Override
  public Mono<String> cancelInstances(CancelInstanceRequest cancelInstanceRequest) {
    return Mono.fromRunnable(() -> processInstanceService.cancelInstance(cancelInstanceRequest))
        .subscribeOn(Schedulers.boundedElastic())
        .thenReturn("Request Accepted");
  }

  private static final int BATCH_CANCEL_MAX_SIZE = 500;

  @Override
  public Mono<String> cancelInstancesBatch(BatchInstanceRequest batchCancelRequest) {
    if (batchCancelRequest == null
        || batchCancelRequest.getProcessInstanceIds() == null
        || batchCancelRequest.getProcessInstanceIds().isEmpty()) {
      return Mono.error(new RestExceptions("processInstanceIds must not be empty", 400));
    }
    if (batchCancelRequest.getProcessInstanceIds().size() > BATCH_CANCEL_MAX_SIZE) {
      return Mono.error(
          new RestExceptions(
              "processInstanceIds must not exceed " + BATCH_CANCEL_MAX_SIZE + " items per request",
              400));
    }
    return Mono.fromRunnable(() -> processInstanceService.cancelInstancesBatch(batchCancelRequest))
        .subscribeOn(Schedulers.boundedElastic())
        .thenReturn("ACCEPTED");
  }

  @Override
  public Mono<String> modifyInstance(UpdateInstanceRequest updateInstanceRequest) {
    return Mono.fromCallable(() -> processInstanceService.modifyInstance(updateInstanceRequest))
        .subscribeOn(Schedulers.boundedElastic())
        .map(ok -> ok ? "ACCEPTED" : "Failed");
  }

  @Override
  public Mono<String> retryInstance(RetryProcessEvent retryProcessEvent) {
    return Mono.fromCallable(() -> processInstanceService.retryInstance(retryProcessEvent))
        .subscribeOn(Schedulers.boundedElastic())
        .map(ok -> ok ? "ACCEPTED" : "Failed");
  }

  private static final int BATCH_RETRY_MAX_SIZE = 500;

  @Override
  public Mono<String> retryInstancesBatch(BatchInstanceRequest batchInstanceRequest) {
    if (batchInstanceRequest == null
        || batchInstanceRequest.getProcessInstanceIds() == null
        || batchInstanceRequest.getProcessInstanceIds().isEmpty()) {
      return Mono.error(new RestExceptions("processInstanceIds must not be empty", 400));
    }
    if (batchInstanceRequest.getProcessInstanceIds().size() > BATCH_RETRY_MAX_SIZE) {
      return Mono.error(
          new RestExceptions(
              "processInstanceIds must not exceed " + BATCH_RETRY_MAX_SIZE + " items per request",
              400));
    }
    return Mono.fromRunnable(() -> processInstanceService.retryInstancesBatch(batchInstanceRequest))
        .subscribeOn(Schedulers.boundedElastic())
        .thenReturn("ACCEPTED");
  }

  @Override
  public Mono<String> resolveIncident(BatchInstanceRequest batchInstanceRequest) {
    return Mono.fromRunnable(() -> processInstanceService.resolveIncident(batchInstanceRequest))
        .subscribeOn(Schedulers.boundedElastic())
        .thenReturn("ACCEPTED");
  }

  @Override
  public Mono<CompensateInstanceResponse> compensate(
      CompensateInstanceRequest compensateInstanceRequest) {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx =
              ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, new LoggedInUserContext());
          return Mono.fromCallable(
                  () ->
                      processInstanceService.compensateInstance(compensateInstanceRequest, userCtx))
              .subscribeOn(Schedulers.boundedElastic());
        });
  }

  @Override
  public Mono<String> compensateBatch(BatchCompensateInstanceRequest compensateInstanceRequest) {
    if (compensateInstanceRequest == null
        || compensateInstanceRequest.getProcessInstanceIds() == null
        || compensateInstanceRequest.getProcessInstanceIds().isEmpty()) {
      return Mono.error(new RestExceptions("processInstanceIds must not be empty", 400));
    }
    if (compensateInstanceRequest.getProcessInstanceIds().size() > BATCH_RETRY_MAX_SIZE) {
      return Mono.error(
          new RestExceptions(
              "processInstanceIds must not exceed " + BATCH_RETRY_MAX_SIZE + " items per request",
              400));
    }
    return Mono.fromRunnable(
            () -> processInstanceService.compensateInstanceBatch(compensateInstanceRequest))
        .subscribeOn(Schedulers.boundedElastic())
        .thenReturn("ACCEPTED");
  }

  private static LocalDateTime toUtcLocalDateTime(OffsetDateTime offsetDateTime) {
    if (offsetDateTime == null) return null;
    return offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
  }
}
