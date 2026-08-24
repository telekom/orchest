package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.ProcessInstance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link ProcessInstance} documents. Manages the persistence of
 * process execution state.
 */
@Repository
public interface MongoProcessInstanceRepository extends MongoRepository<ProcessInstance, String> {

  /**
   * Finds a process instance by its unique process instance ID.
   *
   * @param processInstanceId the process instance identifier
   * @return the matching process instance, or empty if not found
   */
  Optional<ProcessInstance> findByProcessInstanceId(String processInstanceId);

  /**
   * Finds all child process instance IDs for a given parent process instance.
   *
   * @param parentProcessInstanceId the parent process instance identifier
   * @return list of child process instances (only processInstanceId field projected)
   */
  @Query(value = "{ 'parentProcesActivity.processInstanceId': ?0}", fields = "processInstanceId")
  List<ProcessInstance> findAllChildInstanceIds(String parentProcessInstanceId);

  /**
   * Checks whether a parent process instance has any active child instances (STARTED, RUNNING,
   * HOLD, or INCIDENT state).
   *
   * @param parentProcessInstanceId the parent process instance identifier
   * @return list of active child process instances (only processInstanceId field projected)
   */
  @Query(
      value =
          "{ 'parentProcesActivity.processInstanceId': ?0, 'state': { $in: ['STARTED', 'RUNNING', 'HOLD', 'INCIDENT'] } }",
      fields = "processInstanceId")
  List<ProcessInstance> hasActiveChildInstances(String parentProcessInstanceId);
}
