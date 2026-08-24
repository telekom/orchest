package io.telekom.orchest.cache.blocking;

import io.telekom.orchest.cache.core.Cache;
import io.telekom.orchest.cache.core.CacheDefinition;
import io.telekom.orchest.cache.core.CacheException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

/**
 * Copy-on-write {@link Cache} backed by an immutable snapshot held in an {@link AtomicReference}.
 *
 * <p><b>Read path:</b> non-blocking {@code AtomicReference.get()} returns an immutable map. Readers
 * never contend with refresh work.
 *
 * <p><b>Refresh path:</b> serialized with a {@link ReentrantLock} {@code tryLock} so periodic and
 * ad-hoc refreshes can never overlap. On success the new snapshot atomically replaces the old; on
 * failure (and {@code failSafe=true}) the previous snapshot is retained.
 *
 * <p>Virtual threads drive async {@link #refresh()} so callers aren't charged for I/O waits.
 */
@Slf4j
public class GenericMongoCache<K, E, V> implements Cache<K, V> {

  private final CacheDefinition<K, E, V> def;
  private final Executor asyncExecutor;

  private final AtomicReference<Map<K, V>> snapshot = new AtomicReference<>(Map.of());
  private final ReentrantLock refreshLock = new ReentrantLock();
  private volatile long lastRefreshEpochMillis = -1L;

  /**
   * Creates a new cache instance from the given definition.
   *
   * @param def the cache definition describing loader, keys, and refresh behavior
   * @param asyncExecutor executor for asynchronous refresh invocations
   */
  public GenericMongoCache(CacheDefinition<K, E, V> def, Executor asyncExecutor) {
    this.def = def;
    this.asyncExecutor = asyncExecutor;
  }

  @Override
  public String name() {
    return def.name();
  }

  @Override
  public Optional<V> get(K key) {
    if (key == null) return Optional.empty();
    return Optional.ofNullable(snapshot.get().get(key));
  }

  @Override
  public Collection<V> getAll() {
    return snapshot.get().values();
  }

  @Override
  public Map<K, V> asMap() {
    return snapshot.get();
  }

  @Override
  public int size() {
    return snapshot.get().size();
  }

  @Override
  public long lastRefreshEpochMillis() {
    return lastRefreshEpochMillis;
  }

  @Override
  public void refresh() {
    CompletableFuture.runAsync(this::doRefreshIfFree, asyncExecutor);
  }

  @Override
  public void refreshBlocking() {
    doRefresh(false);
  }

  /**
   * Waits for any in-flight refresh to drain, then forces another one. Useful after a write that
   * the caller knows must be reflected in the cache.
   */
  @Override
  public void reloadOnDemand() {
    refreshLock.lock();
    try {
      runLoad();
    } finally {
      refreshLock.unlock();
    }
  }

  @Override
  public void evict() {
    snapshot.set(Map.of());
    log.info("Cache '{}' evicted", def.name());
  }

  @Override
  public Optional<V> remove(K key) {
    if (key == null) return Optional.empty();
    Map<K, V> current = snapshot.get();
    V existing = current.get(key);
    if (existing == null) return Optional.empty();
    LinkedHashMap<K, V> next = new LinkedHashMap<>(current);
    next.remove(key);
    snapshot.set(Collections.unmodifiableMap(next));
    log.debug("Cache '{}' removed key {}", def.name(), key);
    return Optional.of(existing);
  }

  /** Fire-and-forget variant used by async refresh() and the scheduler. Skips if busy. */
  private void doRefreshIfFree() {
    if (!refreshLock.tryLock()) {
      log.debug("Cache '{}' refresh skipped — another refresh is in flight", def.name());
      return;
    }
    try {
      runLoad();
    } finally {
      refreshLock.unlock();
    }
  }

  /** Blocking variant — waits for in-flight refresh to finish, then runs one. */
  private void doRefresh(boolean ignored) {
    refreshLock.lock();
    try {
      runLoad();
    } finally {
      refreshLock.unlock();
    }
  }

  /** Must be called while holding {@link #refreshLock}. */
  private void runLoad() {
    def.hooks().beforeRefresh(def.name());
    long start = System.nanoTime();
    Throwable lastError = null;
    int attempts = Math.max(1, def.retryAttempts() + 1);

    for (int attempt = 1; attempt <= attempts; attempt++) {
      try {
        Map<K, V> fresh = loadOnce();
        snapshot.set(fresh);
        lastRefreshEpochMillis = System.currentTimeMillis();
        long durationMs = Duration.ofNanos(System.nanoTime() - start).toMillis();
        def.hooks().afterRefresh(def.name(), fresh, durationMs);
        log.debug(
            "Cache '{}' refreshed — {} entries in {} ms", def.name(), fresh.size(), durationMs);
        return;
      } catch (RuntimeException ex) {
        lastError = ex;
        log.warn(
            "Cache '{}' refresh attempt {}/{} failed: {}",
            def.name(),
            attempt,
            attempts,
            ex.toString());
        if (attempt < attempts) sleepQuietly(def.retryBackoff());
      }
    }

    def.hooks().onRefreshError(def.name(), lastError);
    if (!def.failSafe()) {
      throw new CacheException("Refresh failed for cache '" + def.name() + "'", lastError);
    }
    log.error(
        "Cache '{}' refresh failed after {} attempts; retaining previous snapshot",
        def.name(),
        attempts,
        lastError);
  }

  private Map<K, V> loadOnce() {
    Collection<E> raw = def.loader().load();
    if (raw == null) return Map.of();
    // LinkedHashMap preserves loader ordering for predictable iteration; wrapped
    // unmodifiable so handed-out references are safe.
    Map<K, V> built = new LinkedHashMap<>(Math.max(16, raw.size()));
    for (E entity : raw) {
      if (def.filter() != null && !def.filter().test(entity)) continue;
      V value = def.transform() == null ? (V) entity : def.transform().apply(entity);
      if (value == null) continue;
      K key = def.keyExtractor().apply(value);
      if (key == null) continue;
      built.put(key, value);
    }
    return Collections.unmodifiableMap(built);
  }

  private static void sleepQuietly(Duration d) {
    try {
      Thread.sleep(d.toMillis());
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }
}
