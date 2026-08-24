package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing DMN decision definitions. Provides methods to retrieve, save,
 * and query decision definitions.
 */
public interface DecisionDefinitionRepository {

  /**
   * Retrieves all decision definitions.
   *
   * @return A list of all decision definitions.
   */
  List<DecisionDefinition> getDecisionDefinition();

  /**
   * Retrieves all versions of a decision definition by its ID.
   *
   * @param definitionId The decision definition ID.
   * @return A list of decision definitions with the specified ID.
   */
  List<DecisionDefinition> getById(String definitionId);

  /**
   * Retrieves the latest version of a decision definition by its ID.
   *
   * @param decisionId The decision ID.
   * @return An Optional containing the latest decision definition, or empty if not found.
   */
  Optional<DecisionDefinition> getLatestById(String decisionId);

  /**
   * Retrieves the latest version of a decision definition by its decision ID.
   *
   * @param decisionId The decision ID.
   * @param version The decision definition version.
   * @return An Optional containing the latest decision definition, or empty if not found.
   */
  Optional<DecisionDefinition> getByDecisionIdAndVersion(String decisionId, Integer version);

  /**
   * Retrieves the latest version of a decision definition by its definition ID.
   *
   * @param definitionId The decision definition ID.
   * @return An Optional containing the latest decision definition, or empty if not found.
   */
  Optional<DecisionDefinition> getLatestByDefinitionId(String definitionId);

  /**
   * Retrieves a decision definition by ID and version.
   *
   * @param definitionId The decision definition ID.
   * @param version The version number.
   * @return An Optional containing the decision definition, or empty if not found.
   */
  Optional<DecisionDefinition> getByIdAndVersion(String definitionId, Integer version);

  /**
   * Retrieves a decision definition by definitionID and version.
   *
   * @param definitionId The decision ID.
   * @param version The version number.
   * @return An Optional containing the decision definition, or empty if not found.
   */
  Optional<DecisionDefinition> getByDefinitionIdAndVersion(String definitionId, Integer version);

  /**
   * Saves a decision definition.
   *
   * @param processDefinition The decision definition to save.
   * @return The saved decision definition.
   */
  DecisionDefinition save(DecisionDefinition processDefinition);

  /**
   * Retrieves decision definitions with pagination support.
   *
   * @param page The page number (0-indexed).
   * @param size The page size.
   * @return A list of decision definitions for the specified page.
   */
  List<DecisionDefinition> findAllDefinitions(int page, int size);

  /**
   * Deletes a specific version of a decision definition.
   *
   * @param definitionId The decision definition ID.
   * @param version The version number to delete.
   * @return true if the definition was found and deleted, false otherwise.
   */
  boolean deleteByDefinitionIdAndVersion(String definitionId, Integer version);
}
