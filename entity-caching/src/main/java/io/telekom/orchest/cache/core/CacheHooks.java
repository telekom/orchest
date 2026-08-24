package io.telekom.orchest.cache.core;

import java.util.Map;

/**
 * Extension points fired around each refresh cycle. Default methods are no-ops so hosts only
 * override what they need. Hooks run on the refresh thread — keep them cheap.
 *
 * @param <K> key type
 * @param <V> cached value type
 */
public interface CacheHooks<K, V> {

  /** Invoked before the loader runs. */
  default void beforeRefresh(String cacheName) {}

  /** Invoked after a successful refresh, with the new snapshot. Snapshot is immutable. */
  default void afterRefresh(String cacheName, Map<K, V> snapshot, long durationMillis) {}

  /** Invoked when a refresh throws. The previous snapshot is retained. */
  default void onRefreshError(String cacheName, Throwable error) {}

  /** No-op hook singleton — used as default when the host doesn't supply one. */
  @SuppressWarnings("rawtypes")
  CacheHooks NOOP = new CacheHooks() {};

  /**
   * Returns the no-op hook singleton cast to the required type parameters.
   *
   * @param <K> key type
   * @param <V> value type
   * @return a no-op hooks instance
   */
  @SuppressWarnings("unchecked")
  static <K, V> CacheHooks<K, V> noop() {
    return (CacheHooks<K, V>) NOOP;
  }
}
