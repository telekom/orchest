package io.telekom.orchest.adapter.mongo.config;

import io.telekom.orchest.adapter.mongo.model.WorkerRegistry;
import io.telekom.orchest.api.core.adapters.data.model.ActivityState;
import io.telekom.orchest.api.core.adapters.data.repository.ActivityStateRepository;
import io.telekom.orchest.api.core.request.WorkerRegistryRequest;
import io.telekom.orchest.cache.core.CacheDefinition;
import io.telekom.orchest.cache.core.CacheHooks;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Configures in-memory cache definitions for worker registry and activity state data, backed by
 * periodic MongoDB polling via the cache framework.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class InMemoryCachingConfiguration {

  //    @Bean
  //    public CacheDefinition<String, ProcessDefinition,
  // io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition>
  // processDefinitionCacheDefinition(MongoTemplate mongo, ProcessDefinitionMapper mapper) {
  //        return CacheDefinition.<String, ProcessDefinition,
  //
  // io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition>builder()
  //                .name("processDefinitionCache")
  //                .loader(() -> mongo.findAll(ProcessDefinition.class))
  //                .transform(mapper::toDomain)
  //
  // .keyExtractor(io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition::getId)
  //                .refreshInterval(Duration.ofSeconds(5))
  //                .loadOnStartup(true)
  //                .failSafe(true)
  //                .retryAttempts(2)
  //                .retryBackoff(Duration.ofSeconds(1))
  //                .build();
  //    }
  //
  //    @Bean
  //    public CacheDefinition<String, DecisionDefinition,
  // io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition>
  // decisionDefinitionCacheDefinition(MongoTemplate mongo, DecisionDefinitionMapper mapper) {
  //        return CacheDefinition.<String, DecisionDefinition,
  //
  // io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition>builder()
  //                .name("decisionDefinitionCache")
  //                .loader(() -> mongo.findAll(DecisionDefinition.class))
  //                .transform(mapper::toDomain)
  //
  // .keyExtractor(io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition::getId)
  //                .refreshInterval(Duration.ofSeconds(5))
  //                .loadOnStartup(true)
  //                .failSafe(true)
  //                .retryAttempts(2)
  //                .retryBackoff(Duration.ofSeconds(1))
  //                .build();
  //    }

  /**
   * Provides a shared concurrent map tracking which worker types are marked as common workers.
   *
   * @return a thread-safe map of worker type to presence flag
   */
  @Bean("commonWorkersMap")
  public Map<String, Boolean> commonWorkersMap() {
    return new ConcurrentHashMap<>();
  }

  /**
   * Defines the worker registry cache that refreshes every 5 seconds from MongoDB and updates the
   * common workers map on each refresh.
   *
   * @param mongo the MongoTemplate used to load worker registries
   * @param commonWorkersMap shared map updated with common worker types after each refresh
   * @return the cache definition for worker registries keyed by namespace
   */
  @Bean
  public CacheDefinition<String, WorkerRegistry, WorkerRegistry> workerRegistryCacheDefinition(
      MongoTemplate mongo, @Qualifier("commonWorkersMap") Map<String, Boolean> commonWorkersMap) {
    return CacheDefinition.<String, WorkerRegistry, WorkerRegistry>builder()
        .name("workerRegistryCache")
        .loader(() -> mongo.findAll(WorkerRegistry.class))
        .keyExtractor(WorkerRegistry::getNameSpace)
        .refreshInterval(Duration.ofSeconds(5))
        .loadOnStartup(true)
        .failSafe(true)
        .retryAttempts(2)
        .retryBackoff(Duration.ofSeconds(1))
        .hooks(
            new CacheHooks<>() {
              @Override
              public void afterRefresh(
                  String cacheName, Map<String, WorkerRegistry> snapshot, long durationMillis) {
                Map<String, Boolean> newValues = new ConcurrentHashMap<>();
                snapshot
                    .values()
                    .forEach(
                        workerRegistry -> {
                          if (workerRegistry == null
                              || workerRegistry.getWorkerInfos() == null
                              || workerRegistry.getWorkerInfos().isEmpty()) {
                            return;
                          }
                          workerRegistry.getWorkerInfos().stream()
                              .filter(WorkerRegistryRequest.WorkerInfo::isCommonWorker)
                              .forEach(workerInfo -> newValues.put(workerInfo.getType(), true));
                        });
                commonWorkersMap.clear();
                commonWorkersMap.putAll(newValues);
                log.debug("registered common workers count: {}", commonWorkersMap.size());
              }
            })
        .build();
  }

  /**
   * Defines the activity state cache that refreshes every 10 seconds from the repository.
   *
   * @param activityStateRepository repository providing activity state data
   * @return the cache definition for activity states keyed by definitionId_version_activityId
   */
  @Bean
  public CacheDefinition<String, ActivityState, ActivityState> activityStateCacheDefinition(
      ActivityStateRepository activityStateRepository) {
    return CacheDefinition.<String, ActivityState, ActivityState>builder()
        .name("activityStateCache")
        .loader(activityStateRepository::findAll)
        .keyExtractor(
            as -> as.getProcessDefinitionId() + "_" + as.getVersion() + "_" + as.getActivityId())
        .refreshInterval(Duration.ofSeconds(10))
        .loadOnStartup(true)
        .failSafe(true)
        .retryAttempts(2)
        .retryBackoff(Duration.ofSeconds(1))
        .build();
  }
}
