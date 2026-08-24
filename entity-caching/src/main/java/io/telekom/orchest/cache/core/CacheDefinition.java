package io.telekom.orchest.cache.core;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Immutable description of a cache bean: how to load, how to key it, and how often to refresh.
 *
 * <p>Construct via the {@link Builder}. The sealed-like builder enforces the required fields (name,
 * loader, keyExtractor) at build time.
 *
 * @param <K> cache key
 * @param <E> source entity as returned by the loader
 * @param <V> cached value (may equal E, or be a DTO after transform)
 */
public final class CacheDefinition<K, E, V> {

  private final String name;
  private final CacheLoader<E> loader;
  private final KeyExtractor<K, V> keyExtractor;
  private final Function<E, V> transform;
  private final Predicate<E> filter;
  private final Duration refreshInterval;
  private final Duration initialDelay;
  private final boolean loadOnStartup;
  private final boolean failSafe;
  private final int retryAttempts;
  private final Duration retryBackoff;
  private final CacheHooks<K, V> hooks;

  private CacheDefinition(Builder<K, E, V> b) {
    this.name = Objects.requireNonNull(b.name, "cache name");
    this.loader = Objects.requireNonNull(b.loader, "loader");
    this.keyExtractor = Objects.requireNonNull(b.keyExtractor, "keyExtractor");
    this.transform = b.transform;
    this.filter = b.filter;
    this.refreshInterval = b.refreshInterval;
    this.initialDelay = b.initialDelay;
    this.loadOnStartup = b.loadOnStartup;
    this.failSafe = b.failSafe;
    this.retryAttempts = b.retryAttempts;
    this.retryBackoff = b.retryBackoff;
    this.hooks = b.hooks == null ? CacheHooks.noop() : b.hooks;
  }

  /**
   * @return the unique cache name
   */
  public String name() {
    return name;
  }

  /**
   * @return the loader strategy that fetches entities from the data source
   */
  public CacheLoader<E> loader() {
    return loader;
  }

  /**
   * @return the function that extracts a key from a cached value
   */
  public KeyExtractor<K, V> keyExtractor() {
    return keyExtractor;
  }

  /**
   * @return optional transform applied to source entities before caching, or null
   */
  public Function<E, V> transform() {
    return transform;
  }

  /**
   * @return optional filter applied to source entities before caching, or null
   */
  public Predicate<E> filter() {
    return filter;
  }

  /**
   * @return interval between periodic background refreshes
   */
  public Duration refreshInterval() {
    return refreshInterval;
  }

  /**
   * @return delay before the first scheduled refresh after startup
   */
  public Duration initialDelay() {
    return initialDelay;
  }

  /**
   * @return true if the cache should load eagerly at application startup
   */
  public boolean loadOnStartup() {
    return loadOnStartup;
  }

  /**
   * @return true if refresh errors should be swallowed (retaining stale data)
   */
  public boolean failSafe() {
    return failSafe;
  }

  /**
   * @return number of retry attempts on loader failure
   */
  public int retryAttempts() {
    return retryAttempts;
  }

  /**
   * @return back-off duration between retry attempts
   */
  public Duration retryBackoff() {
    return retryBackoff;
  }

  /**
   * @return lifecycle hooks for refresh events
   */
  public CacheHooks<K, V> hooks() {
    return hooks;
  }

  /**
   * Creates a new builder for constructing a {@link CacheDefinition}.
   *
   * @param <K> key type
   * @param <E> source entity type
   * @param <V> cached value type
   * @return a new builder instance
   */
  public static <K, E, V> Builder<K, E, V> builder() {
    return new Builder<>();
  }

  /** Fluent builder for constructing {@link CacheDefinition} instances. */
  public static final class Builder<K, E, V> {
    private String name;
    private CacheLoader<E> loader;
    private KeyExtractor<K, V> keyExtractor;
    private Function<E, V> transform;
    private Predicate<E> filter;
    private Duration refreshInterval = Duration.ofMinutes(5);
    private Duration initialDelay = Duration.ZERO;
    private boolean loadOnStartup = true;
    private boolean failSafe = true;
    private int retryAttempts = 0;
    private Duration retryBackoff = Duration.ofSeconds(1);
    private CacheHooks<K, V> hooks;

    /**
     * @param name unique cache name (required)
     */
    public Builder<K, E, V> name(String name) {
      this.name = name;
      return this;
    }

    /**
     * @param loader strategy for fetching entities (required)
     */
    public Builder<K, E, V> loader(CacheLoader<E> loader) {
      this.loader = loader;
      return this;
    }

    /**
     * @param ke function to extract keys from cached values (required)
     */
    public Builder<K, E, V> keyExtractor(KeyExtractor<K, V> ke) {
      this.keyExtractor = ke;
      return this;
    }

    /**
     * @param t optional transform from source entity to cached value
     */
    public Builder<K, E, V> transform(Function<E, V> t) {
      this.transform = t;
      return this;
    }

    /**
     * @param p optional filter applied to entities before caching
     */
    public Builder<K, E, V> filter(Predicate<E> p) {
      this.filter = p;
      return this;
    }

    /**
     * @param d interval between background refreshes (default 5 minutes)
     */
    public Builder<K, E, V> refreshInterval(Duration d) {
      this.refreshInterval = d;
      return this;
    }

    /**
     * @param d delay before the first scheduled refresh
     */
    public Builder<K, E, V> initialDelay(Duration d) {
      this.initialDelay = d;
      return this;
    }

    /**
     * @param v whether to load eagerly on startup (default true)
     */
    public Builder<K, E, V> loadOnStartup(boolean v) {
      this.loadOnStartup = v;
      return this;
    }

    /**
     * @param v whether to swallow refresh errors and retain stale data (default true)
     */
    public Builder<K, E, V> failSafe(boolean v) {
      this.failSafe = v;
      return this;
    }

    /**
     * @param v number of retry attempts on loader failure (default 0)
     */
    public Builder<K, E, V> retryAttempts(int v) {
      this.retryAttempts = v;
      return this;
    }

    /**
     * @param d back-off between retry attempts (default 1 second)
     */
    public Builder<K, E, V> retryBackoff(Duration d) {
      this.retryBackoff = d;
      return this;
    }

    /**
     * @param h lifecycle hooks for refresh events
     */
    public Builder<K, E, V> hooks(CacheHooks<K, V> h) {
      this.hooks = h;
      return this;
    }

    /**
     * @return the constructed cache definition
     */
    public CacheDefinition<K, E, V> build() {
      return new CacheDefinition<>(this);
    }
  }
}
