package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.orchestrest.api.dto.DefinitionInfo;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.extensions.approvalflow.DeploymentActionRequest;
import io.telekom.orchest.orchestrest.extensions.approvalflow.DeploymentApprovalRequest;
import io.telekom.orchest.orchestrest.extensions.approvalflow.DeploymentApprovalState;
import io.telekom.orchest.orchestrest.extensions.approvalflow.model.*;
import io.telekom.orchest.orchestrest.extensions.approvalflow.repository.ApproverRepository;
import io.telekom.orchest.orchestrest.extensions.approvalflow.repository.DeploymentApprovalRepository;
import io.telekom.orchest.orchestrest.service.decisiondefinition.DecisionDefinitionAPIService;
import io.telekom.orchest.orchestrest.service.processdefinition.ProcessDefinitionAPIService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Service for managing the deployment approval workflow. Handles creating approval requests,
 * processing approval actions (approve/reject), and triggering deployments upon approval.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentApprovalsService {

  private final DeploymentApprovalRepository deploymentApprovalRepository;
  private final ApproverRepository approverRepository;
  private final ProcessDefinitionAPIService processDefinitionService;
  private final DecisionDefinitionAPIService decisionDefinitionService;

  /**
   * Initiates a new deployment approval request. If it's the first version of a definition, it's
   * auto-approved and deployed.
   *
   * @param deploymentApprovalRequest The request details.
   * @return The created DeploymentApproval record.
   */
  public DeploymentApproval requestApproval(DeploymentApprovalRequest deploymentApprovalRequest) {
    ResourceDeploymentRequest deploymentRequest = deploymentApprovalRequest.getDeploymentRequest();
    String type = deploymentApprovalRequest.getType();
    Boolean compensateFlow = deploymentApprovalRequest.getDeploymentRequest().getCompensateFlow();
    DefinitionInfo deploymentVersionInfo;
    if ("BPMN".equalsIgnoreCase(type)) {
      deploymentVersionInfo = processDefinitionService.isNewVersion(deploymentRequest);
    } else {
      deploymentVersionInfo = decisionDefinitionService.isNewVersion(deploymentRequest);
    }
    if (!deploymentVersionInfo.isNewVersion()) {
      log.info(
          "definition: '{}' already present with version: {} ",
          deploymentVersionInfo.getDefinitionId(),
          deploymentVersionInfo.getVersion());
      throw new RestExceptions("Definition already exists", 400);
    }

    log.info(
        "found new definition with id: {} and version: {} ",
        deploymentVersionInfo.getDefinitionId(),
        deploymentVersionInfo.getVersion());

    Integer version = deploymentVersionInfo.getVersion();
    String processId = deploymentVersionInfo.getDefinitionId();
    List<String> approvers = new ArrayList<>();
    Optional<Approver> deploymentApprovers = approverRepository.findApproverByProcessId(processId);
    deploymentApprovers.ifPresentOrElse(
        approver -> approvers.addAll(approver.getApprovers()),
        () -> approvers.add(deploymentApprovalRequest.getRequestedBy()));
    DeploymentApproval deploymentApproval =
        DeploymentApproval.builder()
            .state(DeploymentApprovalState.REQUESTED)
            .requestedBy(deploymentApprovalRequest.getRequestedBy())
            .resourceDeploymentRequest(deploymentRequest)
            .resourceType(deploymentApprovalRequest.getType())
            .definitionId(processId)
            .nextVersion(String.valueOf(version))
            .reviewers(approvers)
            .build();

    // for 1st version we will deploy upfront
    if (version == 1) {
      List<String> requestedBy = List.of(deploymentApproval.getRequestedBy());
      deploymentApproval.setState(DeploymentApprovalState.ACCEPTED);
      deploymentApproval.getResourceDeploymentRequest().setApprovers(requestedBy);
      String resourceType = deploymentApproval.getResourceType();
      if (compensateFlow == null || !compensateFlow) {
        approverRepository.save(
            Approver.builder().processId(processId).approvers(requestedBy).build());
      }
      switch (resourceType) {
        case "BPMN":
          processDefinitionService.deployProcessDefinition(
              deploymentApproval.getResourceDeploymentRequest());
          break;
        case "DMN":
          decisionDefinitionService.deploy(deploymentApproval.getResourceDeploymentRequest());
          break;
      }
    }
    deploymentApprovalRepository.save(deploymentApproval);
    return deploymentApproval;
  }

  /**
   * Processes an action (approve/reject) on a deployment approval. If approved, triggers the actual
   * deployment.
   *
   * @param deploymentActionRequest The action request.
   * @return The updated DeploymentApproval record.
   */
  public DeploymentApproval submitApproval(DeploymentActionRequest deploymentActionRequest) {
    Optional<DeploymentApproval> approval =
        deploymentApprovalRepository.findById(deploymentActionRequest.getDeploymentRequestId());
    if (approval.isEmpty()) {
      throw new RestExceptions(
          "No approval found for id:" + deploymentActionRequest.getDeploymentRequestId(), 400);
    } else if (!approval.get().getState().equals(DeploymentApprovalState.REQUESTED)) {
      throw new RestExceptions(
          "Approval already processed with state: "
              + approval.get().getState()
              + " for id:"
              + deploymentActionRequest.getDeploymentRequestId(),
          400);
    }
    // update approval
    DeploymentApprovalState state = deploymentActionRequest.getState();
    DeploymentApproval deploymentApproval = approval.get();
    deploymentApproval.setApprovalTime(Instant.now());
    deploymentApproval.setState(state);
    deploymentApproval.setAuditLog(
        DeploymentApproval.DeploymentAuditLog.builder()
            .approver(deploymentActionRequest.getApprover())
            .approvalState(state)
            .createTime(Instant.now())
            .build());

    // deploy the resource
    if (state.equals(DeploymentApprovalState.ACCEPTED)) {
      String resourceType = deploymentApproval.getResourceType();
      switch (resourceType) {
        case "BPMN":
          processDefinitionService.deployProcessDefinition(
              deploymentApproval.getResourceDeploymentRequest());
          break;
        case "DMN":
          decisionDefinitionService.deploy(deploymentApproval.getResourceDeploymentRequest());
          break;
      }
    }
    // update approval
    deploymentApprovalRepository.save(deploymentApproval);
    return deploymentApproval;
  }

  /**
   * Retrieves approvals pending review by a specific user.
   *
   * @param reviewer The reviewer ID.
   * @param stateCSV Comma-separated list of approval states to filter by.
   * @return A list of matching deployment approvals.
   */
  public List<DeploymentApproval> getApprovalsForReviewer(String reviewer, String stateCSV) {
    List<DeploymentApprovalState> deploymentApprovalStates =
        Arrays.stream(stateCSV.split(",")).map(DeploymentApprovalState::valueOf).toList();
    return deploymentApprovalRepository.findAllByReviewersAndStateIn(
        List.of(reviewer), deploymentApprovalStates);
  }

  /**
   * Retrieves a paginated list of deployment approvals.
   *
   * @param approvedState Filter by state (optional).
   * @param page Page number.
   * @param size Page size.
   * @return A page of deployment approvals.
   */
  public Page<DeploymentApproval> getApprovals(
      DeploymentApprovalState approvedState, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    if (approvedState != null) {
      return deploymentApprovalRepository.findDeploymentApprovalsByState(
          approvedState, pageRequest);
    } else {
      return deploymentApprovalRepository.findAll(pageRequest);
    }
  }
}
