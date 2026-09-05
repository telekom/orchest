package de.telekom.solutions.kmsclient;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import software.amazon.awssdk.regions.Region;

@Data
@ConfigurationProperties(prefix = "kms")
public class KMSProperties {

  Map<String, KMSClientProperties> clients;

  @Data
  @NoArgsConstructor
  public static class KMSClientProperties {
    private boolean enabled;
    private String kmsKeyARN;
    @NestedConfigurationProperty private AWS aws;
    @NestedConfigurationProperty private DataKeyCache dataKeyCaching;
    private List<String> attributeJsonPath;
  }

  @Data
  @NoArgsConstructor
  public static class AWS {
    private String iamRoleARN;
    private Region region = Region.EU_CENTRAL_1;
    private String accessKeyId;
    private String secretAccessKey;
    private String sessionToken;
  }

  @Data
  @NoArgsConstructor
  public static class DataKeyCache {
    private int capacity;
    private int maxAge;
    private int maxUsageLimit;
  }
}
