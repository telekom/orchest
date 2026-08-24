package io.telekom.orchest.connectorservice.config.engine;

import io.telekom.orchest.api.core.properties.EngineScale;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the connector service. Mapped to the "engine" prefix in
 * application.yml, mirroring the engine so that Kafka topic partitioning and consumer concurrency
 * behave consistently across the platform.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "engine")
public class EngineProperties {

  private EngineScale engineScale = EngineScale.LOCAL;
  private int brokerCount = 1;
  private Duration topicDataRetention = Duration.of(5, ChronoUnit.DAYS);
  private long topicDataRetentionBytes = 5368709120L; // 5GB default
}
