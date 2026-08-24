package io.telekom.orchest.orchestrest.configurations.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link
 * io.telekom.orchest.orchestrest.configurations.security.JsonAuthenticationEntryPoint}.
 */
class JsonAuthenticationEntryPointTest {

  private final JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();

  @Test
  @DisplayName("returns 401 with meta JSON body")
  void commence_returnsUnauthorizedJsonBody() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/orchest/processInstances").build());

    StepVerifier.create(entryPoint.commence(exchange, new BadCredentialsException("bad token")))
        .verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(exchange.getResponse().getHeaders().getContentType().toString())
        .contains("application/json");

    String body = exchange.getResponse().getBodyAsString().block();
    assertThat(body).contains("\"meta\"");
    assertThat(body).contains("\"code\":401");
    assertThat(body).contains("\"message\":\"Unauthorized\"");
  }
}
