package io.telekom.orchest.adapter.mongo.config;

import static java.time.ZoneOffset.UTC;

import com.mongodb.AwsCredential;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.telekom.orchest.adapter.mongo.convertors.converters.OffsetDateTimeConverter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

/**
 * Configuration for MongoDB connectivity. Handles database connection, AWS IAM authentication (if
 * configured), auditing, and transaction management.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableMongoAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@EnableMongoRepositories(
    basePackages = {
      "io.telekom.orchest.adapter.mongo.repository",
      "io.telekom.orchest.orchestrest.extensions"
    })
public class MongoConfiguration extends AbstractMongoClientConfiguration {

  private final OrchestMongoProperties properties;
  private final List<Converter<?, ?>> converters = new ArrayList<>();

  /**
   * Adds AWS IAM credentials to the MongoClientSettings builder if IAM auth is configured.
   *
   * @param builder the MongoClientSettings builder to configure
   * @param iamRoleARN the AWS IAM role ARN to assume for authentication
   */
  public void addCredentials(MongoClientSettings.Builder builder, String iamRoleARN) {
    log.info("auth type selected for mongo is: {}", properties.getAuthType());
    if (OrchestMongoProperties.AuthType.AWS_IAM.equals(properties.getAuthType())) {
      Supplier<AwsCredential> credentialSupplier =
          getCredentialProvider(
              iamRoleARN, 900, Region.of(properties.getAwsRegion())); // min value 900
      MongoCredential credential =
          MongoCredential.createAwsCredential(null, null)
              .withMechanismProperty(
                  MongoCredential.AWS_CREDENTIAL_PROVIDER_KEY, credentialSupplier);
      builder.credential(credential);
    }
  }

  /**
   * Creates an AWS credential supplier that assumes the given IAM role via STS.
   *
   * @param roleARN the IAM role ARN to assume
   * @param refreshTimeInSeconds the duration in seconds for the temporary credentials
   * @param region the AWS region for the STS client
   * @return a supplier that provides fresh AWS credentials on each invocation
   */
  public static Supplier<AwsCredential> getCredentialProvider(
      String roleARN, Integer refreshTimeInSeconds, Region region) {
    return () -> {
      StsClient stsClient = StsClient.builder().region(region).build();
      String sessionName = roleARN.split("/")[roleARN.split("/").length - 1];
      AssumeRoleRequest roleRequest =
          AssumeRoleRequest.builder()
              .roleArn(roleARN)
              .roleSessionName(sessionName)
              .durationSeconds(refreshTimeInSeconds)
              .build();

      AssumeRoleResponse roleResponse = stsClient.assumeRole(roleRequest);
      Credentials loadedCredentials = roleResponse.credentials();

      printExpiry(loadedCredentials.expiration());
      stsClient.close();

      return new AwsCredential(
          loadedCredentials.accessKeyId(),
          loadedCredentials.secretAccessKey(),
          loadedCredentials.sessionToken());
    };
  }

  private static void printExpiry(Instant time) {
    DateTimeFormatter formatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault());
    log.info("The Mongo token expires on {}", formatter.format(time));
  }

  /**
   * Creates a MongoDB transaction manager with rollback-on-commit-failure enabled.
   *
   * @param mongoDatabaseFactory the database factory for creating transactions
   * @return the configured transaction manager
   */
  @Bean("mongoTransactionManager")
  MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
    MongoTransactionManager manager = new MongoTransactionManager(mongoDatabaseFactory);
    manager.setRollbackOnCommitFailure(true);
    return manager;
  }

  /**
   * Provides the current UTC time for Spring Data MongoDB auditing annotations.
   *
   * @return a DateTimeProvider returning the current OffsetDateTime in UTC
   */
  @Bean(name = "auditingDateTimeProvider")
  public DateTimeProvider dateTimeProvider() {
    return () -> Optional.of(OffsetDateTime.now(UTC));
  }

  @Override
  public MongoCustomConversions customConversions() {
    converters.add(new OffsetDateTimeConverter.ToDate());
    converters.add(new OffsetDateTimeConverter.FromDate());
    converters.add(new OffsetDateTimeConverter.FromString());
    return new MongoCustomConversions(converters);
  }

  @Override
  protected String getDatabaseName() {
    return properties.getDatabaseName();
  }

  @Override
  protected boolean autoIndexCreation() {
    return properties.isAutoIndexCreation();
  }

  @Bean
  @Override
  public MongoClient mongoClient() {
    ConnectionString connectionString = new ConnectionString(properties.getUri());
    MongoClientSettings.Builder builder =
        MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .applyToSocketSettings(
                socketSetting ->
                    socketSetting
                        .connectTimeout(
                            properties.getConnectTimeoutSeconds(),
                            java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(
                            properties.getReadTimeoutSeconds(),
                            java.util.concurrent.TimeUnit.SECONDS));
    addCredentials(builder, properties.getIamRoleArn());
    return MongoClients.create(builder.build());
  }

  @Bean
  @Primary
  @Override
  public MongoTemplate mongoTemplate(
      MongoDatabaseFactory mongoDbFactory,
      org.springframework.data.mongodb.core.convert.MappingMongoConverter mappingMongoConverter) {
    return new MongoTemplate(mongoDbFactory, mappingMongoConverter);
  }

  /**
   * Creates a secondary MongoTemplate with secondaryPreferred read preference for analytics
   * queries.
   *
   * @param mongoDatabaseFactory the database factory
   * @return a MongoTemplate configured for analytics workloads
   */
  @Bean("analyticsMongoTemplate")
  public MongoTemplate analyticsMongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
    MongoTemplate template = new MongoTemplate(mongoDatabaseFactory);
    if (properties.isSecondaryPreferredForAnalytics()) {
      template.setReadPreference(ReadPreference.secondaryPreferred());
      log.info("Analytics MongoTemplate configured with readPreference=secondaryPreferred");
    }
    return template;
  }
}
