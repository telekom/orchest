package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.request.MessageEventRequest;
import io.telekom.orchest.api.core.request.SignalEventRequest;
import io.telekom.orchest.api.core.request.Variables;
import io.telekom.orchest.api.core.response.MessageEventResponse;
import io.telekom.orchest.api.core.response.SignalEventResponse;
import io.telekom.orchest.api.core.utils.StateChangeUtils;
import io.telekom.orchest.orchestrest.api.request.SendMessageEventRequest;
import io.telekom.orchest.orchestrest.api.request.SendSignalEventRequest;
import io.telekom.orchest.orchestrest.event.EventProducer;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Service for publishing BPMN message and signal events to the orchestration engine via Kafka. */
@Component
@RequiredArgsConstructor
public class EventingAPIService {

  private final EventProducer eventProducer;

  public MessageEventResponse publishMessage(SendMessageEventRequest request) {
    String id = UUID.randomUUID().toString();
    Map<String, Object> vars = request.getVariables() != null ? request.getVariables() : Map.of();
    MessageEventRequest kafkaRequest =
        new MessageEventRequest(
            id,
            request.getMessageName(),
            request.getCorrelationKey(),
            Variables.builder().variables(vars).build(),
            List.of(StateChangeUtils.buildStateChanges(NodeState.TRIGGERED)));
    eventProducer.sendMessageEvent(kafkaRequest);
    return new MessageEventResponse(id, request.getMessageName(), request.getCorrelationKey());
  }

  public SignalEventResponse publishSignal(SendSignalEventRequest request) {
    String id = UUID.randomUUID().toString();
    Map<String, Object> vars = request.getVariables() != null ? request.getVariables() : Map.of();
    SignalEventRequest kafkaRequest =
        new SignalEventRequest(
            id,
            request.getSignalName(),
            Variables.builder().variables(vars).build(),
            List.of(StateChangeUtils.buildStateChanges(NodeState.TRIGGERED)));
    eventProducer.sendSignalEvent(kafkaRequest);
    return new SignalEventResponse(id, request.getSignalName());
  }
}
