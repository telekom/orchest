package io.telekom.orchest.enginecore.bpmn.service;

import io.telekom.orchest.api.core.adapters.data.model.SignalEvent;
import io.telekom.orchest.api.core.adapters.data.repository.SignalEventRepository;
import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles all signal-related event registration: intermediate signal events, start signal events,
 * and gateway-linked signal events.
 */
@Slf4j
@RequiredArgsConstructor
public class SignalEventRegistrar {

  private final SignalEventRepository signalEventRepository;

  /**
   * Registers or updates a start signal event subscription for a process definition.
   *
   * @param signalName the signal name to subscribe to
   * @param signalNode the start event node
   * @param processDefinitionId the owning process definition ID
   * @return the persisted signal event entry
   */
  public SignalEvent registerStartSignalEvent(
      String signalName, BaseNode signalNode, String processDefinitionId) {
    Optional<SignalEvent> existing =
        signalEventRepository.findSignalStartEvent(processDefinitionId);
    SignalEvent signalEvent;
    if (existing.isPresent()) {
      signalEvent = existing.get();
      signalEvent.setSignalName(signalName);
    } else {
      signalEvent =
          SignalEvent.builder()
              .state(IntermediateEventState.REGISTERED)
              .signalName(signalName)
              .nodeInformation(signalNode)
              .isStartEvent(true)
              .processDefinitionId(processDefinitionId)
              .build();
    }
    return signalEventRepository.registerEvent(signalEvent);
  }

  /**
   * Registers an intermediate signal event subscription for a process instance.
   *
   * @param signalName the signal name
   * @param processInstanceId the process instance ID
   * @param signalNode the catch event node
   * @param processDefinitionId the process definition ID
   * @param isStartEvent whether this is a start event subscription
   * @return the persisted signal event entry
   */
  public SignalEvent registerSignalEvent(
      String signalName,
      String processInstanceId,
      BaseNode signalNode,
      String processDefinitionId,
      boolean isStartEvent) {
    SignalEvent signalEvent =
        SignalEvent.builder()
            .processInstanceId(processInstanceId)
            .state(IntermediateEventState.REGISTERED)
            .signalName(signalName)
            .nodeInformation(signalNode)
            .isStartEvent(isStartEvent)
            .processDefinitionId(processDefinitionId)
            .build();
    return signalEventRepository.registerEvent(signalEvent);
  }

  /**
   * Registers a signal event linked to an Event-Based Gateway for exclusive race semantics.
   *
   * @param signalName the signal name
   * @param processInstanceId the process instance ID
   * @param signalNode the catch event node
   * @param linkedEventId the shared linked event ID for gateway cleanup
   * @return the persisted signal event entry
   */
  public SignalEvent registerSignalEventForGateway(
      String signalName, String processInstanceId, BaseNode signalNode, String linkedEventId) {
    SignalEvent signalEvent =
        SignalEvent.builder()
            .processInstanceId(processInstanceId)
            .state(IntermediateEventState.REGISTERED)
            .signalName(signalName)
            .nodeInformation(signalNode)
            .isLinkedEvent(true)
            .linkedEventId(linkedEventId)
            .build();
    return signalEventRepository.registerEvent(signalEvent);
  }

  public void deleteStartEventByDefinitionId(String definitionId) {
    signalEventRepository.deleteStartEventByDefinitionId(definitionId);
  }

  public void deleteByLinkedEventId(String linkedEventId) {
    signalEventRepository.deleteByLinkedEventId(linkedEventId);
  }

  public void deleteByProcessInstanceId(String processInstanceId) {
    signalEventRepository.deleteByProcessInstanceId(processInstanceId);
  }
}
