package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.ProcessEnvVariablesMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoProcessEnvVariablesRepository;
import io.telekom.orchest.api.core.adapters.data.model.ProcessEnvVariables;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessEnvVariablesRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * MongoDB implementation of the {@link ProcessEnvVariablesRepository}. Adapts the Spring Data
 * MongoDB repository to the domain-agnostic repository interface for process environment variables.
 */
@Component
@RequiredArgsConstructor
public class MongoProcessEnvVariablesRepositoryAdapter implements ProcessEnvVariablesRepository {

  private final MongoProcessEnvVariablesRepository repository;
  private final MongoTemplate mongoTemplate;
  private final ProcessEnvVariablesMapper mapper;

  /**
   * Persists a process environment variables entry.
   *
   * @param processEnvVariables the environment variables to save
   * @return the saved environment variables
   */
  @Override
  public ProcessEnvVariables save(ProcessEnvVariables processEnvVariables) {
    return mapper.toDomain(repository.save(mapper.toDocument(processEnvVariables)));
  }

  /**
   * Returns all process environment variable entries.
   *
   * @return list of all environment variable entries
   */
  @Override
  public List<ProcessEnvVariables> getProcessDefinitions() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  /**
   * Returns all environment variable entries for a given process definition.
   *
   * @param definitionId the process definition ID
   * @return list of matching environment variable entries
   */
  @Override
  public List<ProcessEnvVariables> getByDefinitionIdId(String definitionId) {
    return repository.findAllByProcessDefinitionId(definitionId).stream()
        .map(mapper::toDomain)
        .toList();
  }

  /**
   * Returns a paginated list of environment variable entries sorted by creation date descending.
   *
   * @param page the zero-based page index
   * @param size the page size
   * @return list of environment variable entries for the requested page
   */
  @Override
  public List<ProcessEnvVariables> getAll(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return mongoTemplate.find(new Query().with(pageRequest), ProcessEnvVariables.class);
  }

  /**
   * Removes a process environment variables entry.
   *
   * @param processEnvVariables the environment variables entry to remove
   */
  @Override
  public void remove(ProcessEnvVariables processEnvVariables) {
    repository.delete(mapper.toDocument(processEnvVariables));
  }
}
