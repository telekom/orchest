package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import reactor.core.publisher.Mono;

/** Global exception handler providing JSON error responses for unhandled exceptions. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RestExceptions.class)
  Mono<ResponseEntity<ResponseDTO<Void>>> handleRestException(RestExceptions ex) {
    ResponseDTO<Void> body = new ResponseDTO<>();
    body.setMeta(new ResponseDTO.Meta(ex.getCode(), ex.getMessage()));
    HttpStatus status = HttpStatus.resolve(ex.getCode());
    if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
    return Mono.just(ResponseEntity.status(status).body(body));
  }

  // NoResourceFoundException must propagate so Spring WebFlux can serve static resources
  // (Swagger UI, webjars) correctly — catching it here would break those paths.
  @ExceptionHandler(Exception.class)
  Mono<ResponseEntity<ResponseDTO<Void>>> handleGeneric(Exception ex) {
    if (ex instanceof NoResourceFoundException) {
      return Mono.error(ex);
    }
    ResponseDTO<Void> body = new ResponseDTO<>();
    body.setMeta(new ResponseDTO.Meta(500, ex.getMessage()));
    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
  }
}
