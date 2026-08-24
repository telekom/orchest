package io.telekom.orchest.orchestrest.extensions.audittrail;

import io.telekom.orchest.orchestrest.extensions.audittrail.model.AuditTrailEntry;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller exposing audit trail query endpoints (by user, time range, or path). */
@RestController
@RequestMapping("/auditTrails")
@ConditionalOnProperty(prefix = "orchest.audit-trail", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class AuditTrailAPI {

  private final AuditTrailService auditTrailService;

  @GetMapping("/user")
  public Mono<List<AuditTrailEntry>> getByUser(
      @RequestParam String email, @RequestParam(defaultValue = "50") int limit) {
    return Mono.fromCallable(() -> auditTrailService.getByUser(email, limit))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @GetMapping("/time")
  public Mono<List<AuditTrailEntry>> getByTimeRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
      @RequestParam(defaultValue = "100") int limit) {
    return Mono.fromCallable(() -> auditTrailService.getByTimeRange(from, to, limit))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @GetMapping("/path")
  public Mono<List<AuditTrailEntry>> getByPath(
      @RequestParam String path,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
      @RequestParam(defaultValue = "100") int limit) {
    return Mono.fromCallable(() -> auditTrailService.getByPath(path, from, to, limit))
        .subscribeOn(Schedulers.boundedElastic());
  }
}
