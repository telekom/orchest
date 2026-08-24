package io.telekom.orchest.orchestrest.configurations.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.telekom.orchest.adapter.mongo.model.UserApiToken;
import io.telekom.orchest.orchestrest.service.UserApiTokenService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link
 * io.telekom.orchest.orchestrest.configurations.security.ApiKeyAuthWebFilter}.
 */
@ExtendWith(MockitoExtension.class)
class ApiKeyAuthWebFilterTest {

  @Mock private UserApiTokenService tokenService;

  @Mock private WebFilterChain filterChain;

  private ApiKeyAuthWebFilter filter;
  private ApiTokenProperties properties;

  @BeforeEach
  void setUp() {
    properties = new ApiTokenProperties();
    properties.setEnabled(true);
    filter = new ApiKeyAuthWebFilter(tokenService, properties);
  }

  @Test
  @DisplayName("passes through when no X-API-Key header present")
  void noHeader_passesThrough() {
    MockServerHttpRequest request =
        MockServerHttpRequest.get("/orchest/processDefinitions").build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);
    when(filterChain.filter(exchange)).thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

    verify(filterChain).filter(exchange);
    verify(tokenService, never()).validateToken(any());
  }

  @Test
  @DisplayName("authenticates with valid API key")
  void validKey_authenticates() {
    UserApiToken token =
        UserApiToken.builder()
            .id("mongo-id")
            .tokenId("tid")
            .userId("api-user@example.com")
            .roles(List.of("ORCHEST_ADMIN"))
            .expiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusDays(30))
            .revoked(false)
            .build();
    when(tokenService.validateToken("orchest_validkey123")).thenReturn(Optional.of(token));

    MockServerHttpRequest request =
        MockServerHttpRequest.get("/orchest/processDefinitions")
            .header("X-API-Key", "orchest_validkey123")
            .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);
    when(filterChain.filter(exchange)).thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

    verify(filterChain).filter(exchange);
    assertThat((Object) exchange.getAttribute(ApiKeyAuthWebFilter.PRE_AUTHENTICATED_ATTR))
        .isEqualTo(Boolean.TRUE);
    assertThat((Object) exchange.getAttribute(ApiKeyAuthWebFilter.LOGGED_IN_USER_CONTEXT_ATTR))
        .isNotNull();
  }

  @Test
  @DisplayName("returns 401 for invalid API key")
  void invalidKey_returns401() {
    when(tokenService.validateToken("orchest_badtoken")).thenReturn(Optional.empty());

    MockServerHttpRequest request =
        MockServerHttpRequest.get("/orchest/processDefinitions")
            .header("X-API-Key", "orchest_badtoken")
            .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(filterChain, never()).filter(any());

    String body = exchange.getResponse().getBodyAsString().block();
    assertThat(body).contains("\"meta\"");
    assertThat(body).contains("\"code\":401");
    assertThat(body).contains("\"message\":\"Invalid or expired API key\"");
  }

  @Test
  @DisplayName("skips whitelisted paths")
  void whitelistedPath_skipsValidation() {
    MockServerHttpRequest request =
        MockServerHttpRequest.get("/actuator/health")
            .header("X-API-Key", "orchest_something")
            .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);
    when(filterChain.filter(exchange)).thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

    verify(filterChain).filter(exchange);
    verify(tokenService, never()).validateToken(any());
  }

  @Test
  @DisplayName("disabled feature passes through even with API key header")
  void featureDisabled_passesThrough() {
    properties.setEnabled(false);
    filter = new ApiKeyAuthWebFilter(tokenService, properties);

    MockServerHttpRequest request =
        MockServerHttpRequest.get("/orchest/processDefinitions")
            .header("X-API-Key", "orchest_something")
            .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);
    when(filterChain.filter(exchange)).thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, filterChain)).verifyComplete();

    verify(filterChain).filter(exchange);
    verify(tokenService, never()).validateToken(any());
  }
}
