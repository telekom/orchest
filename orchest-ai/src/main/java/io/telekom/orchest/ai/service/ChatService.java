package io.telekom.orchest.ai.service;

import static io.telekom.orchest.ai.prompts.ProcessInstancePrompts.*;

import io.telekom.orchest.ai.model.ChatRequest;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service that dispatches user prompts to the AI chat model with appropriate system prompts and MCP
 * tools.
 */
@RequiredArgsConstructor
public class ChatService {

  private final boolean enabled;
  private final ChatClient chatClient;
  private final List<ToolCallback> toolCallbacks;

  /**
   * Sends a prompt to the AI model synchronously using the detailed analysis system prompt.
   *
   * @param prompt the user's question or process instance ID
   * @return the AI-generated response content, or {@code null} if AI is disabled
   */
  public String askAI(String prompt) {
    if (!enabled) {
      return null;
    }
    UserMessage userMessage = new UserMessage(prompt);
    return chatClient
        .prompt(
            Prompt.builder()
                .messages(PROCESS_INSTANCE_DETAILED_ANALYSIS_SYSTEM_PROMPT, userMessage)
                .build())
        .tools(t -> t.callbacks(toolCallbacks))
        .call()
        .content();
  }

  public static final String TOOL_COUNT_PREFIX = "__TOOL_COUNT:";

  /**
   * Streams AI responses for the given chat request, selecting the system prompt based on chat
   * type. Appends a tool-call count metadata token at the end of the stream.
   *
   * @param chatRequest the request containing the user message and analysis type
   * @return a reactive stream of response tokens, or {@code null} if AI is disabled
   */
  public Flux<String> askAIStream(ChatRequest chatRequest) {
    if (!enabled) {
      return null;
    }

    UserMessage userMessage = new UserMessage(chatRequest.getUserMessage());
    SystemMessage systemMessage =
        switch (chatRequest.getChatType()) {
          case INCIDENT_ANALYSIS -> PROCESS_INSTANCE_INCIDENT_ANALYSIS_SYSTEM_PROMPT;
          case SUGGEST_FIX -> PROCESS_INSTANCE_SUGGEST_FIX_SYSTEM_PROMPT;
          default -> PROCESS_INSTANCE_PATTERN_ANALYSIS_SYSTEM_PROMPT;
        };

    AtomicInteger toolCallCount = new AtomicInteger(0);
    Flux<String> content =
        chatClient
            .prompt(Prompt.builder().messages(systemMessage, userMessage).build())
            .tools(t -> t.callbacks(toolCallbacks))
            .stream()
            .content();

    return content.concatWith(Mono.fromCallable(() -> TOOL_COUNT_PREFIX + toolCallCount.get()));
  }
}
