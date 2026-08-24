package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.api.core.adapters.data.model.ProcessEnvVariables;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/processDefinitionsEnvs")
@Tag(
    name = "Process Environment Variables",
    description = "Manage environment variables injected into process instances at runtime")
/** REST API interface for process definition environment variable management. */
public interface IProcessDefinitionEnvironmentAPI {

  @PostMapping
  @Operation(
      summary = "Add an environment variable",
      description = "Creates a new environment variable binding for a process definition")
  Mono<ResponseDTO<ProcessEnvVariables>> addProcessEnvVariable(
      @RequestBody @Valid ProcessEnvVariables processEnvVariables);

  @DeleteMapping
  @Operation(summary = "Delete an environment variable")
  Mono<ResponseDTO<ProcessEnvVariables>> deleteProcessEnvVariable(
      @RequestBody @Valid ProcessEnvVariables processEnvVariables);

  @PatchMapping
  @Operation(summary = "Update an environment variable")
  Mono<ResponseDTO<ProcessEnvVariables>> updateProcessEnvVariable(
      @RequestBody @Valid ProcessEnvVariables processEnvVariables);

  @GetMapping("/{processDefinitionId}")
  @Operation(
      summary = "Get environment variables for a process",
      responses = {
        @ApiResponse(responseCode = "200", description = "Variables found"),
        @ApiResponse(responseCode = "404", description = "Process definition not found")
      })
  Mono<ResponseDTO<List<ProcessEnvVariables>>> getProcessDefinition(
      @Parameter(description = "Process definition ID") @PathVariable String processDefinitionId);

  @GetMapping
  @Operation(summary = "List all environment variable bindings (paginated)")
  Mono<Page<ProcessEnvVariables>> getProcessDefinitions(
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size);
}
