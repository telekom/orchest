package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.PendingTask;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Repository interface for managing pending tasks (service tasks waiting for worker completion).
 * Provides methods to find, save, and remove pending tasks.
 */
public interface PendingTaskRepository {
  /**
   * Finds all pending tasks for the specified worker IDs.
   *
   * @param workerIds The collection of worker IDs to search for.
   * @return A stream of pending tasks matching the worker IDs.
   */
  Stream<PendingTask> findAllByWorkerIdIn(Collection<String> workerIds);

  /**
   * Removes a pending task.
   *
   * @param task The pending task to remove.
   */
  void remove(PendingTask task);

  /**
   * Saves a pending task.
   *
   * @param pendingTask The pending task to save.
   * @return The saved pending task.
   */
  PendingTask save(PendingTask pendingTask);

  /**
   * Deletes all pending tasks for a specific process instance.
   *
   * @param processInstanceId The process instance ID.
   */
  void deleteByProcessInstanceId(String processInstanceId);
}
