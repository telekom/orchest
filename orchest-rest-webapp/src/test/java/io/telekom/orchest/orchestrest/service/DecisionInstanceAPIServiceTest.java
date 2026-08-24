package io.telekom.orchest.orchestrest.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.model.DecisionInstance;
import io.telekom.orchest.api.core.model.dmn.DIState;
import io.telekom.orchest.orchestrest.api.dto.DecisionInstanceDTO;
import io.telekom.orchest.orchestrest.api.dto.PagedRequestDTO;
import io.telekom.orchest.orchestrest.service.decisioninstance.DecisionInstanceAPIService;
import io.telekom.orchest.orchestrest.service.decisioninstance.DecisionInstanceDataService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Unit tests for {@link
 * io.telekom.orchest.orchestrest.service.decisioninstance.DecisionInstanceAPIService}.
 */
@ExtendWith(MockitoExtension.class)
class DecisionInstanceAPIServiceTest {

  @Mock private DecisionInstanceDataService decisionInstanceDataService;

  @InjectMocks private DecisionInstanceAPIService service;

  private DecisionInstance createSampleDecisionInstance() {
    return DecisionInstance.builder()
        .decisionInstanceId("di-001")
        .definitionId("decision-1")
        .processInstanceId("pi-123")
        .version(1)
        .state(DIState.EXECUTED)
        .executedAt(OffsetDateTime.now())
        .matchedRuleIds(List.of("rule-1"))
        .inputVariables(Map.of("input", "val"))
        .outputVariables(Map.of("output", "res"))
        .resourceXMLUTF8String("<dmn>xml</dmn>")
        .build();
  }

  @Nested
  @DisplayName("getDecisionInstance")
  class GetDecisionInstance {

    @Test
    @DisplayName("should return DTO when decision instance found")
    void getInstance_found() {
      DecisionInstance instance = createSampleDecisionInstance();
      when(decisionInstanceDataService.getDecisionInstance("di-001"))
          .thenReturn(Optional.of(instance));

      Optional<DecisionInstanceDTO> result = service.getDecisionInstance("di-001");

      assertThat(result).isPresent();
      DecisionInstanceDTO dto = result.get();
      assertThat(dto.getDecisionInstanceId()).isEqualTo("di-001");
      assertThat(dto.getDecisionId()).isEqualTo("decision-1");
      assertThat(dto.getProcessInstanceId()).isEqualTo("pi-123");
      assertThat(dto.getState()).isEqualTo("EXECUTED");
      assertThat(dto.getVersion()).isEqualTo(1);
      assertThat(dto.getMatchedRuleIds()).containsExactly("rule-1");
      assertThat(dto.getInputVariables()).containsEntry("input", "val");
      assertThat(dto.getOutputVariables()).containsEntry("output", "res");
    }

    @Test
    @DisplayName("should return empty when decision instance not found")
    void getInstance_notFound() {
      when(decisionInstanceDataService.getDecisionInstance("nonexistent"))
          .thenReturn(Optional.empty());

      Optional<DecisionInstanceDTO> result = service.getDecisionInstance("nonexistent");

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getDecisionInstances (paginated)")
  class GetDecisionInstances {

    @Test
    @DisplayName("should return page of mapped DTOs")
    void getInstances_success() {
      DecisionInstance instance = createSampleDecisionInstance();
      Page<DecisionInstance> page = new PageImpl<>(List.of(instance), PageRequest.of(0, 10), 1);

      when(decisionInstanceDataService.getDecisionInstances(any(PagedRequestDTO.class)))
          .thenReturn(page);

      PagedRequestDTO requestDTO =
          PagedRequestDTO.builder()
              .definitionId("decision-1")
              .page(0)
              .size(10)
              .sort("-executedAt")
              .build();

      Page<DecisionInstanceDTO> result = service.getDecisionInstances(requestDTO);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().getDecisionId()).isEqualTo("decision-1");
      assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("should return empty page when no instances match")
    void getInstances_empty() {
      Page<DecisionInstance> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
      when(decisionInstanceDataService.getDecisionInstances(any(PagedRequestDTO.class)))
          .thenReturn(emptyPage);

      PagedRequestDTO requestDTO =
          PagedRequestDTO.builder().page(0).size(10).sort("-executedAt").build();

      Page<DecisionInstanceDTO> result = service.getDecisionInstances(requestDTO);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }
  }
}
