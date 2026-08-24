package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.extensions.approvalflow.DeploymentActionRequest;
import io.telekom.orchest.orchestrest.extensions.approvalflow.DeploymentApprovalRequest;
import io.telekom.orchest.orchestrest.extensions.approvalflow.DeploymentApprovalState;
import io.telekom.orchest.orchestrest.extensions.approvalflow.model.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/deploymentApprovals")
@Tag(
    name = "Deployment Approvals",
    description = "Multi-reviewer approval workflow for process/decision deployments")
/** REST API interface for the deployment approval workflow. */
public interface IDeploymentApprovals {

  @PostMapping("/requestApproval")
  @Operation(
      summary = "Request deployment approval",
      description = "Submits a new deployment for review by configured approvers")
  Mono<ResponseDTO<DeploymentApproval>> requestApproval(
      @RequestBody @Valid DeploymentApprovalRequest deploymentApprovalRequest);

  @GetMapping("/reviews")
  @Operation(
      summary = "Get approvals pending for current reviewer",
      description = "Returns approvals assigned to the authenticated user, filterable by state")
  Mono<ResponseDTO<List<DeploymentApproval>>> getApprovalsByReviewer(
      @Parameter(
              description = "Comma-separated approval states to include (e.g. REQUESTED,APPROVED)")
          @RequestParam(defaultValue = "REQUESTED")
          String stateCSV);

  @PostMapping("/submitApproval")
  @Operation(
      summary = "Submit approval decision",
      description = "Approve or reject a pending deployment request")
  Mono<ResponseDTO<DeploymentApproval>> submitApproval(
      @RequestBody @Valid DeploymentActionRequest deploymentActionRequest);

  @GetMapping
  @Operation(summary = "List approvals (paginated)", description = "Filterable by approval state")
  Mono<Page<DeploymentApproval>> getApprovals(
      @Parameter(description = "Filter by approval state") @RequestParam
          DeploymentApprovalState approvedState,
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size);

  @PostMapping("/addApprovers")
  @Operation(
      summary = "Add an approver",
      description = "Register a new approver for a process definition")
  Mono<ResponseDTO<Approver>> addApprover(@RequestBody @Valid Approver approver);

  @DeleteMapping("/removeApprover")
  @Operation(summary = "Remove an approver")
  Mono<ResponseDTO<Approver>> deleteApprover(
      @Parameter(description = "Approver ID") @RequestParam String approverId,
      @Parameter(description = "Approver email address") @RequestParam String approverEmail);

  @GetMapping("/getApprovers")
  @Operation(
      summary = "Get approver config for a process",
      description = "Returns the configured approver for a given process definition")
  Mono<ResponseDTO<Approver>> getApprover(
      @Parameter(description = "Process definition ID") @RequestParam String processId);

  @GetMapping("/getDefinitions")
  @Operation(
      summary = "Get definitions for current user",
      description =
          "Returns all process definitions where the current user is a configured approver")
  Mono<ResponseDTO<List<Approver>>> getDefinitionsForUser();
}
