package io.telekom.orchest.adapter.mongo.scheduler;

import io.telekom.orchest.scheduling.spi.ScheduledTaskStore;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * Wires the Mongo-backed {@link ScheduledTaskStore} into any service that depends on {@code
 * mongo-data-adapter}.
 *
 * <p>Gated by {@code orchest.scheduler.enabled} (default true) so a host that wants to opt out can
 * do so cleanly without having to exclude packages.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "orchest.scheduler",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class MongoSchedulerConfiguration {

  /**
   * Provides a UTC system clock for the scheduler, unless one is already defined.
   *
   * @return a UTC clock instance
   */
  @Bean
  @ConditionalOnMissingBean(Clock.class)
  public Clock schedulerClock() {
    return Clock.systemUTC();
  }

  /**
   * Creates the MongoDB-backed scheduled task store unless an alternative implementation exists.
   *
   * @param mongoOperations the MongoOperations for database access
   * @param schedulerClock the clock for timestamp generation
   * @return a ScheduledTaskStore backed by MongoDB
   */
  @Bean
  @ConditionalOnMissingBean(ScheduledTaskStore.class)
  public ScheduledTaskStore mongoScheduledTaskStore(
      MongoOperations mongoOperations, Clock schedulerClock) {
    return new MongoScheduledTaskStore(mongoOperations, schedulerClock);
  }
}
