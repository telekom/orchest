package io.telekom.orchest.kafka.kms;

import static software.amazon.awssdk.regions.Region.EU_CENTRAL_1;

import com.amazonaws.encryptionsdk.MasterKeyProvider;
import com.amazonaws.encryptionsdk.kmssdkv2.KmsMasterKey;
import com.amazonaws.encryptionsdk.kmssdkv2.KmsMasterKeyProvider;
import io.telekom.orchest.config.OrchestClientProperties;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.KmsClientBuilder;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

/**
 * Utility class for building AWS KMS (Key Management Service) clients and providers. Provides
 * methods to create KMS master key providers, KMS clients, and credential providers with support
 * for both local development and AWS IAM role-based authentication.
 */
@Slf4j
public class AWSBuilderUtils {

  private AWSBuilderUtils() {
    throw new IllegalStateException("AWSBuilder Utility  class");
  }

  /**
   * Builds a KMS master key provider for encryption/decryption operations. Configures the provider
   * with the KMS key ARN and appropriate credentials.
   *
   * @param properties The client properties containing AWS configuration.
   * @return A configured KMS master key provider.
   */
  public static MasterKeyProvider<KmsMasterKey> buildKeyProvider(
      OrchestClientProperties properties) {
    return KmsMasterKeyProvider.builder()
        .builderSupplier(() -> buildKMSClientBuilder(properties, UUID.randomUUID().toString()))
        .defaultRegion(EU_CENTRAL_1)
        .buildStrict(properties.getEncryption().getKmsKeyARN());
  }

  /**
   * Builds a KMS client builder with configured region and credentials.
   *
   * @param properties The client properties containing AWS configuration.
   * @param sessionPrefix The prefix for the session name (used for IAM role sessions).
   * @return A configured KMS client builder.
   */
  public static KmsClientBuilder buildKMSClientBuilder(
      OrchestClientProperties properties, String sessionPrefix) {
    OrchestClientProperties.Encryption encryption = properties.getEncryption();
    return KmsClient.builder()
        .region(Region.of(encryption.getRegion()))
        .credentialsProvider(AWSBuilderUtils.credentialsProvider(encryption, sessionPrefix));
  }

  /**
   * Creates an AWS credentials provider based on the execution environment. For local development,
   * uses static credentials from properties. For other environments, uses STS assume role
   * credentials provider.
   *
   * @param encryption The AWS configuration properties.
   * @param sessionPrefix The prefix for the session name.
   * @return An AWS credentials provider configured for the environment.
   */
  public static AwsCredentialsProvider credentialsProvider(
      OrchestClientProperties.Encryption encryption, String sessionPrefix) {
    String sessionName = sessionPrefix + "-KMS-Client" + Thread.currentThread().getId();
    @SuppressWarnings("java:S6242")
    StsClient stsClient =
        StsClient.builder()
            .region(Region.of(encryption.getRegion()))
            .overrideConfiguration(AWSBuilderUtils.overrideConfigs())
            .build();
    return StsAssumeRoleCredentialsProvider.builder()
        .stsClient(stsClient)
        .refreshRequest(
            () ->
                AssumeRoleRequest.builder()
                    .roleArn(encryption.getIamRoleARN())
                    .roleSessionName(sessionName)
                    .build())
        .build();
  }

  /**
   * Creates a client override configuration with timeout and retry settings. Configures a 30-second
   * API call timeout and retry policy with 3 retries.
   *
   * @return A configured ClientOverrideConfiguration instance.
   */
  public static ClientOverrideConfiguration overrideConfigs() {
    return ClientOverrideConfiguration.builder()
        .apiCallTimeout(Duration.of(30000, ChronoUnit.MILLIS))
        .retryPolicy(
            builder ->
                builder
                    .numRetries(3)
                    .retryCondition(RetryCondition.defaultRetryCondition())
                    .backoffStrategy(BackoffStrategy.defaultStrategy()))
        .build();
  }
}
