package io.telekom.orchest.orchestrest.extensions.pagevisit;

import io.telekom.orchest.orchestrest.extensions.pagevisit.model.PageVisitStats;
import io.telekom.orchest.orchestrest.extensions.pagevisit.repository.PageVisitRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Service layer for recording page visits and retrieving visit statistics. */
@Service
@RequiredArgsConstructor
public class PageVisitService {

  private final PageVisitRepository repository;

  public void record(String url, String userId) {
    repository.recordVisit(url, userId, LocalDate.now());
  }

  public List<PageVisitStats> getStatsForDate(LocalDate date) {
    return repository.getUniqueUserCountPerUrl(date);
  }

  public List<PageVisitStats> getStatsForUrl(String url, LocalDate from, LocalDate to) {
    LocalDate effectiveTo = to != null ? to : LocalDate.now();
    LocalDate effectiveFrom = from != null ? from : effectiveTo.minusDays(30);
    return repository.getUniqueUserCountForUrl(url, effectiveFrom, effectiveTo);
  }
}
