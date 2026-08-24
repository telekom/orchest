package io.telekom.orchest.orchestrest.configurations.security;

import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

/**
 * Test configuration that disables JWT expiry validation for local testing with expired tokens.
 *
 * <p>DO NOT USE IN PRODUCTION!
 */
@TestConfiguration
@Profile("test-jwt")
/** Test security configuration that disables authentication for integration tests. */
public class TestSecurityConfig {

  private final JwtSecurityProperties jwtSecurityProperties;

  public TestSecurityConfig(JwtSecurityProperties jwtSecurityProperties) {
    this.jwtSecurityProperties = jwtSecurityProperties;
  }

  @Bean
  @Primary
  public ReactiveJwtDecoder testReactiveJwtDecoder() {
    JwtSecurityProperties.IssuerConfig firstIssuer = jwtSecurityProperties.getIssuers().getFirst();

    NimbusReactiveJwtDecoder decoder =
        NimbusReactiveJwtDecoder.withJwkSetUri(firstIssuer.getJwkSetUri()).build();

    List<String> acceptedAudiences =
        jwtSecurityProperties.getAllAudiences().stream()
            .map(JwtSecurityProperties.AudienceConfig::getAud)
            .toList();

    // ponytail: test-only — skips expiry, validates audience only
    decoder.setJwtValidator(
        token -> {
          List<String> tokenAudiences = token.getAudience();
          if (tokenAudiences != null
              && tokenAudiences.stream().anyMatch(acceptedAudiences::contains)) {
            return OAuth2TokenValidatorResult.success();
          }
          return OAuth2TokenValidatorResult.failure(
              new org.springframework.security.oauth2.core.OAuth2Error(
                  "invalid_token", "Token audience is not accepted", null));
        });

    return decoder;
  }
}
