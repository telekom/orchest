package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.extensions.sensitivevariables.model.ProcessSensitiveVariables;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/processDefinitionsSensitiveVariables")
@Tag(
    name = "Sensitive Variables",
    description =
        "Configure which process variables are treated as sensitive (masked in logs and UI)")
/** REST API interface for managing sensitive variable configurations per process definition. */
public interface IProcessDefinitionSensitiveVariables {

  @PostMapping
  @Operation(
      summary = "Add sensitive variable config",
      description = "Marks variable names as sensitive for a process definition")
  Mono<ResponseDTO<ProcessSensitiveVariables>> addProcessSensitiveVariables(
      @RequestBody @Valid ProcessSensitiveVariables processSensitiveVariables);

  @DeleteMapping
  @Operation(summary = "Delete sensitive variable config")
  Mono<ResponseDTO<String>> deleteProcessSensitiveVariables(
      @RequestBody @Valid ProcessSensitiveVariables processSensitiveVariables);

  @PatchMapping
  @Operation(summary = "Update sensitive variable config")
  Mono<ResponseDTO<ProcessSensitiveVariables>> updateProcessSensitiveVariables(
      @RequestBody @Valid ProcessSensitiveVariables processSensitiveVariables);

  @GetMapping("/{processDefinitionId}")
  @Operation(summary = "Get sensitive variables for a process")
  Mono<ResponseDTO<List<ProcessSensitiveVariables>>> getProcessSensitiveVariables(
      @Parameter(description = "Process definition ID") @PathVariable String processDefinitionId);

  @GetMapping
  @Operation(summary = "List all sensitive variable configs (paginated)")
  Mono<Page<ProcessSensitiveVariables>> getAll(
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size);
}
