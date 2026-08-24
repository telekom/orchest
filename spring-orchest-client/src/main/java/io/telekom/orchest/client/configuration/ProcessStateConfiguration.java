package io.telekom.orchest.client.configuration;

import io.telekom.orchest.client.OrchestProperties;
import io.telekom.orchest.client.clients.processstate.ProcessStateScheduler;
import io.telekom.orchest.client.listener.DynamicKafkaConsumer;
import io.telekom.orchest.rest.client.OrchesTRestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Conditional configuration that enables periodic process-state polling. Active by default; disable
 * with {@code orchest.processState.enabled=false}.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(
    value = "orchest.processState.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class ProcessStateConfiguration {

  /**
   * Creates the process-state scheduler bean.
   *
   * @param orchesTRestClient the REST client for state API calls
   * @param dynamicKafkaConsumer the consumer manager to pause/resume
   * @param orchestProperties the OrchesT configuration properties
   * @return the scheduler instance
   */
  @Bean
  public ProcessStateScheduler processStateScheduler(
      OrchesTRestClient orchesTRestClient,
      DynamicKafkaConsumer dynamicKafkaConsumer,
      OrchestProperties orchestProperties) {
    return new ProcessStateScheduler(orchesTRestClient, dynamicKafkaConsumer, orchestProperties);
  }
}
