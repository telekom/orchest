package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.dto.UserTaskDTO;
import io.telekom.orchest.orchestrest.api.request.AssignUserTaskRequest;
import io.telekom.orchest.orchestrest.api.request.CompleteUserTaskRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/userTasks")
@Tag(
    name = "User Tasks",
    description = "Manage BPMN User Task lifecycle — list, claim, unclaim, complete, and reassign")
/** REST API interface for user task operations. */
public interface IUserTaskAPI {

  @GetMapping
  @Operation(summary = "List all user tasks (paginated)")
  Mono<Page<UserTaskDTO>> getTasks(
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
      @Parameter(description = "Sort field with direction prefix (+ASC / -DESC)")
          @RequestParam(required = false, defaultValue = "-createdAt")
          String sort);

  @GetMapping("/{taskId}")
  @Operation(
      summary = "Get user task details",
      responses = {
        @ApiResponse(responseCode = "200", description = "Task found"),
        @ApiResponse(responseCode = "404", description = "Task not found")
      })
  Mono<ResponseDTO<UserTaskDTO>> getTask(
      @Parameter(description = "Task ID") @PathVariable String taskId);

  @GetMapping("/process/{processInstanceId}")
  @Operation(summary = "List tasks for a process instance")
  Mono<Page<UserTaskDTO>> getTasksForProcessInstance(
      @Parameter(description = "Process instance ID") @PathVariable String processInstanceId,
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
      @Parameter(description = "Sort field with direction prefix (+ASC / -DESC)")
          @RequestParam(required = false, defaultValue = "-createdAt")
          String sort);

  @PostMapping("/{taskId}/claim")
  @Operation(
      summary = "Claim a user task",
      description = "Assigns the task to the authenticated user")
  Mono<ResponseDTO<UserTaskDTO>> claimTask(
      @Parameter(description = "Task ID") @PathVariable String taskId);

  @PostMapping("/{taskId}/unclaim")
  @Operation(
      summary = "Unclaim a user task",
      description = "Releases the task back to the pool of unassigned tasks")
  Mono<ResponseDTO<UserTaskDTO>> unclaimTask(
      @Parameter(description = "Task ID") @PathVariable String taskId);

  @PostMapping("/{taskId}/complete")
  @Operation(
      summary = "Complete a user task",
      description = "Completes the task with optional output variables")
  Mono<ResponseDTO<UserTaskDTO>> completeTask(
      @Parameter(description = "Task ID") @PathVariable String taskId,
      @RequestBody(required = false) CompleteUserTaskRequest request);

  @PatchMapping("/{taskId}/assign")
  @Operation(summary = "Reassign a user task", description = "Assigns the task to a different user")
  Mono<ResponseDTO<UserTaskDTO>> reassignTask(
      @Parameter(description = "Task ID") @PathVariable String taskId,
      @RequestBody @Valid AssignUserTaskRequest request);
}
