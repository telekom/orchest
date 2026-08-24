package io.telekom.orchest.orchestrest.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.request.BatchInstanceRequest;
import io.telekom.orchest.api.core.request.CancelInstanceRequest;
import io.telekom.orchest.api.core.request.ProcessInvocationRequest;
import io.telekom.orchest.api.core.request.RetryProcessEvent;
import io.telekom.orchest.api.core.request.UpdateInstanceRequest;
import io.telekom.orchest.api.core.response.ProcessInvocationResponse;
import io.telekom.orchest.orchestrest.InfrastructureContainers;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceScrollDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceStats;
import io.telekom.orchest.orchestrest.service.processInstance.ProcessInstanceAPIService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
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

/** Integration tests for the process instance REST API endpoints. */
@WebFluxTest(ProcessInstanceAPI.class)
@Testcontainers(disabledWithoutDocker = true)
class ProcessInstanceAPITest extends InfrastructureContainers {

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "orchest.kafka.config.bootstrap-servers", InfrastructureContainers::kafkaBootstrapServers);
  }

  @Autowired private WebTestClient webTestClient;

  @MockitoBean private ProcessInstanceAPIService processInstanceService;

  @MockitoBean private MeterRegistry meterRegistry;

  @Nested
  @DisplayName("POST /processInstances/create")
  class CreateProcessInstance {

    @Test
    @DisplayName("should return 200 with process instance details on successful creation")
    void createProcessInstance_success() {
      ProcessInvocationRequest request =
          ProcessInvocationRequest.builder()
              .processDefinitionId("test-process")
              .version(1)
              .variables(Map.of("key", "value"))
              .build();

      ProcessInvocationResponse response =
          new ProcessInvocationResponse("pi-123", "test-process", 1);
      when(processInstanceService.createProcessInstance(any(ProcessInvocationRequest.class)))
          .thenReturn(response);

      webTestClient
          .post()
          .uri("/processInstances/create")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.processInstanceId")
          .isEqualTo("pi-123")
          .jsonPath("$.data.processId")
          .isEqualTo("test-process")
          .jsonPath("$.data.version")
          .isEqualTo(1)
          .jsonPath("$.meta.code")
          .isEqualTo(200)
          .jsonPath("$.meta.message")
          .isEqualTo("process instance created");

      verify(processInstanceService).createProcessInstance(any(ProcessInvocationRequest.class));
    }

    @Test
    @DisplayName("should return 200 even when version is null (uses default -1)")
    void createProcessInstance_nullVersion() {
      ProcessInvocationRequest request =
          ProcessInvocationRequest.builder().processDefinitionId("test-process").build();

      ProcessInvocationResponse response =
          new ProcessInvocationResponse("pi-456", "test-process", -1);
      when(processInstanceService.createProcessInstance(any(ProcessInvocationRequest.class)))
          .thenReturn(response);

      webTestClient
          .post()
          .uri("/processInstances/create")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.version")
          .isEqualTo(-1);
    }
  }

  @Nested
  @DisplayName("GET /processInstances/id/{processInstanceId}")
  class GetProcessInstance {

    @Test
    @DisplayName("should return 200 with process instance when found")
    void getProcessInstance_found() {
      ProcessInstanceDTO dto =
          ProcessInstanceDTO.builder()
              .processInstanceId("pi-123")
              .processDefinitionId("test-process")
              .state("RUNNING")
              .version(1)
              .createdAt("2026-03-23T10:00:00")
              .activeElements(List.of("task-1"))
              .build();

      when(processInstanceService.getProcessInstance(eq("pi-123"), any()))
          .thenReturn(Optional.of(dto));

      webTestClient
          .get()
          .uri("/processInstances/id/pi-123")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.processInstanceId")
          .isEqualTo("pi-123")
          .jsonPath("$.data.processDefinitionId")
          .isEqualTo("test-process")
          .jsonPath("$.data.state")
          .isEqualTo("RUNNING")
          .jsonPath("$.meta.code")
          .isEqualTo(200)
          .jsonPath("$.meta.message")
          .isEqualTo("Found processInstance");
    }

    @Test
    @DisplayName("should return 404 when process instance not found")
    void getProcessInstance_notFound() {
      when(processInstanceService.getProcessInstance(eq("nonexistent"), any()))
          .thenReturn(Optional.empty());

      webTestClient
          .get()
          .uri("/processInstances/id/nonexistent")
          .exchange()
          .expectStatus()
          .isNotFound()
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(404);
    }
  }

  @Nested
  @DisplayName("GET /processInstances")
  class GetProcessInstances {

    @Test
    @DisplayName("should return paginated process instances with default parameters")
    void getProcessInstances_defaultParams() {
      ProcessInstanceDTO dto =
          ProcessInstanceDTO.builder()
              .processInstanceId("pi-1")
              .processDefinitionId("proc-def")
              .state("COMPLETED")
              .createdAt("2026-03-23T10:00:00")
              .build();
      Page<ProcessInstanceDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
      when(processInstanceService.getProcessInstances(any(), any())).thenReturn(page);

      webTestClient
          .get()
          .uri("/processInstances")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.content[0].processInstanceId")
          .isEqualTo("pi-1");
    }

    @Test
    @DisplayName("should return empty page when no instances match")
    void getProcessInstances_empty() {
      Page<ProcessInstanceDTO> emptyPage =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
      when(processInstanceService.getProcessInstances(any(), any())).thenReturn(emptyPage);

      webTestClient
          .get()
          .uri("/processInstances")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.content")
          .isEmpty();
    }
  }

  @Nested
  @DisplayName("GET /processInstances/scroll")
  class ScrollProcessInstances {

    @Test
    @DisplayName("should return scroll slice with metadata")
    void scroll_success() {
      ProcessInstanceScrollDTO dto =
          ProcessInstanceScrollDTO.builder()
              .content(
                  List.of(
                      ProcessInstanceDTO.builder()
                          .processInstanceId("pi-scroll")
                          .processDefinitionId("proc-def")
                          .state("RUNNING")
                          .build()))
              .from(0)
              .to(10)
              .hasNext(true)
              .build();
      when(processInstanceService.scrollProcessInstances(any(), eq(0), eq(10), any()))
          .thenReturn(dto);

      webTestClient
          .get()
          .uri(
              u ->
                  u.path("/processInstances/scroll")
                      .queryParam("to", 10)
                      .queryParam("sort", "-createdAt")
                      .build())
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.from")
          .isEqualTo(0)
          .jsonPath("$.to")
          .isEqualTo(10)
          .jsonPath("$.hasNext")
          .isEqualTo(true)
          .jsonPath("$.content[0].processInstanceId")
          .isEqualTo("pi-scroll");
    }
  }

  @Nested
  @DisplayName("GET /processInstances/stats")
  class GetProcessInstancesStats {

    @Test
    @DisplayName("should return process instance statistics")
    void getStats_success() {
      ProcessInstanceStats stats = new ProcessInstanceStats();
      stats.setCompleted(10);
      stats.setActive(5);
      stats.setIncidents(2);
      stats.setFailed(1);

      when(processInstanceService.getProcessInstancesStats()).thenReturn(stats);

      webTestClient
          .get()
          .uri("/processInstances/stats")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.completed")
          .isEqualTo(10)
          .jsonPath("$.active")
          .isEqualTo(5)
          .jsonPath("$.incidents")
          .isEqualTo(2)
          .jsonPath("$.failed")
          .isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("PATCH /processInstances/cancelInstances")
  class CancelInstances {

    @Test
    @DisplayName("should return 202 ACCEPTED for valid cancel request")
    void cancelInstances_success() {
      CancelInstanceRequest request = new CancelInstanceRequest();
      request.setInstances(
          List.of(
              new CancelInstanceRequest.Request("pi-1", "proc-def-1"),
              new CancelInstanceRequest.Request("pi-2", "proc-def-1")));

      doNothing().when(processInstanceService).cancelInstance(any(CancelInstanceRequest.class));

      webTestClient
          .patch()
          .uri("/processInstances/cancelInstances")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isAccepted()
          .expectBody(String.class)
          .isEqualTo("Request Accepted");
    }
  }

  @Nested
  @DisplayName("POST /processInstances/cancelBatch")
  class CancelInstancesBatch {

    @Test
    @DisplayName("should return 202 ACCEPTED for valid batch cancel request")
    void cancelBatch_success() {
      BatchInstanceRequest request =
          BatchInstanceRequest.builder()
              .processInstanceIds(List.of("pi-1", "pi-2", "pi-3"))
              .build();

      doNothing()
          .when(processInstanceService)
          .cancelInstancesBatch(any(BatchInstanceRequest.class));

      webTestClient
          .post()
          .uri("/processInstances/cancelBatch")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isAccepted()
          .expectBody(String.class)
          .isEqualTo("ACCEPTED");
    }

    @Test
    @DisplayName("should return 400 when processInstanceIds is empty")
    void cancelBatch_emptyList() {
      BatchInstanceRequest request =
          BatchInstanceRequest.builder().processInstanceIds(Collections.emptyList()).build();

      webTestClient
          .post()
          .uri("/processInstances/cancelBatch")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    @DisplayName("should return 400 when processInstanceIds exceeds 500")
    void cancelBatch_exceedsMax() {
      List<String> ids = IntStream.rangeClosed(1, 501).mapToObj(i -> "pi-" + i).toList();
      BatchInstanceRequest request = BatchInstanceRequest.builder().processInstanceIds(ids).build();

      webTestClient
          .post()
          .uri("/processInstances/cancelBatch")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    @DisplayName("should accept exactly 500 processInstanceIds")
    void cancelBatch_exactlyMax() {
      List<String> ids = IntStream.rangeClosed(1, 500).mapToObj(i -> "pi-" + i).toList();
      BatchInstanceRequest request = BatchInstanceRequest.builder().processInstanceIds(ids).build();

      doNothing()
          .when(processInstanceService)
          .cancelInstancesBatch(any(BatchInstanceRequest.class));

      webTestClient
          .post()
          .uri("/processInstances/cancelBatch")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isAccepted();
    }
  }

  @Nested
  @DisplayName("POST /processInstances/retry")
  class RetryInstance {

    @Test
    @DisplayName("should return 202 ACCEPTED when retry succeeds")
    void retryInstance_success() {
      RetryProcessEvent event =
          RetryProcessEvent.builder()
              .processInstanceId("pi-123")
              .activityId("task-1")
              .previousActivityId("task-1")
              .build();

      when(processInstanceService.retryInstance(any(RetryProcessEvent.class))).thenReturn(true);

      webTestClient
          .post()
          .uri("/processInstances/retry")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(event)
          .exchange()
          .expectStatus()
          .isAccepted()
          .expectBody(String.class)
          .isEqualTo("ACCEPTED");
    }

    @Test
    @DisplayName("should return 202 with 'Failed' message when retry fails")
    void retryInstance_failure() {
      RetryProcessEvent event =
          RetryProcessEvent.builder()
              .processInstanceId("pi-123")
              .activityId("task-1")
              .previousActivityId("task-1")
              .build();

      when(processInstanceService.retryInstance(any(RetryProcessEvent.class))).thenReturn(false);

      webTestClient
          .post()
          .uri("/processInstances/retry")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(event)
          .exchange()
          .expectStatus()
          .isAccepted()
          .expectBody(String.class)
          .isEqualTo("Failed");
    }
  }

  @Nested
  @DisplayName("POST /processInstances/retryBatch")
  class RetryInstancesBatch {

    @Test
    @DisplayName("should return 202 ACCEPTED for valid batch retry request")
    void retryBatch_success() {
      BatchInstanceRequest request =
          BatchInstanceRequest.builder().processInstanceIds(List.of("pi-1", "pi-2")).build();

      doNothing().when(processInstanceService).retryInstancesBatch(any(BatchInstanceRequest.class));

      webTestClient
          .post()
          .uri("/processInstances/retryBatch")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isAccepted()
          .expectBody(String.class)
          .isEqualTo("ACCEPTED");
    }

    @Test
    @DisplayName("should return 400 when processInstanceIds is empty")
    void retryBatch_emptyList() {
      BatchInstanceRequest request =
          BatchInstanceRequest.builder().processInstanceIds(Collections.emptyList()).build();

      webTestClient
          .post()
          .uri("/processInstances/retryBatch")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    @DisplayName("should return 400 when exceeding 500 limit")
    void retryBatch_exceedsMax() {
      List<String> ids = IntStream.rangeClosed(1, 501).mapToObj(i -> "pi-" + i).toList();
      BatchInstanceRequest request = BatchInstanceRequest.builder().processInstanceIds(ids).build();

      webTestClient
          .post()
          .uri("/processInstances/retryBatch")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isBadRequest();
    }
  }

  @Nested
  @DisplayName("PATCH /processInstances/modifyInstance")
  class ModifyInstance {

    @Test
    @DisplayName("should return 202 ACCEPTED when modification succeeds")
    void modifyInstance_success() {
      UpdateInstanceRequest request =
          UpdateInstanceRequest.builder()
              .processInstanceId("pi-123")
              .fromNodeId("node-a")
              .toNodeId("node-b")
              .build();

      when(processInstanceService.modifyInstance(any(UpdateInstanceRequest.class)))
          .thenReturn(true);

      webTestClient
          .patch()
          .uri("/processInstances/modifyInstance")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isAccepted()
          .expectBody(String.class)
          .isEqualTo("ACCEPTED");
    }

    @Test
    @DisplayName("should return 202 with 'Failed' when modification fails")
    void modifyInstance_failure() {
      UpdateInstanceRequest request =
          UpdateInstanceRequest.builder()
              .processInstanceId("pi-123")
              .fromNodeId("node-a")
              .toNodeId("node-b")
              .build();

      when(processInstanceService.modifyInstance(any(UpdateInstanceRequest.class)))
          .thenReturn(false);

      webTestClient
          .patch()
          .uri("/processInstances/modifyInstance")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isAccepted()
          .expectBody(String.class)
          .isEqualTo("Failed");
    }
  }

  @Nested
  @DisplayName("GET /processInstances/pageData")
  class GetPageData {

    @Test
    @DisplayName("should return paginated ID data")
    void getPageData_success() {
      Page<ProcessInstance> page =
          new PageImpl<>(List.of(new ProcessInstance("1", "1", 1)), PageRequest.of(0, 10), 2);
      when(processInstanceService.getPageData(any())).thenReturn(page);

      webTestClient
          .get()
          .uri("/processInstances/pageData")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.content")
          .isArray();
    }
  }
}
