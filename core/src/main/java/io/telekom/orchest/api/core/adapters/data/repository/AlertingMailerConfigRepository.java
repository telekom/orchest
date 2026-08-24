package io.telekom.orchest.api.core.adapters.data.repository;

import io.telekom.orchest.api.core.adapters.data.model.AlertingMailerConfig;
import java.util.List;
import java.util.Optional;

/** Repository for persisting and querying {@link AlertingMailerConfig} documents. */
public interface AlertingMailerConfigRepository {

  /**
   * Saves a mailer configuration.
   *
   * @param config the mailer configuration to save
   * @return the saved mailer configuration
   */
  AlertingMailerConfig save(AlertingMailerConfig config);

  /**
   * Finds a mailer configuration by its unique ID.
   *
   * @param id the configuration ID
   * @return an Optional containing the configuration, or empty if not found
   */
  Optional<AlertingMailerConfig> findById(String id);

  /**
   * Finds a mailer configuration by process ID.
   *
   * @param processId the process definition ID
   * @return an Optional containing the configuration, or empty if not found
   */
  Optional<AlertingMailerConfig> findByProcessId(String processId);

  /**
   * Retrieves all mailer configurations.
   *
   * @return list of all mailer configurations
   */
  List<AlertingMailerConfig> findAll();

  /**
   * Retrieves all mailer configurations with pagination.
   *
   * @param page the page number (0-indexed)
   * @param size the page size
   * @return a paginated result of mailer configurations
   */
  MailerConfigPage findAll(int page, int size);

  /**
   * Deletes a mailer configuration by its unique ID.
   *
   * @param id the configuration ID to delete
   */
  void deleteById(String id);

  record MailerConfigPage(
      List<AlertingMailerConfig> content, long totalElements, int totalPages, int page, int size) {}
}
