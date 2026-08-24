package io.telekom.orchest.ai.config;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for the OrchesT AI module, bound to the {@code orchest.ai} prefix. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "orchest.ai")
public class OrchestAiProperties {

  private boolean enabled;
  private String endPoint;
  private String apiKey;
  private String deploymentName;
  private String apiVersion;
  private String model;
  private Map<String, McpServerProperties> mcpServers;

  /** Properties for a single MCP server connection (URL, enabled flag, custom headers). */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class McpServerProperties {
    private String url;
    private boolean enabled;
    private Map<String, String> headers;
  }
}
