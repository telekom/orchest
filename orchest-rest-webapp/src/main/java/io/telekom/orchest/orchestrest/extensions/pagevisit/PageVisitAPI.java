package io.telekom.orchest.orchestrest.extensions.pagevisit;

import io.telekom.orchest.orchestrest.extensions.pagevisit.model.PageVisitStats;
import java.time.LocalDate;
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

/** REST controller exposing page visit statistics (unique users per URL per day). */
@RestController
@RequestMapping("/pageVisits")
@ConditionalOnProperty(prefix = "orchest.page-visits", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PageVisitAPI {

  private final PageVisitService pageVisitService;

  // GET /pageVisits?date=2026-05-26
  // Returns unique user count per URL for the given day (defaults to today)
  @GetMapping
  public Mono<List<PageVisitStats>> getVisitsForDate(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date) {
    LocalDate effectiveDate = date != null ? date : LocalDate.now();
    return Mono.fromCallable(() -> pageVisitService.getStatsForDate(effectiveDate))
        .subscribeOn(Schedulers.boundedElastic());
  }

  // GET /pageVisits/url?url=/processInstances&from=2026-05-01&to=2026-05-26
  // Returns daily unique user count for a specific URL over a date range (default: last 30 days)
  @GetMapping("/url")
  public Mono<List<PageVisitStats>> getVisitsForUrl(
      @RequestParam String url,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return Mono.fromCallable(() -> pageVisitService.getStatsForUrl(url, from, to))
        .subscribeOn(Schedulers.boundedElastic());
  }
}
