package io.telekom.orchest.adapter.kafka.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the same property tree as {@link KafkaProperties} (bootstrap servers, producer, consumer,
 * streams, admin, listener, template, security, ssl, jaas, properties, etc.) under the {@code
 * orchest.kafka} prefix instead of {@code spring.kafka}.
 *
 * <p>Use this type anywhere a {@link KafkaProperties} instance is expected; it is a subtype and
 * exposes the same API (including {@code buildConsumerProperties()}, {@code
 * buildProducerProperties()}, and so on).
 */
@Data
@NoArgsConstructor
@Configuration("orchestKafkaProperties")
@ConfigurationProperties(prefix = "orchest.kafka")
public class OrchestKafkaProperties {

  @NestedConfigurationProperty private KafkaProperties config;

  private Integer replicaCount = 3;
  private int defaultPartitions = 3;
  private String suffix;

  /**
   * Sets and sanitizes the topic suffix (uppercased, hyphens replaced with underscores).
   *
   * @param suffix the raw suffix value from configuration
   */
  public void setSuffix(String suffix) {
    if (suffix != null) {
      // sanitize suffix
      this.suffix = StringUtils.upperCase(suffix).replace("-", "_");
    }
  }
}
