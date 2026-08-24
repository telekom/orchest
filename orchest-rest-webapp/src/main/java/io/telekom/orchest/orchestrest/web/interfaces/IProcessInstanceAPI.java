package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.api.core.response.CompensateInstanceResponse;
import io.telekom.orchest.api.core.response.ProcessInvocationResponse;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceDTO;
import io.telekom.orchest.orchestrest.api.dto.ProcessInstanceScrollDTO;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/** REST API interface for process instance lifecycle operations. */
@RestController
@RequestMapping("/processInstances")
@Tag(
    name = "Process Instances",
    description = "Create, query, cancel, retry, compensate, and modify running process instances")
public interface IProcessInstanceAPI {

  @PostMapping("/create")
  @Operation(
      summary = "Create a process instance",
      description =
          "Starts a new instance of a deployed process definition with the provided variables")
  Mono<ResponseDTO<ProcessInvocationResponse>> createProcessInstance(
      @RequestBody @Valid ProcessInvocationRequest processInvocationRequest);

  @GetMapping("/id/{processInstanceId}")
  @Operation(
      summary = "Get process instance by ID",
      description =
          "Returns full instance details including current state, variables, and activity history")
  Mono<ResponseDTO<ProcessInstanceDTO>> getProcessInstance(
      @Parameter(description = "Process instance ID") @PathVariable String processInstanceId);

  @GetMapping("/pageData")
  @Operation(
      summary = "List process instances (paginated)",
      description =
          "Filterable by process definition, version, state, time range, and free-text search")
  Mono<Page<ProcessInstance>> getPageData(
      @Parameter(description = "Filter by process definition ID") @RequestParam(required = false)
          String processDefinitionId,
      @Parameter(description = "Filter by process version") @RequestParam(required = false)
          Integer version,
      @Parameter(description = "Filter by instance state (e.g. ACTIVE, COMPLETED, INCIDENT)")
          @RequestParam(required = false)
          String state,
      @Parameter(description = "Free-text search across instance data")
          @RequestParam(required = false)
          String searchText,
      @Parameter(description = "Inclusive lower bound on createdAt (ISO-8601 offset date-time)")
          @RequestParam(value = "createdFrom", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          OffsetDateTime createdFrom,
      @Parameter(description = "Exclusive upper bound on createdAt (ISO-8601 offset date-time)")
          @RequestParam(value = "createdTo", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          OffsetDateTime createdTo,
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
      @Parameter(description = "Sort field with direction prefix (+ASC / -DESC)")
          @RequestParam(required = false, defaultValue = "-createdAt")
          String sort);

  @GetMapping("/scroll")
  @Operation(
      summary = "Scroll process instances",
      description =
          "Offset-based scrolling for virtualised lists — returns a slice without total count overhead")
  Mono<ProcessInstanceScrollDTO> scrollProcessInstances(
      @Parameter(description = "Filter by process definition ID") @RequestParam(required = false)
          String processDefinitionId,
      @Parameter(description = "Filter by process version") @RequestParam(required = false)
          Integer version,
      @Parameter(description = "Filter by instance state") @RequestParam(required = false)
          String state,
      @Parameter(description = "Free-text search") @RequestParam(required = false)
          String searchText,
      @Parameter(description = "Inclusive lower bound on createdAt (ISO-8601 offset date-time)")
          @RequestParam(value = "createdFrom", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          OffsetDateTime createdFrom,
      @Parameter(description = "Exclusive upper bound on createdAt (ISO-8601 offset date-time)")
          @RequestParam(value = "createdTo", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          OffsetDateTime createdTo,
      @Parameter(description = "Inclusive 0-based offset") @RequestParam(defaultValue = "0")
          int from,
      @Parameter(description = "Exclusive end offset") @RequestParam int to,
      @Parameter(description = "Sort field with direction prefix (+ASC / -DESC)")
          @RequestParam(required = false, defaultValue = "-createdAt")
          String sort);

  @PatchMapping("/cancelInstances")
  @Operation(
      summary = "Cancel a process instance",
      description = "Terminates a running process instance immediately")
  Mono<String> cancelInstances(@RequestBody @Valid CancelInstanceRequest cancelInstanceRequest);

  @PostMapping("/cancelBatch")
  @Operation(
      summary = "Cancel instances in batch",
      description = "Terminates multiple process instances by their IDs")
  Mono<String> cancelInstancesBatch(@RequestBody @Valid BatchInstanceRequest batchCancelRequest);

  @PatchMapping("/modifyInstance")
  @Operation(
      summary = "Modify a process instance",
      description = "Updates variables or moves the execution token to a different activity")
  Mono<String> modifyInstance(@RequestBody @Valid UpdateInstanceRequest updateInstanceRequest);

  @PostMapping("/retry")
  @Operation(
      summary = "Retry a failed instance",
      description =
          "Retries execution from the failed activity with the same or modified variables")
  Mono<String> retryInstance(@RequestBody @Valid RetryProcessEvent retryProcessEvent);

  @PostMapping("/retryBatch")
  @Operation(summary = "Retry instances in batch")
  Mono<String> retryInstancesBatch(@RequestBody @Valid BatchInstanceRequest batchInstanceRequest);

  @PostMapping("/resolveIncident")
  @Operation(
      summary = "Resolve incident on instance",
      description = "Marks the incident as resolved and resumes execution")
  Mono<String> resolveIncident(BatchInstanceRequest batchInstanceRequest);

  @PostMapping("/compensate")
  @Operation(
      summary = "Compensate a process instance",
      description = "Triggers the compensation boundary events for completed activities")
  Mono<CompensateInstanceResponse> compensate(
      @RequestBody @Valid CompensateInstanceRequest compensateInstanceRequest);

  @PostMapping("/compensateBatch")
  @Operation(summary = "Compensate instances in batch")
  Mono<String> compensateBatch(
      @RequestBody @Valid BatchCompensateInstanceRequest compensateInstanceRequest);
}
