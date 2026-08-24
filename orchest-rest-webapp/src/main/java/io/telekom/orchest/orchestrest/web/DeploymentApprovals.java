package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import io.telekom.orchest.orchestrest.extensions.approvalflow.DeploymentActionRequest;
import io.telekom.orchest.orchestrest.extensions.approvalflow.DeploymentApprovalRequest;
import io.telekom.orchest.orchestrest.extensions.approvalflow.DeploymentApprovalState;
import io.telekom.orchest.orchestrest.extensions.approvalflow.model.*;
import io.telekom.orchest.orchestrest.service.DeploymentApprovalsService;
import io.telekom.orchest.orchestrest.service.DeploymentApproverService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IDeploymentApprovals;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for the deployment approval workflow. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeploymentApprovals implements IDeploymentApprovals {

  private final DeploymentApprovalsService deploymentApprovalsService;
  private final DeploymentApproverService deploymentApproverService;

  @Override
  public Mono<ResponseDTO<DeploymentApproval>> requestApproval(
      DeploymentApprovalRequest deploymentApprovalRequest) {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx =
              ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, new LoggedInUserContext());
          deploymentApprovalRequest.setRequestedBy(userCtx.getUserId());
          return Mono.fromCallable(
                  () -> {
                    DeploymentApproval approval =
                        deploymentApprovalsService.requestApproval(deploymentApprovalRequest);
                    String msg =
                        approval.getState().equals(DeploymentApprovalState.ACCEPTED)
                            ? "Deployment done"
                            : "Deployment Accepted for approval";
                    return RestUtils.buildResponse(approval, 200, msg);
                  })
              .subscribeOn(Schedulers.boundedElastic());
        });
  }

  @Override
  public Mono<ResponseDTO<DeploymentApproval>> submitApproval(
      DeploymentActionRequest deploymentActionRequest) {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx =
              ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, new LoggedInUserContext());
          deploymentActionRequest.setApprover(userCtx.getUserId());
          return Mono.fromCallable(
                  () -> {
                    DeploymentApproval approval =
                        deploymentApprovalsService.submitApproval(deploymentActionRequest);
                    return RestUtils.buildResponse(approval, 200, "Submitted");
                  })
              .subscribeOn(Schedulers.boundedElastic());
        });
  }

  @Override
  public Mono<ResponseDTO<List<DeploymentApproval>>> getApprovalsByReviewer(String stateCSV) {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx =
              ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, new LoggedInUserContext());
          return Mono.fromCallable(
                  () ->
                      deploymentApprovalsService.getApprovalsForReviewer(
                          userCtx.getUserId(), stateCSV))
              .subscribeOn(Schedulers.boundedElastic())
              .map(list -> RestUtils.buildResponse(list, 200, "Success"));
        });
  }

  @Override
  public Mono<Page<DeploymentApproval>> getApprovals(
      DeploymentApprovalState approvedState, int page, int size) {
    return Mono.fromCallable(
            () -> deploymentApprovalsService.getApprovals(approvedState, page, size))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<ResponseDTO<Approver>> addApprover(Approver approver) {
    return Mono.fromCallable(() -> deploymentApproverService.addApprover(approver))
        .subscribeOn(Schedulers.boundedElastic())
        .map(a -> RestUtils.buildResponse(a, 200, "Created"));
  }

  @Override
  public Mono<ResponseDTO<Approver>> deleteApprover(String approverId, String approverEmail) {
    return Mono.fromCallable(
            () -> deploymentApproverService.removeApprover(approverId, approverEmail))
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            opt ->
                opt.map(a -> RestUtils.buildResponse(a, 200, "Removed"))
                    .orElseThrow(() -> new RestExceptions("Not Found", 404)));
  }

  @Override
  public Mono<ResponseDTO<Approver>> getApprover(String processId) {
    return Mono.fromCallable(() -> deploymentApproverService.getApprover(processId))
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            opt ->
                opt.map(a -> RestUtils.buildResponse(a, 200, "Found"))
                    .orElseThrow(() -> new RestExceptions("Not Found", 404)));
  }

  @Override
  public Mono<ResponseDTO<List<Approver>>> getDefinitionsForUser() {
    return Mono.deferContextual(
        ctx -> {
          LoggedInUserContext userCtx =
              ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, new LoggedInUserContext());
          return Mono.fromCallable(
                  () ->
                      deploymentApproverService.getAllProcessDefinitionsForApprover(
                          userCtx.getUserId()))
              .subscribeOn(Schedulers.boundedElastic())
              .map(list -> RestUtils.buildResponse(list, 200, "Success"));
        });
  }
}
