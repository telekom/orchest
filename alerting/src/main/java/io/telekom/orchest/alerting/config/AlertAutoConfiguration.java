package io.telekom.orchest.alerting.config;

import io.telekom.orchest.alerting.service.AlertLifecycleService;
import io.telekom.orchest.api.core.adapters.data.repository.AlertRepository;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring wiring for the persistent alert lifecycle (raise SPI, scheduler). REST controller is
 * component-scanned separately and gated with web conditions.
 */
@Configuration
public class AlertAutoConfiguration {

  /**
   * Provides a system UTC clock unless one is already defined in the context.
   *
   * @return the clock instance
   */
  @Bean
  @ConditionalOnMissingBean(Clock.class)
  public Clock alertClock() {
    return Clock.systemUTC();
  }

  /**
   * Creates the alert lifecycle service when a repository is available and no override exists.
   *
   * @param alertRepository the alert persistence store
   * @param clock clock for timestamping state transitions
   * @return the lifecycle service
   */
  @Bean
  @ConditionalOnBean(AlertRepository.class)
  @ConditionalOnMissingBean(AlertLifecycleService.class)
  public AlertLifecycleService alertLifecycleService(AlertRepository alertRepository, Clock clock) {
    return new AlertLifecycleService(alertRepository, clock);
  }
}
