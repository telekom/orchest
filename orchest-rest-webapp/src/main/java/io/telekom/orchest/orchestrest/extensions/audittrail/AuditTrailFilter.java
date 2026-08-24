package io.telekom.orchest.orchestrest.extensions.audittrail;

import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import io.telekom.orchest.orchestrest.extensions.audittrail.model.AuditTrailEntry;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * WebFilter that records an audit trail entry for every mutating HTTP request
 * (POST/PUT/PATCH/DELETE).
 */
@Slf4j
@Component
@Order(11)
@ConditionalOnProperty(prefix = "orchest.audit-trail", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class AuditTrailFilter implements WebFilter {

  private static final String BASE_PATH = "/orchest";
  private static final Set<String> AUDITED_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

  private final AuditTrailService auditTrailService;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String method = exchange.getRequest().getMethod().name();

    if (!AUDITED_METHODS.contains(method)) {
      return chain.filter(exchange);
    }

    String path = exchange.getRequest().getPath().value();

    if (path.contains("/actuator")
        || path.contains("/swagger")
        || path.contains("/v3/api-docs")
        || path.contains("/auditTrails")
        || path.contains("/feelPlayground/validate")) {
      return chain.filter(exchange);
    }

    String canonicalPath = path.startsWith(BASE_PATH) ? path.substring(BASE_PATH.length()) : path;

    return chain
        .filter(exchange)
        .then(
            Mono.<Void>deferContextual(
                ctx -> {
                  try {
                    LoggedInUserContext userCtx =
                        ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, null);

                    String userEmail =
                        (userCtx != null && userCtx.getUserId() != null)
                            ? userCtx.getUserId()
                            : "anonymous";
                    boolean isAdmin = userCtx != null && userCtx.isAdmin();
                    List<String> roles =
                        userCtx != null && userCtx.getRoles() != null
                            ? userCtx.getRoles()
                            : List.of();
                    String correlationId = userCtx != null ? userCtx.getCorrelationId() : null;

                    HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                    int status = statusCode != null ? statusCode.value() : 0;

                    AuditTrailEntry entry =
                        AuditTrailEntry.builder()
                            .userEmail(userEmail)
                            .roles(roles)
                            .admin(isAdmin)
                            .httpMethod(method)
                            .path(canonicalPath)
                            .responseStatus(status)
                            .correlationId(correlationId)
                            .build();

                    log.info(
                        "AUDIT | user: {} | resource: {} | method: {} | status: {} | admin: {}",
                        maskEmail(userEmail),
                        canonicalPath,
                        method,
                        status,
                        isAdmin);

                    Mono.fromRunnable(() -> auditTrailService.record(entry))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(
                            null,
                            e ->
                                log.warn(
                                    "Failed to record audit trail for {} {}: {}",
                                    method,
                                    canonicalPath,
                                    e.getMessage()));
                  } catch (Exception e) {
                    log.warn(
                        "Audit trail processing failed for {} {}: {}",
                        method,
                        canonicalPath,
                        e.getMessage());
                  }
                  return Mono.<Void>empty();
                }))
        .onErrorResume(
            e -> {
              log.warn(
                  "Audit trail filter error for {} {}: {}", method, canonicalPath, e.getMessage());
              return Mono.<Void>empty();
            });
  }

  static String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return "***";
    }
    String[] parts = email.split("@", 2);
    String local = parts[0];
    String domain = parts[1];

    int visibleLocal = Math.min(3, local.length());
    String maskedLocal =
        local.substring(0, visibleLocal) + "*".repeat(Math.max(0, local.length() - visibleLocal));

    int lastDot = domain.lastIndexOf('.');
    if (lastDot > 0) {
      String domainBody = domain.substring(0, lastDot);
      String tld = domain.substring(lastDot);
      int visibleDomain = Math.min(3, domainBody.length());
      String maskedDomain =
          domainBody.substring(0, visibleDomain)
              + "*".repeat(Math.max(0, domainBody.length() - visibleDomain))
              + tld;
      return maskedLocal + "@" + maskedDomain;
    }
    return maskedLocal + "@" + domain;
  }
}
