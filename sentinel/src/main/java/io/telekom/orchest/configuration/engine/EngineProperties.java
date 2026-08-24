package io.telekom.orchest.configuration.engine;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuration properties for the OrchesT engine, bound to {@code engine.*}. */
@Data
@Configuration
@ConfigurationProperties(prefix = "engine")
public class EngineProperties {

  /** The cluster sizing profile controlling Kafka concurrency settings. */
  private EngineScale engineScale = EngineScale.LOCAL;
}
