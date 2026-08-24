package io.telekom.orchest.enginecore.bpmn.service;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessInstanceRepository;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing process instances. Handles retrieval and persistence of process instances.
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class ProcessInstanceService {

  private final ProcessInstanceRepository processInstanceRepository;

  /**
   * Retrieves a process instance by its unique ID.
   *
   * @param processInstanceId The process instance ID.
   * @return An Optional containing the ProcessInstance if found.
   */
  public Optional<ProcessInstance> getInstanceById(String processInstanceId) {
    return processInstanceRepository.getById(processInstanceId);
  }

  /**
   * Retrieves a process instance only if it is in an active state.
   *
   * <p>An instance is considered active if it is NOT in CANCELLED, COMPLETED, or TERMINATED state.
   *
   * @param processInstanceId The process instance ID.
   * @return An Optional containing the active ProcessInstance, or empty if not found or inactive.
   */
  public Optional<ProcessInstance> getProcessInstanceIfActive(String processInstanceId) {
    Optional<ProcessInstance> processInstance =
        processInstanceRepository.getById(processInstanceId);
    if (processInstance.isEmpty()) {
      log.info("No process instance found for the worker event with PI id: {}", processInstanceId);
      return Optional.empty();
    } else if (List.of(PIState.CANCELLED, PIState.COMPLETED, PIState.TERMINATED)
        .contains(processInstance.get().getState())) {
      log.info(
          "No active process instance found for the worker event with PI id: {}",
          processInstanceId);
      return Optional.empty();
    }
    return processInstance;
  }

  /**
   * Saves a process instance.
   *
   * @param processInstance The process instance to save.
   * @return The saved ProcessInstance.
   */
  public ProcessInstance save(ProcessInstance processInstance) {
    return processInstanceRepository.save(processInstance);
  }

  /**
   * Checks whether any active (non-terminal) child instances exist for a given parent.
   *
   * @param parentProcessInstanceId The parent process instance ID.
   * @return true if at least one child instance is still active.
   */
  public boolean hasActiveChildInstances(String parentProcessInstanceId) {
    return processInstanceRepository.hasActiveChildInstances(parentProcessInstanceId);
  }
}
