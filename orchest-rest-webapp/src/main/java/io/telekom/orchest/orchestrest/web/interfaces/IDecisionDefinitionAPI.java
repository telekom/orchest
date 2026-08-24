package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.api.core.adapters.data.dto.DecisionEvaluationResponseDTO;
import io.telekom.orchest.api.core.request.ResourceDeploymentRequest;
import io.telekom.orchest.api.core.response.ResourceDeploymentResponse;
import io.telekom.orchest.orchestrest.api.dto.ResourceDefinitionDTO;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/decisionDefinitions")
@Tag(
    name = "Decision Definitions",
    description = "Deploy, query, evaluate, and delete DMN decision definitions")
/** REST API interface for DMN decision definition operations. */
public interface IDecisionDefinitionAPI {

  @PostMapping("/upload")
  @Operation(
      summary = "Deploy a decision resource",
      description = "Parses and stores a DMN decision definition from the provided XML payload")
  Mono<ResponseDTO<ResourceDeploymentResponse>> deployResource(
      @RequestBody @Valid ResourceDeploymentRequest resourceDeploymentRequest);

  @GetMapping("/ids")
  @Operation(
      summary = "List all decision definition IDs",
      description = "Returns the latest version of each distinct decision definition")
  Mono<ResponseDTO<List<ResourceDefinitionDTO>>> listDecisionDefinitions();

  @GetMapping("/{decisionDefinitionId}/{version}")
  @Operation(
      summary = "Get decision definition by ID and version",
      responses = {
        @ApiResponse(responseCode = "200", description = "Decision definition found"),
        @ApiResponse(responseCode = "404", description = "Decision definition not found")
      })
  Mono<ResponseDTO<ResourceDefinitionDTO>> getDecisionDefinition(
      @Parameter(description = "Decision definition ID") @PathVariable String decisionDefinitionId,
      @Parameter(description = "Version number (null for latest)") @PathVariable(required = false)
          Integer version);

  @GetMapping
  @Operation(summary = "List decision definitions (paginated)")
  Mono<Page<ResourceDefinitionDTO>> getDecisionDefinitions(
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size);

  @PostMapping("/evaluate")
  @Operation(
      summary = "Evaluate a decision",
      description =
          "Evaluates a DMN decision table with the provided input variables and returns matched output entries")
  Mono<ResponseDTO<DecisionEvaluationResponseDTO>> evaluateDecision(
      @RequestBody IDecisionInstanceAPI.EvaluateDecisionRequest decisionInstanceDTO);

  @DeleteMapping("/{decisionDefinitionId}/{version}")
  @Operation(
      summary = "Delete a decision definition version",
      responses = {
        @ApiResponse(responseCode = "200", description = "Deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Decision definition not found")
      })
  Mono<ResponseDTO<Void>> deleteDecisionDefinition(
      @Parameter(description = "Decision definition ID") @PathVariable String decisionDefinitionId,
      @Parameter(description = "Version number to delete") @PathVariable Integer version);
}
