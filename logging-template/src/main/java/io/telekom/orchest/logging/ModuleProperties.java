package io.telekom.orchest.logging;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuration properties for the logging module. Binds the {@code logging.format} property. */
@Data
@NoArgsConstructor
@Configuration("loggingProperties")
@ConfigurationProperties(prefix = "logging")
public class ModuleProperties {

  private String format;
}
