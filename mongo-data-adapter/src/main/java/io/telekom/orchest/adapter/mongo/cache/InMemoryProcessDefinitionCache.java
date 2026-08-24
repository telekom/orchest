package io.telekom.orchest.adapter.mongo.cache;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * In-memory cache for process definitions, keyed by definition ID with version-aware lookups.
 * Populated via MongoDB change streams and used for fast process definition resolution at runtime.
 */
@Slf4j
@Component
public class InMemoryProcessDefinitionCache {
  private final Map<String, List<ProcessDefinition>> cache = new ConcurrentHashMap<>();
  private final Map<String, String> entityIdToDefinitionIdMap = new ConcurrentHashMap<>();

  /**
   * Initializes the cache with a list of process definitions. Currently a no-op placeholder.
   *
   * @param processDefinitions the initial set of process definitions to load
   */
  public void init(List<ProcessDefinition> processDefinitions) {
    //        log.info("Initializing process definition cache...");
    //        processDefinitions.forEach(this::put);
    //        log.info("Initialized process definition cache.");
  }

  /**
   * Adds or updates a process definition in the cache.
   *
   * @param value the process definition to cache
   */
  public void put(ProcessDefinition value) {
    String key = value.getDefinitionId();
    List<ProcessDefinition> processDefinitions;
    if (cache.containsKey(key)) {
      processDefinitions = cache.get(key);
    } else {
      processDefinitions = new ArrayList<>();
    }
    processDefinitions.add(value);
    cache.put(key, processDefinitions);
    entityIdToDefinitionIdMap.put(value.getId(), value.getDefinitionId());
  }

  /**
   * Removes all versions of a process definition from the cache using its MongoDB entity ID.
   *
   * @param entityId the MongoDB document ID of the process definition
   */
  public void removeViaEntityId(String entityId) {
    cache.remove(entityIdToDefinitionIdMap.get(entityId));
    entityIdToDefinitionIdMap.remove(entityId);
  }

  /**
   * Resolves a process definition ID from its MongoDB entity ID.
   *
   * @param entityId the MongoDB document ID
   * @return the logical process definition ID, or null if not found
   */
  public String getDefinitionIdViaEntity(String entityId) {
    return entityIdToDefinitionIdMap.get(entityId);
  }

  /**
   * Returns the latest version of a process definition by its definition ID.
   *
   * @param processDefinitionId the logical process definition ID
   * @return the highest-versioned process definition, or null if not cached
   */
  public ProcessDefinition get(String processDefinitionId) {
    List<ProcessDefinition> processDefinitions = cache.get(processDefinitionId);
    if (processDefinitions == null || processDefinitions.isEmpty()) {
      return null;
    }
    processDefinitions.sort(Comparator.comparing(ProcessDefinition::getVersion).reversed());
    return processDefinitions.getFirst();
  }

  /**
   * Returns all cached versions of a process definition.
   *
   * @param processDefinitionId the logical process definition ID
   * @return all versions, or null if not cached
   */
  public List<ProcessDefinition> getAll(String processDefinitionId) {
    return cache.get(processDefinitionId);
  }

  /**
   * Returns a specific version of a process definition.
   *
   * @param processDefinitionId the logical process definition ID
   * @param version the version number to retrieve
   * @return the matching process definition, or null if not found
   */
  public ProcessDefinition getWithVersion(String processDefinitionId, Integer version) {
    List<ProcessDefinition> processDefinitions = cache.get(processDefinitionId);
    if (processDefinitions == null) {
      return null;
    }
    return processDefinitions.stream()
        .filter(processDefinition -> processDefinition.getVersion().equals(version))
        .findFirst()
        .orElse(null);
  }

  /**
   * Checks whether a process definition is present in the cache.
   *
   * @param key the process definition ID to check
   * @return true if the definition is cached
   */
  public boolean contains(String key) {
    return cache.containsKey(key);
  }

  /**
   * Removes a specific version of a process definition from the cache.
   *
   * @param definitionId the logical process definition ID
   * @param version the version number to remove
   */
  public void removeVersion(String definitionId, Integer version) {
    List<ProcessDefinition> processDefinitions = cache.get(definitionId);
    if (processDefinitions != null) {
      processDefinitions.removeIf(pd -> pd.getVersion().equals(version));
      if (processDefinitions.isEmpty()) {
        cache.remove(definitionId);
      }
    }
  }

  /** Clears all entries from the cache. */
  public void clear() {
    cache.clear();
  }
}
