package io.telekom.orchest.orchestrest.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.model.DecisionDefinition;
import io.telekom.orchest.api.core.adapters.data.repository.DecisionDefinitionRepository;
import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.api.core.response.ResourceDeploymentResponse;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.orchestrest.api.dto.ResourceDefinitionDTO;
import io.telekom.orchest.orchestrest.service.decisiondefinition.DecisionDefinitionAPIService;
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

/**
 * Unit tests for {@link
 * io.telekom.orchest.orchestrest.service.decisiondefinition.DecisionDefinitionAPIService}.
 */
@ExtendWith(MockitoExtension.class)
class DecisionDefinitionAPIServiceTest {

  @Mock private OrchestWorkflowEngine orchestWorkflowEngine;

  @Mock private OrchestRestTelemetryService orchestRestTelemetryService;

  @Mock private DecisionDefinitionRepository decisionDefinitionRepository;

  @InjectMocks private DecisionDefinitionAPIService service;

  private DecisionDefinition createDecisionDefinition(
      String defId, Integer version, String xml, List<String> decisionIds) {
    DecisionDefinition def = new DecisionDefinition();
    def.setDefinitionId(defId);
    def.setVersion(version);
    def.setDefinitionXML(xml);
    def.setDecisionIds(decisionIds);
    def.setCreatedAt(OffsetDateTime.now());
    return def;
  }

  @Nested
  @DisplayName("deploy")
  class Deploy {

    @Test
    @DisplayName("should deploy and return response with definition ID and version")
    void deploy_success() {
      ResourceDeploymentRequest request =
          ResourceDeploymentRequest.builder().resourceUTF8XML("<dmn>xml</dmn>").build();

      DecisionDefinition deployed =
          createDecisionDefinition("decision-1", 1, "<dmn>xml</dmn>", List.of("dec-1"));

      when(orchestWorkflowEngine.deployDecisionDefinition(anyString())).thenReturn(deployed);

      ResourceDeploymentResponse response = service.deploy(request);

      assertThat(response.getProcessId()).isEqualTo("decision-1");
      assertThat(response.getVersion()).isEqualTo(1);
      verify(orchestRestTelemetryService)
          .incrementResourceDeploymentMetrics("decision-1", 1, "DMN");
    }
  }

  @Nested
  @DisplayName("listDecisionIds")
  class ListDecisionIds {

    @Test
    @DisplayName("should return list of decision definition DTOs")
    void listIds_success() {
      DecisionDefinition def =
          createDecisionDefinition("decision-1", 1, "<dmn/>", List.of("dec-1"));

      when(decisionDefinitionRepository.getDecisionDefinition()).thenReturn(List.of(def));

      List<ResourceDefinitionDTO> result = service.listDecisionIds();

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getDefinitionId()).isEqualTo("decision-1");
    }

    @Test
    @DisplayName("should return empty list when none exist")
    void listIds_empty() {
      when(decisionDefinitionRepository.getDecisionDefinition())
          .thenReturn(Collections.emptyList());

      List<ResourceDefinitionDTO> result = service.listDecisionIds();

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getDecisionDefinition")
  class GetDecisionDefinition {

    @Test
    @DisplayName("should return DTO when found")
    void getDefinition_found() {
      DecisionDefinition def =
          createDecisionDefinition("decision-1", 1, "<dmn/>", List.of("dec-1"));

      when(decisionDefinitionRepository.getByDefinitionIdAndVersion("decision-1", 1))
          .thenReturn(Optional.of(def));

      Optional<ResourceDefinitionDTO> result = service.getDecisionDefinition("decision-1", 1);

      assertThat(result).isPresent();
      assertThat(result.get().getDefinitionId()).isEqualTo("decision-1");
    }

    @Test
    @DisplayName("should return empty when not found")
    void getDefinition_notFound() {
      when(decisionDefinitionRepository.getByDefinitionIdAndVersion("missing", 1))
          .thenReturn(Optional.empty());

      Optional<ResourceDefinitionDTO> result = service.getDecisionDefinition("missing", 1);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getDecisionDefinitions (paginated)")
  class GetDecisionDefinitions {

    @Test
    @DisplayName("should return paged definitions")
    void getDefinitions_paginated() {
      DecisionDefinition def =
          createDecisionDefinition("decision-1", 1, "<dmn/>", List.of("dec-1"));

      when(decisionDefinitionRepository.findAllDefinitions(0, 10)).thenReturn(List.of(def));

      Page<ResourceDefinitionDTO> result = service.getDecisionDefinitions(0, 10);

      assertThat(result.getContent()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("deleteDecisionDefinition")
  class DeleteDecisionDefinition {

    @Test
    @DisplayName("should return true when deleted successfully")
    void delete_success() {
      when(decisionDefinitionRepository.deleteByDefinitionIdAndVersion("decision-1", 1))
          .thenReturn(true);

      boolean result = service.deleteDecisionDefinition("decision-1", 1);

      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("should return false when not found")
    void delete_notFound() {
      when(decisionDefinitionRepository.deleteByDefinitionIdAndVersion("missing", 1))
          .thenReturn(false);

      boolean result = service.deleteDecisionDefinition("missing", 1);

      assertThat(result).isFalse();
    }
  }
}
