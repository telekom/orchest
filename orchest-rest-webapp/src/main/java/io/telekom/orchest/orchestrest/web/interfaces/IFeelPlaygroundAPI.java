package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.orchestrest.api.dto.FeelEvaluationDTO;
import io.telekom.orchest.orchestrest.api.dto.FeelValidationDTO;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.request.FeelEvaluateRequest;
import io.telekom.orchest.orchestrest.api.request.FeelValidateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/feelPlayground")
@Tag(
    name = "FEEL Playground",
    description =
        "Evaluate and validate FEEL (Friendly Enough Expression Language) expressions interactively")
/** REST API interface for the FEEL expression playground. */
public interface IFeelPlaygroundAPI {

  @PostMapping("/evaluate")
  @Operation(
      summary = "Evaluate a FEEL expression",
      description =
          "Evaluates a FEEL expression against the provided context variables and returns the result")
  Mono<ResponseDTO<FeelEvaluationDTO>> evaluate(@RequestBody @Valid FeelEvaluateRequest request);

  @PostMapping("/validate")
  @Operation(
      summary = "Validate a FEEL expression",
      description = "Checks a FEEL expression for syntax errors without evaluating it")
  Mono<ResponseDTO<FeelValidationDTO>> validate(@RequestBody @Valid FeelValidateRequest request);
}
