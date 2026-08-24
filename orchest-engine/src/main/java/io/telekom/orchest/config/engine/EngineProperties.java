package io.telekom.orchest.config.engine;

import io.telekom.orchest.api.core.properties.EngineScale;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the engine module. Mapped to the "engine" prefix in application.yml.
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "engine")
public class EngineProperties {

  /**
   * Defines the scale of the engine deployment (e.g., LOCAL, SMALL, LARGE). This affects Kafka
   * topic partitions and replication factors.
   */
  private EngineScale engineScale = EngineScale.LOCAL;

  private int brokerCount = 1;
  private Duration topicDataRetention = Duration.of(5, ChronoUnit.DAYS);
  private long topicDataRetentionBytes = 5368709120L; // 5GB default

  @PostConstruct
  public void init() {
    log.info("Engine scale and broker counts are: {}, {}", engineScale, brokerCount);
  }
}
