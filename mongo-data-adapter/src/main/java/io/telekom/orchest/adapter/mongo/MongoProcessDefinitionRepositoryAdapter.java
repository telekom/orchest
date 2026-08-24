package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.cache.InMemoryProcessDefinitionCache;
import io.telekom.orchest.adapter.mongo.mapper.ProcessDefinitionMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoProcessDefinitionRepository;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessDefinitionRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * MongoDB implementation of the {@link ProcessDefinitionRepository}. Adapts the Spring Data MongoDB
 * repository to the domain-agnostic repository interface.
 */
@Component
@RequiredArgsConstructor
public class MongoProcessDefinitionRepositoryAdapter implements ProcessDefinitionRepository {

  private final MongoProcessDefinitionRepository repository;
  private final ProcessDefinitionMapper mapper;
  private final MongoTemplate mongoTemplate;
  private final InMemoryProcessDefinitionCache inMemoryProcessDefinitionCache;

  /** Initializes the in-memory process definition cache on startup. */
  @PostConstruct
  public void initCache() {
    inMemoryProcessDefinitionCache.clear();
    inMemoryProcessDefinitionCache.init(getProcessDefinitions());
  }

  /**
   * Returns all process definitions.
   *
   * @return list of all process definitions
   */
  @Override
  public List<ProcessDefinition> getProcessDefinitions() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  /**
   * Returns process definitions with projected fields. Currently not implemented.
   *
   * @param fields the fields to include in the projection
   * @return empty list (not yet implemented)
   */
  @Override
  public List<ProcessDefinition> getProcessDefinitions(List<String> fields) {
    return List.of();
  }

  /**
   * Returns all versions of a process definition by its ID, using cache when available.
   *
   * @param definitionId the process definition ID
   * @return list of all versions for the given definition
   */
  @Override
  public List<ProcessDefinition> getById(String definitionId) {
    // cache lookup
    List<ProcessDefinition> processDefinitions =
        inMemoryProcessDefinitionCache.getAll(definitionId);
    if (processDefinitions != null) {
      return processDefinitions;
    }
    // DB call
    return repository.findAllByDefinitionId(definitionId).stream().map(mapper::toDomain).toList();
  }

  /**
   * Returns all versions of a process definition with projected fields. Currently not implemented.
   *
   * @param definitionId the process definition ID
   * @param fields the fields to include in the projection
   * @return empty list (not yet implemented)
   */
  @Override
  public List<ProcessDefinition> getById(String definitionId, List<String> fields) {
    return List.of();
  }

  /**
   * Returns the latest version of a process definition, using cache when available.
   *
   * @param definitionId the process definition ID
   * @return the latest version, or empty if no definition exists with the given ID
   */
  @Override
  public Optional<ProcessDefinition> getLatestById(String definitionId) {
    // cache lookup
    ProcessDefinition processDefinition = inMemoryProcessDefinitionCache.get(definitionId);
    if (processDefinition != null) {
      return Optional.of(processDefinition);
    }

    return repository.findFirstByDefinitionIdOrderByVersionDesc(definitionId).map(mapper::toDomain);
  }

  /**
   * Returns a specific version of a process definition, using cache when available.
   *
   * @param definitionId the process definition ID
   * @param version the version number
   * @return the matching definition, or empty if not found
   */
  @Override
  public Optional<ProcessDefinition> getByIdAndVersion(String definitionId, Integer version) {
    // cache lookup
    ProcessDefinition processDefinition =
        inMemoryProcessDefinitionCache.getWithVersion(definitionId, version);
    if (processDefinition != null) {
      return Optional.of(processDefinition);
    }

    return repository.findByDefinitionIdAndVersion(definitionId, version).map(mapper::toDomain);
  }

  /**
   * Returns one entry per distinct process definition ID (deduplicates versions).
   *
   * @return list of distinct process definitions
   */
  @Override
  public List<ProcessDefinition> findDistinctProcessIds() {
    return repository.findDistinctProcessIds().stream().map(mapper::toDomain).toList();
  }

  /**
   * Returns a paginated list of process definitions with only definitionId and version fields.
   *
   * @param page the zero-based page index
   * @param size the page size
   * @return list of process definitions for the requested page
   */
  @Override
  public List<ProcessDefinition> findAllDefinitions(int page, int size) {
    Query query = new Query().with(PageRequest.of(page, size));
    query.fields().include("definitionId", "version");
    return mongoTemplate.find(query, ProcessDefinition.class);
  }

  /**
   * Returns a paginated list of process definitions with custom field projection.
   *
   * @param page the zero-based page index
   * @param size the page size
   * @param fields the fields to include in the projection
   * @return list of process definitions for the requested page
   */
  @Override
  public List<ProcessDefinition> findAllDefinitions(int page, int size, List<String> fields) {
    Query query = new Query().with(PageRequest.of(page, size));
    if (fields != null && !fields.isEmpty()) {
      query.fields().include(fields);
    }
    return mongoTemplate.find(query, ProcessDefinition.class);
  }

  /**
   * Persists a process definition.
   *
   * @param processDefinition the process definition to save
   * @return the saved process definition
   * @throws IllegalStateException if the definition cannot be saved
   */
  @Override
  public ProcessDefinition save(ProcessDefinition processDefinition) throws IllegalStateException {
    return mapper.toDomain(repository.save(mapper.toDocument(processDefinition)));
  }

  /**
   * Deletes a specific version of a process definition and evicts it from the cache.
   *
   * @param definitionId the process definition ID
   * @param version the version to delete
   * @return true if a definition was deleted, false if none matched
   */
  @Override
  public boolean deleteByDefinitionIdAndVersion(String definitionId, Integer version) {
    Long deletedCount = repository.deleteByDefinitionIdAndVersion(definitionId, version);
    if (deletedCount > 0) {
      inMemoryProcessDefinitionCache.removeVersion(definitionId, version);
      return true;
    }
    return false;
  }

  private Query getFindQuery(String definitionId, Integer version) {
    Query query = new Query();
    if (version != null) {
      query.addCriteria(Criteria.where("decisionIds").is(definitionId).and("version").is(version));
    } else {
      query.addCriteria(Criteria.where("decisionIds").is(definitionId));
    }
    return query;
  }
}
