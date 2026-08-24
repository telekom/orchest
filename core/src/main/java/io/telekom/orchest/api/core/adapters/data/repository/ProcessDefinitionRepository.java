package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link ProcessDefinition} entities. Defines standard CRUD
 * operations for process definitions.
 */
public interface ProcessDefinitionRepository {

  /**
   * Saves a process definition to the data store.
   *
   * @param processDefinition The entity to save.
   * @return The saved entity.
   */
  ProcessDefinition save(ProcessDefinition processDefinition);

  /**
   * Retrieves all process definitions.
   *
   * @return A list of all process definitions.
   */
  List<ProcessDefinition> getProcessDefinitions();

  /**
   * Retrieves all process definitions.
   *
   * @param fields list of projections.
   * @return A list of all process definitions.
   */
  List<ProcessDefinition> getProcessDefinitions(List<String> fields);

  /**
   * Retrieves all versions of a process definition by its logical ID.
   *
   * @param definitionId The logical ID of the process (e.g., BPMN process ID).
   * @return A list of matching process definitions.
   */
  List<ProcessDefinition> getById(String definitionId);

  /**
   * Retrieves all versions of a process definition by its logical ID.
   *
   * @param definitionId The logical ID of the process (e.g., BPMN process ID).
   * @param fields list of projections.
   * @return A list of matching process definitions.
   */
  List<ProcessDefinition> getById(String definitionId, List<String> fields);

  /**
   * Retrieves the latest version of a process definition by its logical ID.
   *
   * @param definitionId The logical ID of the process.
   * @return An Optional containing the latest process definition, if found.
   */
  Optional<ProcessDefinition> getLatestById(String definitionId);

  /**
   * Retrieves a specific version of a process definition.
   *
   * @param definitionId The logical ID of the process.
   * @param version The version number.
   * @return An Optional containing the matching process definition, if found.
   */
  Optional<ProcessDefinition> getByIdAndVersion(String definitionId, Integer version);

  /**
   * Finds distinct process IDs (logical IDs) available in the system.
   *
   * @return A list of distinct process definition IDs.
   */
  List<ProcessDefinition> findDistinctProcessIds();

  /**
   * Retrieves a paged list of all process definitions.
   *
   * @param page The page number (0-indexed).
   * @param size The number of items per page.
   * @return A list of process definitions for the requested page.
   */
  List<ProcessDefinition> findAllDefinitions(int page, int size);

  /**
   * Retrieves a paged list of all process definitions.
   *
   * @param page The page number (0-indexed).
   * @param size The number of items per page.
   * @param fields list of projections.
   * @return A list of process definitions for the requested page.
   */
  List<ProcessDefinition> findAllDefinitions(int page, int size, List<String> fields);

  /**
   * Deletes a specific version of a process definition.
   *
   * @param definitionId The logical ID of the process definition.
   * @param version The version number to delete.
   * @return true if the definition was found and deleted, false otherwise.
   */
  boolean deleteByDefinitionIdAndVersion(String definitionId, Integer version);
}
