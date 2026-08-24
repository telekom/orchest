package io.telekom.orchest.orchestrest.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.telekom.orchest.orchestrest.InfrastructureContainers;
import io.telekom.orchest.orchestrest.api.request.VariablesRequest;
import io.telekom.orchest.orchestrest.api.request.VariablesResponse;
import io.telekom.orchest.orchestrest.service.VariablesAPIService;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Integration tests for the variables REST API endpoints. */
@WebFluxTest(VariablesAPI.class)
@Testcontainers(disabledWithoutDocker = true)
class VariablesAPITest extends InfrastructureContainers {

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "orchest.kafka.config.bootstrap-servers", InfrastructureContainers::kafkaBootstrapServers);
  }

  @Autowired private WebTestClient webTestClient;

  @MockitoBean private VariablesAPIService variablesService;

  @MockitoBean private MeterRegistry meterRegistry;

  @Nested
  @DisplayName("GET /variables/{processInstanceId}")
  class GetVariables {

    @Test
    @DisplayName("should return 200 with variables for a process instance")
    void getVariables_success() {
      VariablesResponse response = new VariablesResponse(Map.of("key1", "value1", "key2", 42));
      when(variablesService.getVariables("pi-123")).thenReturn(response);

      webTestClient
          .get()
          .uri("/variables/pi-123")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.variables.key1")
          .isEqualTo("value1")
          .jsonPath("$.data.variables.key2")
          .isEqualTo(42)
          .jsonPath("$.meta.code")
          .isEqualTo(200)
          .jsonPath("$.meta.message")
          .isEqualTo("processInstance variables");
    }

    @Test
    @DisplayName("should return 200 with empty variables map")
    void getVariables_emptyMap() {
      VariablesResponse response = new VariablesResponse(Map.of());
      when(variablesService.getVariables("pi-empty")).thenReturn(response);

      webTestClient
          .get()
          .uri("/variables/pi-empty")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.variables")
          .isEmpty();
    }
  }

  @Nested
  @DisplayName("POST /variables")
  class ModifyVariables {

    @Test
    @DisplayName("should return 200 with updated variables after ADD/UPDATE")
    void modifyVariables_addUpdate() {
      VariablesRequest request = new VariablesRequest();
      request.setProcessInstanceId("pi-123");
      request.setAction(VariablesRequest.Action.UPDATE);
      request.setVariables(Map.of("newKey", "newValue"));

      VariablesResponse response =
          new VariablesResponse(Map.of("newKey", "newValue", "existing", "val"));
      when(variablesService.modifyVariables(any(VariablesRequest.class))).thenReturn(response);

      webTestClient
          .post()
          .uri("/variables")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.variables.newKey")
          .isEqualTo("newValue")
          .jsonPath("$.meta.code")
          .isEqualTo(200)
          .jsonPath("$.meta.message")
          .isEqualTo("variables updated");
    }

    @Test
    @DisplayName("should return 200 with updated variables after DELETE")
    void modifyVariables_delete() {
      VariablesRequest request = new VariablesRequest();
      request.setProcessInstanceId("pi-123");
      request.setAction(VariablesRequest.Action.DELETE);
      request.setVariables(Map.of("toDelete", ""));

      VariablesResponse response = new VariablesResponse(Map.of("remaining", "value"));
      when(variablesService.modifyVariables(any(VariablesRequest.class))).thenReturn(response);

      webTestClient
          .post()
          .uri("/variables")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.variables.remaining")
          .isEqualTo("value");
    }
  }
}
