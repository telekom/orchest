package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.request.VariablesRequest;
import io.telekom.orchest.orchestrest.api.request.VariablesResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/** REST API interface for process instance variable operations. */
@RestController
@RequestMapping("/variables")
@Tag(name = "Variables", description = "Read and modify process instance variables at runtime")
public interface IVariablesAPI {

  @GetMapping("/{processInstanceId}")
  @Operation(
      summary = "Get variables for a process instance",
      responses = {
        @ApiResponse(responseCode = "200", description = "Variables returned"),
        @ApiResponse(responseCode = "404", description = "Process instance not found")
      })
  Mono<ResponseDTO<VariablesResponse>> getVariables(
      @Parameter(description = "Process instance ID") @PathVariable String processInstanceId);

  @PostMapping
  @Operation(
      summary = "Modify variables",
      description = "Sets or updates variables on a running process instance")
  Mono<ResponseDTO<VariablesResponse>> modifyVariables(
      @RequestBody @Valid VariablesRequest variablesRequest);
}
