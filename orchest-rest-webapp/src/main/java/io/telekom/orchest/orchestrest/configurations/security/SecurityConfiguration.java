package io.telekom.orchest.orchestrest.configurations.security;

import com.fasterxml.jackson.core.type.TypeReference;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.orchestrest.service.UserApiTokenService;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

/** Configures Spring Security filter chain with JWT and API-key authentication for WebFlux. */
@Slf4j
@EnableConfigurationProperties({JwtSecurityProperties.class, ApiTokenProperties.class})
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  public static final String[] AUTH_WHITELIST = {
    "/actuator/**",
    "/swagger-ui.html",
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/docs/**",
    "/webjars/**",
    "/rateLimits",
    "/feelPlayground/**",
    "/kafka/**"
  };

  private final JwtSecurityProperties jwtSecurityProperties;

  @Value("${enableSecurity:false}")
  private boolean enableSecurity;

  @Value("${cors.allowed-origins:*}")
  private List<String> allowedOrigins;

  @Bean
  @ConditionalOnProperty(name = "enableSecurity", havingValue = "true")
  public ReactiveJwtDecoder reactiveJwtDecoder() {
    Map<String, ReactiveJwtDecoder> decodersByIssuer =
        jwtSecurityProperties.getIssuers().stream()
            .collect(
                Collectors.toMap(
                    JwtSecurityProperties.IssuerConfig::getIssuerUri, this::buildDecoderForIssuer));

    log.info(
        "Configured JWT decoders for {} issuers: {}",
        decodersByIssuer.size(),
        decodersByIssuer.keySet());

    return token -> {
      String issuer = extractIssuerFromToken(token);
      ReactiveJwtDecoder decoder = decodersByIssuer.get(issuer);
      if (decoder == null) {
        log.warn("No decoder configured for issuer: {}", issuer);
        return Mono.error(new JwtException("Untrusted issuer: " + issuer));
      }
      return decoder.decode(token);
    };
  }

  private ReactiveJwtDecoder buildDecoderForIssuer(
      JwtSecurityProperties.IssuerConfig issuerConfig) {
    NimbusReactiveJwtDecoder decoder =
        NimbusReactiveJwtDecoder.withJwkSetUri(issuerConfig.getJwkSetUri()).build();

    List<String> acceptedAudiences =
        issuerConfig.getAudiences().stream()
            .map(JwtSecurityProperties.AudienceConfig::getAud)
            .toList();

    OAuth2TokenValidator<Jwt> audienceValidator =
        jwt -> {
          List<String> tokenAudiences = jwt.getAudience();
          if (tokenAudiences != null
              && tokenAudiences.stream().anyMatch(acceptedAudiences::contains)) {
            return OAuth2TokenValidatorResult.success();
          }
          return OAuth2TokenValidatorResult.failure(
              new OAuth2Error(
                  "invalid_token",
                  "Token audience not accepted for issuer " + issuerConfig.getName(),
                  null));
        };

    OAuth2TokenValidator<Jwt> defaultValidators =
        JwtValidators.createDefaultWithIssuer(issuerConfig.getIssuerUri());
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(defaultValidators, audienceValidator));
    return decoder;
  }

  private String extractIssuerFromToken(String token) {
    try {
      String[] parts = token.split("\\.");
      String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
      Map<String, Object> claims = JsonMapper.readFromJson(payload, new TypeReference<>() {});
      Object iss = claims.get("iss");
      return iss != null ? iss.toString() : "";
    } catch (Exception e) {
      return "";
    }
  }

  @Bean
  public ApiKeyAuthWebFilter apiKeyAuthWebFilter(
      UserApiTokenService tokenService, ApiTokenProperties properties) {
    return new ApiKeyAuthWebFilter(tokenService, properties);
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(
      ServerHttpSecurity http,
      ApiKeyAuthWebFilter apiKeyAuthWebFilter,
      AuthWebFilter authWebFilter,
      JsonAuthenticationEntryPoint authenticationEntryPoint,
      JsonAccessDeniedHandler accessDeniedHandler,
      Optional<ReactiveJwtDecoder> jwtDecoder) {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .headers(
            headers ->
                headers
                    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                    .hsts(
                        hsts ->
                            hsts.includeSubdomains(true)
                                .maxAge(java.time.Duration.ofSeconds(31536000))))
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .addFilterBefore(apiKeyAuthWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .addFilterBefore(authWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);

    if (enableSecurity) {
      http.authorizeExchange(
              exchange ->
                  exchange.pathMatchers(AUTH_WHITELIST).permitAll().anyExchange().authenticated())
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwtDecoder.ifPresent(jwt::jwtDecoder)));
    } else {
      log.warn("Token Security is disabled");
      http.authorizeExchange(exchange -> exchange.anyExchange().permitAll());
    }

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(allowedOrigins);
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
