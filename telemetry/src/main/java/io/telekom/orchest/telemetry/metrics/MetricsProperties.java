package io.telekom.orchest.telemetry.metrics;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Metrics-pipeline configuration bound to the {@code orchest.metrics} prefix.
 *
 * <p>The defaults are tuned for production: a 30-second flush is short enough that lifetime totals
 * look "live" in the controller, and long enough to keep Mongo write volume negligible (one bulk
 * per pod every 30s, regardless of execution rate).
 *
 * <p>Set {@code enabled=false} to fall back to {@link NoOpMetricsRecorder} (e.g. on the {@code
 * orchest-rest} / {@code sentinel} apps that don't need persistent metrics, or for local dev
 * without a Mongo writeable role).
 */
@Data
@ConfigurationProperties(prefix = "orchest.metrics")
public class MetricsProperties {

  /** Enable / disable the persistent metrics pipeline. Defaults to true. */
  private boolean enabled = true;

  /** Flush interval in milliseconds. Default 30s. */
  private long flushIntervalMs = 30_000L;

  /** Time-bucket retention in days (TTL on metric_time_buckets). Default 90 days. */
  private int bucketRetentionDays = 90;

  /**
   * Pod identity used to tag flushed increments. Defaults to {@code HOSTNAME} (set automatically by
   * Kubernetes), falling back to "unknown" if absent. Override for local dev or non-K8s
   * deployments.
   */
  private String podId = System.getenv().getOrDefault("HOSTNAME", "unknown");
}
