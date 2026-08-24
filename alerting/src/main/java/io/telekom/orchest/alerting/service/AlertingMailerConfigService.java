package io.telekom.orchest.alerting.service;

import io.telekom.orchest.api.core.adapters.data.model.AlertRecipients;
import io.telekom.orchest.api.core.adapters.data.model.AlertingMailerConfig;
import io.telekom.orchest.api.core.adapters.data.repository.AlertingMailerConfigRepository;
import io.telekom.orchest.api.core.adapters.data.repository.AlertingMailerConfigRepository.MailerConfigPage;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** CRUD service for per-process-definition mailer recipient configurations. */
@Service
@RequiredArgsConstructor
public class AlertingMailerConfigService {

  private final AlertingMailerConfigRepository repository;

  /**
   * Returns all mailer configurations.
   *
   * @return all configs
   */
  public List<AlertingMailerConfig> findAll() {
    return repository.findAll();
  }

  /**
   * Returns a paginated list of mailer configurations.
   *
   * @param page zero-based page index
   * @param size page size
   * @return paginated results
   */
  public MailerConfigPage findAll(int page, int size) {
    return repository.findAll(page, size);
  }

  /**
   * Finds a mailer configuration by process definition ID.
   *
   * @param id the process definition identifier
   * @return the config if found
   */
  public Optional<AlertingMailerConfig> findByProcessId(String id) {
    return repository.findByProcessId(id);
  }

  /**
   * Creates a new mailer configuration for a process definition.
   *
   * @param processId the process definition identifier
   * @param alertingRecipient the recipient list (to, cc, bcc)
   * @return the persisted config
   */
  public AlertingMailerConfig create(String processId, AlertRecipients alertingRecipient) {
    return repository.save(
        AlertingMailerConfig.builder()
            .processId(processId)
            .alertingRecipient(alertingRecipient)
            .build());
  }

  /**
   * Updates an existing mailer configuration.
   *
   * @param id the config document ID
   * @param processId the new process definition identifier
   * @param alertingRecipient the updated recipient list
   * @return the updated config, or empty if not found
   */
  public Optional<AlertingMailerConfig> update(
      String id, String processId, AlertRecipients alertingRecipient) {
    return repository
        .findById(id)
        .map(
            existing -> {
              existing.setProcessId(processId);
              existing.setAlertingRecipient(alertingRecipient);
              return repository.save(existing);
            });
  }

  /**
   * Deletes a mailer configuration by its document ID.
   *
   * @param id the config document ID
   */
  public void deleteById(String id) {
    repository.deleteById(id);
  }
}
