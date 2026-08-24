package io.telekom.orchest.web;

import io.telekom.orchest.telemetry.metrics.MetricGranularity;
import io.telekom.orchest.telemetry.metrics.MetricKey;
import io.telekom.orchest.telemetry.metrics.MetricLifetimeTotal;
import io.telekom.orchest.telemetry.metrics.MetricTimeBucket;
import io.telekom.orchest.telemetry.metrics.MetricType;
import io.telekom.orchest.telemetry.metrics.MetricsFlushService;
import io.telekom.orchest.telemetry.metrics.MetricsRecorder;
import io.telekom.orchest.telemetry.metrics.MetricsRepository;
import io.telekom.orchest.web.dto.LifetimeMetricResponse;
import io.telekom.orchest.web.dto.TimeSeriesPointResponse;
import io.telekom.orchest.web.dto.TimeSeriesResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read API for the persistent metrics pipeline.
 *
 * <p>The controller is read-only against MongoDB except for two operational endpoints: {@code POST
 * /metrics/flush} (force a flush of the local pod) and the live-snapshot GET (which reads pod-local
 * in-memory counters). Queries hit the persisted collections only and therefore see a consistent
 * view across all engine replicas.
 */
@Slf4j
@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsController {

  private final MetricsRepository metricsRepository;
  private final MetricsRecorder metricsRecorder;
  private final MetricsFlushService flushService;

  /**
   * Returns lifetime totals across all engine pods, optionally filtered by metric type and/or
   * definition. This is the canonical "how many ever?" answer.
   *
   * <pre>
   *   GET /metrics/lifetime
   *   GET /metrics/lifetime?type=PROCESS_INSTANCE_STARTED
   *   GET /metrics/lifetime?type=DECISION_INSTANCE_EVALUATED&amp;definitionId=loanApproval
   * </pre>
   */
  @GetMapping("/lifetime")
  public ResponseEntity<List<LifetimeMetricResponse>> getLifetimeTotals(
      @RequestParam(value = "type", required = false) MetricType type,
      @RequestParam(value = "definitionId", required = false) String definitionId) {

    List<MetricLifetimeTotal> totals = metricsRepository.findLifetimeTotals(type, definitionId);
    List<LifetimeMetricResponse> body = new ArrayList<>(totals.size());
    for (MetricLifetimeTotal t : totals) {
      body.add(LifetimeMetricResponse.from(t));
    }
    return ResponseEntity.ok(body);
  }

  /**
   * Returns the lifetime total for a single key.
   *
   * <pre>
   *   GET /metrics/lifetime/single?type=PROCESS_INSTANCE_STARTED&amp;definitionId=loanApproval&amp;version=3
   * </pre>
   */
  @GetMapping("/lifetime/single")
  public ResponseEntity<LifetimeMetricResponse> getSingleLifetime(
      @RequestParam("type") MetricType type,
      @RequestParam(value = "definitionId", required = false) String definitionId,
      @RequestParam(value = "version", required = false) Integer version) {

    MetricKey key = new MetricKey(type, definitionId, version);
    Optional<MetricLifetimeTotal> total = metricsRepository.findLifetimeTotal(key);
    return ResponseEntity.ok(
        total
            .map(LifetimeMetricResponse::from)
            .orElseGet(
                () ->
                    LifetimeMetricResponse.builder()
                        .metricType(type)
                        .definitionId(definitionId)
                        .version(version)
                        .count(0L)
                        .build()));
  }

  /**
   * Returns a time series for the requested metric over {@code [from, to)}, aggregated to the
   * requested {@link MetricGranularity}. Storage is always at minute granularity; coarser
   * granularities aggregate at query time so writes stay cheap.
   *
   * <pre>
   *   GET /metrics/timeseries?type=PROCESS_INSTANCE_STARTED
   *                          &amp;from=2026-05-01T00:00:00Z
   *                          &amp;to=2026-05-02T00:00:00Z
   *                          &amp;granularity=HOUR
   * </pre>
   */
  @GetMapping("/timeseries")
  public ResponseEntity<TimeSeriesResponse> getTimeSeries(
      @RequestParam("type") MetricType type,
      @RequestParam(value = "definitionId", required = false) String definitionId,
      @RequestParam(value = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant from,
      @RequestParam(value = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(value = "granularity", defaultValue = "MINUTE") MetricGranularity granularity) {

    if (!from.isBefore(to)) {
      return ResponseEntity.badRequest().build();
    }

    List<MetricTimeBucket> buckets =
        metricsRepository.findTimeBuckets(type, definitionId, from, to);

    // Aggregate to requested granularity. LinkedHashMap preserves insertion order so the
    // resulting series is naturally sorted by bucketStart (input is sorted ASC by Mongo).
    Map<Instant, Long> aggregated = new LinkedHashMap<>();
    for (MetricTimeBucket b : buckets) {
      Instant bucket = granularity.bucketStart(b.bucketStart());
      aggregated.merge(bucket, b.count(), Long::sum);
    }

    List<TimeSeriesPointResponse> points = new ArrayList<>(aggregated.size());
    long total = 0L;
    for (Map.Entry<Instant, Long> e : aggregated.entrySet()) {
      points.add(new TimeSeriesPointResponse(e.getKey(), e.getValue()));
      total += e.getValue();
    }
    points.sort(Comparator.comparing(TimeSeriesPointResponse::getBucketStart));

    return ResponseEntity.ok(
        TimeSeriesResponse.builder()
            .metricType(type)
            .definitionId(definitionId)
            .granularity(granularity)
            .from(from)
            .to(to)
            .total(total)
            .points(points)
            .build());
  }

  /**
   * Returns the in-memory un-flushed counters for THIS pod only. Useful for ops to confirm that
   * increments are happening before the next flush lands. NOT a cluster-wide view — call {@code
   * /metrics/lifetime} for that.
   */
  @GetMapping("/snapshot")
  public ResponseEntity<List<LifetimeMetricResponse>> getLocalSnapshot() {
    Map<MetricKey, Long> snap = metricsRecorder.peekSnapshot();
    List<LifetimeMetricResponse> body = new ArrayList<>(snap.size());
    snap.forEach(
        (k, v) ->
            body.add(
                LifetimeMetricResponse.builder()
                    .metricType(k.type())
                    .definitionId(k.definitionId())
                    .version(k.version())
                    .count(v)
                    .build()));
    return ResponseEntity.ok(body);
  }

  /**
   * Forces an immediate flush of the local pod's counters to MongoDB. Intended for ops/CI; the
   * scheduler runs anyway on the configured interval.
   */
  @PostMapping("/flush")
  public ResponseEntity<Map<String, Object>> forceFlush() {
    int flushedKeys = flushService.forceFlush();
    return ResponseEntity.ok(
        Map.of("flushedKeys", flushedKeys, "flushedAt", Instant.now().toString()));
  }
}
