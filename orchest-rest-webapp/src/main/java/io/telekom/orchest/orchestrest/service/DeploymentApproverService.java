package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.orchestrest.extensions.approvalflow.model.Approver;
import io.telekom.orchest.orchestrest.extensions.approvalflow.repository.ApproverRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for managing deployment approvers. Handles the configuration of who is allowed to approve
 * deployments for specific processes.
 */
@Service
@RequiredArgsConstructor
public class DeploymentApproverService {

  private final ApproverRepository approverRepository;

  /**
   * Adds or updates an approver configuration.
   *
   * @param approver The approver configuration to save.
   * @return The saved approver configuration.
   */
  public Approver addApprover(Approver approver) {
    return approverRepository.save(approver);
  }

  /**
   * Retrieves the approver configuration for a specific process ID.
   *
   * @param processId The process ID.
   * @return An Optional containing the approver configuration if found.
   */
  public Optional<Approver> getApprover(String processId) {
    return approverRepository.findApproverByProcessId(processId);
  }

  public List<Approver> getAllProcessDefinitionsForApprover(String user) {
    return approverRepository.findAllDefinitionsForApprover(user);
  }

  public Optional<Approver> removeApprover(String approverId, String approverEmail) {
    Optional<Approver> approverById = approverRepository.findApproverById(approverId);
    if (approverById.isPresent()) {
      Approver approver = approverById.get();
      approver.getApprovers().remove(approverEmail);
      approverRepository.save(approver);
      return Optional.of(approver);
    }
    return Optional.empty();
  }
}
