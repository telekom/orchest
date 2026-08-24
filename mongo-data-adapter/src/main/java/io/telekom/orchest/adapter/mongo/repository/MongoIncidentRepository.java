package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.Incident;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link Incident} documents. Manages persistence of process
 * execution incidents for troubleshooting and alerting.
 */
@Repository
public interface MongoIncidentRepository extends MongoRepository<Incident, String> {

  /**
   * Finds all incidents raised within a specific process instance.
   *
   * @param processInstanceId the process instance identifier
   * @return list of incidents for the given instance
   */
  List<Incident> findByProcessInstanceId(String processInstanceId);

  /**
   * Finds all incidents raised across any instance of a specific process definition.
   *
   * @param processDefinitionId the process definition identifier
   * @return list of incidents for the given definition
   */
  List<Incident> findByProcessDefinitionId(String processDefinitionId);
}
