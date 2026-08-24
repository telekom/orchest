package io.telekom.orchest.orchestrest.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.api.core.response.ResourceDeploymentResponse;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.orchestrest.api.dto.ResourceDefinitionDTO;
import io.telekom.orchest.orchestrest.service.processdefinition.ProcessDefinitionAPIService;
import io.telekom.orchest.orchestrest.service.processdefinition.ProcessDefinitionDataService;
import io.telekom.orchest.telemetry.OrchestRestTelemetryService;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.kafka.core.KafkaAdmin;

/**
 * Unit tests for {@link
 * io.telekom.orchest.orchestrest.service.processdefinition.ProcessDefinitionAPIService}.
 */
@ExtendWith(MockitoExtension.class)
class ProcessDefinitionAPIServiceTest {

  @Mock private OrchestWorkflowEngine orchestWorkflowEngine;

  @Mock private ProcessDefinitionDataService processDefinitionDataService;

  @Mock private KafkaAdmin kafkaAdmin;

  @Mock private OrchestRestTelemetryService orchestRestTelemetryService;

  @InjectMocks private ProcessDefinitionAPIService service;

  @Nested
  @DisplayName("deployProcessDefinition")
  class DeployProcessDefinition {

    @Test
    @DisplayName("should deploy and return response with processId and version")
    void deploy_success() {
      ResourceDeploymentRequest request =
          ResourceDeploymentRequest.builder()
              .resourceUTF8XML("<bpmn>xml</bpmn>")
              .partitionCount(10)
              .build();

      ProcessDefinition deployed =
          ProcessDefinition.builder().definitionId("test-proc").version(1).build();

      when(orchestWorkflowEngine.deployProcessDefinition(anyString(), anyBoolean()))
          .thenReturn(deployed);
      when(kafkaAdmin.getConfigurationProperties())
          .thenReturn(java.util.Map.of("bootstrap.servers", "localhost:9092"));

      ResourceDeploymentResponse response = service.deployProcessDefinition(request);

      assertThat(response.getProcessId()).isEqualTo("test-proc");
      assertThat(response.getVersion()).isEqualTo(1);
      verify(orchestRestTelemetryService)
          .incrementResourceDeploymentMetrics("test-proc", 1, "BPMN");
    }
  }

  @Nested
  @DisplayName("listProcessIds")
  class ListProcessIds {

    @Test
    @DisplayName("should return list of resource definition DTOs")
    void listIds_success() {
      ProcessDefinition def1 =
          ProcessDefinition.builder()
              .definitionId("proc-1")
              .version(1)
              .definitionXML("<xml1/>")
              .createdAt(OffsetDateTime.now())
              .build();

      when(processDefinitionDataService.findDistinctProcessIds()).thenReturn(List.of(def1));

      List<ResourceDefinitionDTO> result = service.listProcessIds();

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getDefinitionId()).isEqualTo("proc-1");
    }

    @Test
    @DisplayName("should return empty list when no definitions exist")
    void listIds_empty() {
      when(processDefinitionDataService.findDistinctProcessIds())
          .thenReturn(Collections.emptyList());

      List<ResourceDefinitionDTO> result = service.listProcessIds();

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getProcessDefinition")
  class GetProcessDefinition {

    @Test
    @DisplayName("should return DTO when definition exists")
    void getDefinition_found() {
      ProcessDefinition def =
          ProcessDefinition.builder()
              .definitionId("proc-1")
              .version(1)
              .definitionXML("<xml/>")
              .build();

      when(processDefinitionDataService.getProcessDefinitionsWithVersion("proc-1", 1))
          .thenReturn(Optional.of(def));

      Optional<ResourceDefinitionDTO> result = service.getProcessDefinition("proc-1", 1);

      assertThat(result).isPresent();
      assertThat(result.get().getDefinitionId()).isEqualTo("proc-1");
      assertThat(result.get().getVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("should return empty when definition not found")
    void getDefinition_notFound() {
      when(processDefinitionDataService.getProcessDefinitionsWithVersion("missing", 1))
          .thenReturn(Optional.empty());

      Optional<ResourceDefinitionDTO> result = service.getProcessDefinition("missing", 1);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getProcessDefinitions (paginated)")
  class GetProcessDefinitions {

    @Test
    @DisplayName("should return paged result including dynamic process definition")
    void getDefinitions_paginated() {
      ProcessDefinition def = ProcessDefinition.builder().definitionId("proc-1").version(1).build();

      when(processDefinitionDataService.getProcessDefinitions(eq(0), eq(10), any()))
          .thenReturn(new java.util.ArrayList<>(List.of(def)));

      Page<ResourceDefinitionDTO> result = service.getProcessDefinitions(0, 10);

      assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1);
    }
  }

  @Nested
  @DisplayName("deleteProcessDefinition")
  class DeleteProcessDefinition {

    @Test
    @DisplayName("should return true when deletion succeeds")
    void deleteDefinition_success() {
      when(processDefinitionDataService.deleteProcessDefinition("proc-1", 1)).thenReturn(true);

      boolean result = service.deleteProcessDefinition("proc-1", 1);

      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("should return false when definition not found")
    void deleteDefinition_notFound() {
      when(processDefinitionDataService.deleteProcessDefinition("missing", 1)).thenReturn(false);

      boolean result = service.deleteProcessDefinition("missing", 1);

      assertThat(result).isFalse();
    }
  }
}
