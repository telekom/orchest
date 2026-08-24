package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.orchestrest.api.exception.RestExceptions;
import io.telekom.orchest.orchestrest.extensions.ratelimit.model.RateLimit;
import io.telekom.orchest.orchestrest.service.DataInteractionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** REST controller for managing per-process rate-limit configurations. */
@RestController
@RequiredArgsConstructor
public class RateLimitAPI {

  private final DataInteractionService dataInteractionService;

  @GetMapping("/rateLimits")
  public Mono<List<RateLimit>> getRateLimits() {
    return Mono.fromCallable(dataInteractionService::getRateLimits)
        .subscribeOn(Schedulers.boundedElastic());
  }

  @GetMapping("/rateLimit/{processDefinitionId}")
  public Mono<RateLimit> getRateLimitForProcessId(@PathVariable String processDefinitionId) {
    return Mono.fromCallable(() -> dataInteractionService.getRateLimit(processDefinitionId))
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            opt ->
                opt.orElseThrow(
                    () ->
                        new RestExceptions(
                            "No rate limit found for: " + processDefinitionId, 404)));
  }

  @PutMapping("/rateLimit")
  public Mono<RateLimit> upsertRateLimit(@RequestBody RateLimit rateLimit) {
    return Mono.fromCallable(() -> dataInteractionService.saveRateLimit(rateLimit))
        .subscribeOn(Schedulers.boundedElastic());
  }
}
