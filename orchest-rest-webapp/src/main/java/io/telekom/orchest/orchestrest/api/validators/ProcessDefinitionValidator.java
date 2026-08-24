package io.telekom.orchest.orchestrest.api.validators;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Validation utilities for process definition existence and executability checks. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessDefinitionValidator {

  /**
   * Returns the process definition if present, otherwise throws a 400 RestExceptions.
   *
   * @param processDefinition the optional to unwrap
   * @param processDefinitionId the ID used in the error message
   * @return the process definition
   * @throws RestExceptions if the definition is not found
   */
  public static ProcessDefinition isPresent(
      Optional<ProcessDefinition> processDefinition, String processDefinitionId) {
    if (processDefinition.isEmpty()) {
      throw new RestExceptions("ProcessDefinition not found with id: " + processDefinitionId, 400);
    }
    return processDefinition.get();
  }

  /**
   * Returns the process definition if present and executable, otherwise throws a 400
   * RestExceptions.
   *
   * @param processDefinition the optional to unwrap
   * @param processDefinitionId the ID used in the error message
   * @return the executable process definition
   * @throws RestExceptions if the definition is not found or not executable
   */
  public static ProcessDefinition getIfExecutable(
      Optional<ProcessDefinition> processDefinition, String processDefinitionId) {
    ProcessDefinition present = isPresent(processDefinition, processDefinitionId);
    if (!present.isExecutableOrLegacy()) {
      throw new RestExceptions(
          "ProcessDefinition '" + processDefinitionId + "' is not executable", 400);
    }
    return present;
  }
}
