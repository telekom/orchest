package io.telekom.orchest.orchestrest.configurations.security;

import static org.assertj.core.api.Assertions.*;

import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/** Unit tests for {@link io.telekom.orchest.orchestrest.configurations.security.AuthWebFilter}. */
class AuthWebFilterTest {

  private AuthWebFilter authWebFilter;

  private JwtSecurityProperties createEmptyJwtProperties() {
    JwtSecurityProperties props = new JwtSecurityProperties();
    props.setIssuers(List.of());
    return props;
  }

  private WebFilterChain captureContext(AtomicReference<LoggedInUserContext> ctxRef) {
    return (exchange) ->
        Mono.deferContextual(
            ctx -> {
              ctxRef.set(ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, null));
              return Mono.empty();
            });
  }

  @Nested
  @DisplayName("Local/Dev profile (security bypassed)")
  class LocalProfile {

    @BeforeEach
    void setUp() throws Exception {
      authWebFilter = new AuthWebFilter(createEmptyJwtProperties());
      setField(authWebFilter, "profile", "local");
      setField(authWebFilter, "enableSecurity", false);
    }

    @Test
    @DisplayName("should set admin context for local profile")
    void localProfile_setsAdminContext() {
      AtomicReference<LoggedInUserContext> ctxRef = new AtomicReference<>();
      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.get("/orchest/processInstances").build());

      StepVerifier.create(authWebFilter.filter(exchange, captureContext(ctxRef))).verifyComplete();

      LoggedInUserContext ctx = ctxRef.get();
      assertThat(ctx).isNotNull();
      assertThat(ctx.getRoles()).contains(AuthWebFilter.ADMIN);
      assertThat(ctx.isAdmin()).isTrue();
      assertThat(ctx.getUserId()).isEqualTo("dev@example.com");
    }

    @Test
    @DisplayName("should continue filter chain for local profile regardless of endpoint")
    void localProfile_continuesFilterChain() {
      AtomicReference<Boolean> chainCalled = new AtomicReference<>(false);
      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.post("/orchest/processInstances/create").build());

      WebFilterChain chain =
          ex -> {
            chainCalled.set(true);
            return Mono.empty();
          };

      StepVerifier.create(authWebFilter.filter(exchange, chain)).verifyComplete();

      assertThat(chainCalled.get()).isTrue();
      assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  @Nested
  @DisplayName("Dev profile")
  class DevProfile {

    @BeforeEach
    void setUp() throws Exception {
      authWebFilter = new AuthWebFilter(createEmptyJwtProperties());
      setField(authWebFilter, "profile", "dev");
      setField(authWebFilter, "enableSecurity", false);
    }

    @Test
    @DisplayName("should set admin context for dev profile")
    void devProfile_setsAdminContext() {
      AtomicReference<LoggedInUserContext> ctxRef = new AtomicReference<>();
      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.get("/orchest/processInstances").build());

      StepVerifier.create(authWebFilter.filter(exchange, captureContext(ctxRef))).verifyComplete();

      LoggedInUserContext ctx = ctxRef.get();
      assertThat(ctx).isNotNull();
      assertThat(ctx.getRoles()).contains(AuthWebFilter.ADMIN);
      assertThat(ctx.isAdmin()).isTrue();
    }
  }

  @Nested
  @DisplayName("Security enabled (production-like)")
  class SecurityEnabled {

    @BeforeEach
    void setUp() throws Exception {
      authWebFilter = new AuthWebFilter(createEmptyJwtProperties());
      setField(authWebFilter, "profile", "prod");
      setField(authWebFilter, "enableSecurity", true);
    }

    @Test
    @DisplayName("should allow admin user to access admin-only endpoints")
    void adminAccess_adminEndpoint() {
      String jwtToken = createJwtToken("admin@example.com", List.of(AuthWebFilter.ADMIN));
      AtomicReference<Boolean> chainCalled = new AtomicReference<>(false);

      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.post("/orchest/processInstances/create")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                  .build());

      StepVerifier.create(
              authWebFilter.filter(
                  exchange,
                  ex -> {
                    chainCalled.set(true);
                    return Mono.empty();
                  }))
          .verifyComplete();

      assertThat(chainCalled.get()).isTrue();
      assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("should return 401 with JSON body when bearer token is missing")
    void missingBearerToken_returns401WithJsonBody() {
      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.get("/orchest/processInstances").build());

      StepVerifier.create(authWebFilter.filter(exchange, ex -> Mono.empty())).verifyComplete();

      assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      String body = exchange.getResponse().getBodyAsString().block();
      assertThat(body).contains("\"meta\"");
      assertThat(body).contains("\"code\":401");
      assertThat(body).contains("\"message\":\"Unauthorized\"");
    }

    @Test
    @DisplayName("should block read-only user from admin-only endpoints")
    void readOnlyUser_adminEndpoint() {
      String jwtToken = createJwtToken("reader@example.com", List.of(AuthWebFilter.READ_USER));

      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.post("/orchest/processInstances/create")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                  .build());

      StepVerifier.create(authWebFilter.filter(exchange, ex -> Mono.empty())).verifyComplete();

      assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("should allow read-only user to access GET endpoints")
    void readOnlyUser_readEndpoint() {
      String jwtToken = createJwtToken("reader@example.com", List.of(AuthWebFilter.READ_USER));
      AtomicReference<Boolean> chainCalled = new AtomicReference<>(false);

      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.get("/orchest/processInstances")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                  .build());

      StepVerifier.create(
              authWebFilter.filter(
                  exchange,
                  ex -> {
                    chainCalled.set(true);
                    return Mono.empty();
                  }))
          .verifyComplete();

      assertThat(chainCalled.get()).isTrue();
    }

    @Test
    @DisplayName("should block user with no recognized roles")
    void noRoles_blocked() {
      String jwtToken = createJwtToken("noroles@example.com", List.of());

      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.get("/orchest/processInstances")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                  .build());

      StepVerifier.create(authWebFilter.filter(exchange, ex -> Mono.empty())).verifyComplete();

      assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("should block read-only user from POST /variables (admin-only)")
    void readOnlyUser_postVariables() {
      String jwtToken = createJwtToken("reader@example.com", List.of(AuthWebFilter.READ_USER));

      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.post("/orchest/variables")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                  .build());

      StepVerifier.create(authWebFilter.filter(exchange, ex -> Mono.empty())).verifyComplete();

      assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("should block read-only user from PATCH /processInstances/cancelInstance")
    void readOnlyUser_cancelInstance() {
      String jwtToken = createJwtToken("reader@example.com", List.of(AuthWebFilter.READ_USER));

      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.method(
                      org.springframework.http.HttpMethod.PATCH,
                      "/orchest/processInstances/cancelInstance")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                  .build());

      StepVerifier.create(authWebFilter.filter(exchange, ex -> Mono.empty())).verifyComplete();

      assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("should allow non-sensitive read user to access GET endpoints")
    void nonSensitiveReadUser_getEndpoint() {
      String jwtToken =
          createJwtToken("viewer@example.com", List.of(AuthWebFilter.READ_USER_NON_SENSITIVE));
      AtomicReference<Boolean> chainCalled = new AtomicReference<>(false);

      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.get("/orchest/processInstances")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                  .build());

      StepVerifier.create(
              authWebFilter.filter(
                  exchange,
                  ex -> {
                    chainCalled.set(true);
                    return Mono.empty();
                  }))
          .verifyComplete();

      assertThat(chainCalled.get()).isTrue();
    }
  }

  @Nested
  @DisplayName("Whitelisted paths (skipped)")
  class WhitelistedPaths {

    @BeforeEach
    void setUp() throws Exception {
      authWebFilter = new AuthWebFilter(createEmptyJwtProperties());
      setField(authWebFilter, "profile", "prod");
      setField(authWebFilter, "enableSecurity", true);
    }

    @Test
    @DisplayName("should skip filtering for Swagger UI path")
    void swaggerUI_skipped() {
      AtomicReference<Boolean> chainCalled = new AtomicReference<>(false);
      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.get("/orchest/swagger-ui/index.html").build());

      StepVerifier.create(
              authWebFilter.filter(
                  exchange,
                  ex -> {
                    chainCalled.set(true);
                    return Mono.empty();
                  }))
          .verifyComplete();

      assertThat(chainCalled.get()).isTrue();
      assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("should skip filtering for actuator health path")
    void actuator_skipped() {
      AtomicReference<Boolean> chainCalled = new AtomicReference<>(false);
      MockServerWebExchange exchange =
          MockServerWebExchange.from(MockServerHttpRequest.get("/orchest/actuator/health").build());

      StepVerifier.create(
              authWebFilter.filter(
                  exchange,
                  ex -> {
                    chainCalled.set(true);
                    return Mono.empty();
                  }))
          .verifyComplete();

      assertThat(chainCalled.get()).isTrue();
    }

    @Test
    @DisplayName("should skip filtering for API docs path")
    void apiDocs_skipped() {
      AtomicReference<Boolean> chainCalled = new AtomicReference<>(false);
      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.get("/orchest/v3/api-docs/swagger").build());

      StepVerifier.create(
              authWebFilter.filter(
                  exchange,
                  ex -> {
                    chainCalled.set(true);
                    return Mono.empty();
                  }))
          .verifyComplete();

      assertThat(chainCalled.get()).isTrue();
    }
  }

  @Nested
  @DisplayName("decodeTokenPayload (static)")
  class DecodeTokenPayload {

    @Test
    @DisplayName("should decode JWT payload correctly")
    void decodePayload() {
      String header =
          Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"RS256\"}".getBytes());
      String payload =
          Base64.getUrlEncoder()
              .withoutPadding()
              .encodeToString("{\"preferred_username\":\"test\"}".getBytes());
      String token = header + "." + payload + ".signature";

      String decoded = AuthWebFilter.decodeTokenPayload(token);

      assertThat(decoded).contains("preferred_username");
      assertThat(decoded).contains("test");
    }
  }

  @Nested
  @DisplayName("Multi-audience role mapping")
  class MultiAudienceRoleMapping {

    @BeforeEach
    void setUp() throws Exception {
      JwtSecurityProperties props = new JwtSecurityProperties();

      JwtSecurityProperties.AudienceConfig orchestConfig =
          new JwtSecurityProperties.AudienceConfig();
      orchestConfig.setName("orchest");
      orchestConfig.setAud("8eed80e7-7752-4e4e-9749-1304c4469ce7");
      orchestConfig.setRoleMapping(java.util.Map.of());

      JwtSecurityProperties.AudienceConfig platformConfig =
          new JwtSecurityProperties.AudienceConfig();
      platformConfig.setName("platform");
      platformConfig.setAud("6992b3ba-ea53-4899-91f3-72d01073da76");
      platformConfig.setRoleMapping(
          java.util.Map.of(
              "ADMINSupportGuiPermissionuat", "ORCHEST_ADMIN",
              "SupportGuiPermissionuat", "ORCHEST_READ_ONLY"));

      JwtSecurityProperties.IssuerConfig issuer = new JwtSecurityProperties.IssuerConfig();
      issuer.setName("test");
      issuer.setIssuerUri("https://test-issuer");
      issuer.setJwkSetUri("https://test-jwk");
      issuer.setAudiences(List.of(orchestConfig, platformConfig));
      props.setIssuers(List.of(issuer));

      authWebFilter = new AuthWebFilter(props);
      setField(authWebFilter, "profile", "prod");
      setField(authWebFilter, "enableSecurity", true);
    }

    @Test
    @DisplayName("should map platform admin role to ORCHEST_ADMIN and allow admin endpoint")
    void platformAdminToken_mapsToOrchestAdmin() {
      String jwtToken =
          createJwtTokenWithAudience(
              "admin@platform.com",
              List.of("ADMINSupportGuiPermissionuat"),
              "6992b3ba-ea53-4899-91f3-72d01073da76");

      AtomicReference<LoggedInUserContext> ctxRef = new AtomicReference<>();
      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.post("/orchest/processInstances/create")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                  .build());

      StepVerifier.create(authWebFilter.filter(exchange, captureContext(ctxRef))).verifyComplete();

      assertThat(ctxRef.get()).isNotNull();
      assertThat(ctxRef.get().getRoles()).contains("ORCHEST_ADMIN");
      assertThat(ctxRef.get().isAdmin()).isTrue();
      assertThat(exchange.getResponse().getStatusCode()).isNotEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName(
        "should map platform normal user role to ORCHEST_READ_ONLY and block admin endpoint")
    void platformNormalUser_blockedFromAdminEndpoint() {
      String jwtToken =
          createJwtTokenWithAudience(
              "user@platform.com",
              List.of("SupportGuiPermissionuat"),
              "6992b3ba-ea53-4899-91f3-72d01073da76");

      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.post("/orchest/processInstances/create")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                  .build());

      StepVerifier.create(authWebFilter.filter(exchange, ex -> Mono.empty())).verifyComplete();

      assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("should map platform normal user role and allow non-admin endpoint")
    void platformNormalUser_allowedNonAdminEndpoint() {
      String jwtToken =
          createJwtTokenWithAudience(
              "user@platform.com",
              List.of("SupportGuiPermissionuat"),
              "6992b3ba-ea53-4899-91f3-72d01073da76");

      AtomicReference<Boolean> chainCalled = new AtomicReference<>(false);
      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.get("/orchest/processInstances")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                  .build());

      StepVerifier.create(
              authWebFilter.filter(
                  exchange,
                  ex -> {
                    chainCalled.set(true);
                    return Mono.empty();
                  }))
          .verifyComplete();

      assertThat(chainCalled.get()).isTrue();
    }

    @Test
    @DisplayName("should pass through orchest roles unchanged when no mapping defined")
    void orchestToken_rolesPassThrough() {
      String jwtToken =
          createJwtTokenWithAudience(
              "admin@example.com",
              List.of("ORCHEST_ADMIN"),
              "8eed80e7-7752-4e4e-9749-1304c4469ce7");

      AtomicReference<LoggedInUserContext> ctxRef = new AtomicReference<>();
      MockServerWebExchange exchange =
          MockServerWebExchange.from(
              MockServerHttpRequest.post("/orchest/processInstances/create")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                  .build());

      StepVerifier.create(authWebFilter.filter(exchange, captureContext(ctxRef))).verifyComplete();

      assertThat(ctxRef.get()).isNotNull();
      assertThat(ctxRef.get().getRoles()).contains("ORCHEST_ADMIN");
      assertThat(ctxRef.get().isAdmin()).isTrue();
    }
  }

  // --- Helper methods ---

  private String createJwtToken(String username, List<String> roles) {
    String header =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes());
    String rolesJson =
        "[" + String.join(",", roles.stream().map(r -> "\"" + r + "\"").toList()) + "]";
    String payloadJson =
        "{\"preferred_username\":\"" + username + "\",\"roles\":" + rolesJson + "}";
    String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes());
    return header + "." + payload + ".fake-signature";
  }

  private String createJwtTokenWithAudience(String username, List<String> roles, String audience) {
    String header =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes());
    String rolesJson =
        "[" + String.join(",", roles.stream().map(r -> "\"" + r + "\"").toList()) + "]";
    String payloadJson =
        "{\"preferred_username\":\""
            + username
            + "\",\"roles\":"
            + rolesJson
            + ",\"aud\":\""
            + audience
            + "\"}";
    String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes());
    return header + "." + payload + ".fake-signature";
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
