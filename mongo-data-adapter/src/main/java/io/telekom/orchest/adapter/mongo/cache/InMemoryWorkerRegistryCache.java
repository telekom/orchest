package io.telekom.orchest.adapter.mongo.cache;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * In-memory lookup for common worker types, backed by the worker registry cache. Provides fast
 * checks to determine if a given worker type is registered as a common worker.
 */
@Slf4j
@Component
@DependsOn("cacheBeanRegistrar")
@RequiredArgsConstructor
public class InMemoryWorkerRegistryCache {

  private final Map<String, Boolean> commonWorkersMap;

  /**
   * Checks whether the given worker type is registered as a common worker.
   *
   * @param workerName the worker type name to check
   * @return true if the worker type is registered as common
   */
  public boolean isCommonWorker(String workerName) {
    return commonWorkersMap.containsKey(workerName);
  }
}
