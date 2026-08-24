package io.telekom.orchest.orchestrest.configurations.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Returns a JSON-formatted 401 Unauthorized response for unauthenticated requests. */
@Component
public class JsonAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
    return SecurityResponseWriter.writeError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized");
  }
}
