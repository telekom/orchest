package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.PendingTask;
import java.util.Collection;
import java.util.stream.Stream;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link PendingTask} documents. Used to store and retrieve
 * tasks waiting for worker availability.
 */
@Repository
public interface MongoPendingTaskRepository extends MongoRepository<PendingTask, String> {

  /**
   * Finds all pending tasks assigned to any of the given worker IDs.
   *
   * @param workerIds collection of worker identifiers to match
   * @return a stream of matching pending tasks
   */
  Stream<PendingTask> findAllByWorkerIdIn(Collection<String> workerIds);

  /**
   * Deletes all pending tasks belonging to a specific process instance.
   *
   * @param processInstanceId the process instance identifier
   */
  void deleteByProcessInstanceId(String processInstanceId);
}
