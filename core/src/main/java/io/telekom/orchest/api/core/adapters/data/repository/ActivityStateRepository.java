package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.ActivityState;
import java.util.List;
import java.util.Optional;

/** Repository for persisting and querying {@link ActivityState} documents. */
public interface ActivityStateRepository {

  /**
   * Retrieves all activity states.
   *
   * @return list of all activity states
   */
  List<ActivityState> findAll();

  /**
   * Finds all activity states for a given process definition.
   *
   * @param processDefinitionId the process definition identifier
   * @return list of matching activity states
   */
  List<ActivityState> findByProcessDefinitionId(String processDefinitionId);

  /**
   * Finds all activity states for a given process definition and version.
   *
   * @param processDefinitionId the process definition identifier
   * @param version the process definition version
   * @return list of matching activity states
   */
  List<ActivityState> findByProcessDefinitionIdAndVersion(
      String processDefinitionId, Integer version);

  /**
   * Saves an activity state.
   *
   * @param activityState the activity state to save
   * @return the saved activity state
   */
  ActivityState save(ActivityState activityState);

  /**
   * Deletes an activity state by process definition ID, version, and activity ID.
   *
   * @param processDefinitionId the process definition identifier
   * @param version the process definition version
   * @param activityId the activity identifier
   * @return an Optional containing the deleted activity state, or empty if not found
   */
  Optional<ActivityState> deleteByProcessDefinitionIdAndVersionAndActivityId(
      String processDefinitionId, Integer version, String activityId);
}
