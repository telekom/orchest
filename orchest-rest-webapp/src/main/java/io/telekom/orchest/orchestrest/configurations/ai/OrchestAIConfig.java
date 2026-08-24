package io.telekom.orchest.orchestrest.configurations.ai;

import io.telekom.orchest.ai.config.OrchestAiProperties;
import io.telekom.orchest.ai.service.ChatService;
import io.telekom.orchest.api.core.adapters.data.repository.IncidentRepository;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessInstanceRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configures the AI chat service and tool callbacks when the orchest.ai feature is enabled. */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "orchest.ai", name = "enabled", havingValue = "true")
public class OrchestAIConfig {

  @Bean
  public ChatService chatService(
      OrchestAiProperties orchestAiProperties,
      @Autowired(required = false) ChatClient openAiChatClient,
      ProcessInstanceRepository processInstanceRepository,
      IncidentRepository incidentRepository,
      @Autowired(required = false) List<ToolCallback> mcpToolCallbacks) {
    var localCallbacks =
        ToolCallbacks.from(new ProcessInstanceTools(processInstanceRepository, incidentRepository));

    List<ToolCallback> allCallbacks = new ArrayList<>();
    Collections.addAll(allCallbacks, localCallbacks);
    if (mcpToolCallbacks != null) {
      allCallbacks.addAll(mcpToolCallbacks);
    }

    List<ToolCallback> loggingCallbacks =
        allCallbacks.stream().map(OrchestAIConfig::wrapWithLogging).toList();

    return new ChatService(orchestAiProperties.isEnabled(), openAiChatClient, loggingCallbacks);
  }

  private static ToolCallback wrapWithLogging(ToolCallback delegate) {
    return new ToolCallback() {
      @Override
      public @NonNull ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
      }

      @Override
      public @NonNull ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
      }

      @Override
      public String call(String toolInput) {
        log.info("AI tool invoked: {} | input: {}", delegate.getToolDefinition().name(), toolInput);
        String result = delegate.call(toolInput);
        log.info(
            "AI tool completed: {} with result: {}", delegate.getToolDefinition().name(), result);
        return result;
      }

      @Override
      public String call(String toolInput, ToolContext toolContext) {
        log.info("AI tool invoked: {} | input: {}", delegate.getToolDefinition().name(), toolInput);
        String result = delegate.call(toolInput, toolContext);
        log.info(
            "AI tool completed: {} with result: {}", delegate.getToolDefinition().name(), result);
        return result;
      }
    };
  }
}
