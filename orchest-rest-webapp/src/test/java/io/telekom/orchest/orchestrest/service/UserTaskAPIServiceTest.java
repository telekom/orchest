package io.telekom.orchest.orchestrest.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.dto.ResumeActivityEventRequest;
import io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance;
import io.telekom.orchest.enginecore.bpmn.service.UserTaskService;
import io.telekom.orchest.orchestrest.api.dto.UserTaskDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import io.telekom.orchest.orchestrest.event.EventProducer;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/** Unit tests for {@link io.telekom.orchest.orchestrest.service.UserTaskAPIService}. */
@ExtendWith(MockitoExtension.class)
class UserTaskAPIServiceTest {

  @Mock private UserTaskService userTaskService;

  @Mock private DataInteractionService dataInteractionService;

  @Mock private EventProducer eventProducer;

  @InjectMocks private UserTaskAPIService service;

  private UserTaskInstance createSampleTask() {
    return UserTaskInstance.builder()
        .taskId("task-001")
        .processInstanceId("pi-123")
        .processDefinitionId("proc-def")
        .activityId("activity-1")
        .taskName("Review Document")
        .state(UserTaskInstance.TaskState.CREATED)
        .assignee("user@example.com")
        .candidateUsers(List.of("user1", "user2"))
        .candidateGroups(List.of("group1"))
        .createdAt(OffsetDateTime.now())
        .build();
  }

  private LoggedInUserContext userCtx(String userId, List<String> roles) {
    return LoggedInUserContext.builder()
        .userId(userId)
        .roles(roles)
        .admin(roles.contains("ORCHEST_ADMIN"))
        .build();
  }

  @Nested
  @DisplayName("getTask")
  class GetTask {

    @Test
    @DisplayName("should return DTO when task found")
    void getTask_found() {
      UserTaskInstance task = createSampleTask();
      when(userTaskService.getTask("task-001")).thenReturn(Optional.of(task));

      Optional<UserTaskDTO> result = service.getTask("task-001");

      assertThat(result).isPresent();
      assertThat(result.get().getTaskId()).isEqualTo("task-001");
      assertThat(result.get().getTaskName()).isEqualTo("Review Document");
      assertThat(result.get().getState()).isEqualTo("CREATED");
    }

    @Test
    @DisplayName("should return empty when task not found")
    void getTask_notFound() {
      when(userTaskService.getTask("nonexistent")).thenReturn(Optional.empty());

      Optional<UserTaskDTO> result = service.getTask("nonexistent");

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getAllTasks")
  class GetAllTasks {

    @Test
    @DisplayName("should delegate to data interaction service")
    void getAllTasks_delegatesCorrectly() {
      io.telekom.orchest.adapter.mongo.model.UserTaskInstance mongoTask =
          new io.telekom.orchest.adapter.mongo.model.UserTaskInstance();
      mongoTask.setTaskId("task-001");
      mongoTask.setProcessInstanceId("pi-123");
      mongoTask.setProcessDefinitionId("proc-def");
      mongoTask.setActivityId("activity-1");
      mongoTask.setTaskName("Review");
      mongoTask.setState("CREATED");

      Page<io.telekom.orchest.adapter.mongo.model.UserTaskInstance> mongoPage =
          new PageImpl<>(List.of(mongoTask), PageRequest.of(0, 10), 1);

      when(dataInteractionService.getAllUserTasks(0, 10, "-createdAt")).thenReturn(mongoPage);

      Page<UserTaskDTO> result = service.getAllTasks(0, 10, "-createdAt");

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().getFirst().getTaskId()).isEqualTo("task-001");
    }
  }

  @Nested
  @DisplayName("claimTask")
  class ClaimTask {

    @Test
    @DisplayName("should claim task and return DTO on success")
    void claimTask_success() {
      LoggedInUserContext ctx = userCtx("user@example.com", List.of("ORCHEST_ADMIN"));

      UserTaskInstance claimed = createSampleTask();
      claimed.setState(UserTaskInstance.TaskState.CLAIMED);
      claimed.setClaimedBy("user@example.com");

      when(userTaskService.claimTask("task-001", "user@example.com", List.of("ORCHEST_ADMIN")))
          .thenReturn(claimed);

      UserTaskDTO result = service.claimTask("task-001", ctx);

      assertThat(result.getState()).isEqualTo("CLAIMED");
      assertThat(result.getClaimedBy()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("should throw RestExceptions with 404 when task not found")
    void claimTask_notFound() {
      LoggedInUserContext ctx = userCtx("user@example.com", List.of("ORCHEST_ADMIN"));

      when(userTaskService.claimTask(anyString(), anyString(), anyList()))
          .thenThrow(new IllegalArgumentException("Task not found"));

      assertThatThrownBy(() -> service.claimTask("nonexistent", ctx))
          .isInstanceOf(RestExceptions.class)
          .extracting("code")
          .isEqualTo(404);
    }

    @Test
    @DisplayName("should throw RestExceptions with 403 when not authorized")
    void claimTask_unauthorized() {
      LoggedInUserContext ctx = userCtx("user@example.com", List.of("ORCHEST_READ_ONLY"));

      when(userTaskService.claimTask(anyString(), anyString(), anyList()))
          .thenThrow(new IllegalStateException("Not authorized"));

      assertThatThrownBy(() -> service.claimTask("task-001", ctx))
          .isInstanceOf(RestExceptions.class)
          .extracting("code")
          .isEqualTo(403);
    }
  }

  @Nested
  @DisplayName("unclaimTask")
  class UnclaimTask {

    @Test
    @DisplayName("should unclaim task on success")
    void unclaimTask_success() {
      LoggedInUserContext ctx = userCtx("user@example.com", Collections.emptyList());

      UserTaskInstance unclaimed = createSampleTask();
      unclaimed.setState(UserTaskInstance.TaskState.CREATED);
      unclaimed.setClaimedBy(null);

      when(userTaskService.unclaimTask("task-001", "user@example.com")).thenReturn(unclaimed);

      UserTaskDTO result = service.unclaimTask("task-001", ctx);

      assertThat(result.getState()).isEqualTo("CREATED");
      assertThat(result.getClaimedBy()).isNull();
    }

    @Test
    @DisplayName("should throw 404 when task not found during unclaim")
    void unclaimTask_notFound() {
      LoggedInUserContext ctx = userCtx("user@example.com", Collections.emptyList());
      when(userTaskService.unclaimTask(anyString(), anyString()))
          .thenThrow(new IllegalArgumentException("Not found"));

      assertThatThrownBy(() -> service.unclaimTask("nonexistent", ctx))
          .isInstanceOf(RestExceptions.class)
          .extracting("code")
          .isEqualTo(404);
    }

    @Test
    @DisplayName("should throw 403 when user did not claim the task")
    void unclaimTask_forbidden() {
      LoggedInUserContext ctx = userCtx("other@example.com", Collections.emptyList());
      when(userTaskService.unclaimTask(anyString(), anyString()))
          .thenThrow(new IllegalStateException("Cannot unclaim"));

      assertThatThrownBy(() -> service.unclaimTask("task-001", ctx))
          .isInstanceOf(RestExceptions.class)
          .extracting("code")
          .isEqualTo(403);
    }
  }

  @Nested
  @DisplayName("completeTask")
  class CompleteTask {

    @Test
    @DisplayName("should complete task and send resume event")
    void completeTask_success() {
      LoggedInUserContext ctx = userCtx("user@example.com", List.of("ORCHEST_ADMIN"));

      UserTaskInstance task = createSampleTask();
      task.setState(UserTaskInstance.TaskState.CLAIMED);
      task.setClaimedBy("user@example.com");
      when(userTaskService.getTask("task-001")).thenReturn(Optional.of(task));

      UserTaskInstance completed = createSampleTask();
      completed.setState(UserTaskInstance.TaskState.COMPLETED);
      completed.setCompletedAt(OffsetDateTime.now());
      when(userTaskService.completeTask("task-001", "user@example.com")).thenReturn(completed);

      Map<String, Object> variables = Map.of("approved", true);
      UserTaskDTO result = service.completeTask("task-001", variables, ctx);

      assertThat(result.getState()).isEqualTo("COMPLETED");

      ArgumentCaptor<ResumeActivityEventRequest> captor =
          ArgumentCaptor.forClass(ResumeActivityEventRequest.class);
      verify(eventProducer).sendResumeEvent(captor.capture());
      assertThat(captor.getValue().getProcessInstanceId()).isEqualTo("pi-123");
      assertThat(captor.getValue().getActivityId()).isEqualTo("activity-1");
      assertThat(captor.getValue().getVariables()).isNotNull();
    }

    @Test
    @DisplayName("should complete without variables (null)")
    void completeTask_noVariables() {
      LoggedInUserContext ctx = userCtx("user@example.com", List.of("ORCHEST_ADMIN"));

      UserTaskInstance task = createSampleTask();
      task.setState(UserTaskInstance.TaskState.CLAIMED);
      when(userTaskService.getTask("task-001")).thenReturn(Optional.of(task));

      UserTaskInstance completed = createSampleTask();
      completed.setState(UserTaskInstance.TaskState.COMPLETED);
      when(userTaskService.completeTask("task-001", "user@example.com")).thenReturn(completed);

      UserTaskDTO result = service.completeTask("task-001", null, ctx);

      assertThat(result.getState()).isEqualTo("COMPLETED");

      ArgumentCaptor<ResumeActivityEventRequest> captor =
          ArgumentCaptor.forClass(ResumeActivityEventRequest.class);
      verify(eventProducer).sendResumeEvent(captor.capture());
      assertThat(captor.getValue().getVariables()).isNull();
    }

    @Test
    @DisplayName("should throw 404 when task not found")
    void completeTask_notFound() {
      LoggedInUserContext ctx = userCtx("user@example.com", List.of("ORCHEST_ADMIN"));
      when(userTaskService.getTask("missing")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.completeTask("missing", null, ctx))
          .isInstanceOf(RestExceptions.class)
          .extracting("code")
          .isEqualTo(404);
    }

    @Test
    @DisplayName("should throw 403 when user is not authorized for unclaimed task")
    void completeTask_unauthorizedForUnclaimedTask() {
      LoggedInUserContext ctx = userCtx("unauthorized@example.com", List.of("ORCHEST_READ_ONLY"));

      UserTaskInstance task = createSampleTask();
      task.setState(UserTaskInstance.TaskState.CREATED);
      task.setAssignee("other@example.com");
      task.setCandidateUsers(List.of("other@example.com"));
      when(userTaskService.getTask("task-001")).thenReturn(Optional.of(task));
      when(userTaskService.isUserAuthorized(
              task, "unauthorized@example.com", List.of("ORCHEST_READ_ONLY")))
          .thenReturn(false);

      assertThatThrownBy(() -> service.completeTask("task-001", null, ctx))
          .isInstanceOf(RestExceptions.class)
          .extracting("code")
          .isEqualTo(403);
    }
  }

  @Nested
  @DisplayName("reassignTask")
  class ReassignTask {

    @Test
    @DisplayName("should reassign task and return DTO")
    void reassignTask_success() {
      UserTaskInstance reassigned = createSampleTask();
      reassigned.setAssignee("newuser@example.com");

      when(userTaskService.reassignTask("task-001", "newuser@example.com")).thenReturn(reassigned);

      UserTaskDTO result = service.reassignTask("task-001", "newuser@example.com");

      assertThat(result.getAssignee()).isEqualTo("newuser@example.com");
    }

    @Test
    @DisplayName("should throw 404 when task not found during reassign")
    void reassignTask_notFound() {
      when(userTaskService.reassignTask(anyString(), anyString()))
          .thenThrow(new IllegalArgumentException("Not found"));

      assertThatThrownBy(() -> service.reassignTask("missing", "user@example.com"))
          .isInstanceOf(RestExceptions.class)
          .extracting("code")
          .isEqualTo(404);
    }

    @Test
    @DisplayName("should throw 400 when reassignment is invalid")
    void reassignTask_invalidState() {
      when(userTaskService.reassignTask(anyString(), anyString()))
          .thenThrow(new IllegalStateException("Task already completed"));

      assertThatThrownBy(() -> service.reassignTask("task-001", "user@example.com"))
          .isInstanceOf(RestExceptions.class)
          .extracting("code")
          .isEqualTo(400);
    }
  }
}
