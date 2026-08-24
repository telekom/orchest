package io.telekom.orchest.scheduling.runtime;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Runtime tuning passed into {@link DefaultTaskScheduler} and {@link TaskDispatcher}.
 *
 * <p>Defaults are deliberately conservative — sized for hundreds of due tasks per second across a
 * small dispatcher fleet, with sub-second firing latency. Tune via the {@code orchest.scheduler.*}
 * properties exposed by {@link io.telekom.orchest.scheduling.config.SchedulerProperties}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerRuntimeProperties {

  @Builder.Default private Duration pollInterval = Duration.ofSeconds(1);

  @Builder.Default private int batchSize = 50;

  @Builder.Default private Duration leaseDuration = Duration.ofSeconds(60);

  @Builder.Default private int dispatcherThreads = 4;

  @Builder.Default private int defaultMaxAttempts = 5;

  @Builder.Default private Duration backoffBase = Duration.ofSeconds(5);

  @Builder.Default private Duration backoffMax = Duration.ofMinutes(15);
}
