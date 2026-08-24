package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.adapter.mongo.model.Connector;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/connectors")
@Tag(
    name = "Connectors",
    description = "Manage reusable connector configurations for process activities")
/** REST API interface for connector management operations. */
public interface IConnectorsAPI {

  @GetMapping("/{name}")
  @Operation(
      summary = "Get connector by name",
      responses = {
        @ApiResponse(responseCode = "200", description = "Connector found"),
        @ApiResponse(responseCode = "404", description = "Connector not found")
      })
  Mono<ResponseDTO<Connector>> getConnector(
      @Parameter(description = "Connector name") @PathVariable String name);

  @PostMapping("/upload")
  @Operation(
      summary = "Upload connectors",
      description = "Upload one or more connector configurations in bulk")
  Mono<ResponseDTO<List<Connector>>> uploadConnector(
      @Parameter(description = "List of connector data maps to upload") @RequestParam
          List<Map<String, Object>> connectorsData);

  @GetMapping
  @Operation(summary = "List connectors (paginated)")
  Mono<Page<Connector>> getConnectors(
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size);
}
