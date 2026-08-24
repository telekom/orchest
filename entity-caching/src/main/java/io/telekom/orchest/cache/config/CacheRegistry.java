package io.telekom.orchest.cache.config;

import io.telekom.orchest.cache.core.Cache;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime registry of all cache beans created by the starter. Host applications can inject this to
 * look caches up by name (e.g., from an admin controller).
 */
public class CacheRegistry {

  private final Map<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();

  /**
   * Registers a cache instance under its declared name.
   *
   * @param cache the cache to register
   */
  public void register(Cache<?, ?> cache) {
    caches.put(cache.name(), cache);
  }

  /**
   * Finds a cache by name.
   *
   * @param name the cache name
   * @param <K> key type
   * @param <V> value type
   * @return the cache, or empty if not registered
   */
  @SuppressWarnings("unchecked")
  public <K, V> Optional<Cache<K, V>> find(String name) {
    return Optional.ofNullable((Cache<K, V>) caches.get(name));
  }

  /**
   * Returns all registered caches.
   *
   * @return unmodifiable view of all cache instances
   */
  public Collection<Cache<?, ?>> all() {
    return caches.values();
  }
}
