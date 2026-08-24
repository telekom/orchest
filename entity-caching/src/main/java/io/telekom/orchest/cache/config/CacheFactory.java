package io.telekom.orchest.cache.config;

import io.telekom.orchest.cache.blocking.GenericMongoCache;
import io.telekom.orchest.cache.core.Cache;
import io.telekom.orchest.cache.core.CacheDefinition;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

/**
 * Builds cache beans from a {@link CacheDefinition}, merging in YAML overrides from {@link
 * CacheProperties}, then wires them to the shared {@link TaskScheduler} for periodic refresh.
 *
 * <p>Kept as a plain component (not a bean) so host auto-wiring stays simple — the
 * auto-configuration calls it once per registered definition.
 */
public class CacheFactory {

  private static final Logger log = LoggerFactory.getLogger(CacheFactory.class);

  private final CacheProperties properties;
  private final TaskScheduler taskScheduler;
  private final Executor asyncExecutor;
  private final CacheRegistry registry;

  /**
   * Creates a new cache factory.
   *
   * @param properties externalized cache configuration
   * @param taskScheduler scheduler for periodic refresh scheduling
   * @param asyncExecutor executor for async refresh calls
   * @param registry registry where created caches are tracked
   */
  public CacheFactory(
      CacheProperties properties,
      TaskScheduler taskScheduler,
      Executor asyncExecutor,
      CacheRegistry registry) {
    this.properties = Objects.requireNonNull(properties);
    this.taskScheduler = Objects.requireNonNull(taskScheduler);
    this.asyncExecutor = Objects.requireNonNull(asyncExecutor);
    this.registry = Objects.requireNonNull(registry);
  }

  /** Build a blocking cache from the definition, apply YAML overrides, register + schedule. */
  public <K, E, V> Cache<K, V> build(CacheDefinition<K, E, V> definition) {
    CacheProperties.CacheConfig override = properties.getCaches().get(definition.name());

    if (override != null && Boolean.FALSE.equals(override.getEnabled())) {
      log.info("Cache '{}' is disabled via configuration — skipping", definition.name());
      return null;
    }

    CacheDefinition<K, E, V> effective = applyOverrides(definition, override);

    GenericMongoCache<K, E, V> cache = new GenericMongoCache<>(effective, asyncExecutor);
    registry.register(cache);

    if (effective.loadOnStartup()) {
      // Run the initial load on the scheduler so app startup isn't blocked by IO.
      taskScheduler.schedule(
          () -> safeRefresh(cache), Instant.now().plus(effective.initialDelay()));
    }

    if (effective.refreshInterval() != null && !effective.refreshInterval().isZero()) {
      Instant firstRun =
          Instant.now()
              .plusMillis(
                  Math.max(
                      effective.initialDelay().toMillis(), effective.refreshInterval().toMillis()));
      taskScheduler.scheduleWithFixedDelay(
          () -> safeRefresh(cache), firstRun, effective.refreshInterval());
      log.info(
          "Cache '{}' scheduled: interval={}, initialDelay={}",
          effective.name(),
          effective.refreshInterval(),
          effective.initialDelay());
      // We deliberately don't track the future — scheduler shutdown cancels it.
    }
    return cache;
  }

  private <K, E, V> CacheDefinition<K, E, V> applyOverrides(
      CacheDefinition<K, E, V> def, CacheProperties.CacheConfig override) {
    if (override == null) return def;
    return CacheDefinition.<K, E, V>builder()
        .name(def.name())
        .loader(def.loader())
        .keyExtractor(def.keyExtractor())
        .transform(def.transform())
        .filter(def.filter())
        .refreshInterval(
            override.getRefreshInterval() != null
                ? override.getRefreshInterval()
                : def.refreshInterval())
        .initialDelay(
            override.getInitialDelay() != null ? override.getInitialDelay() : def.initialDelay())
        .loadOnStartup(
            override.getLoadOnStartup() != null ? override.getLoadOnStartup() : def.loadOnStartup())
        .failSafe(override.getFailSafe() != null ? override.getFailSafe() : def.failSafe())
        .retryAttempts(
            override.getRetryAttempts() != null ? override.getRetryAttempts() : def.retryAttempts())
        .retryBackoff(
            override.getRetryBackoff() != null ? override.getRetryBackoff() : def.retryBackoff())
        .hooks(def.hooks())
        .build();
  }

  private static void safeRefresh(Cache<?, ?> cache) {
    try {
      cache.refreshBlocking();
    } catch (RuntimeException ex) {
      // Cache is fail-safe by default; this only fires if the host opted out.
      log.error("Scheduled refresh of cache '{}' threw", cache.name(), ex);
    }
  }

  /**
   * Returns the per-cache YAML overrides.
   *
   * @return map of cache name to its configuration overrides
   */
  public Map<String, CacheProperties.CacheConfig> overrides() {
    return properties.getCaches();
  }
}
