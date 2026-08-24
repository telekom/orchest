package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.api.core.request.BatchInstanceRequest;
import io.telekom.orchest.api.core.request.BatchRaiseIncidentRequest;
import io.telekom.orchest.api.core.request.RaiseIncidentRequest;
import io.telekom.orchest.orchestrest.api.dto.IncidentDTO;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/incidents")
@Tag(
    name = "Incident Management",
    description = "Raise, query, and resolve process execution incidents")
/** REST API interface for incident management operations. */
public interface IIncidentManagementAPI {

  @GetMapping("/{processInstanceId}")
  @Operation(
      summary = "Get incident by process instance ID",
      responses = {
        @ApiResponse(responseCode = "200", description = "Incident found"),
        @ApiResponse(responseCode = "404", description = "Incident not found")
      })
  Mono<ResponseDTO<IncidentDTO>> getIncident(
      @Parameter(description = "Process instance ID") @PathVariable String processInstanceId);

  @GetMapping
  @Operation(
      summary = "List incidents (paginated)",
      description = "Filterable by process definition, version, time range, and free-text search")
  Mono<Page<IncidentDTO>> getIncidents(
      @Parameter(description = "Filter by process definition ID") @RequestParam(required = false)
          String processDefinitionId,
      @Parameter(description = "Filter by process version") @RequestParam(required = false)
          Integer version,
      @Parameter(description = "Free-text search across incident data")
          @RequestParam(required = false)
          String searchText,
      @Parameter(description = "Inclusive lower bound on createdAt (ISO-8601)")
          @RequestParam(value = "from", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime from,
      @Parameter(description = "Inclusive upper bound on createdAt (ISO-8601)")
          @RequestParam(value = "to", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime to,
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
      @Parameter(description = "Sort field with direction prefix (+ASC / -DESC)")
          @RequestParam(required = false, defaultValue = "-createdAt")
          String sort);

  @PostMapping("/resolve/{processInstanceId}")
  @Operation(
      summary = "Resolve an incident",
      description = "Marks a single incident as resolved and resumes the affected process instance")
  Mono<String> resolveIncident(
      @Parameter(description = "Process instance ID") @PathVariable String processInstanceId);

  @PostMapping("/resolve")
  @Operation(
      summary = "Resolve incidents in batch",
      description = "Resolves multiple incidents by their process instance IDs")
  Mono<String> resolveIncidentsBatch(@RequestBody @Valid BatchInstanceRequest batchInstanceRequest);

  @PostMapping("/raise")
  @Operation(
      summary = "Raise an incident",
      description = "Manually raises an incident for a process instance")
  Mono<String> raiseIncident(@RequestBody @Valid RaiseIncidentRequest raiseIncidentRequest);

  @PostMapping("/raise/batch")
  @Operation(
      summary = "Raise incidents in batch",
      description = "Raises incidents for multiple process instances in one call")
  Mono<String> raiseIncidentsBatch(
      @RequestBody @Valid BatchRaiseIncidentRequest batchRaiseIncidentRequest);
}
