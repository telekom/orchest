package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.ProcessStateMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoProcessStateRepository;
import io.telekom.orchest.api.core.adapters.data.model.ProcessState;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessStateRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * MongoDB implementation of the {@link ProcessStateRepository}. Adapts the Spring Data MongoDB
 * repository to the domain-agnostic repository interface for process execution state.
 */
@Component
@RequiredArgsConstructor
public class MongoProcessStateRepositoryAdapter implements ProcessStateRepository {

  private final MongoProcessStateRepository repository;
  private final ProcessStateMapper mapper;

  /**
   * Finds the process state for a given process ID.
   *
   * @param processId the process ID
   * @return the process state, or empty if not found
   */
  @Override
  public Optional<ProcessState> findByProcessId(String processId) {
    return repository.findByProcessId(processId).map(mapper::toDomain);
  }

  /**
   * Returns all process states.
   *
   * @return list of all process states
   */
  @Override
  public List<ProcessState> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  /**
   * Saves a process state, updating the existing document if one exists for the same process ID.
   *
   * @param processState the process state to save
   * @return the saved process state
   */
  @Override
  public ProcessState save(ProcessState processState) {
    return repository
        .findByProcessId(processState.getProcessId())
        .map(
            existing -> {
              if (processState.getId() == null) {
                processState.setId(existing.getId());
              }
              return mapper.toDomain(repository.save(mapper.toDocument(processState)));
            })
        .orElseGet(() -> mapper.toDomain(repository.save(mapper.toDocument(processState))));
  }

  /**
   * Deletes the process state for a given process ID and returns the deleted state.
   *
   * @param processId the process ID whose state should be deleted
   * @return the deleted process state, or empty if none existed
   */
  @Override
  public Optional<ProcessState> deleteByProcessId(String processId) {
    return repository
        .findByProcessId(processId)
        .map(
            document -> {
              repository.delete(document);
              return mapper.toDomain(document);
            });
  }
}
