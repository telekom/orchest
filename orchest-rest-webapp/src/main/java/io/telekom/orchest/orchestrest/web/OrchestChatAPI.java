package io.telekom.orchest.orchestrest.web;

import io.telekom.orchest.ai.model.ChatRequest;
import io.telekom.orchest.ai.service.ChatService;
import io.telekom.orchest.orchestrest.web.interfaces.IOrchestChatAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/** REST controller implementation for the AI chat analysis feature. */
@Component
@ConditionalOnProperty(prefix = "orchest.ai", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrchestChatAPI implements IOrchestChatAPI {

  private final ChatService chatService;

  @Override
  public Flux<ServerSentEvent<String>> analyse(ChatRequest request) {
    if (request.getChatType() == null) {
      request.setChatType(ChatRequest.ChatType.DEFAULT_ANALYSIS);
    }
    return chatService
        .askAIStream(request)
        .map(
            token -> {
              if (token.startsWith(ChatService.TOOL_COUNT_PREFIX)) {
                String count = token.substring(ChatService.TOOL_COUNT_PREFIX.length());
                return ServerSentEvent.<String>builder()
                    .event("metadata")
                    .data("{\"toolCallCount\":" + count + "}")
                    .build();
              }
              return ServerSentEvent.<String>builder()
                  .event("message")
                  .data("{\"content\":" + escapeJson(token) + "}")
                  .build();
            });
  }

  private static String escapeJson(String s) {
    return "\""
        + s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        + "\"";
  }
}
