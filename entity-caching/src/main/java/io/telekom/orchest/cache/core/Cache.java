package io.telekom.orchest.cache.core;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Generic, type-safe view over an in-memory cache of Mongo-backed entities.
 *
 * <p>Implementations are required to be thread-safe. Reads must be non-blocking; writes (refresh /
 * evict / reload) may synchronize internally but must not block concurrent readers.
 *
 * @param <K> key type
 * @param <V> cached value type (typically an entity or its DTO projection)
 */
public interface Cache<K, V> {

  /** Unique name identifying this cache bean. */
  String name();

  /** Look up a single value by key. */
  Optional<V> get(K key);

  /** Snapshot of every value currently in cache. Safe to iterate without locking. */
  Collection<V> getAll();

  /** Immutable snapshot of the key → value mapping at the time of the call. */
  Map<K, V> asMap();

  /** Current cache size (number of entries). */
  int size();

  /**
   * Trigger an asynchronous refresh; returns immediately. No-op if a refresh is already running.
   */
  void refresh();

  /** Synchronous refresh; blocks until the refresh completes or fails. */
  void refreshBlocking();

  /** Force a full reload even if another refresh is in-flight (waits for it, then reloads). */
  void reloadOnDemand();

  /** Drop all entries. The next read will see an empty cache until the next refresh. */
  void evict();

  /**
   * Remove a single entry by key. Returns the removed value if present. Atomically swaps the
   * snapshot — concurrent readers see either the old or new map.
   */
  Optional<V> remove(K key);

  /** Millis since epoch of the last successful refresh, or -1 if never refreshed. */
  long lastRefreshEpochMillis();
}
