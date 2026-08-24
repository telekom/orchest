package io.telekom.orchest.orchestrest.extensions.pagevisit;

import io.telekom.orchest.orchestrest.configurations.LoggedInUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** WebFilter that records page visits for authenticated users on each request. */
@Slf4j
@Component
@Order(10)
@ConditionalOnProperty(prefix = "orchest.page-visits", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PageVisitFilter implements WebFilter {

  private static final String BASE_PATH = "/orchest";

  private final PageVisitService pageVisitService;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().value();

    // Skip actuator, swagger, and the visit-stats API itself to avoid noise
    if (path.contains("/actuator")
        || path.contains("/swagger")
        || path.contains("/v3/api-docs")
        || path.contains("/pageVisits")) {
      return chain.filter(exchange);
    }

    // Strip base path prefix so stored URLs are canonical (/processInstances, not
    // /orchest/processInstances)
    String url = path.startsWith(BASE_PATH) ? path.substring(BASE_PATH.length()) : path;
    // Collapse dynamic path segments — keep only the first two segments (e.g.
    // /processInstances/id/{id} -> /processInstances/id)
    url = toCanonicalUrl(url);

    final String canonicalUrl = url;
    return chain
        .filter(exchange)
        .then(
            Mono.deferContextual(
                ctx -> {
                  LoggedInUserContext userCtx =
                      ctx.getOrDefault(LoggedInUserContext.CONTEXT_KEY, null);
                  if (userCtx != null && userCtx.getUserId() != null) {
                    String userId = userCtx.getUserId();
                    Mono.fromRunnable(() -> pageVisitService.record(canonicalUrl, userId))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(
                            null,
                            e ->
                                log.warn(
                                    "Failed to record page visit for {}: {}",
                                    canonicalUrl,
                                    e.getMessage()));
                  }
                  return Mono.empty();
                }));
  }

  // Keep only the first two path segments to group dynamic routes:
  // /processInstances/id/abc123 -> /processInstances/id
  // /processInstances -> /processInstances
  private static String toCanonicalUrl(String path) {
    if (path == null || path.isEmpty()) return "/";
    String[] segments = path.split("/");
    StringBuilder sb = new StringBuilder();
    int kept = 0;
    for (String seg : segments) {
      if (seg.isEmpty()) continue;
      if (kept == 2) break;
      sb.append("/").append(seg);
      kept++;
    }
    return sb.isEmpty() ? "/" : sb.toString();
  }
}
