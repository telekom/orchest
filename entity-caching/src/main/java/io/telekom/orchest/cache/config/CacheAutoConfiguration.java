package io.telekom.orchest.cache.config;

import io.telekom.orchest.cache.core.CacheDefinition;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Spring Boot auto-configuration entry point.
 *
 * <p>Activation rules:
 *
 * <ul>
 *   <li>Mongo driver must be on the classpath (no point if the host can't talk to Mongo).
 *   <li>{@code telekom.cache.enabled} must not be {@code false}.
 * </ul>
 */
@AutoConfiguration
// @ConditionalOnClass(name = "com.mongodb.MongoClientSettings")
@ConditionalOnProperty(
    prefix = "orchest.caching",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {

  /**
   * Provides the central registry tracking all created cache beans.
   *
   * @return a new cache registry
   */
  @Bean
  @ConditionalOnMissingBean
  public CacheRegistry cacheRegistry() {
    return new CacheRegistry();
  }

  /**
   * Dedicated scheduler for cache refreshes. Separated from any host-provided {@code TaskScheduler}
   * so a slow refresh can't starve other scheduled work.
   */
  @Bean(name = "cacheRefreshScheduler", destroyMethod = "shutdown")
  @ConditionalOnMissingBean(name = "cacheRefreshScheduler")
  public TaskScheduler cacheRefreshScheduler(CacheProperties props) {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(Math.max(1, props.getSchedulerPoolSize()));
    scheduler.setThreadNamePrefix("cache-refresh-");
    scheduler.setRemoveOnCancelPolicy(true);
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.setAwaitTerminationSeconds(30);
    scheduler.initialize();
    return scheduler;
  }

  /**
   * Virtual-thread executor for async refresh() calls — lets us fan out I/O-bound loads cheaply
   * without adding platform threads.
   */
  @Bean(name = "cacheAsyncExecutor")
  @ConditionalOnMissingBean(name = "cacheAsyncExecutor")
  public Executor cacheAsyncExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  /**
   * Creates the factory responsible for building cache instances from definitions.
   *
   * @param props cache configuration properties
   * @param cacheRefreshScheduler scheduler for periodic refreshes
   * @param cacheAsyncExecutor executor for async refresh calls
   * @param registry the cache registry
   * @return a new cache factory
   */
  @Bean
  @ConditionalOnMissingBean
  public CacheFactory cacheFactory(
      CacheProperties props,
      @Qualifier("cacheRefreshScheduler") TaskScheduler cacheRefreshScheduler,
      @Qualifier("cacheAsyncExecutor") Executor cacheAsyncExecutor,
      CacheRegistry registry) {
    return new CacheFactory(props, cacheRefreshScheduler, cacheAsyncExecutor, registry);
  }

  /**
   * Creates the registrar that builds and registers cache singletons from collected definitions.
   *
   * @param bf the bean factory for singleton registration
   * @param factory cache factory
   * @param definitions all declared cache definitions
   * @return the cache bean registrar
   */
  @Bean
  public CacheBeanRegistrar cacheBeanRegistrar(
      ConfigurableListableBeanFactory bf,
      CacheFactory factory,
      ObjectProvider<CacheDefinition<?, ?, ?>> definitions) {
    return new CacheBeanRegistrar(bf, factory, definitions.stream().toList());
  }
}
