package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.dto.StatsDTO;
import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.service.StatisticsAPIService;
import io.telekom.orchest.orchestrest.utils.RestUtils;
import io.telekom.orchest.orchestrest.web.interfaces.IStatisticsAPI;
import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller implementation for aggregated instance statistics. */
@Component
@RequiredArgsConstructor
public class StatisticsAPI implements IStatisticsAPI {

  private final StatisticsAPIService statisticsAPIService;

  @Override
  public Mono<ResponseDTO<StatsDTO>> stats(
      Boolean hardReload, OffsetDateTime from, OffsetDateTime to, String tz) {
    if (from != null && to != null && !from.isBefore(to)) {
      return Mono.error(new RestExceptions("'from' must be before 'to'", 400));
    }
    ZoneId zone;
    try {
      zone = resolveZone(tz);
    } catch (RestExceptions e) {
      return Mono.error(e);
    }
    return Mono.fromCallable(() -> statisticsAPIService.getStats(hardReload, from, to, zone))
        .subscribeOn(Schedulers.boundedElastic())
        .map(stats -> RestUtils.buildResponse(stats, 200, "Success"));
  }

  private ZoneId resolveZone(String tz) {
    if (tz == null || tz.isBlank()) {
      return ZoneId.of("UTC");
    }
    try {
      return ZoneId.of(tz);
    } catch (DateTimeException e) {
      throw new RestExceptions("invalid timezone '" + tz + "'", 400);
    }
  }
}
