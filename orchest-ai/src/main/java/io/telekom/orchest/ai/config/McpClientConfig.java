package io.telekom.orchest.ai.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.mcp.client.webflux.transport.WebClientStreamableHttpTransport;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for MCP (Model Context Protocol) client connections. Discovers and initializes MCP
 * server connections defined in application properties.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(
    prefix = "orchest.ai",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false)
public class McpClientConfig {

  /**
   * Builds tool callbacks from all enabled MCP servers defined in properties.
   *
   * @param properties the OrchesT AI configuration containing MCP server definitions
   * @return list of tool callbacks aggregated from connected MCP servers, or empty list if none
   */
  @Bean
  public List<ToolCallback> mcpToolCallbacks(OrchestAiProperties properties) {
    if (properties.getMcpServers() == null || properties.getMcpServers().isEmpty()) {
      return Collections.emptyList();
    }

    List<McpSyncClient> clients = new ArrayList<>();

    properties
        .getMcpServers()
        .forEach(
            (name, server) -> {
              if (!server.isEnabled()) {
                log.info("MCP server '{}' is disabled, skipping", name);
                return;
              }
              try {
                var webClientBuilder = WebClient.builder().baseUrl(server.getUrl());
                if (server.getHeaders() != null) {
                  server.getHeaders().forEach(webClientBuilder::defaultHeader);
                }

                var transport = buildTransport(server, webClientBuilder);
                McpSyncClient client = McpClient.sync(transport).build();
                client.initialize();
                clients.add(client);
                log.info("MCP server '{}' connected at {}", name, server.getUrl());
              } catch (Exception e) {
                log.warn(
                    "Failed to connect MCP server '{}' at {}: {}",
                    name,
                    server.getUrl(),
                    e.getMessage());
              }
            });

    if (clients.isEmpty()) {
      return Collections.emptyList();
    }

    return McpToolUtils.getToolCallbacksFromSyncClients(clients);
  }

  private WebClientStreamableHttpTransport buildTransport(
      OrchestAiProperties.McpServerProperties server, WebClient.Builder webClientBuilder) {
    return WebClientStreamableHttpTransport.builder(webClientBuilder)
        .endpoint(server.getUrl())
        .build();
  }
}
