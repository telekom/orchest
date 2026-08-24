package io.telekom.orchest.telemetry;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the Telemetry module. Mapped to the "telemetry" prefix in
 * application.yml.
 */
@Data
@NoArgsConstructor
@Configuration("telemetryProperties")
@ConfigurationProperties(prefix = "telemetry")
public class ModuleProperties {

  /** Whether telemetry is enabled globally. */
  private boolean enabled;
}
