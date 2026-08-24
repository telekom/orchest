package io.telekom.orchest.orchestrest.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.telekom.orchest.orchestrest.InfrastructureContainers;
import io.telekom.orchest.orchestrest.api.dto.UserTaskDTO;
import io.telekom.orchest.orchestrest.api.request.AssignUserTaskRequest;
import io.telekom.orchest.orchestrest.api.request.CompleteUserTaskRequest;
import io.telekom.orchest.orchestrest.service.UserTaskAPIService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Integration tests for the user task REST API endpoints. */
@WebFluxTest(UserTaskAPI.class)
@Testcontainers(disabledWithoutDocker = true)
class UserTaskAPITest extends InfrastructureContainers {

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "orchest.kafka.config.bootstrap-servers", InfrastructureContainers::kafkaBootstrapServers);
  }

  @Autowired private WebTestClient webTestClient;

  @MockitoBean private UserTaskAPIService userTaskAPIService;

  @MockitoBean private MeterRegistry meterRegistry;

  private UserTaskDTO createSampleTaskDTO() {
    return UserTaskDTO.builder()
        .taskId("task-001")
        .processInstanceId("pi-123")
        .processDefinitionId("proc-def")
        .activityId("activity-1")
        .taskName("Review Document")
        .state("CREATED")
        .assignee("user@example.com")
        .candidateUsers(List.of("user1", "user2"))
        .candidateGroups(List.of("group1"))
        .createdAt("2026-03-23T10:00:00")
        .build();
  }

  @Nested
  @DisplayName("GET /userTasks")
  class GetMyTasks {

    @Test
    @DisplayName("should return paginated list of user tasks")
    void getMyTasks_success() {
      UserTaskDTO dto = createSampleTaskDTO();
      Page<UserTaskDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

      when(userTaskAPIService.getAllTasks(eq(0), eq(10), eq("-createdAt"))).thenReturn(page);

      webTestClient
          .get()
          .uri("/userTasks")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.content[0].taskId")
          .isEqualTo("task-001")
          .jsonPath("$.content[0].taskName")
          .isEqualTo("Review Document")
          .jsonPath("$.content[0].state")
          .isEqualTo("CREATED");
    }

    @Test
    @DisplayName("should return empty page when no tasks available")
    void getMyTasks_empty() {
      Page<UserTaskDTO> emptyPage =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
      when(userTaskAPIService.getAllTasks(eq(0), eq(10), eq("-createdAt"))).thenReturn(emptyPage);

      webTestClient
          .get()
          .uri("/userTasks")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.content")
          .isEmpty();
    }
  }

  @Nested
  @DisplayName("GET /userTasks/{taskId}")
  class GetTask {

    @Test
    @DisplayName("should return task when found")
    void getTask_found() {
      UserTaskDTO dto = createSampleTaskDTO();
      when(userTaskAPIService.getTask("task-001")).thenReturn(Optional.of(dto));

      webTestClient
          .get()
          .uri("/userTasks/task-001")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.taskId")
          .isEqualTo("task-001")
          .jsonPath("$.data.processInstanceId")
          .isEqualTo("pi-123")
          .jsonPath("$.meta.code")
          .isEqualTo(200)
          .jsonPath("$.meta.message")
          .isEqualTo("User task found");
    }

    @Test
    @DisplayName("should return 404 when task not found")
    void getTask_notFound() {
      when(userTaskAPIService.getTask("nonexistent")).thenReturn(Optional.empty());

      webTestClient
          .get()
          .uri("/userTasks/nonexistent")
          .exchange()
          .expectStatus()
          .isNotFound()
          .expectBody()
          .jsonPath("$.meta.code")
          .isEqualTo(404);
    }
  }

  @Nested
  @DisplayName("GET /userTasks/process/{processInstanceId}")
  class GetTasksForProcessInstance {

    @Test
    @DisplayName("should return tasks for a given process instance")
    void getTasksForProcess_success() {
      UserTaskDTO dto = createSampleTaskDTO();
      Page<UserTaskDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

      when(userTaskAPIService.getTasksForProcessInstance("pi-123", 0, 10, "-createdAt"))
          .thenReturn(page);

      webTestClient
          .get()
          .uri("/userTasks/process/pi-123")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.content[0].processInstanceId")
          .isEqualTo("pi-123");
    }
  }

  @Nested
  @DisplayName("POST /userTasks/{taskId}/claim")
  class ClaimTask {

    @Test
    @DisplayName("should return 200 with claimed task on success")
    void claimTask_success() {
      UserTaskDTO claimed = createSampleTaskDTO();
      claimed.setState("CLAIMED");
      claimed.setClaimedBy("user@example.com");

      when(userTaskAPIService.claimTask(eq("task-001"), any())).thenReturn(claimed);

      webTestClient
          .post()
          .uri("/userTasks/task-001/claim")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.state")
          .isEqualTo("CLAIMED")
          .jsonPath("$.data.claimedBy")
          .isEqualTo("user@example.com")
          .jsonPath("$.meta.message")
          .isEqualTo("Task claimed successfully");
    }
  }

  @Nested
  @DisplayName("POST /userTasks/{taskId}/unclaim")
  class UnclaimTask {

    @Test
    @DisplayName("should return 200 with unclaimed task on success")
    void unclaimTask_success() {
      UserTaskDTO unclaimed = createSampleTaskDTO();
      unclaimed.setState("CREATED");
      unclaimed.setClaimedBy(null);

      when(userTaskAPIService.unclaimTask(eq("task-001"), any())).thenReturn(unclaimed);

      webTestClient
          .post()
          .uri("/userTasks/task-001/unclaim")
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.state")
          .isEqualTo("CREATED")
          .jsonPath("$.meta.message")
          .isEqualTo("Task unclaimed successfully");
    }
  }

  @Nested
  @DisplayName("POST /userTasks/{taskId}/complete")
  class CompleteTask {

    @Test
    @DisplayName("should return 200 with completed task and variables")
    void completeTask_withVariables() {
      CompleteUserTaskRequest request =
          CompleteUserTaskRequest.builder().variables(Map.of("approved", true)).build();

      UserTaskDTO completed = createSampleTaskDTO();
      completed.setState("COMPLETED");
      completed.setCompletedAt("2026-03-23T11:00:00");

      when(userTaskAPIService.completeTask(eq("task-001"), eq(Map.of("approved", true)), any()))
          .thenReturn(completed);

      webTestClient
          .post()
          .uri("/userTasks/task-001/complete")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.state")
          .isEqualTo("COMPLETED")
          .jsonPath("$.meta.message")
          .isEqualTo("Task completed successfully");
    }

    @Test
    @DisplayName("should handle completion without variables (empty body)")
    void completeTask_noVariables() {
      UserTaskDTO completed = createSampleTaskDTO();
      completed.setState("COMPLETED");

      when(userTaskAPIService.completeTask(eq("task-001"), isNull(), any())).thenReturn(completed);

      webTestClient
          .post()
          .uri("/userTasks/task-001/complete")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(Map.of())
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.state")
          .isEqualTo("COMPLETED");
    }
  }

  @Nested
  @DisplayName("PATCH /userTasks/{taskId}/assign")
  class ReassignTask {

    @Test
    @DisplayName("should return 200 with reassigned task")
    void reassignTask_success() {
      AssignUserTaskRequest request =
          AssignUserTaskRequest.builder().assignee("newuser@example.com").build();

      UserTaskDTO reassigned = createSampleTaskDTO();
      reassigned.setAssignee("newuser@example.com");

      when(userTaskAPIService.reassignTask("task-001", "newuser@example.com"))
          .thenReturn(reassigned);

      webTestClient
          .patch()
          .uri("/userTasks/task-001/assign")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.data.assignee")
          .isEqualTo("newuser@example.com")
          .jsonPath("$.meta.message")
          .isEqualTo("Task reassigned successfully");
    }
  }
}
