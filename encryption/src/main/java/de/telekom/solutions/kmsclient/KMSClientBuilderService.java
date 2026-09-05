package de.telekom.solutions.kmsclient;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.caching.CachingCryptoMaterialsManager;
import com.amazonaws.encryptionsdk.caching.LocalCryptoMaterialsCache;
import de.telekom.solutions.kmsclient.api.utils.AWSBuilderUtils;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KMSClientBuilderService {

  private final KMSProperties kmsProperties;

  @Value("${spring.application.name:application}")
  private String appName;

  @Value("${spring.profiles.active:local}")
  private String activeProfile;

  private static final String PARITITION_KEY = "mong-encryption";

  public CryptoService build(String clientName) {
    KMSProperties.KMSClientProperties kmsClientProperties =
        kmsProperties.getClients().get(clientName);
    if (kmsClientProperties == null) {
      throw new RuntimeException("client properties not found for KMS: " + clientName);
    }
    KMSProperties.DataKeyCache dataKeyCaching = kmsClientProperties.getDataKeyCaching();
    CachingCryptoMaterialsManager cachingCryptoMaterialsManager =
        CachingCryptoMaterialsManager.newBuilder()
            .withMasterKeyProvider(
                AWSBuilderUtils.buildKeyProvider(
                    kmsClientProperties,
                    appName + "-" + activeProfile,
                    activeProfile.equalsIgnoreCase("local")))
            .withCache(new LocalCryptoMaterialsCache(dataKeyCaching.getCapacity()))
            .withMaxAge(dataKeyCaching.getMaxAge(), TimeUnit.SECONDS)
            .withPartitionId(PARITITION_KEY)
            .build();
    return new CryptoService(
        AwsCrypto.standard(), cachingCryptoMaterialsManager, kmsClientProperties.isEnabled());
  }

  public static CryptoService build(
      KMSProperties.KMSClientProperties kmsClientProperties,
      String sessionPrefix,
      String activeProfile) {
    KMSProperties.DataKeyCache dataKeyCaching = kmsClientProperties.getDataKeyCaching();
    CachingCryptoMaterialsManager cachingCryptoMaterialsManager =
        CachingCryptoMaterialsManager.newBuilder()
            .withMasterKeyProvider(
                AWSBuilderUtils.buildKeyProvider(
                    kmsClientProperties, sessionPrefix, activeProfile.equals("local")))
            .withCache(new LocalCryptoMaterialsCache(dataKeyCaching.getCapacity()))
            .withMaxAge(dataKeyCaching.getMaxAge(), TimeUnit.SECONDS)
            .withMessageUseLimit(dataKeyCaching.getMaxUsageLimit())
            .withPartitionId(PARITITION_KEY)
            .build();
    return new CryptoService(
        AwsCrypto.standard(), cachingCryptoMaterialsManager, kmsClientProperties.isEnabled());
  }
}
