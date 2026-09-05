package de.telekom.solutions.kmsclient.api.utils;

import static software.amazon.awssdk.regions.Region.EU_CENTRAL_1;

import com.amazonaws.encryptionsdk.MasterKeyProvider;
import com.amazonaws.encryptionsdk.kmssdkv2.KmsMasterKey;
import com.amazonaws.encryptionsdk.kmssdkv2.KmsMasterKeyProvider;
import de.telekom.solutions.kmsclient.KMSProperties;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.KmsClientBuilder;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

@Slf4j
public class AWSBuilderUtils {

  private AWSBuilderUtils() {
    throw new IllegalStateException("AWSBuilder Utility  class");
  }

  public static MasterKeyProvider<KmsMasterKey> buildKeyProvider(
      KMSProperties.KMSClientProperties kmsProperties, String sessionPrefix, boolean isLocal) {
    return KmsMasterKeyProvider.builder()
        .builderSupplier(() -> buildKMSClientBuilder(kmsProperties, sessionPrefix, isLocal))
        .defaultRegion(EU_CENTRAL_1)
        .buildStrict(kmsProperties.getKmsKeyARN());
  }

  public static KmsClientBuilder buildKMSClientBuilder(
      KMSProperties.KMSClientProperties kmsProperties, String sessionPrefix, boolean isLocal) {
    KMSProperties.AWS aws = kmsProperties.getAws();
    return KmsClient.builder()
        .region(aws.getRegion())
        .credentialsProvider(AWSBuilderUtils.credentialsProvider(aws, sessionPrefix, isLocal));
  }

  public static AwsCredentialsProvider credentialsProvider(
      KMSProperties.AWS aws, String sessionPrefix, boolean isLocal) {
    if (isLocal) {
      log.info("using local static credential provider for KMS");
      return () ->
          AwsSessionCredentials.builder()
              .accessKeyId(aws.getAccessKeyId())
              .secretAccessKey(aws.getSecretAccessKey())
              .sessionToken(aws.getSessionToken())
              .build();
    }

    String sessionName = sessionPrefix + "-KMS-Client" + Thread.currentThread().getId();
    @SuppressWarnings("java:S6242")
    StsClient stsClient =
        StsClient.builder()
            .region(aws.getRegion())
            .overrideConfiguration(AWSBuilderUtils.overrideConfigs())
            .build();
    return StsAssumeRoleCredentialsProvider.builder()
        .stsClient(stsClient)
        .refreshRequest(
            () ->
                AssumeRoleRequest.builder()
                    .roleArn(aws.getIamRoleARN())
                    .roleSessionName(sessionName)
                    .build())
        .build();
  }

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
