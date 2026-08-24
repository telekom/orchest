package io.telekom.orchest.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the housekeeping job. Controls whether the job is enabled and the
 * retention interval for data purging.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "housekeeping")
public class HouseKeepingProperties {
  /** Whether the housekeeping job is enabled. */
  private boolean enabled;

  /** The retention period for data. Records older than this interval will be purged. */
  private Duration interval;
}
