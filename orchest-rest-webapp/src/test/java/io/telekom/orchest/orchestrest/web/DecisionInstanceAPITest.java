package io.telekom.orchest.orchestrest.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.telekom.orchest.orchestrest.InfrastructureContainers;
import io.telekom.orchest.orchestrest.api.dto.DecisionInstanceDTO;
import io.telekom.orchest.orchestrest.service.decisioninstance.DecisionInstanceAPIService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Integration tests for the decision instance REST API endpoints. */
@WebFluxTest(DecisionInstanceAPI.class)
@Testcontainers(disabledWithoutDocker = true)
class DecisionInstanceAPITest extends InfrastructureContainers {

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "orchest.kafka.config.bootstrap-servers", InfrastructureContainers::kafkaBootstrapServers);
  }

  @Autowired private WebTestClient webTestClient;

  @MockitoBean private DecisionInstanceAPIService decisionInstanceAPIService;

  @MockitoBean private MeterRegistry meterRegistry;

  @Nested
  @DisplayName("GET /decisionInstances")
  class GetDecisionInstances {

    @Test
    @DisplayName("should return paginated decision instances")
    void getInstances_success() {
      DecisionInstanceDTO dto =
          DecisionInstanceDTO.builder()
              .decisionId("decision-1")
              .decisionInstanceId("di-001")
              .processInstanceId("pi-123")
              .state("EVALUATED")
              .version(1)
              .executedAt("2026-03-23T10:00:00")
              .matchedRuleIds(List.of("rule-1"))
              .inputVariables(Map.of("input", "val"))
              .outputVariables(Map.of("output", "res"))
              .build();

      Page<DecisionInstanceDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
      when(decisionInstanceAPIService.getDecisionInstances(any())).thenReturn(page);

      webTestClient
          .get()
          .uri("/decisionInstances")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.content[0].decisionId")
          .isEqualTo("decision-1")
          .jsonPath("$.content[0].decisionInstanceId")
          .isEqualTo("di-001")
          .jsonPath("$.content[0].state")
          .isEqualTo("EVALUATED");
    }

    @Test
    @DisplayName("should return empty page when no instances match")
    void getInstances_empty() {
      Page<DecisionInstanceDTO> emptyPage =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
      when(decisionInstanceAPIService.getDecisionInstances(any())).thenReturn(emptyPage);

      webTestClient
          .get()
          .uri("/decisionInstances")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.content")
          .isEmpty();
    }

    @Test
    @DisplayName("should pass filter parameters to service")
    void getInstances_withFilters() {
      Page<DecisionInstanceDTO> emptyPage =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 0);
      when(decisionInstanceAPIService.getDecisionInstances(any())).thenReturn(emptyPage);

      webTestClient
          .get()
          .uri(
              u ->
                  u.path("/decisionInstances")
                      .queryParam("decisionId", "decision-1")
                      .queryParam("version", 2)
                      .queryParam("searchText", "some-text")
                      .queryParam("page", 0)
                      .queryParam("size", 5)
                      .queryParam("sort", "-executedAt")
                      .build())
          .exchange()
          .expectStatus()
          .isOk();

      verify(decisionInstanceAPIService).getDecisionInstances(any());
    }
  }

  @Nested
  @DisplayName("GET /decisionInstances/{decisionInstanceId}")
  class GetDecisionInstance {

    @Test
    @DisplayName("should return decision instance when found")
    void getInstance_found() {
      DecisionInstanceDTO dto =
          DecisionInstanceDTO.builder()
              .decisionId("decision-1")
              .decisionInstanceId("di-001")
              .processInstanceId("pi-123")
              .state("EVALUATED")
              .version(1)
              .executedAt("2026-03-23T10:00:00")
              .inputVariables(Map.of("input", "val"))
              .outputVariables(Map.of("output", "res"))
              .resourceUTF8XML("<dmn>xml</dmn>")
              .build();

      when(decisionInstanceAPIService.getDecisionInstance("di-001")).thenReturn(Optional.of(dto));

      webTestClient
          .get()
          .uri("/decisionInstances/di-001")
          .exchange()
          .expectStatus()
          .isCreated()
          .expectBody()
          .jsonPath("$.data.decisionInstanceId")
          .isEqualTo("di-001")
          .jsonPath("$.data.decisionId")
          .isEqualTo("decision-1")
          .jsonPath("$.data.state")
          .isEqualTo("EVALUATED")
          .jsonPath("$.meta.code")
          .isEqualTo(200);
    }

    @Test
    @DisplayName("should return 404 when decision instance not found")
    void getInstance_notFound() {
      when(decisionInstanceAPIService.getDecisionInstance("nonexistent"))
          .thenReturn(Optional.empty());

      webTestClient
          .get()
          .uri("/decisionInstances/nonexistent")
          .exchange()
          .expectStatus()
          .isNotFound()
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(400);
    }
  }
}
