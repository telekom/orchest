package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.api.core.adapters.data.dto.ResumeActivityEventRequest;
import io.telekom.orchest.api.core.adapters.data.model.UserTaskInstance;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.enginecore.bpmn.service.UserTaskService;
import io.telekom.orchest.orchestrest.api.dto.UserTaskDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import io.telekom.orchest.orchestrest.event.EventProducer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * Service layer for User Task REST API operations. Handles authorization checks, delegates to
 * {@link UserTaskService} for lifecycle operations, and triggers process continuation via {@link
 * OrchestWorkflowEngine#resumeActivity}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserTaskAPIService {

  private final UserTaskService userTaskService;
  private final DataInteractionService dataInteractionService;
  private final EventProducer eventProducer;

  /** Gets a single user task by its task ID. */
  public Optional<UserTaskDTO> getTask(String taskId) {
    return userTaskService.getTask(taskId).map(this::toDTO);
  }

  /** Lists all user tasks with pagination (no user filtering). */
  public Page<UserTaskDTO> getAllTasks(int page, int size, String sort) {
    return dataInteractionService.getAllUserTasks(page, size, sort).map(this::toDTO);
  }

  /** Lists all active user tasks for a specific process instance, with pagination. */
  public Page<UserTaskDTO> getTasksForProcessInstance(
      String processInstanceId, int page, int size, String sort) {
    return dataInteractionService
        .getUserTasksForProcessInstance(processInstanceId, page, size, sort)
        .map(this::toDTO);
  }

  /**
   * Claims a user task for the given user.
   *
   * @param taskId The task ID to claim.
   * @param userContext The authenticated user context.
   * @return The updated task DTO.
   */
  public UserTaskDTO claimTask(String taskId, LoggedInUserContext userContext) {
    String userId = userContext.getUserId();
    List<String> userGroups = userContext.getRoles();

    try {
      UserTaskInstance claimed = userTaskService.claimTask(taskId, userId, userGroups);
      return toDTO(claimed);
    } catch (IllegalArgumentException e) {
      throw new RestExceptions(e.getMessage(), 404);
    } catch (IllegalStateException e) {
      throw new RestExceptions(e.getMessage(), 403);
    }
  }

  /**
   * Unclaims a user task, releasing it back to the candidate pool.
   *
   * @param taskId The task ID to unclaim.
   * @return The updated task DTO.
   */
  public UserTaskDTO unclaimTask(String taskId, LoggedInUserContext userContext) {
    String userId = userContext.getUserId();

    try {
      UserTaskInstance unclaimed = userTaskService.unclaimTask(taskId, userId);
      return toDTO(unclaimed);
    } catch (IllegalArgumentException e) {
      throw new RestExceptions(e.getMessage(), 404);
    } catch (IllegalStateException e) {
      throw new RestExceptions(e.getMessage(), 403);
    }
  }

  /**
   * Reassigns a user task to a different user. Requires admin privileges.
   *
   * @param taskId The task ID to reassign.
   * @param newAssignee The new assignee user ID.
   * @return The updated task DTO.
   */
  public UserTaskDTO reassignTask(String taskId, String newAssignee) {
    try {
      UserTaskInstance reassigned = userTaskService.reassignTask(taskId, newAssignee);
      return toDTO(reassigned);
    } catch (IllegalArgumentException e) {
      throw new RestExceptions(e.getMessage(), 404);
    } catch (IllegalStateException e) {
      throw new RestExceptions(e.getMessage(), 400);
    }
  }

  /**
   * Completes a user task and continues the process execution.
   *
   * <p>This method:
   *
   * <ol>
   *   <li>Validates the user is authorized to complete the task
   *   <li>Marks the task as COMPLETED in the user task store
   *   <li>Calls {@code resumeActivity()} on the workflow engine to continue the process
   * </ol>
   *
   * @param taskId The task ID to complete.
   * @param variables Output variables from the user task.
   * @return The completed task DTO.
   */
  public UserTaskDTO completeTask(
      String taskId, Map<String, Object> variables, LoggedInUserContext userContext) {
    String userId = userContext.getUserId();
    List<String> userGroups = userContext.getRoles();

    try {
      // Validate authorization for unclaimed tasks
      UserTaskInstance task =
          userTaskService
              .getTask(taskId)
              .orElseThrow(() -> new IllegalArgumentException("User task not found: " + taskId));

      if (task.getState() == UserTaskInstance.TaskState.CREATED
          && !userTaskService.isUserAuthorized(task, userId, userGroups)) {
        // Task not yet claimed — user must be authorized to complete it
        throw new IllegalStateException(
            "User '" + userId + "' is not authorized to complete task " + taskId);
      }

      // Complete the task
      UserTaskInstance completed = userTaskService.completeTask(taskId, userId);

      // Resume the process instance to continue execution
      Variables engineVariables =
          variables != null ? Variables.builder().variables(variables).build() : null;

      eventProducer.sendResumeEvent(
          new ResumeActivityEventRequest(
              completed.getProcessInstanceId(), completed.getActivityId(), engineVariables));

      log.info(
          "User task '{}' (taskId={}) completed by '{}'. Process instance {} resumed from activity {}",
          completed.getTaskName(),
          taskId,
          userId,
          completed.getProcessInstanceId(),
          completed.getActivityId());

      return toDTO(completed);

    } catch (IllegalArgumentException e) {
      throw new RestExceptions(e.getMessage(), 404);
    } catch (IllegalStateException e) {
      throw new RestExceptions(e.getMessage(), 403);
    }
  }

  /** Converts a UserTaskInstance domain model to a UserTaskDTO. */
  private UserTaskDTO toDTO(UserTaskInstance task) {
    return UserTaskDTO.builder()
        .taskId(task.getTaskId())
        .processInstanceId(task.getProcessInstanceId())
        .processDefinitionId(task.getProcessDefinitionId())
        .activityId(task.getActivityId())
        .taskName(task.getTaskName())
        .state(task.getState() != null ? task.getState().name() : null)
        .assignee(task.getAssignee())
        .candidateUsers(task.getCandidateUsers())
        .candidateGroups(task.getCandidateGroups())
        .claimedBy(task.getClaimedBy())
        .dueDate(task.getDueDate())
        .followUpDate(task.getFollowUpDate())
        .formKey(task.getFormKey())
        .variables(task.getVariables())
        .createdAt(task.getCreatedAt() != null ? task.getCreatedAt().toString() : null)
        .claimedAt(task.getClaimedAt() != null ? task.getClaimedAt().toString() : null)
        .completedAt(task.getCompletedAt() != null ? task.getCompletedAt().toString() : null)
        .build();
  }

  /** Converts a MongoDB UserTaskInstance document to a UserTaskDTO. */
  private UserTaskDTO toDTO(io.telekom.orchest.adapter.mongo.model.UserTaskInstance task) {
    return UserTaskDTO.builder()
        .taskId(task.getTaskId())
        .processInstanceId(task.getProcessInstanceId())
        .processDefinitionId(task.getProcessDefinitionId())
        .activityId(task.getActivityId())
        .taskName(task.getTaskName())
        .state(task.getState())
        .assignee(task.getAssignee())
        .candidateUsers(task.getCandidateUsers())
        .candidateGroups(task.getCandidateGroups())
        .claimedBy(task.getClaimedBy())
        .dueDate(task.getDueDate())
        .followUpDate(task.getFollowUpDate())
        .formKey(task.getFormKey())
        .variables(task.getVariables())
        .createdAt(task.getCreatedAt() != null ? task.getCreatedAt().toString() : null)
        .claimedAt(task.getClaimedAt() != null ? task.getClaimedAt().toString() : null)
        .completedAt(task.getCompletedAt() != null ? task.getCompletedAt().toString() : null)
        .build();
  }
}
