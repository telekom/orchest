package io.telekom.orchest.orchestrest.configurations.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for multi-issuer JWT validation with audience-based role mapping. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtSecurityProperties {

  private List<IssuerConfig> issuers = new ArrayList<>();

  public List<AudienceConfig> getAllAudiences() {
    return issuers.stream().flatMap(issuer -> issuer.getAudiences().stream()).toList();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class IssuerConfig {
    private String name;
    private String issuerUri;
    private String jwkSetUri;
    private List<AudienceConfig> audiences = new ArrayList<>();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AudienceConfig {
    private String name;
    private String aud;
    private Map<String, String> roleMapping = new HashMap<>();
  }
}
