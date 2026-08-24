package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/processDefinitions")
@Tag(
    name = "Process Definitions",
    description = "Deploy, query, and delete BPMN process definitions")
/** REST API interface for BPMN process definition operations. */
public interface IProcessDefinitionAPI {

  @PostMapping("/upload")
  @Operation(
      summary = "Deploy a process resource",
      description =
          "Parses and stores a BPMN process definition, creating Kafka topics and consumer groups as needed")
  Mono<ResponseDTO<ResourceDeploymentResponse>> deployResource(
      @RequestBody @Valid ResourceDeploymentRequest resourceDeploymentRequest);

  @GetMapping("/ids")
  @Operation(
      summary = "List all process definition IDs",
      description = "Returns the latest version of each distinct process definition")
  Mono<ResponseDTO<List<ResourceDefinitionDTO>>> listProcessDefinitionsIds();

  @GetMapping("/{processDefinitionId}/{version}")
  @Operation(
      summary = "Get process definition by ID and version",
      responses = {
        @ApiResponse(responseCode = "200", description = "Process definition found"),
        @ApiResponse(responseCode = "404", description = "Process definition not found")
      })
  Mono<ResponseDTO<ResourceDefinitionDTO>> getProcessDefinition(
      @Parameter(description = "Process definition ID") @PathVariable String processDefinitionId,
      @Parameter(description = "Version number (null for latest)") @PathVariable(required = false)
          Integer version);

  @GetMapping
  @Operation(summary = "List process definitions (paginated)")
  Mono<Page<ResourceDefinitionDTO>> getProcessDefinitions(
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size);

  @DeleteMapping("/{processDefinitionId}/{version}")
  @Operation(
      summary = "Delete a process definition version",
      responses = {
        @ApiResponse(responseCode = "200", description = "Deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Process definition not found")
      })
  Mono<ResponseDTO<Void>> deleteProcessDefinition(
      @Parameter(description = "Process definition ID") @PathVariable String processDefinitionId,
      @Parameter(description = "Version number to delete") @PathVariable Integer version);
}
