package io.telekom.orchest.telemetry.metrics;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Periodic drainer that moves Micrometer-backed pod-local counters into MongoDB.
 *
 * <h2>Concurrency model</h2>
 *
 * Only one flush runs at a time per JVM ({@link #flushLock}). Concurrent increments during a flush
 * are not blocked — they land in the same Micrometer counters and are picked up on the next cycle
 * by virtue of the high-water mark advancing.
 *
 * <h2>Retry / loss policy</h2>
 *
 * On flush failure, the snapshot is fully restored to the in-memory recorder (which rewinds the
 * per-key high-water mark) so the next cycle retries the same deltas. The only way to lose data is
 * JVM death between a successful drain and a Mongo write — an acceptable trade-off vs. the overhead
 * of a per-pod write-ahead log.
 *
 * <h2>Shutdown</h2>
 *
 * {@link #drainOnShutdown()} is invoked at container shutdown to flush whatever is still in memory.
 * Combined with Spring's graceful shutdown ({@code server.shutdown: graceful}, configured per-app)
 * this means a normal pod restart loses no metrics.
 */
@Slf4j
@RequiredArgsConstructor
public class MetricsFlushService {

  private final MetricsRecorder recorder;
  private final MetricsRepository metricsRepository;
  private final MetricsProperties properties;

  private final ReentrantLock flushLock = new ReentrantLock();

  /**
   * Periodic flush. {@code initialDelay} matches {@code fixedDelay} so we do not flush immediately
   * on startup — let the engine warm up first.
   */
  @Scheduled(
      fixedDelayString = "${orchest.metrics.flush-interval-ms:30000}",
      initialDelayString = "${orchest.metrics.flush-interval-ms:30000}")
  public void flush() {
    if (!properties.isEnabled()) {
      return;
    }
    doFlush(false);
  }

  /** Flushes remaining in-memory counters to the repository during application shutdown. */
  @PreDestroy
  public void drainOnShutdown() {
    if (!properties.isEnabled()) {
      return;
    }
    log.info("metrics: draining in-memory counters before shutdown");
    doFlush(true);
  }

  /**
   * Public hook used by the metrics REST controller for ops/tests. Returns the number of distinct
   * keys flushed.
   */
  public int forceFlush() {
    return doFlush(true);
  }

  private int doFlush(boolean blocking) {
    if (blocking) {
      flushLock.lock();
    } else if (!flushLock.tryLock()) {
      log.debug("metrics: previous flush still running — skipping this tick");
      return 0;
    }
    try {
      Map<MetricKey, Long> snapshot = recorder.drainSnapshot();
      if (snapshot.isEmpty()) {
        return 0;
      }

      Instant now = Instant.now();
      List<MetricIncrement> increments = new ArrayList<>(snapshot.size());
      for (Map.Entry<MetricKey, Long> e : snapshot.entrySet()) {
        increments.add(new MetricIncrement(e.getKey(), e.getValue(), properties.getPodId(), now));
      }

      try {
        metricsRepository.applyIncrements(increments);
        log.debug(
            "metrics: flushed {} keys to mongo (pod={})", increments.size(), properties.getPodId());
        return increments.size();
      } catch (RuntimeException ex) {
        log.warn(
            "metrics: flush failed, restoring {} keys for retry — cause={}",
            snapshot.size(),
            ex.toString());
        recorder.restore(snapshot);
        return 0;
      }
    } finally {
      flushLock.unlock();
    }
  }
}
