package io.telekom.orchest.scheduling.runtime;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Computes the next-fire instant after a failed attempt.
 *
 * <p>Default policy is exponential back-off with jitter, capped at {@code maxBackoff}. Jitter is
 * critical in distributed deployments — without it, a wave of synchronised failures (e.g. a brief
 * Kafka outage) would re-attempt all at the same moment, causing a thundering herd.
 */
@FunctionalInterface
public interface BackoffPolicy {

  /**
   * @param attempt the number of failed attempts so far (1 after the first failure)
   * @return how long to wait before the next attempt
   */
  Duration nextDelay(int attempt);

  /**
   * Exponential back-off: {@code base * 2^(attempt-1)}, capped at {@code max}, plus full jitter.
   */
  static BackoffPolicy exponential(Duration base, Duration max) {
    long baseMs = Math.max(1, base.toMillis());
    long maxMs = Math.max(baseMs, max.toMillis());
    return attempt -> {
      int safeAttempt = Math.min(Math.max(attempt, 1), 30); // shift cap to avoid overflow
      long exp = baseMs * (1L << (safeAttempt - 1));
      long capped = Math.min(exp, maxMs);
      long jittered = ThreadLocalRandom.current().nextLong(baseMs, capped + 1);
      return Duration.ofMillis(jittered);
    };
  }
}
