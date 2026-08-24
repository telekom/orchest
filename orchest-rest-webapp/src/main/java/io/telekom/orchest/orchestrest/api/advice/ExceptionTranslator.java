package io.telekom.orchest.orchestrest.api.advice;

import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Translates domain exceptions into appropriate HTTP error responses. */
@Slf4j
@RestControllerAdvice
public class ExceptionTranslator {

  @ExceptionHandler(RestExceptions.class)
  public Mono<ResponseEntity<ResponseDTO<Object>>> handleRestExceptions(
      RestExceptions ex, ServerWebExchange exchange) {
    log.error("REST exception: {} {}", ex.getClass().getName(), ex.getMessage(), ex);
    return Mono.just(
        new ResponseEntity<>(
            RestUtils.buildMeta(ex.getCode(), ex.getMessage()), HttpStatus.valueOf(ex.getCode())));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public Mono<ResponseEntity<ResponseDTO<Object>>> handleNoResourceFound(
      NoResourceFoundException ex, ServerWebExchange exchange) {
    return Mono.error(ex);
  }

  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<ResponseDTO<Object>>> handleGenericExceptions(
      Exception ex, ServerWebExchange exchange) {
    log.error("Unexpected exception: ", ex);
    return Mono.just(
        new ResponseEntity<>(
            RestUtils.buildMeta(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), ExceptionUtils.getStackTrace(ex)),
            HttpStatus.INTERNAL_SERVER_ERROR));
  }
}
