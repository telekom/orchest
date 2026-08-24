package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing BPMN process instances. Provides methods to retrieve and save
 * process instances.
 */
public interface ProcessInstanceRepository {

  /**
   * Retrieves all process instances.
   *
   * @return A list of all process instances.
   */
  List<ProcessInstance> getProcessInstances();

  /**
   * Retrieves all process instances.
   *
   * @param fields list of projection field.
   * @return A list of all process instances.
   */
  List<ProcessInstance> getProcessInstances(List<String> fields);

  /**
   * Retrieves a process instance by its ID.
   *
   * @param processInstanceId The process instance ID.
   * @return An Optional containing the process instance, or empty if not found.
   */
  Optional<ProcessInstance> getById(String processInstanceId);

  /**
   * Retrieves a process instance by its ID with specific fields.
   *
   * @param processInstanceId The process instance ID.
   * @param fields list of projection field.
   * @return An Optional containing the process instance, or empty if not found.
   */
  Optional<ProcessInstance> getById(String processInstanceId, List<String> fields);

  /**
   * Saves a process instance.
   *
   * @param processInstance The process instance to save.
   * @return The saved process instance.
   */
  ProcessInstance save(ProcessInstance processInstance);

  /**
   * Checks whether any active (non-terminal) child instances exist for a given parent.
   *
   * @param parentProcessInstanceId The parent process instance ID.
   * @return true if at least one child instance is still active.
   */
  boolean hasActiveChildInstances(String parentProcessInstanceId);
}
