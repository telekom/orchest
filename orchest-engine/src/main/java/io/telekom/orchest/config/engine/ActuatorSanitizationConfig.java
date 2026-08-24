package io.telekom.orchest.config.engine;

import java.util.Set;
import org.springframework.boot.actuate.endpoint.SanitizableData;
import org.springframework.boot.actuate.endpoint.SanitizingFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for sanitizing actuator endpoint data, exposing only whitelisted environment
 * variables.
 */
@Configuration
public class ActuatorSanitizationConfig {

  private static final Set<String> EXPOSED_VARIABLES = Set.of("APP_VERSION");

  /**
   * Creates a sanitizing function that exposes only whitelisted variables and sanitizes all others.
   *
   * @return the custom sanitizing function
   */
  @Bean
  public SanitizingFunction customSanitizer() {
    return data -> {
      if (EXPOSED_VARIABLES.contains(data.getKey().toUpperCase())) {
        return data;
      }
      return data.withValue(SanitizableData.SANITIZED_VALUE);
    };
  }
}
