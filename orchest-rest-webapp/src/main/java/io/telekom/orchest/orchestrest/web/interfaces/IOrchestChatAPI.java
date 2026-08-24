package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.ai.model.ChatRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chats")
@Tag(
    name = "Orchest Chat",
    description = "AI-powered analysis of process incidents and execution data")
/** REST API interface for the AI chat analysis feature. */
public interface IOrchestChatAPI {

  @PostMapping(value = "/analyse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(
      summary = "Analyse with AI (streaming)",
      description =
          "Sends a natural-language question to the AI assistant and streams the response as SSE")
  Flux<ServerSentEvent<String>> analyse(@RequestBody ChatRequest request);
}
