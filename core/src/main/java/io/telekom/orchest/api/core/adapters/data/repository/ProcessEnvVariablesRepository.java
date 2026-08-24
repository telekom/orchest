package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.model.ProcessEnvVariables;
import java.util.List;

/**
 * Repository interface for managing {@link ProcessDefinition} entities. Defines standard CRUD
 * operations for process definitions.
 */
public interface ProcessEnvVariablesRepository {

  /**
   * Saves a process definition to the data store.
   *
   * @param processEnvVariables The entity to save.
   * @return The saved entity.
   */
  ProcessEnvVariables save(ProcessEnvVariables processEnvVariables);

  /**
   * Retrieves all process definitions.
   *
   * @return A list of all process definitions.
   */
  List<ProcessEnvVariables> getProcessDefinitions();

  /**
   * Retrieves all versions of a process definition by its logical ID.
   *
   * @param definitionId The logical ID of the process (e.g., BPMN process ID).
   * @return A list of matching process definitions.
   */
  List<ProcessEnvVariables> getByDefinitionIdId(String definitionId);

  /**
   * Retrieves process environment variables with pagination support.
   *
   * @param page The page number (0-indexed).
   * @param size The page size.
   * @return A list of process environment variables for the specified page.
   */
  List<ProcessEnvVariables> getAll(int page, int size);

  /**
   * Removes a process environment variables entity from the data store.
   *
   * @param processEnvVariables The entity to remove.
   */
  void remove(ProcessEnvVariables processEnvVariables);
}
