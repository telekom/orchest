package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.AlertingMailerConfigMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoAlertingMailerConfigRepository;
import io.telekom.orchest.api.core.adapters.data.model.AlertingMailerConfig;
import io.telekom.orchest.api.core.adapters.data.repository.AlertingMailerConfigRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * MongoDB implementation of the {@link AlertingMailerConfigRepository}. Delegates to Spring Data
 * MongoDB for alerting mailer configuration persistence and retrieval.
 */
@Component
@RequiredArgsConstructor
public class MongoAlertingMailerConfigRepositoryAdapter implements AlertingMailerConfigRepository {

  private final MongoAlertingMailerConfigRepository repository;
  private final AlertingMailerConfigMapper mapper;

  /**
   * Persists an alerting mailer configuration to the database.
   *
   * @param config the mailer configuration to save
   * @return the persisted mailer configuration
   */
  @Override
  public AlertingMailerConfig save(AlertingMailerConfig config) {
    return mapper.toDomain(repository.save(mapper.toDocument(config)));
  }

  /**
   * Finds an alerting mailer configuration by its unique identifier.
   *
   * @param id the configuration identifier
   * @return the configuration if found, or empty
   */
  @Override
  public Optional<AlertingMailerConfig> findById(String id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  /**
   * Finds an alerting mailer configuration by its associated process ID.
   *
   * @param processId the process identifier
   * @return the configuration if found, or empty
   */
  @Override
  public Optional<AlertingMailerConfig> findByProcessId(String processId) {
    return repository.findByProcessId(processId).map(mapper::toDomain);
  }

  /**
   * Retrieves all alerting mailer configurations.
   *
   * @return list of all mailer configurations
   */
  @Override
  public List<AlertingMailerConfig> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  /**
   * Retrieves all alerting mailer configurations with pagination.
   *
   * @param page the zero-based page index
   * @param size the page size
   * @return a paginated result of mailer configurations
   */
  @Override
  public MailerConfigPage findAll(int page, int size) {
    var result = repository.findAll(PageRequest.of(page, size));
    var content = result.getContent().stream().map(mapper::toDomain).toList();
    return new MailerConfigPage(
        content, result.getTotalElements(), result.getTotalPages(), page, size);
  }

  /**
   * Deletes an alerting mailer configuration by its unique identifier.
   *
   * @param id the configuration identifier to delete
   */
  @Override
  public void deleteById(String id) {
    repository.deleteById(id);
  }
}
