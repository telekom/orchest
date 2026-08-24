package io.telekom.orchest.orchestrest.web.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.telekom.orchest.api.core.response.MessageEventResponse;
import io.telekom.orchest.api.core.response.SignalEventResponse;
import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import io.telekom.orchest.orchestrest.api.request.SendMessageEventRequest;
import io.telekom.orchest.orchestrest.api.request.SendSignalEventRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/eventing")
@Tag(
    name = "Eventing",
    description =
        "Publish BPMN message and signal events to correlate with waiting process instances")
/** REST API interface for publishing BPMN message and signal events. */
public interface IEventingService {

  @PostMapping("/sendMessage")
  @Operation(
      summary = "Publish a message event",
      description =
          "Correlates a message event with waiting intermediate catch events or start events by message name and correlation key")
  Mono<ResponseDTO<MessageEventResponse>> publishMessage(
      @RequestBody @Valid SendMessageEventRequest request);

  @PostMapping("/sendSignal")
  @Operation(
      summary = "Publish a signal event",
      description =
          "Broadcasts a signal to all subscribed intermediate catch events and signal start events")
  Mono<ResponseDTO<SignalEventResponse>> publishSignal(
      @RequestBody @Valid SendSignalEventRequest request);
}
