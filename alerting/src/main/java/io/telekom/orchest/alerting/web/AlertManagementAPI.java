package io.telekom.orchest.alerting.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.alerting.api.ActorRequest;
import io.telekom.orchest.alerting.api.AlertResponse;
import io.telekom.orchest.alerting.api.PagedAlertResponse;
import io.telekom.orchest.alerting.api.exception.AlertNotFoundException;
import io.telekom.orchest.alerting.api.exception.AlertTransitionException;
import io.telekom.orchest.alerting.service.AlertLifecycleService;
import io.telekom.orchest.alerting.service.AlertingMailerConfigService;
import io.telekom.orchest.api.core.adapters.data.model.AlertRecipients;
import io.telekom.orchest.api.core.adapters.data.model.AlertState;
import io.telekom.orchest.api.core.adapters.data.model.AlertingMailerConfig;
import io.telekom.orchest.api.core.adapters.data.repository.AlertRepository.AlertPage;
import io.telekom.orchest.api.core.adapters.data.repository.AlertRepository.AlertStats;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * REST controller exposing alert lifecycle operations (list, acknowledge, silence, unmute, resolve)
 * and CRUD for per-process mailer recipient configurations.
 */
@Tag(
    name = "Alert Management",
    description =
        "Lifecycle API for persistent telemetry alerts — list, acknowledge, silence, unmute, and resolve.")
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
@ConditionalOnBean(AlertLifecycleService.class)
public class AlertManagementAPI {

  private final AlertLifecycleService alertLifecycleService;
  private final AlertingMailerConfigService mailerConfigService;

  @Operation(
      summary = "List alerts",
      description =
          "Returns a paginated, filterable, sortable list of alerts. Supports filtering by state, processDefinitionId, and creation time range.",
      responses = @ApiResponse(responseCode = "200", description = "Paginated alert list"))
  @GetMapping
  public Mono<PagedAlertResponse> list(
      @Parameter(description = "Filter by alert lifecycle state") @RequestParam(required = false)
          AlertState state,
      @Parameter(description = "Filter by process definition ID (stored in alert metadata)")
          @RequestParam(required = false)
          String processDefinitionId,
      @Parameter(
              description =
                  "Inclusive lower bound for createdAt (ISO-8601 instant, e.g. 2024-01-01T00:00:00Z)")
          @RequestParam(required = false)
          Instant createdFrom,
      @Parameter(description = "Inclusive upper bound for createdAt (ISO-8601 instant)")
          @RequestParam(required = false)
          Instant createdTo,
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
      @Parameter(
              description =
                  "Sort field with direction prefix: +field (ASC) or -field (DESC). Allowed fields: count, createdAt, updatedAt, severity, state",
              example = "-count")
          @RequestParam(defaultValue = "-count")
          String sort) {
    return Mono.fromCallable(
            () -> {
              AlertPage result =
                  alertLifecycleService.list(
                      state, processDefinitionId, createdFrom, createdTo, page, size, sort);
              List<AlertResponse> content =
                  result.content().stream().map(AlertResponse::from).toList();
              return new PagedAlertResponse(
                  content,
                  result.totalElements(),
                  result.totalPages(),
                  result.page(),
                  result.size());
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(
      summary = "Firing alert statistics",
      description =
          "Returns aggregated stats of all FIRING alerts grouped by processDefinitionId and version. The totalCount is the sum of each alert's count within that group.",
      responses = @ApiResponse(responseCode = "200", description = "Aggregated firing stats"))
  @GetMapping("/stats/firing")
  public Mono<List<AlertStats>> firingStats() {
    return Mono.fromCallable(alertLifecycleService::firingStats)
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(
      summary = "Get alerts by states (paginated)",
      description = "Returns a paginated list of alerts matching any of the provided states.")
  @GetMapping("/by-states")
  public Mono<PagedAlertResponse> listByStates(
      @Parameter(description = "One or more alert states to filter by") @RequestParam
          List<AlertState> states,
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
    return Mono.fromCallable(
            () -> {
              AlertPage result = alertLifecycleService.listByStates(states, page, size);
              List<AlertResponse> content =
                  result.content().stream().map(AlertResponse::from).toList();
              return new PagedAlertResponse(
                  content,
                  result.totalElements(),
                  result.totalPages(),
                  result.page(),
                  result.size());
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(
      summary = "Get alert by ID",
      responses = {
        @ApiResponse(responseCode = "200", description = "Alert found"),
        @ApiResponse(
            responseCode = "404",
            description = "Alert not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  @GetMapping("/{id}")
  public Mono<AlertResponse> get(@Parameter(description = "Alert ID") @PathVariable String id) {
    return Mono.fromCallable(() -> AlertResponse.from(alertLifecycleService.getRequired(id)))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(
      summary = "Acknowledge an alert",
      description = "Transitions alert to ACKNOWLEDGED state. Idempotent if already acknowledged.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Alert acknowledged"),
        @ApiResponse(responseCode = "404", description = "Alert not found"),
        @ApiResponse(
            responseCode = "409",
            description = "Invalid state transition (alert is RESOLVED)")
      })
  @PostMapping("/{id}/acknowledge")
  public Mono<AlertResponse> acknowledge(
      @Parameter(description = "Alert ID") @PathVariable String id,
      @RequestBody(required = false) ActorRequest body) {
    return Mono.fromCallable(
            () -> AlertResponse.from(alertLifecycleService.acknowledge(id, actor(body))))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(
      summary = "Silence an alert",
      description = "Transitions alert to SILENCED state — suppresses notifications until unmuted.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Alert silenced"),
        @ApiResponse(responseCode = "404", description = "Alert not found"),
        @ApiResponse(
            responseCode = "409",
            description = "Invalid state transition (alert is RESOLVED)")
      })
  @PostMapping("/{id}/silence")
  public Mono<AlertResponse> silence(
      @Parameter(description = "Alert ID") @PathVariable String id,
      @RequestBody(required = false) ActorRequest body) {
    return Mono.fromCallable(
            () -> AlertResponse.from(alertLifecycleService.silence(id, actor(body))))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(
      summary = "Unmute a silenced alert",
      description = "Returns a SILENCED alert back to FIRING state, resuming notifications.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Alert unmuted"),
        @ApiResponse(responseCode = "404", description = "Alert not found"),
        @ApiResponse(responseCode = "409", description = "Alert is not in SILENCED state")
      })
  @PostMapping("/{id}/unmute")
  public Mono<AlertResponse> unmute(@Parameter(description = "Alert ID") @PathVariable String id) {
    return Mono.fromCallable(() -> AlertResponse.from(alertLifecycleService.unmute(id)))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(
      summary = "Resolve an alert",
      description = "Marks alert as RESOLVED — no further notifications will be sent.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Alert resolved"),
        @ApiResponse(responseCode = "404", description = "Alert not found"),
        @ApiResponse(responseCode = "409", description = "Alert is already RESOLVED")
      })
  @PostMapping("/{id}/resolve")
  public Mono<AlertResponse> resolve(@Parameter(description = "Alert ID") @PathVariable String id) {
    return Mono.fromCallable(() -> AlertResponse.from(alertLifecycleService.resolve(id)))
        .subscribeOn(Schedulers.boundedElastic());
  }

  // ─── Mailer Config CRUD ───────────────────────────────────────────────────

  @Operation(summary = "List mailer configs (paginated)")
  @GetMapping("/mailer-configs")
  public Mono<PagedMailerConfigResponse> listMailerConfigs(
      @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
    return Mono.fromCallable(
            () -> {
              var result = mailerConfigService.findAll(page, size);
              return new PagedMailerConfigResponse(
                  result.content(),
                  result.totalElements(),
                  result.totalPages(),
                  result.page(),
                  result.size());
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(
      summary = "Get mailer config by ID",
      responses = {
        @ApiResponse(responseCode = "200", description = "Config found"),
        @ApiResponse(responseCode = "404", description = "Config not found")
      })
  @GetMapping("/mailer-configs/{id}")
  public Mono<ResponseEntity<AlertingMailerConfig>> getMailerConfig(@PathVariable String id) {
    return Mono.fromCallable(
            () ->
                mailerConfigService
                    .findByProcessId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build()))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(summary = "Create a mailer config mapping")
  @PostMapping("/mailer-configs")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<AlertingMailerConfig> createMailerConfig(@RequestBody MailerConfigRequest request) {
    return Mono.fromCallable(
            () ->
                mailerConfigService.create(request.getProcessId(), request.getAlertingRecipient()))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(
      summary = "Update a mailer config mapping",
      responses = {
        @ApiResponse(responseCode = "200", description = "Config updated"),
        @ApiResponse(responseCode = "404", description = "Config not found")
      })
  @PutMapping("/mailer-configs/{id}")
  public Mono<ResponseEntity<AlertingMailerConfig>> updateMailerConfig(
      @PathVariable String id, @RequestBody MailerConfigRequest request) {
    return Mono.fromCallable(
            () ->
                mailerConfigService
                    .update(id, request.getProcessId(), request.getAlertingRecipient())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build()))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Operation(summary = "Delete a mailer config mapping")
  @DeleteMapping("/mailer-configs/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteMailerConfig(@PathVariable String id) {
    return Mono.fromRunnable(() -> mailerConfigService.deleteById(id))
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MailerConfigRequest {
    private String processId;
    private AlertRecipients alertingRecipient;
  }

  @Schema(description = "Paginated mailer config response")
  public record PagedMailerConfigResponse(
      List<AlertingMailerConfig> content, long totalElements, int totalPages, int page, int size) {}

  // ─── Exception Handlers ─────────────────────────────────────────────────────

  @ExceptionHandler(AlertNotFoundException.class)
  public Mono<ResponseEntity<Map<String, String>>> notFound(AlertNotFoundException ex) {
    return Mono.just(
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage())));
  }

  @ExceptionHandler(AlertTransitionException.class)
  public Mono<ResponseEntity<Map<String, String>>> conflict(AlertTransitionException ex) {
    return Mono.just(
        ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage())));
  }

  private static String actor(ActorRequest body) {
    return body == null ? null : body.getActor();
  }

  @Schema(description = "Error response body")
  private record ErrorResponse(@Schema(description = "Error message") String error) {}
}
