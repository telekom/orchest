package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.PendingTaskMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoPendingTaskRepository;
import io.telekom.orchest.api.core.adapters.data.model.PendingTask;
import io.telekom.orchest.api.core.adapters.data.repository.PendingTaskRepository;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * MongoDB implementation of the {@link PendingTaskRepository}. Adapts the Spring Data MongoDB
 * repository to the domain-agnostic repository interface. Manages pending tasks that are waiting
 * for worker availability.
 */
@Repository
@RequiredArgsConstructor
public class MongoPendingTaskRepositoryAdapter implements PendingTaskRepository {

  private final MongoPendingTaskRepository repository;
  private final PendingTaskMapper mapper;

  /**
   * Finds all pending tasks assigned to any of the given worker IDs.
   *
   * @param workerIds the collection of worker IDs to match
   * @return stream of matching pending tasks
   */
  @Override
  public Stream<PendingTask> findAllByWorkerIdIn(Collection<String> workerIds) {
    return repository.findAllByWorkerIdIn(workerIds).map(mapper::toDomain);
  }

  /**
   * Removes a specific pending task.
   *
   * @param task the pending task to remove
   */
  @Override
  public void remove(PendingTask task) {
    repository.delete(mapper.toDocument(task));
  }

  /**
   * Persists a pending task.
   *
   * @param pendingTask the pending task to save
   * @return the saved pending task
   */
  @Override
  public PendingTask save(PendingTask pendingTask) {
    return mapper.toDomain(repository.save(mapper.toDocument(pendingTask)));
  }

  /**
   * Deletes all pending tasks for a given process instance.
   *
   * @param processInstanceId the process instance ID whose pending tasks should be removed
   */
  @Override
  public void deleteByProcessInstanceId(String processInstanceId) {
    repository.deleteByProcessInstanceId(processInstanceId);
  }
}
