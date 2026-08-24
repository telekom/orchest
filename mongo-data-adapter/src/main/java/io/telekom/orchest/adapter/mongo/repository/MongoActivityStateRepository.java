package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.ActivityState;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link ActivityState} documents. Manages persistence of BPMN
 * activity states keyed by process definition, version, and activity ID.
 */
@Repository
public interface MongoActivityStateRepository extends MongoRepository<ActivityState, String> {

  /**
   * Finds all activity states belonging to a given process definition.
   *
   * @param processDefinitionId the process definition identifier
   * @return list of matching activity states
   */
  List<ActivityState> findByProcessDefinitionId(String processDefinitionId);

  /**
   * Finds all activity states for a specific version of a process definition.
   *
   * @param processDefinitionId the process definition identifier
   * @param version the definition version
   * @return list of matching activity states
   */
  List<ActivityState> findByProcessDefinitionIdAndVersion(
      String processDefinitionId, Integer version);

  /**
   * Finds a single activity state by its composite key of process definition, version, and activity
   * ID.
   *
   * @param processDefinitionId the process definition identifier
   * @param version the definition version
   * @param activityId the BPMN activity identifier
   * @return the matching activity state, or empty if not found
   */
  Optional<ActivityState> findByProcessDefinitionIdAndVersionAndActivityId(
      String processDefinitionId, Integer version, String activityId);
}
