package io.telekom.orchest.orchestrest.configurations.security;

import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Utility for writing JSON error responses from security filters. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SecurityResponseWriter {

  static Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String message) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(status);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    ResponseDTO<Void> body = RestUtils.buildMeta(status.value(), message);
    byte[] bytes = JsonMapper.writeToJson(body).getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = response.bufferFactory().wrap(bytes);
    return response.writeWith(Mono.just(buffer));
  }
}
