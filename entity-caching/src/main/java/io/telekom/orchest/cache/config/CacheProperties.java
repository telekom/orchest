package io.telekom.orchest.cache.config;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code telekom.cache.*} from application.yml. Each entry under {@code caches.<name>} is an
 * override for the programmatically-registered {@link
 * io.telekom.orchest.cache.core.CacheDefinition} sharing the same name — allowing ops to tune
 * refresh cadence without a code change.
 */
@ConfigurationProperties(prefix = "orchest.caching")
public class CacheProperties {

  /** Master kill switch for the entire library. */
  private boolean enabled = true;

  /** Size of the scheduler pool driving periodic refreshes. */
  private int schedulerPoolSize = 2;

  /** Per-cache overrides keyed by cache name. */
  private Map<String, CacheConfig> caches = new LinkedHashMap<>();

  /**
   * @return whether the caching library is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * @param v whether to enable the caching library
   */
  public void setEnabled(boolean v) {
    this.enabled = v;
  }

  /**
   * @return the scheduler thread pool size
   */
  public int getSchedulerPoolSize() {
    return schedulerPoolSize;
  }

  /**
   * @param v the scheduler thread pool size
   */
  public void setSchedulerPoolSize(int v) {
    this.schedulerPoolSize = v;
  }

  /**
   * @return per-cache configuration overrides
   */
  public Map<String, CacheConfig> getCaches() {
    return caches;
  }

  /**
   * @param caches per-cache configuration overrides
   */
  public void setCaches(Map<String, CacheConfig> caches) {
    this.caches = caches;
  }

  /** Per-cache YAML override for tuning individual cache behavior without code changes. */
  public static class CacheConfig {
    private Boolean enabled;
    private Duration refreshInterval;
    private Duration initialDelay;
    private Boolean loadOnStartup;
    private Boolean failSafe;
    private Integer retryAttempts;
    private Duration retryBackoff;

    /**
     * @return whether this cache is enabled (null means use default)
     */
    public Boolean getEnabled() {
      return enabled;
    }

    /**
     * @param v whether this cache is enabled
     */
    public void setEnabled(Boolean v) {
      this.enabled = v;
    }

    /**
     * @return override for the refresh interval
     */
    public Duration getRefreshInterval() {
      return refreshInterval;
    }

    /**
     * @param v override for the refresh interval
     */
    public void setRefreshInterval(Duration v) {
      this.refreshInterval = v;
    }

    /**
     * @return override for the initial delay before first refresh
     */
    public Duration getInitialDelay() {
      return initialDelay;
    }

    /**
     * @param v override for the initial delay
     */
    public void setInitialDelay(Duration v) {
      this.initialDelay = v;
    }

    /**
     * @return override for eager loading on startup
     */
    public Boolean getLoadOnStartup() {
      return loadOnStartup;
    }

    /**
     * @param v override for eager loading on startup
     */
    public void setLoadOnStartup(Boolean v) {
      this.loadOnStartup = v;
    }

    /**
     * @return override for fail-safe mode
     */
    public Boolean getFailSafe() {
      return failSafe;
    }

    /**
     * @param v override for fail-safe mode
     */
    public void setFailSafe(Boolean v) {
      this.failSafe = v;
    }

    /**
     * @return override for the number of retry attempts
     */
    public Integer getRetryAttempts() {
      return retryAttempts;
    }

    /**
     * @param v override for the number of retry attempts
     */
    public void setRetryAttempts(Integer v) {
      this.retryAttempts = v;
    }

    /**
     * @return override for retry back-off duration
     */
    public Duration getRetryBackoff() {
      return retryBackoff;
    }

    /**
     * @param v override for retry back-off duration
     */
    public void setRetryBackoff(Duration v) {
      this.retryBackoff = v;
    }
  }
}
