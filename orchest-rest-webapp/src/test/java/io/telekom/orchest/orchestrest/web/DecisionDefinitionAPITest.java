package io.telekom.orchest.orchestrest.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.api.core.response.ResourceDeploymentResponse;
import io.telekom.orchest.orchestrest.InfrastructureContainers;
import io.telekom.orchest.orchestrest.api.dto.ResourceDefinitionDTO;
import io.telekom.orchest.orchestrest.service.decisiondefinition.DecisionDefinitionAPIService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Integration tests for the decision definition REST API endpoints. */
@WebFluxTest(DecisionDefinitionAPI.class)
@Testcontainers
class DecisionDefinitionAPITest extends InfrastructureContainers {

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "orchest.kafka.config.bootstrap-servers", InfrastructureContainers::kafkaBootstrapServers);
  }

  @Autowired private WebTestClient webTestClient;

  @MockitoBean private DecisionDefinitionAPIService decisionDefinitionService;

  @MockitoBean private MeterRegistry meterRegistry;

  @Nested
  @DisplayName("POST /decisionDefinitions/upload")
  class DeployResource {

    @Test
    @DisplayName("should return 201 CREATED with deployment details")
    void deploy_success() {
      ResourceDeploymentRequest request =
          ResourceDeploymentRequest.builder().resourceUTF8XML("<dmn>test</dmn>").build();

      ResourceDeploymentResponse response =
          ResourceDeploymentResponse.builder().processId("decision-1").version(1).build();

      when(decisionDefinitionService.deploy(any(ResourceDeploymentRequest.class)))
          .thenReturn(response);

      webTestClient
          .post()
          .uri("/decisionDefinitions/upload")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isCreated()
          .expectBody()
          .jsonPath("$.data.processId")
          .isEqualTo("decision-1")
          .jsonPath("$.data.version")
          .isEqualTo(1)
          .jsonPath("$.meta.code")
          .isEqualTo(201);
    }
  }

  @Nested
  @DisplayName("GET /decisionDefinitions/ids")
  class ListDecisionDefinitions {

    @Test
    @DisplayName("should return list of decision definition IDs")
    void listIds_success() {
      ResourceDefinitionDTO dto1 =
          ResourceDefinitionDTO.builder().definitionId("decision-1").version(1).build();

      when(decisionDefinitionService.listDecisionIds()).thenReturn(List.of(dto1));

      webTestClient
          .get()
          .uri("/decisionDefinitions/ids")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data[0].definitionId")
          .isEqualTo("decision-1")
          .jsonPath("$.meta.code")
          .isEqualTo(200);
    }

    @Test
    @DisplayName("should return empty list when no definitions exist")
    void listIds_empty() {
      when(decisionDefinitionService.listDecisionIds()).thenReturn(Collections.emptyList());

      webTestClient
          .get()
          .uri("/decisionDefinitions/ids")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data")
          .isEmpty();
    }
  }

  @Nested
  @DisplayName("GET /decisionDefinitions/{decisionDefinitionId}/{version}")
  class GetDecisionDefinition {

    @Test
    @DisplayName("should return decision definition when found")
    void getDefinition_found() {
      ResourceDefinitionDTO dto =
          ResourceDefinitionDTO.builder()
              .definitionId("decision-1")
              .resourceXML("<dmn>xml</dmn>")
              .version(1)
              .build();

      when(decisionDefinitionService.getDecisionDefinition("decision-1", 1))
          .thenReturn(Optional.of(dto));

      webTestClient
          .get()
          .uri("/decisionDefinitions/decision-1/1")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.definitionId")
          .isEqualTo("decision-1")
          .jsonPath("$.data.version")
          .isEqualTo(1);
    }

    @Test
    @DisplayName("should return 404 when definition not found")
    void getDefinition_notFound() {
      when(decisionDefinitionService.getDecisionDefinition("nonexistent", 1))
          .thenReturn(Optional.empty());

      webTestClient
          .get()
          .uri("/decisionDefinitions/nonexistent/1")
          .exchange()
          .expectStatus()
          .isNotFound()
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(404);
    }
  }

  @Nested
  @DisplayName("GET /decisionDefinitions")
  class GetDecisionDefinitions {

    @Test
    @DisplayName("should return paginated decision definitions")
    void getDefinitions_paginated() {
      ResourceDefinitionDTO dto =
          ResourceDefinitionDTO.builder().definitionId("decision-1").version(1).build();
      Page<ResourceDefinitionDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

      when(decisionDefinitionService.getDecisionDefinitions(0, 10)).thenReturn(page);

      webTestClient
          .get()
          .uri(
              u ->
                  u.path("/decisionDefinitions")
                      .queryParam("page", 0)
                      .queryParam("size", 10)
                      .build())
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.content[0].definitionId")
          .isEqualTo("decision-1");
    }
  }

  @Nested
  @DisplayName("DELETE /decisionDefinitions/{decisionDefinitionId}/{version}")
  class DeleteDecisionDefinition {

    @Test
    @DisplayName("should return 200 when deletion succeeds")
    void deleteDefinition_success() {
      when(decisionDefinitionService.deleteDecisionDefinition("decision-1", 1)).thenReturn(true);

      webTestClient
          .delete()
          .uri("/decisionDefinitions/decision-1/1")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(200);
    }

    @Test
    @DisplayName("should return 404 when definition not found for deletion")
    void deleteDefinition_notFound() {
      when(decisionDefinitionService.deleteDecisionDefinition("nonexistent", 1)).thenReturn(false);

      webTestClient
          .delete()
          .uri("/decisionDefinitions/nonexistent/1")
          .exchange()
          .expectStatus()
          .isNotFound()
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(404);
    }
  }
}
