package io.telekom.orchest.cache.config;

import io.telekom.orchest.cache.core.Cache;
import io.telekom.orchest.cache.core.CacheDefinition;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Collects every {@link CacheDefinition} bean the host app declares, builds a corresponding cache
 * bean, and registers it as a named singleton.
 *
 * <p>Why register as a singleton rather than declare a {@code @Bean}? With multi-cache support each
 * cache has distinct generic parameters (K, V) and a host-chosen name. A single {@code @Bean}
 * method can't emit N differently-named beans — so we do it imperatively at context-init time. Host
 * code injects by name:
 *
 * <pre>{@code
 * @Autowired @Qualifier("customerCache")
 * Cache<String, Customer> customerCache;
 * }</pre>
 *
 * <p>Because these are real singletons in the bean factory, any dependent bean sees updates
 * automatically — the cache's internal {@code AtomicReference} is shared.
 */
public class CacheBeanRegistrar implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(CacheBeanRegistrar.class);

  private final ConfigurableListableBeanFactory beanFactory;
  private final CacheFactory cacheFactory;
  private final List<CacheDefinition<?, ?, ?>> definitions;

  /**
   * Creates a new registrar.
   *
   * @param beanFactory the Spring bean factory for singleton registration
   * @param cacheFactory factory that builds cache instances
   * @param definitions all cache definitions collected from the context
   */
  public CacheBeanRegistrar(
      ConfigurableListableBeanFactory beanFactory,
      CacheFactory cacheFactory,
      List<CacheDefinition<?, ?, ?>> definitions) {
    this.beanFactory = beanFactory;
    this.cacheFactory = cacheFactory;
    this.definitions = definitions;
  }

  /** Builds and registers all cache singletons from the collected definitions. */
  @Override
  public void afterPropertiesSet() {
    for (CacheDefinition<?, ?, ?> def : definitions) {
      Cache<?, ?> built = cacheFactory.build(cast(def));
      if (built == null) continue;
      if (beanFactory.containsSingleton(def.name())) {
        log.warn(
            "A bean named '{}' already exists — cache not registered as singleton", def.name());
      } else {
        beanFactory.registerSingleton(def.name(), built);
        log.info("Registered Cache bean '{}'", def.name());
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static CacheDefinition cast(CacheDefinition<?, ?, ?> def) {
    return (CacheDefinition) def;
  }
}
