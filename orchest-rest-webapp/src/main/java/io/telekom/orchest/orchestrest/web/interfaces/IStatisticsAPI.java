package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.dto.StatsDTO;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/stats")
@Tag(
    name = "Statistics",
    description =
        "Aggregated platform statistics — process instance counts, decision evaluations, incidents")
/** REST API interface for aggregated instance statistics. */
public interface IStatisticsAPI {

  @GetMapping
  @Operation(
      summary = "Get platform statistics",
      description =
          "Returns aggregated counts of process instances, decision evaluations, and incidents within an optional time window. Results are cached unless hardReload is true.")
  Mono<ResponseDTO<StatsDTO>> stats(
      @Parameter(description = "Force fresh computation, bypassing cache")
          @RequestParam(defaultValue = "false")
          Boolean hardReload,
      @Parameter(description = "Inclusive lower bound on createdAt/executedAt (ISO-8601 instant)")
          @RequestParam(value = "from", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          OffsetDateTime from,
      @Parameter(description = "Exclusive upper bound on createdAt/executedAt (ISO-8601 instant)")
          @RequestParam(value = "to", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          OffsetDateTime to,
      @Parameter(
              description = "IANA zone ID used to render lastUpdatedAt (defaults to UTC)",
              example = "Europe/Berlin")
          @RequestParam(value = "tz", required = false)
          String tz);
}
