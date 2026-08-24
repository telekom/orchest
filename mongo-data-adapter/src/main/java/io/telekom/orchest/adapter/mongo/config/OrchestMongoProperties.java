package io.telekom.orchest.adapter.mongo.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the MongoDB Data Adapter module. Mapped to the "mongo-data-adapter"
 * prefix in the application.yml. Controls connection URI, database name, and other MongoDB specific
 * settings.
 */
@Data
@NoArgsConstructor
@Configuration("orchestMongoProperties")
@ConfigurationProperties(prefix = "orchest.mongo")
public class OrchestMongoProperties {

  /** MongoDB connection URI. */
  private String uri;

  /** Name of the database to use. */
  private String databaseName;

  private AuthType authType;

  /** IAM role name (ARN) for AWS authentication, if applicable. */
  private String iamRoleArn;

  /** Whether to automatically create indexes on startup. Defaults to false. */
  private boolean autoIndexCreation;

  private int connectTimeoutSeconds = 60;

  private int readTimeoutSeconds = 60;

  private String awsRegion = "eu-central-1";

  /**
   * When true, a secondary MongoTemplate is created with readPreference=secondaryPreferred for use
   * by analytics/statistics queries. Reduces load on the primary node.
   */
  private boolean secondaryPreferredForAnalytics = true;

  public enum AuthType {
    AWS_IAM,
    SCRAM
  }
}
