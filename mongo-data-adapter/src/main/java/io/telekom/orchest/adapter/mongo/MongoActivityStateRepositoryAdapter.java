package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.ActivityStateMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoActivityStateRepository;
import io.telekom.orchest.api.core.adapters.data.model.ActivityState;
import io.telekom.orchest.api.core.adapters.data.repository.ActivityStateRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * MongoDB implementation of the {@link ActivityStateRepository}. Delegates to Spring Data MongoDB
 * and maps between domain and document models for activity state persistence.
 */
@Component
@RequiredArgsConstructor
public class MongoActivityStateRepositoryAdapter implements ActivityStateRepository {

  private final MongoActivityStateRepository repository;
  private final ActivityStateMapper mapper;

  /**
   * Retrieves all activity states from the database.
   *
   * @return list of all activity states
   */
  @Override
  public List<ActivityState> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  /**
   * Finds all activity states for the given process definition.
   *
   * @param processDefinitionId the process definition identifier
   * @return list of matching activity states
   */
  @Override
  public List<ActivityState> findByProcessDefinitionId(String processDefinitionId) {
    return repository.findByProcessDefinitionId(processDefinitionId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Finds all activity states for the given process definition and version.
   *
   * @param processDefinitionId the process definition identifier
   * @param version the process definition version
   * @return list of matching activity states
   */
  @Override
  public List<ActivityState> findByProcessDefinitionIdAndVersion(
      String processDefinitionId, Integer version) {
    return repository.findByProcessDefinitionIdAndVersion(processDefinitionId, version).stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Saves an activity state, performing an upsert based on process definition ID, version, and
   * activity ID. Updates timestamps accordingly.
   *
   * @param activityState the activity state to save
   * @return the persisted activity state
   */
  @Override
  public ActivityState save(ActivityState activityState) {
    return repository
        .findByProcessDefinitionIdAndVersionAndActivityId(
            activityState.getProcessDefinitionId(),
            activityState.getVersion(),
            activityState.getActivityId())
        .map(
            existing -> {
              if (activityState.getId() == null) {
                activityState.setId(existing.getId());
              }
              activityState.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
              if (activityState.getCreatedAt() == null) {
                activityState.setCreatedAt(existing.getCreatedAt());
              }
              return mapper.toDomain(repository.save(mapper.toDocument(activityState)));
            })
        .orElseGet(
            () -> {
              activityState.setId(null);
              activityState.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
              activityState.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
              return mapper.toDomain(repository.save(mapper.toDocument(activityState)));
            });
  }

  /**
   * Deletes an activity state matching the given process definition ID, version, and activity ID.
   *
   * @param processDefinitionId the process definition identifier
   * @param version the process definition version
   * @param activityId the activity identifier
   * @return the deleted activity state, or empty if not found
   */
  @Override
  public Optional<ActivityState> deleteByProcessDefinitionIdAndVersionAndActivityId(
      String processDefinitionId, Integer version, String activityId) {
    return repository
        .findByProcessDefinitionIdAndVersionAndActivityId(processDefinitionId, version, activityId)
        .map(
            document -> {
              repository.delete(document);
              return mapper.toDomain(document);
            });
  }
}
