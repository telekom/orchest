package io.telekom.orchest.telemetry.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring wiring for the persistent metrics pipeline.
 *
 * <h2>Bean topology</h2>
 *
 * <ul>
 *   <li>{@link MetricsProperties} — bound to {@code orchest.metrics.*}.
 *   <li>{@link MicrometerMetricsRecorder} — primary {@link MetricsRecorder}, registered when {@code
 *       orchest.metrics.enabled=true} (the default). Requires a {@link MeterRegistry} on the
 *       classpath (always present in our apps because Micrometer is a transitive dependency of
 *       {@code telemetry}).
 *   <li>{@link NoOpMetricsRecorder} — fallback {@link MetricsRecorder}, registered only when no
 *       other {@code MetricsRecorder} bean exists. Activates automatically in {@code
 *       orchest-rest}/{@code sentinel} when they set {@code orchest.metrics.enabled=false}.
 *   <li>{@link MetricsFlushService} — registered when the pipeline is enabled <strong>and</strong>
 *       a {@link MetricsRepository} bean is present (provided by {@code mongo-data-adapter}).
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(MetricsProperties.class)
public class MetricsAutoConfiguration {

  /**
   * Creates the primary Micrometer-backed metrics recorder when the pipeline is enabled.
   *
   * @param meterRegistry the Micrometer meter registry
   * @return the active metrics recorder
   */
  @Bean
  @ConditionalOnProperty(
      prefix = "orchest.metrics",
      name = "enabled",
      havingValue = "true",
      matchIfMissing = true)
  public MetricsRecorder metricsRecorder(MeterRegistry meterRegistry) {
    return new MicrometerMetricsRecorder(meterRegistry);
  }

  /**
   * Creates a no-op fallback metrics recorder when no other recorder is registered.
   *
   * @return the no-op metrics recorder singleton
   */
  @Bean
  @ConditionalOnMissingBean(MetricsRecorder.class)
  public MetricsRecorder noOpMetricsRecorder() {
    return NoOpMetricsRecorder.INSTANCE;
  }

  /**
   * Creates the flush service that periodically persists in-memory metric deltas to the repository.
   *
   * @param recorder the metrics recorder to drain
   * @param metricsRepository the repository for persisting metric increments
   * @param properties the metrics configuration properties
   * @return the configured flush service
   */
  @Bean
  @ConditionalOnProperty(
      prefix = "orchest.metrics",
      name = "enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ConditionalOnBean(MetricsRepository.class)
  public MetricsFlushService metricsFlushService(
      MetricsRecorder recorder, MetricsRepository metricsRepository, MetricsProperties properties) {
    return new MetricsFlushService(recorder, metricsRepository, properties);
  }
}
