package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.ProcessState;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link ProcessState} documents. Manages the current execution
 * state of deployed processes.
 */
@Repository
public interface MongoProcessStateRepository extends MongoRepository<ProcessState, String> {

  /**
   * Finds the process state for a given process ID.
   *
   * @param processId the process identifier
   * @return the matching process state, or empty if not found
   */
  Optional<ProcessState> findByProcessId(String processId);
}
