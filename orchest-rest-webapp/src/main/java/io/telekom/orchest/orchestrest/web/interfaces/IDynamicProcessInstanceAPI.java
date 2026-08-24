package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.api.core.request.DynamicProcessInvocationRequest;
import io.telekom.orchest.api.core.response.DynamicProcessInvocationResponse;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/dyanmicProcessInstances")
@Tag(
    name = "Dynamic Process Instances",
    description = "Create process instances from dynamically-composed BPMN definitions")
/** REST API interface for dynamic process instance creation. */
public interface IDynamicProcessInstanceAPI {

  @PostMapping("/create")
  @Operation(
      summary = "Create a dynamic process instance",
      description =
          "Starts a new process instance from a dynamically-composed BPMN definition with the provided variables")
  Mono<ResponseDTO<DynamicProcessInvocationResponse>> createProcessInstance(
      @RequestBody @Valid DynamicProcessInvocationRequest processInvocationRequest);
}
