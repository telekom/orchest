package io.telekom.orchest.enginecore.bpmn.service;

import io.telekom.orchest.api.core.adapters.data.model.MessageEventStore;
import io.telekom.orchest.api.core.adapters.data.repository.MessageEventRepository;
import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles all message-related event registration: intermediate message events, start message
 * events, and gateway-linked message events.
 */
@Slf4j
@RequiredArgsConstructor
public class MessageEventRegistrar {

  private final MessageEventRepository messageEventRepository;

  /**
   * Registers or updates a start message event subscription for a process definition.
   *
   * @param messageName the message name to subscribe to
   * @param messageNode the start event node
   * @param processDefinitionId the owning process definition ID
   */
  public void registerStartMessageEvent(
      String messageName, BaseNode messageNode, String processDefinitionId) {
    Optional<MessageEventStore> existing =
        messageEventRepository.findStartMessageEvent(processDefinitionId);
    MessageEventStore messageEventStore;
    if (existing.isPresent()) {
      messageEventStore = existing.get();
      messageEventStore.setMessageName(messageName);
    } else {
      messageEventStore =
          MessageEventStore.builder()
              .state(IntermediateEventState.REGISTERED)
              .messageName(messageName)
              .nodeInformation(messageNode)
              .processDefinitionId(processDefinitionId)
              .isStartEvent(true)
              .build();
    }
    messageEventRepository.registerEvent(messageEventStore);
  }

  /**
   * Registers an intermediate message event subscription for a process instance.
   *
   * @param messageName the message name
   * @param correlationValue the correlation key value
   * @param processInstanceId the process instance ID
   * @param messageNode the catch event node
   * @return the persisted message event store entry
   */
  public MessageEventStore registerMessageEvent(
      String messageName, String correlationValue, String processInstanceId, BaseNode messageNode) {
    return registerMessageEvent(
        messageName, correlationValue, processInstanceId, messageNode, null, false);
  }

  public MessageEventStore registerMessageEvent(
      String messageName,
      String correlationValue,
      String processInstanceId,
      BaseNode messageNode,
      String processDefinitionId,
      boolean isStartEvent) {
    MessageEventStore messageEventStore =
        MessageEventStore.builder()
            .processInstanceId(processInstanceId)
            .state(IntermediateEventState.REGISTERED)
            .messageName(messageName)
            .correlationKey(correlationValue)
            .nodeInformation(messageNode)
            .processDefinitionId(processDefinitionId)
            .isStartEvent(isStartEvent)
            .build();
    return messageEventRepository.registerEvent(messageEventStore);
  }

  /**
   * Registers a message event linked to an Event-Based Gateway for exclusive race semantics.
   *
   * @param messageName the message name
   * @param correlationValue the correlation key value
   * @param processInstanceId the process instance ID
   * @param messageNode the catch event node
   * @param linkedEventId the shared linked event ID for gateway cleanup
   * @return the persisted message event store entry
   */
  public MessageEventStore registerMessageEventForGateway(
      String messageName,
      String correlationValue,
      String processInstanceId,
      BaseNode messageNode,
      String linkedEventId) {
    MessageEventStore messageEventStore =
        MessageEventStore.builder()
            .processInstanceId(processInstanceId)
            .state(IntermediateEventState.REGISTERED)
            .messageName(messageName)
            .correlationKey(correlationValue)
            .nodeInformation(messageNode)
            .isLinkedEvent(true)
            .linkedEventId(linkedEventId)
            .build();
    return messageEventRepository.registerEvent(messageEventStore);
  }

  public void deleteStartEventByDefinitionId(String definitionId) {
    messageEventRepository.deleteStartEventByDefinitionId(definitionId);
  }

  public void deleteByLinkedEventId(String linkedEventId) {
    messageEventRepository.deleteByLinkedEventId(linkedEventId);
  }

  public void deleteByProcessInstanceId(String processInstanceId) {
    messageEventRepository.deleteByProcessInstanceId(processInstanceId);
  }
}
