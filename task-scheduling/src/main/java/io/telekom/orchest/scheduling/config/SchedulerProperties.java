package io.telekom.orchest.scheduling.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** External tuning for the task scheduler. Bound to {@code orchest.scheduler.*}. */
@Data
@ConfigurationProperties(prefix = "orchest.scheduler")
public class SchedulerProperties {

  /** Master switch. When {@code false} the auto-configuration backs off entirely. */
  private boolean enabled = true;

  /**
   * When {@code true}, the JVM also runs a {@link
   * io.telekom.orchest.scheduling.runtime.TaskDispatcher}. Producer-only services (e.g. {@code
   * orchest-engine}) should keep this {@code false}.
   */
  private boolean dispatcherEnabled = false;

  /** How often the dispatcher polls the store. Default 1s. */
  private Duration pollInterval = Duration.ofSeconds(1);

  /** Max tasks claimed per poll. */
  private int batchSize = 50;

  /**
   * Lease duration. Should comfortably exceed the longest expected handler runtime; if a worker
   * doesn't ack within this window, another dispatcher will steal the lease.
   */
  private Duration leaseDuration = Duration.ofSeconds(60);

  /** Worker thread pool size for handler invocations. */
  private int dispatcherThreads = 4;

  /** Default {@code maxAttempts} when a producer leaves it unset. */
  private int defaultMaxAttempts = 5;

  /** Initial back-off delay after a failed attempt. */
  private Duration backoffBase = Duration.ofSeconds(5);

  /** Upper bound on back-off (capped after enough exponential growth). */
  private Duration backoffMax = Duration.ofMinutes(15);
}
