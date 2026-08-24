package io.telekom.orchest.orchestrest.configurations.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for the API token authentication feature. */
@Data
@ConfigurationProperties(prefix = "orchest.api-tokens")
public class ApiTokenProperties {

  private boolean enabled = true;
  private int maxTtlDays = 365;
  private int tokenByteLength = 32;
}
