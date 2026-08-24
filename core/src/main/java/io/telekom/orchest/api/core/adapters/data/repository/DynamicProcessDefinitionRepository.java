package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.DynamicProcessDefinition;

/** Repository interface for managing {@link DynamicProcessDefinition} entities. */
public interface DynamicProcessDefinitionRepository {

  /**
   * Saves a process definition to the data store.
   *
   * @param processDefinition The entity to save.
   * @return The saved entity.
   */
  DynamicProcessDefinition save(DynamicProcessDefinition processDefinition);

  /**
   * Retrieves a dynamic process definition by its associated process instance ID.
   *
   * @param processInstanceId the process instance identifier
   * @return the matching dynamic process definition
   */
  DynamicProcessDefinition getByProcessInstanceId(String processInstanceId);
}
