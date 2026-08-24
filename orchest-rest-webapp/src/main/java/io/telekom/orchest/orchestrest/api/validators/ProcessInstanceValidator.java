package io.telekom.orchest.orchestrest.api.validators;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Validation utilities for process instance existence checks. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessInstanceValidator {

  /**
   * Returns the process instance if present, otherwise throws a 400 RestExceptions.
   *
   * @param processInstance the optional to unwrap
   * @param processInstanceId the ID used in the error message
   * @return the process instance
   * @throws RestExceptions if the instance is not found
   */
  public static ProcessInstance getIfPresent(
      Optional<ProcessInstance> processInstance, String processInstanceId) {
    if (processInstance.isEmpty()) {
      throw new RestExceptions("ProcessInstance not found with id: " + processInstanceId, 400);
    }
    return processInstance.get();
  }
}
