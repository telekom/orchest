package io.telekom.orchest.orchestrest.api.advice;

import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Tests for ExceptionTranslator controller advice. Uses a standalone WebTestClient setup with a
 * test-only controller to trigger specific exceptions.
 */
/** Unit tests for {@link io.telekom.orchest.orchestrest.api.advice.ExceptionTranslator}. */
class ExceptionTranslatorTest {

  private WebTestClient webTestClient;

  @RestController
  @RequestMapping("/test-exceptions")
  static class TestExceptionController {

    @GetMapping("/rest-400")
    public Mono<String> throw400() {
      return Mono.error(new RestExceptions("Bad request error", 400));
    }

    @GetMapping("/rest-404")
    public Mono<String> throw404() {
      return Mono.error(new RestExceptions("Resource not found", 404));
    }

    @GetMapping("/rest-409")
    public Mono<String> throw409() {
      return Mono.error(new RestExceptions("Conflict error", 409));
    }

    @GetMapping("/rest-422")
    public Mono<String> throw422() {
      return Mono.error(new RestExceptions("Unprocessable entity", 422, "Extra details here"));
    }

    @GetMapping("/generic")
    public Mono<String> throwGeneric() {
      return Mono.error(new RuntimeException("Something unexpected happened"));
    }
  }

  @BeforeEach
  void setUp() {
    webTestClient =
        WebTestClient.bindToController(new TestExceptionController())
            .controllerAdvice(new ExceptionTranslator())
            .build();
  }

  @Nested
  @DisplayName("RestExceptions handling")
  class RestExceptionsHandling {

    @Test
    @DisplayName("should map RestExceptions with code 400 to HTTP 400")
    void handle400() {
      webTestClient
          .get()
          .uri("/test-exceptions/rest-400")
          .exchange()
          .expectStatus()
          .isBadRequest()
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(400)
          .jsonPath("$.meta.message")
          .isEqualTo("Bad request error");
    }

    @Test
    @DisplayName("should map RestExceptions with code 404 to HTTP 404")
    void handle404() {
      webTestClient
          .get()
          .uri("/test-exceptions/rest-404")
          .exchange()
          .expectStatus()
          .isNotFound()
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(404)
          .jsonPath("$.meta.message")
          .isEqualTo("Resource not found");
    }

    @Test
    @DisplayName("should map RestExceptions with code 409 to HTTP 409")
    void handle409() {
      webTestClient
          .get()
          .uri("/test-exceptions/rest-409")
          .exchange()
          .expectStatus()
          .isEqualTo(409)
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(409)
          .jsonPath("$.meta.message")
          .isEqualTo("Conflict error");
    }

    @Test
    @DisplayName("should map RestExceptions with code 422 to HTTP 422")
    void handle422() {
      webTestClient
          .get()
          .uri("/test-exceptions/rest-422")
          .exchange()
          .expectStatus()
          .isEqualTo(422)
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(422)
          .jsonPath("$.meta.message")
          .isEqualTo("Unprocessable entity");
    }
  }

  @Nested
  @DisplayName("Generic exception handling")
  class GenericExceptionHandling {

    @Test
    @DisplayName("should map generic RuntimeException to HTTP 500")
    void handleGenericException() {
      webTestClient
          .get()
          .uri("/test-exceptions/generic")
          .exchange()
          .expectStatus()
          .isEqualTo(500)
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(500);
    }
  }
}
