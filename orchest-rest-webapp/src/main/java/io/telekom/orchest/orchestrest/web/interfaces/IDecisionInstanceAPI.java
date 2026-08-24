package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.orchestrest.api.dto.DecisionInstanceDTO;
import io.telekom.orchest.orchestrest.api.dto.DecisionInstanceScrollDTO;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/** REST API interface for decision instance queries and scrolling. */
@RestController
@RequestMapping("/decisionInstances")
@Tag(name = "Decision Instances", description = "Query historical decision evaluation instances")
public interface IDecisionInstanceAPI {

  record EvaluateDecisionRequest(
      String decisionId, Integer version, Map<String, Object> inputVariables) {}

  @GetMapping("/pageData")
  @Operation(
      summary = "List decision instances (paginated)",
      description =
          "Filterable by decision ID, version, execution time range, and free-text search")
  Mono<Page<DecisionInstanceDTO>> getDecisionInstances(
      @Parameter(description = "Filter by decision definition ID") @RequestParam(required = false)
          String decisionId,
      @Parameter(description = "Inclusive lower bound on executedAt (ISO-8601)")
          @RequestParam(value = "from", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime from,
      @Parameter(description = "Inclusive upper bound on executedAt (ISO-8601)")
          @RequestParam(value = "to", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime to,
      @Parameter(description = "Filter by decision version") @RequestParam(required = false)
          Integer version,
      @Parameter(description = "Free-text search across instance data")
          @RequestParam(required = false)
          String searchText,
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
      @Parameter(description = "Sort field with direction prefix (+ASC / -DESC). e.g. -executedAt")
          @RequestParam(required = false, defaultValue = "-executedAt")
          String sort);

  @GetMapping("/scroll")
  @Operation(
      summary = "Scroll decision instances",
      description =
          "Offset-based scrolling for virtualised lists — returns a slice without total count overhead")
  Mono<DecisionInstanceScrollDTO> scrollDecisionInstances(
      @Parameter(description = "Filter by decision definition ID") @RequestParam(required = false)
          String decisionId,
      @Parameter(description = "Filter by decision version") @RequestParam(required = false)
          Integer version,
      @Parameter(description = "Free-text search") @RequestParam(required = false)
          String searchText,
      @Parameter(description = "Inclusive lower bound on executedAt (ISO-8601)")
          @RequestParam(value = "executedFrom", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime executedFrom,
      @Parameter(description = "Inclusive upper bound on executedAt (ISO-8601)")
          @RequestParam(value = "executedTo", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime executedTo,
      @Parameter(description = "Inclusive 0-based offset") @RequestParam(defaultValue = "0")
          int from,
      @Parameter(description = "Exclusive end offset") @RequestParam int to,
      @Parameter(description = "Sort field with direction prefix (+ASC / -DESC)")
          @RequestParam(required = false, defaultValue = "-executedAt")
          String sort);

  @GetMapping("/{decisionInstanceId}")
  @Operation(
      summary = "Get decision instance by ID",
      responses = {
        @ApiResponse(responseCode = "200", description = "Instance found"),
        @ApiResponse(responseCode = "404", description = "Instance not found")
      })
  Mono<ResponseDTO<DecisionInstanceDTO>> getDecisionInstance(
      @Parameter(description = "Decision instance ID") @PathVariable String decisionInstanceId);
}
