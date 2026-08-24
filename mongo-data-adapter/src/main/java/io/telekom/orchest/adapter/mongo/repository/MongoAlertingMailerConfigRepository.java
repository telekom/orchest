package io.telekom.orchest.adapter.mongo.repository;

import io.telekom.orchest.adapter.mongo.model.AlertingMailerConfig;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link AlertingMailerConfig} documents. Stores per-process
 * email alerting configurations.
 */
@Repository
public interface MongoAlertingMailerConfigRepository
    extends MongoRepository<AlertingMailerConfig, String> {

  /**
   * Finds the mailer configuration associated with a specific process.
   *
   * @param processId the process identifier
   * @return the matching mailer config, or empty if not configured
   */
  Optional<AlertingMailerConfig> findByProcessId(String processId);
}
