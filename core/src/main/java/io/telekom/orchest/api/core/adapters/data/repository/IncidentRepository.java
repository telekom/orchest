package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.Incident;
import java.util.List;

/** Repository for persisting and querying {@link Incident} documents. */
public interface IncidentRepository {

  /**
   * Finds all incidents for a given process instance.
   *
   * @param processInstanceId the process instance identifier
   * @return list of matching incidents
   */
  List<Incident> findByProcessInstanceId(String processInstanceId);

  /**
   * Finds all incidents for a given process definition.
   *
   * @param processDefinitionId the process definition identifier
   * @return list of matching incidents
   */
  List<Incident> findByProcessDefinitionId(String processDefinitionId);

  /**
   * Saves an incident.
   *
   * @param incident the incident to save
   * @return the saved incident
   */
  Incident save(Incident incident);
}
