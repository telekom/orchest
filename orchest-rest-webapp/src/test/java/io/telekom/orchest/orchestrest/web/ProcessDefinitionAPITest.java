package io.telekom.orchest.orchestrest.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.api.core.response.ResourceDeploymentResponse;
import io.telekom.orchest.orchestrest.InfrastructureContainers;
import io.telekom.orchest.orchestrest.api.dto.ResourceDefinitionDTO;
import io.telekom.orchest.orchestrest.service.processdefinition.ProcessDefinitionAPIService;
import java.time.OffsetDateTime;
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

/** Integration tests for the process definition REST API endpoints. */
@WebFluxTest(ProcessDefinitionAPI.class)
@Testcontainers(disabledWithoutDocker = true)
class ProcessDefinitionAPITest extends InfrastructureContainers {

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "orchest.kafka.config.bootstrap-servers", InfrastructureContainers::kafkaBootstrapServers);
  }

  @Autowired private WebTestClient webTestClient;

  @MockitoBean private ProcessDefinitionAPIService processDefinitionService;

  @MockitoBean private MeterRegistry meterRegistry;

  @Nested
  @DisplayName("POST /processDefinitions/upload")
  class DeployResource {

    @Test
    @DisplayName("should return 201 CREATED with deployment details on success")
    void deployResource_success() {
      ResourceDeploymentRequest request =
          ResourceDeploymentRequest.builder()
              .resourceUTF8XML("<bpmn>test</bpmn>")
              .partitionCount(10)
              .build();

      ResourceDeploymentResponse response =
          ResourceDeploymentResponse.builder().processId("test-process").version(1).build();

      when(processDefinitionService.deployProcessDefinition(any(ResourceDeploymentRequest.class)))
          .thenReturn(response);

      webTestClient
          .post()
          .uri("/processDefinitions/upload")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isCreated()
          .expectBody()
          .jsonPath("$.data.processId")
          .isEqualTo("test-process")
          .jsonPath("$.data.version")
          .isEqualTo(1)
          .jsonPath("$.meta.code")
          .isEqualTo(201)
          .jsonPath("$.meta.message")
          .isEqualTo("process deployment success");
    }
  }

  @Nested
  @DisplayName("GET /processDefinitions/ids")
  class ListProcessDefinitionsIds {

    @Test
    @DisplayName("should return list of process definition IDs")
    void listIds_success() {
      ResourceDefinitionDTO dto1 =
          ResourceDefinitionDTO.builder().definitionId("proc-1").version(1).build();
      ResourceDefinitionDTO dto2 =
          ResourceDefinitionDTO.builder().definitionId("proc-2").version(2).build();

      when(processDefinitionService.listProcessIds()).thenReturn(List.of(dto1, dto2));

      webTestClient
          .get()
          .uri("/processDefinitions/ids")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data[0].definitionId")
          .isEqualTo("proc-1")
          .jsonPath("$.data[1].definitionId")
          .isEqualTo("proc-2")
          .jsonPath("$.meta.code")
          .isEqualTo(200);
    }

    @Test
    @DisplayName("should return empty list when no definitions exist")
    void listIds_empty() {
      when(processDefinitionService.listProcessIds()).thenReturn(Collections.emptyList());

      webTestClient
          .get()
          .uri("/processDefinitions/ids")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data")
          .isEmpty();
    }
  }

  @Nested
  @DisplayName("GET /processDefinitions/{processDefinitionId}/{version}")
  class GetProcessDefinition {

    @Test
    @DisplayName("should return process definition when found")
    void getDefinition_found() {
      ResourceDefinitionDTO dto =
          ResourceDefinitionDTO.builder()
              .definitionId("test-proc")
              .resourceXML("<bpmn>xml</bpmn>")
              .version(1)
              .createdAt(OffsetDateTime.now())
              .build();

      when(processDefinitionService.getProcessDefinition("test-proc", 1))
          .thenReturn(Optional.of(dto));

      webTestClient
          .get()
          .uri("/processDefinitions/test-proc/1")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.definitionId")
          .isEqualTo("test-proc")
          .jsonPath("$.data.version")
          .isEqualTo(1)
          .jsonPath("$.meta.code")
          .isEqualTo(200);
    }

    @Test
    @DisplayName("should return 404 when process definition not found")
    void getDefinition_notFound() {
      when(processDefinitionService.getProcessDefinition("nonexistent", 1))
          .thenReturn(Optional.empty());

      webTestClient
          .get()
          .uri("/processDefinitions/nonexistent/1")
          .exchange()
          .expectStatus()
          .isNotFound()
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(404);
    }
  }

  @Nested
  @DisplayName("GET /processDefinitions")
  class GetProcessDefinitions {

    @Test
    @DisplayName("should return paginated process definitions")
    void getDefinitions_paginated() {
      ResourceDefinitionDTO dto =
          ResourceDefinitionDTO.builder().definitionId("proc-1").version(1).build();
      Page<ResourceDefinitionDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

      when(processDefinitionService.getProcessDefinitions(0, 10)).thenReturn(page);

      webTestClient
          .get()
          .uri(
              u ->
                  u.path("/processDefinitions")
                      .queryParam("page", 0)
                      .queryParam("size", 10)
                      .build())
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.content[0].definitionId")
          .isEqualTo("proc-1");
    }

    @Test
    @DisplayName("should use default page parameters")
    void getDefinitions_defaultParams() {
      Page<ResourceDefinitionDTO> emptyPage =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
      when(processDefinitionService.getProcessDefinitions(0, 10)).thenReturn(emptyPage);

      webTestClient
          .get()
          .uri("/processDefinitions")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.content")
          .isEmpty();
    }
  }

  @Nested
  @DisplayName("DELETE /processDefinitions/{processDefinitionId}/{version}")
  class DeleteProcessDefinition {

    @Test
    @DisplayName("should return 200 when deletion is successful")
    void deleteDefinition_success() {
      when(processDefinitionService.deleteProcessDefinition("proc-1", 1)).thenReturn(true);

      webTestClient
          .delete()
          .uri("/processDefinitions/proc-1/1")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(200);
    }

    @Test
    @DisplayName("should return 404 when process definition not found for deletion")
    void deleteDefinition_notFound() {
      when(processDefinitionService.deleteProcessDefinition("nonexistent", 1)).thenReturn(false);

      webTestClient
          .delete()
          .uri("/processDefinitions/nonexistent/1")
          .exchange()
          .expectStatus()
          .isNotFound()
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(404);
    }
  }
}
