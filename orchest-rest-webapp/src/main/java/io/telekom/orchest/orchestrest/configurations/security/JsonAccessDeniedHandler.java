package io.telekom.orchest.orchestrest.configurations.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Returns a JSON-formatted 403 Forbidden response when access is denied. */
@Component
public class JsonAccessDeniedHandler implements ServerAccessDeniedHandler {

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
    return SecurityResponseWriter.writeError(
        exchange, HttpStatus.FORBIDDEN, "Permission denied to access resource");
  }
}
