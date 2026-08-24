package io.telekom.orchest.orchestrest.extensions.sensitivevariables;

import io.telekom.orchest.cache.core.Cache;
import io.telekom.orchest.orchestrest.extensions.sensitivevariables.model.ProcessSensitiveVariables;
import io.telekom.orchest.orchestrest.extensions.sensitivevariables.repository.ProcessSensitiveVariablesRepository;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/** Service for managing sensitive variable configurations per process definition. */
@Service
public class ProcessSensitiveVariablesService {

  private final ProcessSensitiveVariablesRepository repository;
  private final Cache<String, ProcessSensitiveVariables> processSensitiveVariablesCache;

  public ProcessSensitiveVariablesService(
      ProcessSensitiveVariablesRepository repository,
      @Lazy Cache<String, ProcessSensitiveVariables> processSensitiveVariablesCache) {
    this.repository = repository;
    this.processSensitiveVariablesCache = processSensitiveVariablesCache;
  }

  public Optional<ProcessSensitiveVariables> findByProcessDefinitionId(String processDefinitionId) {
    return repository.findByProcessDefinitionId(processDefinitionId);
  }

  public ProcessSensitiveVariables create(ProcessSensitiveVariables processSensitiveVariables) {
    //        processSensitiveVariables.setCreatedAt(OffsetDateTime.now());
    //        processSensitiveVariables.setUpdatedAt(OffsetDateTime.now());
    return repository.save(processSensitiveVariables);
  }

  public ProcessSensitiveVariables update(ProcessSensitiveVariables processSensitiveVariables) {
    //        processSensitiveVariables.setUpdatedAt(OffsetDateTime.now());
    return repository.save(processSensitiveVariables);
  }

  public void remove(ProcessSensitiveVariables processSensitiveVariables) {
    repository.delete(processSensitiveVariables);
  }

  public Page<ProcessSensitiveVariables> findAll(int page, int size) {
    return repository.findAll(PageRequest.of(page, size));
  }

  public Optional<ProcessSensitiveVariables> get(String processDefinitionId) {
    return processSensitiveVariablesCache.get(processDefinitionId);
  }
}
