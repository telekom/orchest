package io.telekom.orchest.scheduling.config;

import io.telekom.orchest.scheduling.api.TaskHandler;
import io.telekom.orchest.scheduling.api.TaskScheduler;
import io.telekom.orchest.scheduling.runtime.BackoffPolicy;
import io.telekom.orchest.scheduling.runtime.DefaultTaskScheduler;
import io.telekom.orchest.scheduling.runtime.SchedulerRuntimeProperties;
import io.telekom.orchest.scheduling.runtime.TaskDispatcher;
import io.telekom.orchest.scheduling.runtime.TaskHandlerRegistry;
import io.telekom.orchest.scheduling.spi.ScheduledTaskStore;
import java.time.Clock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the task scheduler.
 *
 * <p>Wires up the producer-side {@link TaskScheduler} whenever a {@link ScheduledTaskStore} bean is
 * present (the host application provides one — the Mongo default lives in {@code
 * mongo-data-adapter}). The {@link TaskDispatcher} is only started when {@code
 * orchest.scheduler.dispatcher-enabled=true}.
 */
@AutoConfiguration
@ConditionalOnProperty(
    prefix = "orchest.scheduler",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(SchedulerProperties.class)
public class TaskSchedulerAutoConfiguration {

  /**
   * Provides a UTC clock for timestamp generation, overridable for testing.
   *
   * @return a UTC system clock
   */
  @Bean
  @ConditionalOnMissingBean(Clock.class)
  public Clock schedulerClock() {
    return Clock.systemUTC();
  }

  /**
   * Maps external {@link SchedulerProperties} to the internal runtime properties object.
   *
   * @param props externalized configuration properties
   * @return runtime properties for scheduler and dispatcher
   */
  @Bean
  @ConditionalOnMissingBean
  public SchedulerRuntimeProperties schedulerRuntimeProperties(SchedulerProperties props) {
    return SchedulerRuntimeProperties.builder()
        .pollInterval(props.getPollInterval())
        .batchSize(props.getBatchSize())
        .leaseDuration(props.getLeaseDuration())
        .dispatcherThreads(props.getDispatcherThreads())
        .defaultMaxAttempts(props.getDefaultMaxAttempts())
        .backoffBase(props.getBackoffBase())
        .backoffMax(props.getBackoffMax())
        .build();
  }

  /**
   * Creates the exponential back-off policy used by the dispatcher for retry delays.
   *
   * @param runtimeProps runtime properties containing base and max back-off durations
   * @return the back-off policy
   */
  @Bean(name = "schedulerBackoffPolicy")
  @ConditionalOnMissingBean(name = "schedulerBackoffPolicy")
  public BackoffPolicy schedulerBackoffPolicy(SchedulerRuntimeProperties runtimeProps) {
    return BackoffPolicy.exponential(runtimeProps.getBackoffBase(), runtimeProps.getBackoffMax());
  }

  /**
   * Creates the producer-side {@link TaskScheduler} backed by the provided store.
   *
   * @param store the task persistence backend
   * @param runtimeProps runtime configuration
   * @param schedulerClock clock for timestamp generation
   * @return the task scheduler
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(ScheduledTaskStore.class)
  public TaskScheduler taskScheduler(
      ScheduledTaskStore store, SchedulerRuntimeProperties runtimeProps, Clock schedulerClock) {
    return new DefaultTaskScheduler(store, runtimeProps, schedulerClock);
  }

  /**
   * Builds the handler registry by collecting all {@link TaskHandler} beans from the context.
   *
   * @param handlers provider of all available task handler beans
   * @return the populated handler registry
   */
  @Bean
  @ConditionalOnMissingBean
  public TaskHandlerRegistry taskHandlerRegistry(ObjectProvider<TaskHandler> handlers) {
    TaskHandlerRegistry registry = new TaskHandlerRegistry();
    handlers
        .orderedStream()
        .forEach(
            handler -> {
              try {
                String type = handler.type();
                registry.register(type, handler);
              } catch (UnsupportedOperationException ignored) {
                // Handlers that don't override type() must be registered manually by the host.
              }
            });
    return registry;
  }

  /**
   * Creates and starts the task dispatcher that polls for due tasks and invokes handlers.
   *
   * @param store the task persistence backend
   * @param registry handler registry mapping types to handlers
   * @param runtimeProps runtime configuration
   * @param schedulerBackoffPolicy back-off policy for retries
   * @param schedulerClock clock for timestamp generation
   * @return the task dispatcher
   */
  @Bean(destroyMethod = "stop")
  @ConditionalOnMissingBean
  @ConditionalOnBean(ScheduledTaskStore.class)
  @ConditionalOnProperty(
      prefix = "orchest.scheduler",
      name = "dispatcher-enabled",
      havingValue = "true")
  public TaskDispatcher taskDispatcher(
      ScheduledTaskStore store,
      TaskHandlerRegistry registry,
      SchedulerRuntimeProperties runtimeProps,
      BackoffPolicy schedulerBackoffPolicy,
      Clock schedulerClock) {
    return new TaskDispatcher(
        store, registry, runtimeProps, schedulerBackoffPolicy, schedulerClock);
  }
}
