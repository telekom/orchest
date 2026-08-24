package io.telekom.orchest.config.engine;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/** Configuration for Cross-Origin Resource Sharing (CORS) filters applied to all endpoints. */
@Configuration
public class CorsConfig {

  @Value("${cors.allowed-origins:*}")
  private List<String> allowedOrigins;

  /**
   * Creates a CORS filter bean with configured allowed origins, methods, and headers.
   *
   * @return the configured CORS filter
   */
  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(allowedOrigins);
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return new CorsFilter(source);
  }
}
