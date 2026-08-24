package io.telekom.orchest.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the OrchesT Client. Mapped to the "orchest" prefix in application
 * properties. Controls Kafka settings, worker behavior, security, and alerting.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "orchest")
public class OrchestProperties {

  /** To enable Worker consumer */
  private boolean enableWorkers = true;

  /** Name of the processIds whose workers are implemented */
  private List<String> processIds = new ArrayList<>();

  /** Security protocol for kafka auth */
  private long pollTimeoutMs = 30_000;

  /** Worker consumer thread concurrency */
  private int workerThreadCount = 3;

  private Encryption encryption = new Encryption();

  /** Map to log key:value pair from variables to the loggers */
  private Map<String, String> logVariables;

  private int deployPartitions = 10;

  private IncidentAlert incidentAlert = new IncidentAlert();

  private Rest rest;

  /** REST API connection settings for the OrchesT server. */
  @Data
  @NoArgsConstructor
  public static class Rest {
    private String url;
    private String apiKey;
  }

  /** AWS KMS encryption settings for Kafka payload encryption. */
  @Data
  @NoArgsConstructor
  public static class Encryption {
    private boolean enableEncryption;
    private String kmsKeyARN;
    private String iamRoleARN;
  }

  /** Configuration for incident alerting (MS Teams webhook). */
  @Data
  public static class IncidentAlert {
    private boolean enabled;

    /** MS Teams webhook URL for sending client incident alerts */
    private String incidentAlertChannel;
  }
}
