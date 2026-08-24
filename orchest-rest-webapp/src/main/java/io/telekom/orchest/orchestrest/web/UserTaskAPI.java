package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.dto.UserTaskDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.api.request.AssignUserTaskRequest;
import io.telekom.orchest.orchestrest.api.request.CompleteUserTaskRequest;
import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import io.telekom.orchest.orchestrest.service.UserTaskAPIService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IUserTaskAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for user task operations (claim, complete, assign). */
@Component
@RequiredArgsConstructor
public class UserTaskAPI implements IUserTaskAPI {

  private final UserTaskAPIService userTaskAPIService;

  @Override
  public Mono<Page<UserTaskDTO>> getTasks(int page, int size, String sort) {
    return Mono.fromCallable(() -> userTaskAPIService.getAllTasks(page, size, sort))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<ResponseDTO<UserTaskDTO>> getTask(String taskId) {
    return Mono.fromCallable(() -> userTaskAPIService.getTask(taskId))
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            opt ->
                opt.map(dto -> RestUtils.buildResponse(dto, 200, "User task found"))
                    .orElseThrow(() -> new RestExceptions("User task not found: " + taskId, 404)));
  }

  @Override
  public Mono<Page<UserTaskDTO>> getTasksForProcessInstance(
      String processInstanceId, int page, int size, String sort) {
    return Mono.fromCallable(
            () ->
                userTaskAPIService.getTasksForProcessInstance(processInstanceId, page, size, sort))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<ResponseDTO<UserTaskDTO>> claimTask(String taskId) {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx =
              ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, new LoggedInUserContext());
          return Mono.fromCallable(() -> userTaskAPIService.claimTask(taskId, userCtx))
              .subscribeOn(Schedulers.boundedElastic())
              .map(claimed -> RestUtils.buildResponse(claimed, 200, "Task claimed successfully"));
        });
  }

  @Override
  public Mono<ResponseDTO<UserTaskDTO>> unclaimTask(String taskId) {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx =
              ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, new LoggedInUserContext());
          return Mono.fromCallable(() -> userTaskAPIService.unclaimTask(taskId, userCtx))
              .subscribeOn(Schedulers.boundedElastic())
              .map(
                  unclaimed ->
                      RestUtils.buildResponse(unclaimed, 200, "Task unclaimed successfully"));
        });
  }

  @Override
  public Mono<ResponseDTO<UserTaskDTO>> completeTask(
      String taskId, CompleteUserTaskRequest request) {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx =
              ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, new LoggedInUserContext());
          var variables = request != null ? request.getVariables() : null;
          return Mono.fromCallable(
                  () -> userTaskAPIService.completeTask(taskId, variables, userCtx))
              .subscribeOn(Schedulers.boundedElastic())
              .map(
                  completed ->
                      RestUtils.buildResponse(completed, 200, "Task completed successfully"));
        });
  }

  @Override
  public Mono<ResponseDTO<UserTaskDTO>> reassignTask(String taskId, AssignUserTaskRequest request) {
    return Mono.fromCallable(() -> userTaskAPIService.reassignTask(taskId, request.getAssignee()))
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            reassigned -> RestUtils.buildResponse(reassigned, 200, "Task reassigned successfully"));
  }
}
