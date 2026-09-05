package de.telekom.solutions.kmsclient;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoMaterialsManager;
import com.amazonaws.encryptionsdk.caching.CachingCryptoMaterialsManager;
import com.amazonaws.encryptionsdk.caching.LocalCryptoMaterialsCache;
import de.telekom.solutions.kmsclient.api.json.deserializers.JsonPathCipherDeserializer;
import de.telekom.solutions.kmsclient.api.json.serializers.JsonPathCipherSerializer;
import de.telekom.solutions.kmsclient.api.utils.AWSBuilderUtils;
import de.telekom.solutions.kmsclient.api.utils.SerDerUtils;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
@ComponentScan("de.telekom.solutions.kmsclient")
@EnableConfigurationProperties(KMSProperties.class)
@RequiredArgsConstructor
public class KMSConfiguration {

  private final KMSProperties kmsProperties;

  @Value("${spring.application.name:application}")
  private String appName;

  @Value("${spring.profiles.active:local}")
  private String activeProfile;

  @Bean
  @Primary
  public CryptoMaterialsManager cachingCryptoMaterialsManager(KMSProperties kmsProperties) {
    Optional<String> primaryCmm = kmsProperties.getClients().keySet().stream().findFirst();
    if (primaryCmm.isPresent()) {
      KMSProperties.KMSClientProperties kmsClientProperties =
          kmsProperties.getClients().get(primaryCmm.get());
      KMSProperties.DataKeyCache dataKeyCaching = kmsClientProperties.getDataKeyCaching();
      return CachingCryptoMaterialsManager.newBuilder()
          .withMasterKeyProvider(
              AWSBuilderUtils.buildKeyProvider(
                  kmsClientProperties, getSessionPrefix(), activeProfile.equalsIgnoreCase("local")))
          .withCache(new LocalCryptoMaterialsCache(dataKeyCaching.getCapacity()))
          .withMaxAge(dataKeyCaching.getMaxAge(), TimeUnit.SECONDS)
          .withMessageUseLimit(dataKeyCaching.getMaxUsageLimit())
          .build();
    }
    return CachingCryptoMaterialsManager.newBuilder().build();
  }

  @Bean
  @Primary
  public AwsCrypto awsCrypto() {
    return AwsCrypto.standard();
  }

  @Bean
  public CryptoService cryptoService(
      AwsCrypto awsCrypto, CryptoMaterialsManager cachingCryptoMaterialsManager) {
    Optional<String> primaryCmm = kmsProperties.getClients().keySet().stream().findFirst();
    if (primaryCmm.isPresent()) {
      KMSProperties.KMSClientProperties kmsClientProperties =
          kmsProperties.getClients().get(primaryCmm.get());
      CryptoService cryptoService =
          new CryptoService(
              awsCrypto, cachingCryptoMaterialsManager, kmsClientProperties.isEnabled());

      /*
       * This encrypt is added to prevent KMS from cold start.
       * This will load keys at context up
       * */
      if (kmsClientProperties.isEnabled()) {
        cryptoService.encrypt("load-keys");
      }
      return cryptoService;
    }
    return new CryptoService(awsCrypto, cachingCryptoMaterialsManager, false);
  }

  @Bean
  public StaticCryptoService staticCryptoService(CryptoService cryptoService) {
    return new StaticCryptoService(cryptoService);
  }

  @Bean
  @ConditionalOnProperty(value = "kms.attributeJsonPath")
  public JsonPathCipherSerializer jsonPathCipherSerializer(KMSProperties kmsProperties) {
    Optional<KMSProperties.KMSClientProperties> clientProperties =
        kmsProperties.getClients().values().stream().findFirst();
    return clientProperties
        .map(
            kmsClientProperties ->
                new JsonPathCipherSerializer(
                    SerDerUtils.cipherPathsMap(kmsClientProperties.getAttributeJsonPath())))
        .orElseGet(() -> new JsonPathCipherSerializer(new HashMap<>()));
  }

  @Bean
  @ConditionalOnProperty(value = "kms.attributeJsonPath")
  public JsonPathCipherDeserializer jsonPathCipherDeserializer(KMSProperties kmsProperties) {
    Optional<KMSProperties.KMSClientProperties> clientProperties =
        kmsProperties.getClients().values().stream().findFirst();
    return clientProperties
        .map(
            kmsClientProperties ->
                new JsonPathCipherDeserializer(
                    SerDerUtils.cipherPathsMap(kmsClientProperties.getAttributeJsonPath())))
        .orElseGet(() -> new JsonPathCipherDeserializer(new HashMap<>()));
  }

  private String getSessionPrefix() {
    return appName + "-" + activeProfile;
  }
}
