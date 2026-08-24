package io.telekom.orchest.alerting;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the Telemetry module. Mapped to the "telemetry" prefix in
 * application.yml.
 */
@Data
@NoArgsConstructor
@Configuration("alertingProperties")
@ConfigurationProperties(prefix = "alerting")
public class ModuleProperties {

  /** Whether alerting is enabled. */
  private boolean enabled;

  /** Default interval between re-notifications for FIRING alerts (milliseconds). */
  private long defaultResendIntervalMs = 300_000L;

  /**
   * Opt-in poller that sends notifications for due FIRING alerts. Enable on only one deployment to
   * avoid duplicate emails.
   */
  @NestedConfigurationProperty private Scheduler scheduler = new Scheduler();

  /** Map of configurations for different alert types (e.g. MS_TEAMS, MS_OUTLOOK). */
  @NestedConfigurationProperty private Map<AlertType, AlertingConfig> configs;

  /** Supported alert types. */
  public enum AlertType {
    MS_TEAMS,
    EMAIL;
  }

  /** Configuration details for a specific alert channel. */
  @Data
  @NoArgsConstructor
  public static class AlertingConfig {
    private boolean enabled;
    private String host;
    private Integer port;
    private String userName;
    private String password;
    private String authToken;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Scheduler {
    /** When true, this process runs the alert notification poller. */
    private boolean enabled;

    /** How often the poller looks for due FIRING alerts (milliseconds). */
    private long pollIntervalMs = 30_000L;
  }
}
