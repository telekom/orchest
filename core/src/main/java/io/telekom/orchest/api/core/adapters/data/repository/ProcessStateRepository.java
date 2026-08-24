package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.ProcessState;
import java.util.List;
import java.util.Optional;

/** Repository for per-process enable/disable state flags. */
public interface ProcessStateRepository {

  /**
   * Finds the process state for a given process ID.
   *
   * @param processId the process definition identifier
   * @return an Optional containing the process state, or empty if not found
   */
  Optional<ProcessState> findByProcessId(String processId);

  /**
   * Retrieves all process states.
   *
   * @return list of all process states
   */
  List<ProcessState> findAll();

  /**
   * Saves a process state.
   *
   * @param processState the process state to save
   * @return the saved process state
   */
  ProcessState save(ProcessState processState);

  /**
   * Deletes the process state for the given process id, if it exists.
   *
   * @param processId The process definition id.
   * @return The deleted process state, or empty if none existed.
   */
  Optional<ProcessState> deleteByProcessId(String processId);
}
